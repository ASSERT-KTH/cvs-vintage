/**
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.entity;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;

import javax.ejb.EJBException;
import javax.transaction.Status;
import javax.transaction.Transaction;

import org.jboss.ejb.BeanLock;
import org.jboss.ejb.BeanLockManager;
import org.jboss.ejb.Container;
import org.jboss.ejb.EntityCache;
import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.EnterpriseContext;
import org.jboss.ejb.InstanceCache;
import org.jboss.ejb.plugins.AbstractInterceptor;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationResponse;
import org.jboss.invocation.PayloadKey;
import org.jboss.metadata.ConfigurationMetaData;
import org.jboss.tm.TransactionLocal;

/**
 * The role of this interceptor is to synchronize the state of the cache with
 * the underlying storage.  It does this with the ejbLoad and ejbStore
 * semantics of the EJB specification.  In the presence of a transaction this
 * is triggered by transaction demarcation. It registers a callback with the
 * underlying transaction monitor through the JTA interfaces.  If there is no
 * transaction the policy is to store state upon returning from invocation.
 * The synchronization polices A,B,C of the specification are taken care of
 * here.
 *
 * <p><b>WARNING: critical code</b>, get approval from senior developers
 * before changing.
 *
 * @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @author <a href="mailto:Scott.Stark@jboss.org">Scott Stark</a>
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.3 $
 */
public class EntitySynchronizationInterceptor extends AbstractInterceptor
{
   public InvocationResponse invoke(Invocation invocation) throws Exception
   {
      EntityContainer container = (EntityContainer)getContainer();
      EntityEnterpriseContext ctx = 
            (EntityEnterpriseContext)invocation.getEnterpriseContext();
      Transaction tx = invocation.getTransaction();

      if(log.isTraceEnabled())
      {
         log.trace("invoke called for ctx "+ctx+", tx="+tx);
      }

      // If this is not a life cycle invocation (create, remove, load, store,
      // activate, passivate) and the state is invalid, validate it.
      if(!ctx.isValid() && !invocation.getType().isHome())
      {
         try
         {
            // Not valid... tell the persistence manager to load the state
            container.getPersistenceManager().loadEntity(ctx);
         }
         catch(Exception ex)
         {
            // readonly does not synchronize, lock or belong with transaction.
            if(!container.isReadOnly()) 
            {
               Method method = invocation.getMethod();
               if(method == null ||
                     !container.getBeanMetaData().isMethodReadOnly(
                        method.getName()))
               {
                  clearContextTx(
                        "loadEntity Exception", 
                        ctx, 
                        tx);
               }
            }
            throw ex;
         }
         // Now the state is valid
         ctx.setValid(true);
      }

      // Invocation with a running Transaction
      if(tx != null && tx.getStatus() != Status.STATUS_NO_TRANSACTION)
      {
         // register for transaction callbacks if this is a post create life
         // cycle event or if this is a read-write invocation.
         if(invocation.getId() != null && !container.isReadOnly()) 
         {
            Method method = invocation.getMethod();
            if(method == null ||
                  !container.getBeanMetaData().isMethodReadOnly(method.getName()))
            {
               // register the wrapper with the transaction monitor (but only 
               // register once). The transaction demarcation will trigger the 
               // storage operations
               register(ctx, tx);
            }
         }

         // Invoke down the chain
         return getNext().invoke(invocation);  
      }
      else
      { 
         // No transaction
         try
         {
            InvocationResponse returnValue = getNext().invoke(invocation);

            // Store after each invocation -- not on exception though, or 
            // removal and skip reads too ("get" methods)
            if(ctx.getId() != null && 
               !container.isReadOnly())
            {
               container.storeEntity(ctx);
            }
            // Dain: shouldn't we apply the commit options here?
            return returnValue;
         }
         catch(Exception e)
         {
            // Exception - force reload on next call
            ctx.setValid(false);
            throw e;
         }
      }
   }

   /**
    *  Register a transaction synchronization callback with a context.
    */
   private void register(EntityEnterpriseContext ctx, Transaction tx)
   {
      if(log.isTraceEnabled())
      {
         log.trace("register, ctx="+ctx+", tx="+tx);
      }

      try
      {
         // associate the entity bean with the transaction so that
         // we can do things like synchronizeEntitiesWithinTransaction
         // do this after registerSynchronization, just in case there was 
         // an exception
         EntityContainer.getEntityInvocationRegistry().associate(ctx, tx);
         ctx.hasTxSynchronization(true);
      }
      catch (Exception e)
      {
         // Something is seriously hosed. The state in the instance is to be discarded, 
         // we force a reload of state
         synchronized (ctx)
         {
            ctx.setValid(false);
         }

         // If anything goes wrong with the association remove the 
         // ctx-tx association
//         clearContextTx("Exception", ctx, tx);

         if(e instanceof EJBException)
         {
            throw (EJBException)e;
         }
         throw new EJBException(e);
      }
   }

   private void clearContextTx(
         String msg, 
         EntityEnterpriseContext ctx, 
         Transaction tx)
   {
      BeanLock lock = getContainer().getLockManager().getLock(ctx.getId());
      try 
      {
         lock.sync();
         try
         {
            if(log.isTraceEnabled())
            {
               log.trace(msg + ", clear tx for ctx=" + ctx + ", tx=" + tx);
            }

            // The context is no longer synchronized on the TX
            ctx.hasTxSynchronization(false);
            ctx.setTransaction(null);
            lock.wontSynchronize(tx);
         }
         finally
         {
            lock.releaseSync();
         }
      }
      catch(InterruptedException e) 
      {
         log.error("Thread interrupted while clearing transaction context", e);
      }
   }
}
