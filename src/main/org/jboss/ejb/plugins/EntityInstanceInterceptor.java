/*
* jBoss, the OpenSource EJB server
*
* Distributable under GPL license.
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
import org.jboss.metadata.EntityMetaData;
import org.jboss.logging.Logger;

/**
*   This container acquires the given instance. 
*
*   @see <related>
*   @author Rickard Öberg (rickard.oberg@telkel.com)
*   @author <a href="marc.fleury@telkel.com">Marc Fleury</a>
*   @version $Revision: 1.14 $
*/
public class EntityInstanceInterceptor
extends AbstractInterceptor
{
    // Constants -----------------------------------------------------
    
    // Attributes ----------------------------------------------------
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
    
    // Interceptor implementation --------------------------------------
    public Object invokeHome(MethodInvocation mi)
    throws Exception
    {
       // Get context
       EnterpriseContext ctx = ((EntityContainer)getContainer()).getInstancePool().get();
       mi.setEnterpriseContext(ctx);
       
       // It is a new context for sure so we can lock it
       ctx.lock();
       
       try
       {
         // Invoke through interceptors
         return getNext().invokeHome(mi);
       } finally
       {
         // Always unlock, no matter what
         ctx.unlock();
         
         // Still free? Not free if create() was called successfully
         if (mi.getEnterpriseContext().getId() == null)
         {
          container.getInstancePool().free(mi.getEnterpriseContext());
         } 
         else
         {
          // DEBUG           Logger.debug("Entity was created; not returned to pool");
          synchronized (ctx) {
              
              //Let the waiters know
              ctx.notifyAll();
          }
         }
       }
    }
    
    public Object invoke(MethodInvocation mi)
    throws Exception
    {
       // The id store is a CacheKey in the case of Entity 
       CacheKey key = (CacheKey) mi.getId();
       
       // Get cache
       InstanceCache cache = ((EntityContainer)getContainer()).getInstanceCache();
       
       EnterpriseContext ctx = null;
       
       // We synchronize the locking logic (so that the invoke is unsynchronized and can be reentrant)
       synchronized (cache)
       {
         do
         {
          // Get context
          ctx = cache.get(key);
          
          // Do we have a running transaction with the context
          if (ctx.getTransaction() != null &&
              // And are we trying to enter with another transaction
              !ctx.getTransaction().equals(mi.getTransaction())) 
          {
              // Let's put the thread to sleep a lock release will wake the thread
              synchronized (ctx)
              {
                 // Possible deadlock
                 Logger.log("LOCKING-WAITING for id "+ctx.getId()+" ctx.hash "+ctx.hashCode()+" tx.hash "+ctx.getTransaction().hashCode());
                 
                 try{ctx.wait(5000);}
                   catch (InterruptedException ie) {}
              }
              
              // Try your luck again
              ctx = null;
              continue;
          }
          
          if (!ctx.isLocked()){
              
              //take it!
              ctx.lock();  
          }                                                            
          
          else 
                {
                    if (!isCallAllowed(mi)) {
                        
                        // Go to sleep and wait for the lock to be released
                        // This is not one of the "home calls" so we need to wait for the lock
                        synchronized (ctx)
                        {
                            // Possible deadlock
                            Logger.log("LOCKING-WAITING for id "+ctx.getId()+" ctx.hash "+ctx.hashCode());
                            
                            try{ctx.wait(5000);}
                                catch (InterruptedException ie) {}
                        }
                        
                        // Try your luck again
                        ctx = null;
                        continue;
                        // Not allowed reentrant call
                        //throw new RemoteException("Reentrant call");
                    
              } else
              {
                 //We are in a home call so take the lock, take it!
                 ctx.lock();  
              }
          }
                                                                     
         } while (ctx == null);
       }
         
       // Set context on the method invocation
       mi.setEnterpriseContext(ctx);
       
       try {
         // Go on, you won
         return getNext().invoke(mi);
       
       } 
       catch (RemoteException e)
       {
         // Discard instance
         // EJB 1.1 spec 12.3.1
         ((EntityContainer)getContainer()).getInstanceCache().remove(key.id);
         
         throw e;
       } catch (RuntimeException e)
       {
         // Discard instance
         // EJB 1.1 spec 12.3.1
         ((EntityContainer)getContainer()).getInstanceCache().remove(key.id);
         
         throw e;
       } catch (Error e)
       {
         // Discard instance
         // EJB 1.1 spec 12.3.1
         ((EntityContainer)getContainer()).getInstanceCache().remove(key.id);
         
         throw e;
       } finally
       {
         //         Logger.debug("Release instance for "+id);
         if (ctx != null)
         {
          
          synchronized (ctx) {
              
              // unlock the context
              ctx.unlock();
              
              if (ctx.getId() == null)                             
              {
                 // Remove from cache
                 cache.remove(key.id);
                 
                 // It has been removed -> send to free pool
                 container.getInstancePool().free(ctx);
              }
              
              // notify the thread waiting on ctx
              ctx.notifyAll();
          }
         }
       }
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

