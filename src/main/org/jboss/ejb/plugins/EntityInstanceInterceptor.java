/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins;

import java.lang.reflect.Method;
import java.rmi.RemoteException;

import org.jboss.ejb.Container;
import org.jboss.ejb.BeanLock;
import org.jboss.ejb.BeanLockManager;
import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.EnterpriseContext;
import org.jboss.ejb.InstanceCache;
import org.jboss.ejb.InstancePool;
import org.jboss.invocation.Invocation;


/**
 * The instance interceptors role is to acquire a context representing
 * the target object from the cache.
 *
 * <p>This particular container interceptor implements pessimistic locking
 * on the transaction that is associated with the retrieved instance.  If
 * there is a transaction associated with the target component and it is
 * different from the transaction associated with the Invocation
 * coming in then the policy is to wait for transactional commit. 
 *   
 * <p>We also implement serialization of calls in here (this is a spec
 * requirement). This is a fine grained notify, notifyAll mechanism. We
 * notify on ctx serialization locks and notifyAll on global transactional
 * locks.
 *   
 * <p><b>WARNING: critical code</b>, get approval from senior developers
 * before changing.
 *    
 * @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @author <a href="mailto:Scott.Stark@jboss.org">Scott Stark</a>
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1.55 $
 */
public class EntityInstanceInterceptor extends AbstractInterceptor
{
   public Object invoke(Invocation invocation) throws Exception
   {
      // FIXME: There is definately dupliucat code here...
      if(invocation.getType().isHome()) 
      {
         EntityContainer container = (EntityContainer) getContainer();

         // Get context
         EntityEnterpriseContext ctx = 
            (EntityEnterpriseContext)container.getInstancePool().get();

         // Pass it to the method invocation
         invocation.setEnterpriseContext(ctx);

         // Give it the transaction
         ctx.setTransaction(invocation.getTransaction());

         // Set the current security information
         ctx.setPrincipal(invocation.getPrincipal());

         // Invoke through interceptors
         //      Object returnValue = getNext().invokeHome(invocation);
         Object returnValue = getNext().invoke(invocation);

         // Is the context now with an identity? in which case we need to insert
         if(ctx.getId() != null)
         {
            BeanLock lock = container.getLockManager().getLock(
                  ctx.getCacheKey());

            // lock all access to BeanLock
            lock.sync(); 

            try
            {
               // marcf: possible race on creation and usage
               // insert instance in cache,
               container.getInstanceCache().insert(ctx);
            }
            finally
            {
               lock.releaseSync();
            }
         }

         // Do not send back to pools in any case, let the instance be GC'ed
         return returnValue;
      }
      else
      {
         EntityContainer container = (EntityContainer) getContainer();

         // The key
         Object key = invocation.getId();

         // The context
         EntityEnterpriseContext ctx = 
            (EntityEnterpriseContext) container.getInstanceCache().get(key);

         boolean trace = log.isTraceEnabled();
         if(trace)
         {
            log.trace("Begin invoke, key=" + key);
         }

         // Associate transaction, in the new design the lock already has the 
         // transaction from the previous interceptor

         // Don't set the transction if a read-only method.  With a read-only 
         // method, the ctx can be shared between multiple transactions.
         if(!container.isReadOnly()) 
         {
            Method method = invocation.getMethod();
            if(method == null ||
                  !container.getBeanMetaData().isMethodReadOnly(method.getName()))
            {
               ctx.setTransaction(invocation.getTransaction());
            }
         }

         // Set the current security information
         ctx.setPrincipal(invocation.getPrincipal());

         // Set context on the method invocation
         invocation.setEnterpriseContext(ctx);

         boolean exceptionThrown = false;
         try
         {
            return getNext().invoke(invocation);
         }
         catch(RemoteException e)
         {
            exceptionThrown = true;
            throw e;
         }
         catch(RuntimeException e)
         {
            exceptionThrown = true;
            throw e;
         }
         catch(Error e)
         {
            exceptionThrown = true;
            throw e;
         }
         finally
         {
            // ctx can be null if cache.get throws an Exception, for
            // example when activating a bean.
            if(ctx != null)
            {
               // If an exception has been thrown,
               if(exceptionThrown &&
                     // If tx, the ctx has been registered in an 
                     // InstanceSynchronization.  That will remove the context, 
                     // so we shouldn't.  If no synchronization then we need to 
                     // do it by hand
                     !ctx.hasTxSynchronization())
               {
                  // Discard instance [EJB 1.1 spec 12.3.1]
                  container.getInstanceCache().remove(key);

                  if(trace)
                  {
                     log.trace("Ending invoke, exceptionThrown, ctx="+ctx);
                  }
               }
               else if(ctx.getId() == null)
               {
                  // The key from the Invocation still identifies the 
                  // right cachekey
                  container.getInstanceCache().remove(key);

                  if(trace)
                  {
                     log.trace("Ending invoke, cache removal, ctx=" + ctx);
                  }
                  // no more pool return
               }
            }

            if(trace)
            {
               log.trace("End invoke, key=" + key + ", ctx=" + ctx);
            }
         }
      }
   }
}
