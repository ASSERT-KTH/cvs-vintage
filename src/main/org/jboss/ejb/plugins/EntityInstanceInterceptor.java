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
import org.jboss.metadata.EntityMetaData;
import org.jboss.logging.Logger;
import org.jboss.util.Sync;

/**
*   This container acquires the given instance.
*
*   @see <related>
*   @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
*   @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
*   @author <a href="mailto:sebastien.alborini@m4x.org">Sebastien Alborini</a>
*   @version $Revision: 1.31 $
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
            if (ctx.getId() == null)
            {
                container.getInstancePool().free(ctx);
            }
        }
    }

    public Object invoke(MethodInvocation mi)
    throws Exception
    {
        // The id store is a CacheKey in the case of Entity
        CacheKey key = (CacheKey)mi.getId();

        // Get cache
        AbstractInstanceCache cache = (AbstractInstanceCache)container.getInstanceCache();
        BeanSemaphore mutex = (BeanSemaphore)cache.getLock(key);

        EnterpriseContext ctx = null;
	boolean exceptionThrown = false;

        try
        {
	    boolean waitingOnTransaction = false; // So we don't output LOCKING-WAITING all the time
	    boolean waitingOnContext = false; // So we don't output LOCKING-WAITING all the time
            do
            {
                if (mi.getTransaction() != null && mi.getTransaction().getStatus() == Status.STATUS_MARKED_ROLLBACK)
                    throw new RuntimeException("Transaction marked for rollback, possibly a timeout");

		try
		{
		    
		    mutex.acquire();

		    // This loop guarantees a mutex lock for the specified object id 
		    // When a cache.remove happens, it disassociates the mutex from the bean's id,
		    // Thus, the mutex in this code could be invalid.  We avoid this problem with the following loop.
		    //
		    // Also we should have a while loop so we don't mess up the finally clause that does
		    // mutex.release()
		    //
		    while (!mutex.isValid())
		    {
			BeanSemaphore newmutex = (BeanSemaphore)cache.getLock(key);
			mutex.release();
			mutex = newmutex;
			
			// Avoid infinite loop.
			if (mi.getTransaction() != null && mi.getTransaction().getStatus() == Status.STATUS_MARKED_ROLLBACK)
			    throw new RuntimeException("Transaction marked for rollback, possibly a timeout");
			
			mutex.acquire();
		    }
                    // Get context
                    ctx = cache.get(key);

                    // Do we have a running transaction with the context
                    Transaction tx = ctx.getTransaction();
                    if (tx != null &&
                        // And are we trying to enter with another transaction
                        !tx.equals(mi.getTransaction()))
                    {
                        // Let's put the thread to sleep a lock release will wake the thread
                        // Possible deadlock
			if (!waitingOnTransaction)
			{
			    Logger.debug("LOCKING-WAITING (TRANSACTION) in Thread " 
					 + Thread.currentThread().getName() 
					 + " for id "+ctx.getId()+" ctx.hash "+ctx.hashCode()
					 +" tx:"+((tx == null) ? "null" : tx.toString()));
			    waitingOnTransaction = true;
			}

                        // Try your luck again
                        ctx = null;
			Thread.yield(); // Give the OS some help.
                        continue;
                    }
		    if (waitingOnTransaction) 
		    {
			Logger.debug("FINISHED-LOCKING-WAITING (TRANSACTION) in Thread " 
				     + Thread.currentThread().getName() 
				     + " for id "+ctx.getId()
				     +" ctx.hash "+ctx.hashCode()
				     +" tx:"+((tx == null) ? "null" : tx.toString()));
			waitingOnTransaction = false;
		    }
		    // OK so we now know that for this PrimaryKey, no other
		    // thread has a transactional lock on the bean.
		    // So, let's setTransaction of the ctx here instead of in later code.
		    // I really don't understand why it wasn't  done here anyways before.
		    // Later on, in the finally clause, I'll check to see if the ctx was
		    // invoked upon and if not, dissassociate the transaction with the ctx.
		    // If there is no transactional assocation here, there is a race condition
		    // for re-entrant entity beans, so don't remove the code below.
		    //
		    // If this "waitingOnTransaction" loop is ever removed in favor of
		    // something like using db locks instead, this transactional assocation
		    // must be removed.
		    if (mi.getTransaction() != null && tx != null && !tx.equals(mi.getTransaction()))
		    {
			// Do transactional "lock" on ctx right now!
			ctx.setTransaction(mi.getTransaction());
		    }
		    // If we get here it's the right tx, or no tx
		    if (!ctx.isLocked())
		    {
			//take it!
			ctx.lock();
		    }
		    else
		    {
			if (!isCallAllowed(mi)) 
			{
			    // Go to sleep and wait for the lock to be released
                            // This is not one of the "home calls" so we need to wait for the lock
			    
                            // Possible deadlock
			    if (!waitingOnContext) 
			    {
				Logger.debug("LOCKING-WAITING (CTX) in Thread " 
					     + Thread.currentThread().getName() 
					     + " for id "+ctx.getId()
					     +" ctx.hash "+ctx.hashCode());
				waitingOnContext = true;
			    }
			    
                            // Try your luck again
			    ctx = null;
			    Thread.yield(); // Help out the OS.
			    continue;
			}
			else
			{
                                //We are in a home call so take the lock, take it!
			    ctx.lock();
			}
		    }
		    if (waitingOnContext) 
		    {
			Logger.debug("FINISHED-LOCKING-WAITING (CTX) in Thread " 
				     + Thread.currentThread().getName() 
				     + " for id "
				     + ctx.getId()
				     + " ctx.hash " + ctx.hashCode());
			waitingOnContext = false;
		    }
                }
		catch (InterruptedException ignored) {}
		finally
		{
		    mutex.release();
		}
            } while (ctx == null);

            // Set context on the method invocation
            mi.setEnterpriseContext(ctx);

            // Go on, you won
            return getNext().invoke(mi);

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
	    try
	    {
		mutex.acquire();
		// Logger.debug("Release instance for "+id);
		// ctx can be null if cache.get throws an Exception, for
		// example when activating a bean.
		if (ctx != null)
		{
		    // unlock the context
		    ctx.unlock();
		    
		    Transaction tx = ctx.getTransaction();
		    if (tx != null 
			&& mi.getTransaction() != null
			&& tx.equals(mi.getTransaction()) 
			&& !((EntityEnterpriseContext)ctx).isInvoked())
		    {
			// The context has been associated with this method's transaction
			// but the entity has not been invoked upon yet, so let's
			// disassociate the transaction from the ctx.
			// I'm doing this because I'm assuming that the bean hasn't been registered with
			// the TxManager.
			ctx.setTransaction(null);
		    }
		    
		    // If an exception has been thrown, DO NOT remove the ctx
		    // if the ctx has been registered in an InstanceSynchronization.
		    // InstanceSynchronization will remove the key for us
		    if (exceptionThrown && 
			(tx == null || (mi.getTransaction() != null && !((EntityEnterpriseContext)ctx).isInvoked()))) 
		    {
			// Discard instance
			// EJB 1.1 spec 12.3.1
			//
			cache.remove(key);
		    }
		    else if (ctx.getId() == null)
		    {
			// Work only if no transaction was encapsulating this remove()
			if (ctx.getTransaction() == null)
			{
			    // Here we arrive if the bean has been removed and no
			    // transaction was associated with the remove, or if
			    // the bean has been passivated
				    
			    // Remove from cache
			    cache.remove(key);
			    
			    // It has been removed -> send to the pool
			    // REVISIT: FIXME:
			    // We really should only let passivation free an instance because it is
			    // quite possible that another thread is working with
			    // the same context, but let's do it anyways.
			    //
			    container.getInstancePool().free(ctx);
			}
			else 
			{
			    // We want to remove the bean, but it has a Tx associated with 
			    // the remove() method. We remove it from the cache, to avoid
			    // that a successive insertion with same pk will break the
			    // cache. Anyway we don't free the context, since the tx must
			    // finish. The EnterpriseContext instance will be GC and not
			    // recycled.
			    cache.remove(key);
			}
		    }
		}
		else
		{
		    /*
		     * This used to happen in the old code.
		     * Why?  If the ctx is null, why should we remove it? Another thread could
		     * be screwed up by this.
		      if (exceptionThrown)
		      {
		      // Discard instance
		      // EJB 1.1 spec 12.3.1
		      cache.remove(key);
		      }
		    */
		}
	    }
	    finally
	    {
		mutex.release();
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
