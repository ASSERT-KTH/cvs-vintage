/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins;

import org.jboss.ejb.Container;
import org.jboss.ejb.BeanLock;
import org.jboss.ejb.BeanLockManager;
import org.jboss.ejb.EntityContainer;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationResponse;

/**
 * The lock interceptors role is to schedule thread wanting to invoke method 
 * on a target bean
 *
 * <p>The policies for implementing scheduling (pessimistic locking etc) is 
 * implemented by pluggable locks
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
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1.13 $
 */
public class EntityLockInterceptor extends AbstractInterceptor
{
   public InvocationResponse invoke(Invocation invocation) throws Exception
   {
      if(invocation.getType().isHome()) 
      {
         return getNext().invoke(invocation);
      }

      // The key.
      Object key = invocation.getId();

      boolean trace = log.isTraceEnabled();
      if(trace)
      {
         log.trace("Begin invoke, key=" + key);
      }

      BeanLock lock = getContainer().getLockManager().getLock(key);
      lock.schedule(invocation);
      try 
      {
         return getNext().invoke(invocation); 
      }
      finally 
      {
         // we are done with the method, decrease the count, if it reaches 0 
         // it will wake up the next thread 
         lock.sync();
         lock.endInvocation(invocation);
         lock.releaseSync(); 
      
         if(trace)
         {
            log.trace("End invoke, key=" + key);
         }
      }
   }
}
