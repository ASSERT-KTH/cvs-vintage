/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins;

import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.Map;

import javax.transaction.Transaction;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;

import javax.ejb.EJBException;
import javax.ejb.EJBObject;

import org.apache.log4j.Category;

import org.jboss.ejb.BeanLock;
import org.jboss.ejb.BeanLockManager;
import org.jboss.ejb.Container;
import org.jboss.ejb.InstanceCache;
import org.jboss.ejb.InstancePool;
import org.jboss.ejb.StatefulSessionContainer;
import org.jboss.ejb.StatefulSessionEnterpriseContext;
import org.jboss.ejb.EnterpriseContext;
import org.jboss.ejb.MethodInvocation;
import org.jboss.metadata.SessionMetaData;

/**
 * This container acquires the given instance. 
 *
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1.22 $
 *
 * <p><b>Revisions:</b>
 * <p><b>20010704 marcf</b>
 * <ul>
 * <li>- Moved to new synchronization
 * </ul>
 * <p><b>20010726 billb</b>
 * <ul>
 * <li>- externalized bean locking in separate object BeanLock
 * </ul>
 */
public class StatefulSessionInstanceInterceptor
   extends AbstractInterceptor
{
   // Constants ----------------------------------------------------
	
   // Attributes ---------------------------------------------------

   /** Instance logger. */
   protected Category log = Category.getInstance(this.getClass());
   
   protected StatefulSessionContainer container;
	
   // Static -------------------------------------------------------

   private static Method getEJBHome;
   private static Method getHandle;
   private static Method getPrimaryKey;
   private static Method isIdentical;
   private static Method remove;
   
   static 
   {
      try 
      {
         Class[] noArg = new Class[0];
         getEJBHome = EJBObject.class.getMethod("getEJBHome", noArg);
         getHandle = EJBObject.class.getMethod("getHandle", noArg);
         getPrimaryKey = EJBObject.class.getMethod("getPrimaryKey", noArg);
         isIdentical = EJBObject.class.getMethod("isIdentical", new Class[] {EJBObject.class});
         remove = EJBObject.class.getMethod("remove", noArg);
      }
      catch (Exception e) {
         e.printStackTrace();
         throw new ExceptionInInitializerError(e);
      }
   }
	
   // Constructors -------------------------------------------------
	
   // Public -------------------------------------------------------
	
   public void setContainer(Container container) 
   { 
      this.container = (StatefulSessionContainer)container; 
   }
	
   public  Container getContainer()
   {
      return container;
   }
   
   // Interceptor implementation -----------------------------------
   
   public Object invokeHome(MethodInvocation mi)
      throws Exception
   {
      // Get context
		
      // get a new context from the pool (this is a home method call)
      EnterpriseContext ctx = container.getInstancePool().get();
		
		
      // set the context on the methodInvocation
      mi.setEnterpriseContext(ctx);
		
      // It is a new context for sure so we can lock it
      ctx.lock();
		
      try
      {
         // Invoke through interceptors
         return getNext().invokeHome(mi);
      } finally
      {
         synchronized (ctx) 
         {
            // Release the lock
            ctx.unlock();
				
            // Still free? Not free if create() was called successfully
            if (ctx.getId() == null)
            {
               container.getInstancePool().free(ctx); 
            }
         }
      }
   }
	
   protected void register(EnterpriseContext ctx, Transaction tx, BeanLock lock)
   {
      // Create a new synchronization
      InstanceSynchronization synch = new InstanceSynchronization(tx, ctx, lock);
		
      try {
         // OSH: An extra check to avoid warning.
         // Can go when we are sure that we no longer get
         // the JTA violation warning.
         if (tx.getStatus() == Status.STATUS_MARKED_ROLLBACK) {
				
            return;
         }
			
         // We want to be notified when the transaction commits
         try
         {
            tx.registerSynchronization(synch);
         }
         catch (Exception ex)
         {
            // synch adds a reference to the lock, so we must release the ref
            // because afterCompletion will never get called.
            getContainer().getLockManager().removeLockRef(lock.getId());
            throw ex;
         }
			
         // EJB 1.1, 6.5.3
         synch.afterBegin();
		
      } catch (RollbackException e) {
		
      } catch (Exception e) {
			
         throw new EJBException(e);
		
      }
   }
	
   public Object invoke(MethodInvocation mi)
      throws Exception
   {
      AbstractInstanceCache cache =
         (AbstractInstanceCache)container.getInstanceCache();
      Object id = mi.getId();
      EnterpriseContext ctx = null;
		
      BeanLock lock = (BeanLock)container.getLockManager().getLock(id);
      try
      {
         lock.sync(); // synchronized(ctx)
         try // lock.sync
         {
            // Get context
            ctx = container.getInstanceCache().get(mi.getId());
            // Associate it with the method invocation
            mi.setEnterpriseContext(ctx);

            // BMT beans will lock and replace tx no matter what, CMT do work on transaction
            if (!((SessionMetaData)container.getBeanMetaData()).isBeanManagedTx()) {
				
               // Do we have a running transaction with the context
               if (ctx.getTransaction() != null &&
                   // And are we trying to enter with another transaction
                   !ctx.getTransaction().equals(mi.getTransaction()))
               {
                  // Calls must be in the same transaction
                  throw new RemoteException("Application Error: tried to enter Stateful bean with different transaction context");
               }
				
               //If the instance will participate in a new transaction we register a sync for it
               if (ctx.getTransaction() == null && mi.getTransaction() != null) {
                  register(ctx, mi.getTransaction(), lock);
               }
            }
			
            if (!ctx.isLocked())
            {
				
               //take it!
               ctx.lock();  
            } 
            else 
            {
               if (!isCallAllowed(mi))
               {
                  // Concurent calls are not allowed
                  throw new RemoteException("Application Error: no concurrent calls on stateful beans");
               }
               else 
               {
                  ctx.lock();
               }
            }
         }
         finally
         {
            lock.releaseSync();
         }
		
         try
         {
            // Invoke through interceptors
            return getNext().invoke(mi);
         } catch (RemoteException e)
         {
            // Discard instance
            container.getInstanceCache().remove(mi.getId());
            ctx = null;
			
            throw e;
         } catch (RuntimeException e)
         {
            // Discard instance
            container.getInstanceCache().remove(mi.getId());
            ctx = null;
			
            throw e;
         } catch (Error e)
         {
            // Discard instance
            container.getInstanceCache().remove(mi.getId());
            ctx = null;
			
            throw e;
         } finally 
         {
            if (ctx != null)
            {
               // Still a valid instance
               lock.sync(); // synchronized(ctx) 
               try
               {
					
                  // release it
                  ctx.unlock();
					
                  // if removed, remove from cache
                  if (ctx.getId() == null)
                  {
                     // Remove from cache
                     container.getInstanceCache().remove(mi.getId());
                  }
               }
               finally
               {
                  lock.releaseSync();
               }
            }
         }
      }
      finally
      {
         container.getLockManager().removeLockRef(lock.getId());
      }
   }
	
   protected boolean isCallAllowed(MethodInvocation mi) 
   {
      Method m = mi.getMethod();
      if (m.equals(getEJBHome) ||
          m.equals(getHandle) ||
          m.equals(getPrimaryKey) ||
          m.equals(isIdentical) ||
          m.equals(remove))
      {
         return true;
      }
      return false;
   }
	
   // Inner classes -------------------------------------------------
	
   private class InstanceSynchronization
      implements Synchronization
   {
      /**
       *  The transaction we follow.
       */
      private Transaction tx;
		
      /**
       *  The context we manage.
       */
      private EnterpriseContext ctx;
		
      // a utility boolean for session sync
      private boolean notifySession = false;
		
      // Utility methods for the notifications
      private Method afterBegin;
      private Method beforeCompletion;
      private Method afterCompletion;
      private BeanLock lock;
		
      /**
       *  Create a new instance synchronization instance.
       */
      InstanceSynchronization(Transaction tx, EnterpriseContext ctx, BeanLock lock)
      {
         this.tx = tx;
         this.ctx = ctx;
         this.lock = lock;
         this.lock.addRef();
			
         // Let's compute it now, to speed things up we could 
         notifySession = (ctx.getInstance() instanceof javax.ejb.SessionSynchronization);
			
         if (notifySession) {
            try {
               // Get the class we are working on
               Class sync = Class.forName("javax.ejb.SessionSynchronization");
					
               // Lookup the methods on it
               afterBegin = sync.getMethod("afterBegin", new Class[0]);
               beforeCompletion = sync.getMethod("beforeCompletion", new Class[0]);
               afterCompletion =  sync.getMethod("afterCompletion", new Class[] {boolean.class});
            }
            catch (Exception e) {
               log.error("failed to setup InstanceSynchronization", e);
            }
         }
      }
		
      // Synchronization implementation -----------------------------

      public void afterBegin() 
      {
         if (notifySession) 
         {
            try 
            {
               afterBegin.invoke(ctx.getInstance(), new Object[0]);
            }
            catch (Exception e) 
            {
               log.error("failed to invoke afterBegin", e);
            }
         }
      }
		
      public void beforeCompletion()
      {
         // DEBUG log.debug("beforeCompletion called");
			
         // lock the context the transaction is being commited (no need for sync)
         ctx.lock();
			
         if (notifySession) {
            try {
					
               beforeCompletion.invoke(ctx.getInstance(), new Object[0]);
            }
            catch (Exception e) {
               log.error("failed to invoke beforeCompletion", e);
            }
         }
      }
		
      public void afterCompletion(int status)
      {
         // DEBUG log.debug("afterCompletion called");
			
         lock.sync();
         try
         {
            // finish the transaction association
            ctx.setTransaction(null);
            
            // unlock this context
            ctx.unlock();
            
            if (notifySession) {
               
               try {
                  
                  if (status == Status.STATUS_COMMITTED) {
                     afterCompletion.invoke(ctx.getInstance(),
                                            new Object[] { Boolean.TRUE });
                  }
                  else {
                     afterCompletion.invoke(ctx.getInstance(),
                                            new Object[] { Boolean.FALSE });
                  }
               }
               catch (Exception e) {
                  log.error("failed to invoke afterCompletion", e);
               }
            }			
         }
         finally
         {
            lock.releaseSync();
            container.getLockManager().removeLockRef(lock.getId());
         }
      }
   }
}

