/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins;

import java.lang.reflect.Method;
import java.rmi.RemoteException;
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
import org.jboss.ejb.CacheKey;
import org.jboss.logging.log4j.JBossCategory;
import org.jboss.metadata.EntityMetaData;
import org.jboss.util.Sync;
import org.jboss.tm.TxManager;

/**
 * The instance interceptors role is to acquire a context representing
 * the target object from the cache.
 *
 * <p>This particular container interceptor implements pessimistic locking
 *    on the transaction that is associated with the retrieved instance.  If
 *    there is a transaction associated with the target component and it is
 *    different from the transaction associated with the MethodInvocation
 *    coming in then the policy is to wait for transactional commit. 
 *   
 * <p>We also implement serialization of calls in here (this is a spec
 *    requirement). This is a fine grained notify, notifyAll mechanism. We
 *    notify on ctx serialization locks and notifyAll on global transactional
 *    locks.
 *   
 * <p><b>WARNING: critical code</b>, get approval from senior developers
 *    before changing.
 *    
 * @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @author <a href="mailto:Scott.Stark@jboss.org">Scott Stark</a>
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1.39 $
 *
 * <p><b>Revisions:</b><br>
 * <p><b>2001/06/28: marcf</b>
 * <ol>
 *   <li>Moved to new synchronization
 *   <li>Pools are gone simple design
 *   <li>two levels of syncrhonization with Tx and ctx
 *   <li>remove busy wait from previous mechanisms
 * </ol>
 * <p><b>2001/07/11: starksm</b>
 * <ol>
 *   <li>Fix a thread starvation problem due to incomplete condition notification
 *   <li>Add support for trace level diagnositics
 * </ol>
 * <p><b>2001/07/12: starksm</b>
 * <ol>
 *   <li>Handle a race condition when there is no ctx transaction
 * </ol>
 * </ol>
 * <p><b>2001/07/16: billb</b>
 * <ol>
 *   <li>Added wait(timeout) code, commented out so that we can easily turn it on
 *   when this new code is done with it's trial period.
 *   <li>Fixed bug when ejbLoad threw an exception and threads waiting 
 *   on TxLock did not get awakened.
 *   
 * </ol>
 */
