/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.ejb.plugins;

import javax.transaction.Transaction;
import org.jboss.ejb.Container;
import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.EnterpriseContext;
import org.jboss.ejb.InstancePool;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationResponse;

/**
 * The instance interceptors role is to acquire a context representing
 * the target object from the cache.
 *    
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1.10 $
 */
public class EntityMultiInstanceInterceptor extends AbstractInterceptor
{
   public InvocationResponse invoke(Invocation invocation) throws Exception
   {
      EntityContainer container = (EntityContainer)getContainer();
      Transaction transaction = invocation.getTransaction();

      EntityEnterpriseContext ctx = null;
      if(invocation.getType().isHome())
      {
         ctx = (EntityEnterpriseContext)container.getInstancePool().get();
      }
      else
      {
         // The key
         Object key = invocation.getId();
         if(transaction != null)
         {
            ctx = container.getTxEntityMap().getCtx(transaction, key);
         }

         if(ctx == null)
         {
            ctx = (EntityEnterpriseContext)container.getInstancePool().get();
            ctx.setCacheKey(key);
            ctx.setId(key);
            container.activateEntity(ctx);
         }
      }

      // Associate transaction, in the new design the lock already has the 
      // transaction from the previous interceptor
      ctx.setTransaction(transaction);

      // Set the current security information
      ctx.setPrincipal(invocation.getPrincipal());

      // Set context on the method invocation
      invocation.setEnterpriseContext(ctx);

      return getNext().invoke(invocation);
   }
}
