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
*   @author Rickard Öberg (rickard.oberg@telkel.com)
*   @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
*   @author <a href="mailto:sebastien.alborini@m4x.org">Sebastien Alborini</a>
*   @version $Revision: 1.26 $
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
        Sync mutex = (Sync)cache.getLock(key);

        EnterpriseContext ctx = null;

        try
        {
            do
            {
                if (mi.getTransaction() != null && mi.getTransaction().getStatus() == Status.STATUS_MARKED_ROLLBACK)
                    throw new RuntimeException("Transaction marked for rollback, possibly a timeout");

				try
				{

					mutex.acquire();

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
                        Logger.debug("LOCKING-WAITING (TRANSACTION) for id "+ctx.getId()+" ctx.hash "+ctx.hashCode()+" tx:"+((tx == null) ? "null" : tx.toString()));

                        // Try your luck again
                        ctx = null;
                        continue;
                    }
                    else
                    {
                        // If we get here it's the right tx, or no tx
                        if (!ctx.isLocked())
                        {
                            //take it!
                            ctx.lock();
                        }
                        else
                        {
                            if (!isCallAllowed(mi)) {

                                // Go to sleep and wait for the lock to be released
                                // This is not one of the "home calls" so we need to wait for the lock

                                // Possible deadlock
                                Logger.debug("LOCKING-WAITING (CTX) for id "+ctx.getId()+" ctx.hash "+ctx.hashCode());

                                // Try your luck again
                                ctx = null;
                                continue;
                                // Not allowed reentrant call
                                //throw new RemoteException("Reentrant call");
                            }
                            else
                            {
                                //We are in a home call so take the lock, take it!
                                ctx.lock();
                            }
                        }
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
            // Discard instance
            // EJB 1.1 spec 12.3.1
            cache.remove(key);

            throw e;
        } catch (RuntimeException e)
        {
            // Discard instance
            // EJB 1.1 spec 12.3.1
            cache.remove(key);

            throw e;
        } catch (Error e)
        {
            // Discard instance
            // EJB 1.1 spec 12.3.1
            cache.remove(key);

            throw e;
        } finally
        {
            //         Logger.debug("Release instance for "+id);
			try
			{
				mutex.acquire();

				// unlock the context
				ctx.unlock();

				if (ctx.getId() == null)
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
						container.getInstancePool().free(ctx);
				    }
				}
				else
				{
					// Yeah, do nothing
				}
            }
			catch (InterruptedException ignored) {}
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