public class EntityInstanceInterceptor
   extends AbstractInterceptor
{
   // Constants -----------------------------------------------------
	
   // Attributes ----------------------------------------------------

   protected EntityContainer container;
   protected int timeout;
	
   // Static --------------------------------------------------------

   /** Use a JBoss custom log4j category for trace level logging */
   static JBossCategory log = (JBossCategory) JBossCategory.getInstance(EntityInstanceInterceptor.class);

   // Constructors --------------------------------------------------
	
   // Public --------------------------------------------------------
   
   public void setContainer(Container container)
   {
      this.container = (EntityContainer)container;
      timeout = 5000;
      if (container.getTransactionManager() != null)
      {
         if (container.getTransactionManager() instanceof TxManager)
         {
            TxManager mgr = (TxManager)container.getTransactionManager();
            timeout = (mgr.getDefaultTransactionTimeout() * 1000) + 50;
         }
      }
      boolean trace = log.isTraceEnabled();
      if ( trace )
         log.trace("wait timeout = " + timeout);
   }
	
   public Container getContainer()
   {
      return container;
   }
	
   // Interceptor implementation --------------------------------------
   
   public Object invokeHome(MethodInvocation mi)
      throws Exception
   {
      // Get context
      EnterpriseContext ctx = ((EntityContainer)getContainer()).getInstancePool().get();
		
      // Pass it to the method invocation
      mi.setEnterpriseContext(ctx);
		
      // Give it the transaction
      ctx.setTransaction(mi.getTransaction());
		
      // This context is brand new. We can lock without more "fuss" 
      // The reason we need to lock it is that it will be put in cache before the end
      // of the call.  So another thread could access it before we are done.
		
      ctx.lock();
		
      try
      {
         // Invoke through interceptors
         return getNext().invokeHome(mi);
      } 
      finally
      {
         //Other threads can be coming for this instance if it is in cache
         synchronized(ctx) {
            // Always unlock, no matter what
            ctx.unlock();
            // Wake everyone up in case of create with Tx contention
            ctx.notifyAll();
         }
         //Do not send back to pools in any case, let the instance be GC'ed
      }
   }
	
   public Object invoke(MethodInvocation mi)
      throws Exception
   {
      // It's all about the context
      EntityEnterpriseContext ctx = null;
		
      // And it must correspond to the key.
      CacheKey key = (CacheKey) mi.getId();
      Transaction tx = null;
      boolean trace = log.isTraceEnabled();
      if( trace )
         log.trace("Begin invoke, key="+key);
      while (ctx == null)
      {
         // Maybe my transaction already expired?  This must be at the top of the loop.
         Transaction miTx = mi.getTransaction();
         if (miTx != null && miTx.getStatus() == Status.STATUS_MARKED_ROLLBACK)
         {
            log.error("Saw rolled back tx="+miTx);
            throw new RuntimeException("Transaction marked for rollback, possibly a timeout");
         }

         // Ok, get moving...
         ctx = (EntityEnterpriseContext) container.getInstanceCache().get(key);
         if( trace )
            log.trace("Begin while ctx==null, ctx="+ctx);
			
         //Next test is independent of whether the context is locked or not, it is purely transactional
         // Is the instance involved with another transaction? if so we implement pessimistic locking
         synchronized(ctx.getTxLock()) 
         {
            tx = ctx.getTransaction();
            if( trace )
               log.trace("Checking tx on ctx="+ctx+", tx="+tx);

            // Do we have a running transaction with the context?
            if (tx != null &&
                // And are we trying to enter with another transaction?
                !tx.equals(mi.getTransaction()))
            {
               // That's no good, only one transaction per context
               // Let's put the thread to sleep the transaction demarcation will wake them up
               if( trace )
                  log.trace("Transactional contention on context"+ctx.getId());

               // Wait for it to finish, note that we use wait() and not wait(5000), why? 
               // cause we got cojones, if there a problem in this code we want a freeze not illusion
               // Threads finishing the transaction must notifyAll() on the ctx.txLock
               //
               // billb: wait() is good for debugging purposes, but transaction timeouts will
               // never rollback this thread.  wait() will wait forever and ever.  You must uncomment
               // the wait(timeout) lines below to turn on transaction timeouts.  BTW, remove this
               // comment when wait() is finally removed.
               try
               {
                  if( trace )
                     log.trace("Begin wait on TxLock="+tx);
                  // FIXME: Uncomment this next line to enable transaction timeouts
                  // ctx.getTxLock().wait(timeout);
                  ctx.getTxLock().wait(); // FIXME, delete this line when 2.5 is released
                  if( trace )
                     log.trace("End wait on TxLock="+tx);
               }

               // We need to try again
               finally {ctx = null; continue;}
            }

            /*
              In future versions we can use copies of the instance per transaction
            */
         }

         // The next test is the pure serialization from the EJB specification.
         // If we are here we either did not have a tx(tx == null) or this is a
         // recursive call and the current ctx.tx == mi.tx
         synchronized(ctx) 
         {
            // synchronized is a time gap, when the thread enters here it can be after "sleep"
            // we need to make sure that stuff is still kosher

            // First make sure another thread who saw a null tx has not already assigned a new tx
            if( tx == null && ctx.getTransaction() != null )
            {
               ctx = null;
               if( trace )
                  log.trace("End synchronized(ctx), ctx="+ctx+", lost ctx.tx race");		
               continue;
            }

            if( trace )
               log.trace("Begin synchronized(ctx), ctx="+ctx+", mi.tx="+miTx);
            // We do not use pools any longer so the only thing that can happen is that 
            // a ctx has a null id (instance removed) (no more "wrong id" problem)
            if (ctx.getId() == null) 
            {
               // This will happen when the instance is removed from cache 
               // We need to go through the same mechs and get the new ctx
               ctx = null;
               if( trace )
                  log.trace("End synchronized(ctx), ctx="+ctx+", null id");		
               continue;
            }
            // So the ctx is still valid and the transaction is still game and we own the context
            // so no one can change ctx.  Make sure that all access to ctx is synchronized.
				
            // The ctx is still kosher go on with the real serialization
				
            //Is the context used already (with or without a transaction), is it locked?
            if (ctx.isLocked()) 
            {
               // It is locked but re-entrant calls permitted (reentrant home ones are ok as well)
               if (!isCallAllowed(mi)) 
               {
                  // This instance is in use and you are not permitted to reenter
                  // Go to sleep and wait for the lock to be released
                  if( trace )
                     log.trace("Thread contention on context"+key);

                  // we want to know about freezes so we wait(), let us know if this locks  
                  // Threads finishing invocation will come here and notify() on the ctx
                  try
                  {
                     if( trace )
                        log.trace("Begin ctx.wait(), ctx="+ctx);
                     // FIXME: Uncomment this next line to enable transaction timeouts
                     // ctx.wait(timeout);
                     ctx.wait(); //FIXME, delete this line when 2.5 is released.
                  }
                  catch (InterruptedException ignored) {}					
                  // We need to try again
                  finally
                  {
                     if( trace )
                        log.trace("End ctx.wait(), ctx="+ctx+", isLocked="+ctx.isLocked());
                     ctx = null;
                     continue;
                  }
               }
               else
               {
                  //We are in a valid reentrant call so take the lock, take it!
                  ctx.lock();
                  if( trace )
                     log.trace("In synchronized(ctx), ctx="+ctx+", reentrant call, have lock");		
               }
            }
            // No one is using that context
            else 
            {
               // We are now using the context
               ctx.lock();
               if( trace )
                  log.trace("In synchronized(ctx), ctx="+ctx+", unused ctx, have lock");		
            }

            // The transaction is associated with the ctx while we own the lock 
            ctx.setTransaction(miTx);
            if( trace )
               log.trace("End synchronized(ctx), ctx="+ctx+", set tx="+miTx);

         }// end sychronized(ctx)
         if( trace )
            log.trace("End while ctx==null, ctx="+ctx);		
      }

      // Set context on the method invocation
      mi.setEnterpriseContext(ctx);
		
      boolean exceptionThrown = false;
		
      try
      {	
         // Go on, you won
         if( trace )
            log.trace("Begin next invoker");
         Object returnValue = getNext().invoke(mi);
         if( trace )
            log.trace("End next invoker");
         return returnValue;
      }
      catch (RemoteException e)
      {
         exceptionThrown = true;
         throw e;
      } catch (RuntimeException e)
      {
         exceptionThrown = true;
         throw e;
      } catch (Error e)
      {
         exceptionThrown = true;
         throw e;
      } 
      finally
      {
         // ctx can be null if cache.get throws an Exception, for
         // example when activating a bean.
         if (ctx != null)
         {
				
            synchronized(ctx) 
            {
               ctx.unlock();
               if( trace )
                  log.trace("Ending invoke, unlock ctx="+ctx);
				
               // If an exception has been thrown, 
               if (exceptionThrown && 					
                   // if tx, the ctx has been registered in an InstanceSynchronization. 
                   // that will remove the context, so we shouldn't.
                   // if no synchronization then we need to do it by hand
                   !ctx.isTxSynchronized()) 
               {
                  // Discard instance
                  // EJB 1.1 spec 12.3.1
                  container.getInstanceCache().remove(key);
                  // Notify all those waiting on TxLock. Since this ctx is not TxSynchronized
                  // there is nobody else to wake up waiting threads.
                  if (ctx.getTransaction() != null 
                      && ctx.getTransaction().equals(mi.getTransaction()))
                  {
                     ctx.setTransaction(null);
                     synchronized(ctx.getTxLock())
                     {
                        ctx.getTxLock().notifyAll();
                     }
                  }
                  if( trace )
                     log.trace("Ending invoke, exceptionThrown, ctx="+ctx);
               }
					
               else if (ctx.getId() == null)
               {
                  // The key from the MethodInvocation still identifies the right cachekey
                  container.getInstanceCache().remove(key);
                  if( trace )
                     log.trace("Ending invoke, cache removal, ctx="+ctx);
                  // no more pool return
               }
					
               // We are done using the context so we wake up the next thread waiting for the ctx
               // marcf: I suspect we could use it only if lock = 0 (code it in the context.lock in fact)
               // this doesn't hurt here, meaning that even if we don't wait for 0 to come up 
               // we will wake up a thread that will go back to sleep and the next coming out of 
               // the body of code will wake the next one etc until we reach 0.  Reentrants are a pain
               // a minor though and I really suspect not checking for 0 is quite ok in all cases.
               if( trace )
                  log.trace("Ending invoke, send notifyAll ctx="+ctx);
               ctx.notifyAll();
            }
         }// synchronized ctx
         if( trace )
            log.trace("End invoke, key="+key+", ctx="+ctx);
      } // finally
   }

   // Private --------------------------------------------------------
	
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
      catch (Exception x) {x.printStackTrace();}
   }
	
   private boolean isCallAllowed(MethodInvocation mi)
   {
      boolean reentrant = ((EntityMetaData)container.getBeanMetaData()).isReentrant();
		
      if (reentrant)
      {
         return true;
      }
      else
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
      }
		
      return false;
   }
}
