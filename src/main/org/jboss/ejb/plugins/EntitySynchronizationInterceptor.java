/*
* jBoss, the OpenSource EJB server
*
* Distributable under GPL license.
* See terms of license at gnu.org.
*/
package org.jboss.ejb.plugins;

import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

import javax.ejb.EJBObject;
import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.NoSuchEntityException;
import javax.ejb.RemoveException;
import javax.ejb.EntityBean;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;

import org.jboss.ejb.Container;
import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.EntityPersistenceManager;
import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.EnterpriseContext;
import org.jboss.ejb.InstanceCache;
import org.jboss.ejb.InstancePool;
import org.jboss.ejb.MethodInvocation;

import org.jboss.logging.Logger;

/**
*   This container filter takes care of EntityBean persistance.
*   Specifically, it calls ejbStore at appropriate times
*
*   Possible options:
*   After each call
*   On tx commit
*      
*   @see <related>
*   @author Rickard Öberg (rickard.oberg@telkel.com)
*   @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
*   @version $Revision: 1.12 $
*/
public class EntitySynchronizationInterceptor
extends AbstractInterceptor
{
    // Constants -----------------------------------------------------
    public static final int TIMEOUT = 10000;
    
    // Commit time options
    public static final int A = 0; // Keep instance cached
    public static final int B = 1; // Invalidate state
    public static final int C = 2; // Passivate
    
    // Attributes ----------------------------------------------------
    
    int commitOption = A;
    
    protected EntityContainer container;
    
    // Static --------------------------------------------------------
    
    // Constructors --------------------------------------------------
    
    // Public --------------------------------------------------------
    public void setContainer(Container container) 
    { 
       this.container = (EntityContainer)container; 
    }
    
    public  Container getContainer()
    {
       return container;
    }
    
    public void setCommitOption(int commitOption)
    {
       this.commitOption = commitOption;
    }
    
    public int getCommitOption()
    {
       return commitOption;
    }
    
    public void register(EnterpriseContext ctx, Transaction tx)
    {
       // Create a new synchronization
       InstanceSynchronization synch = new InstanceSynchronization(tx);
       
       try {
         
         tx.registerSynchronization(synch);
       } 
       catch (Exception e) {
         
         throw new EJBException(e);
       }
       
       // register
       synch.add(ctx);	
    }
    
    public void deregister(EntityEnterpriseContext ctx)
    {
       // MF FIXME: I suspect this is redundant now
       // (won't the pool clean it up?)
       
       // Deassociate ctx with tx
       ctx.setTransaction(null);
       ctx.setInvoked(false);
    }
    
    // Interceptor implementation --------------------------------------
    
    public Object invokeHome(MethodInvocation mi)
    throws Exception
    {
       try
       {
         return getNext().invokeHome(mi);
       } finally
       {
         // Anonymous was sent in, so if it has an id it was created
         EnterpriseContext ctx = mi.getEnterpriseContext();
         if (ctx.getId() != null)
         {
          if (mi.getTransaction() != null &&
              mi.getTransaction().getStatus() == Status.STATUS_ACTIVE)
          {
              // Set tx
              register(ctx, mi.getTransaction());
          }
          
          // Currently synched with underlying storage
          ((EntityEnterpriseContext)ctx).setValid(true);
         }
       }
    }
    
    public Object invoke(MethodInvocation mi)
    throws Exception
    {
       // We are going to work with the context a lot
       EntityEnterpriseContext ctx = (EntityEnterpriseContext)mi.getEnterpriseContext();
       
       // The Tx coming as part of the Method Invocation 
       Transaction tx = mi.getTransaction();
       
       //Logger.debug("CTX in: isValid():"+ctx.isValid()+" isInvoked():"+ctx.isInvoked());
       //Logger.debug("newTx: "+ tx);
       
       // Is my state valid?
       if (!ctx.isValid()) {
         
         // If not tell the persistence manager to load the state
         ((EntityContainer)getContainer()).getPersistenceManager().loadEntity(ctx);
         
         // Now the state is valid
         ctx.setValid(true);
       }
       
       // So we can go on with the invocation
       Logger.log("Tx is "+tx.toString());
       if (tx != null &&
           tx.getStatus() == Status.STATUS_ACTIVE) {
         
         try {
          
          return getNext().invoke(mi);
         } 
         
         finally {
          
          // Do we have a valid bean (not removed)
          if (ctx.getId() != null) {
              
              // If the context was not invoked previously...
              if (!ctx.isInvoked()) {
                 
                 // It is now and this will cause ejbStore to be called...
                 ctx.setInvoked(true);
                 
                 // ... on a transaction callback that we register here.
                 register(ctx, tx);
              }
          } 
          else {
              
              // Entity was removed
              if (ctx.getTransaction() != null) {
                 
                 // Disassociate ctx
                 deregister(ctx);
              }
          }
          
          //Logger.debug("CTX out: isValid():"+ctx.isValid()+" isInvoked():"+ctx.isInvoked());
          //Logger.debug("PresentTx:"+tx);
          
         }
       } 
       
       else {  // No tx
         
         try
         {
          Object result = getNext().invoke(mi);
          
          // Store after each invocation -- not on exception though, or removal
          // And skip reads too ("get" methods)
          if (ctx.getId() != null && !mi.getMethod().getName().startsWith("get"))
              ((EntityContainer)getContainer()).getPersistenceManager().storeEntity(ctx);
          
          return result;
         } catch (Exception e)
         {
          // Exception - force reload on next call
          ctx.setValid(false);
          throw e;
         }
       }
    }
    
    // Protected  ----------------------------------------------------
    
    // Inner classes -------------------------------------------------
    class InstanceSynchronization
    implements Synchronization
    {
       // The transaction we follow
       Transaction tx;
       
       // The context we manage
       EnterpriseContext ctx;
       
       InstanceSynchronization(Transaction tx)
       {
         this.tx = tx;
       }
       
       public void add(EnterpriseContext ctx)
       {
         this.ctx = ctx;
       }
       
       
       // Synchronization implementation -----------------------------
       public void beforeCompletion()
       {
		   
		   Logger.log("beforeCompletion called");
		 try
         {
          try
          {
              // Lock instance
              ((EntityContainer)getContainer()).getInstanceCache().get(((EntityEnterpriseContext) ctx).getCacheKey());
              
              // Store instance if business method was
              if (((EntityEnterpriseContext) ctx).isInvoked())
                 ((EntityContainer)getContainer()).getPersistenceManager().storeEntity((EntityEnterpriseContext)ctx);
          
          } catch (NoSuchEntityException e)
          {
              // Object has been removed -- ignore
          }
         } catch (RemoteException e)
         {
          // Store failed -> rollback!
          try
          {
              tx.setRollbackOnly();
          } catch (SystemException ex)
          {
              // DEBUG ex.printStackTrace();
          }
         }
       }
       
       public void afterCompletion(int status)
       {
		   Logger.log("afterCompletion called");
         // If rolled back -> invalidate instance
         if (status == Status.STATUS_ROLLEDBACK)
         {
          
          try
          {
              
              ctx.setId(null);
              ctx.setTransaction(null);
              container.getInstanceCache().remove(ctx.getId());
              container.getInstancePool().free(ctx); // TODO: should this be done? still valid instance?
          } catch (Exception e)
          {
              // Ignore
          }
         } else
         {
          
          // The transaction is done
          ctx.setTransaction(null);
          
          // We are afterCompletion so the invoked can be set to false (db sync is done)
          ((EntityEnterpriseContext) ctx).setInvoked(false);
          
          switch (commitOption)
          {
              // Keep instance cached after tx commit
              case A:
                 try
                 {
                   // The state is still valid (only point of access is us)
                   ((EntityEnterpriseContext) ctx).setValid(true); 
                   
                   // Release it though
                   container.getInstanceCache().release(ctx);
                 } catch (Exception e)
                 {
                   Logger.debug(e);
                 }
              break;
              
              // Keep instance active, but invalidate state
              case B:
                 try
                 {
                   // Invalidate state (there might be other points of entry)
                   ((EntityEnterpriseContext) ctx).setValid(false); 
                   
                   // Release it though
                   container.getInstanceCache().release(ctx);
                 } catch (Exception e)
                 {
                   Logger.debug(e);
                 }
              break;
              
              // Invalidate everything AND Passivate instance
              case C:
                 try
                 {
                   //Lock in cache
                   container.getInstanceCache().get(((EntityEnterpriseContext)ctx).getCacheKey()); 
                   
                   // Passivate instance
                   ((EntityContainer)getContainer()).getPersistenceManager().passivateEntity((EntityEnterpriseContext)ctx); 
                   
                   //Remove from the cache, it is not active anymore
                   container.getInstanceCache().remove(ctx.getId()); 
                   
                   // Back to the pool
                   container.getInstancePool().free(ctx); 
                 
                 } catch (Exception e)
                 {
                   Logger.debug(e);
                 }
              break;
          
          }
         }
         
         // Notify all who are waiting for this tx to end
         synchronized (tx)
         {
          tx.notifyAll();
         }
       }
    }
}

