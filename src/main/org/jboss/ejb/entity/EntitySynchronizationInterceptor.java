/**
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.entity;

import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;

import javax.transaction.Transaction;

import org.jboss.ejb.BeanLock;
import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.plugins.AbstractInterceptor;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationResponse;

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
 * @version $Revision: 1.6 $
 */
public class EntitySynchronizationInterceptor extends AbstractInterceptor
{
   public InvocationResponse invoke(Invocation invocation) throws Exception
   {
      EntityEnterpriseContext ctx = null;
      boolean didSetReadOnly = false;
      Object id = null;
      boolean trace = log.isTraceEnabled();
      Transaction tx = null;

      try
      {
         EntityContainer container = (EntityContainer)getContainer();
         ctx = (EntityEnterpriseContext)invocation.getEnterpriseContext();
         tx = invocation.getTransaction();

         // mark the context as read only if this is a readonly method and the context
         // was not already readonly
         if(!ctx.isReadOnly() &&
            (container.isReadOnly() ||
             container.getBeanMetaData().isMethodReadOnly(invocation.getMethod()) ))
         {
            ctx.setReadOnly(true);
            didSetReadOnly = true;
         }

         // if we have an id we need to register the invocation
         id = ctx.getId();
         if(id != null)
            EntityContainer.getEntityInvocationRegistry().beginInvocation(ctx, tx);
      }
      catch (Throwable t)
      {
         if (didSetReadOnly)
            ctx.setReadOnly(false);
            
         clearTxLock(invocation, t, trace);

         if (t instanceof Exception)
            throw (Exception) t;
         else if (t instanceof Error)
            throw (Error) t;
         else 
            throw new UndeclaredThrowableException(t);
      }

      // invoke the next but remember if an exception is thrown
      Throwable exceptionThrown = null;
      try
      {
         return getNext().invoke(invocation);
      }
      catch(Throwable t)
      {
         ctx.setValid(false);

         exceptionThrown = t;
         if (t instanceof Exception)
            throw (Exception) t;
         else if (t instanceof Error)
            throw (Error) t;
         else 
            throw new UndeclaredThrowableException(t);
      }
      finally
      {
         // if we registed we need to tell the registry
         // that the invocation is done
         if(id != null)
         {
            // we need to pass the original id into end Invocation because of the
            // lame policy of setting id to null when removing an entity
            EntityContainer.getEntityInvocationRegistry().endInvocation(exceptionThrown, id, ctx, tx);
         }

         // if we marked the context as read only we need to reset it
         if(didSetReadOnly)
         {
            ctx.setReadOnly(false);
         }
      }
   }

   private boolean isReadOnlyInvocation(Invocation invocation)
   {
      EntityContainer container = (EntityContainer)getContainer();

      if(invocation.getType().isHome() || !container.isReadOnly()) 
         return false;

      Method method = invocation.getMethod();
      return method == null ||
         !container.getBeanMetaData().isMethodReadOnly(method.getName());
   }

   private void clearTxLock(Invocation invocation, Throwable t, boolean trace)
      throws Exception
   {
      if (invocation.getType().isHome() == false && isReadOnlyInvocation(invocation) == false)
      {
         Object key = invocation.getId();
         EntityContainer container = (EntityContainer)getContainer();
         BeanLock lock = container.getLockManager().getLock(key);
         lock.sync();
         try
         {
            Transaction tx = lock.getTransaction();
            if (tx != null)
            {
               if (trace)
                  log.trace("Clearing bean lock's tx: " + tx + " key: " + key, t);
  
               lock.wontSynchronize(tx);
            }
         }
         finally
         {
            lock.releaseSync();
         }
      }
   }
}
