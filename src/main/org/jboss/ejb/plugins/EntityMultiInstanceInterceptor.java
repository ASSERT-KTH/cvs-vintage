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
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.11 $
 */
public class EntityMultiInstanceInterceptor extends AbstractInterceptor
{
   public InvocationResponse invoke(Invocation invocation) throws Exception
   {
      EntityContainer container = (EntityContainer)getContainer();
      Transaction tx = invocation.getTransaction();
      EntityEnterpriseContext ctx = null;
      boolean trace = log.isTraceEnabled();

      if(invocation.getType().isHome())
      {
         if(trace)
         {
            log.trace("Begin home invoke");
         }
         ctx = (EntityEnterpriseContext) container.getInstancePool().get();
      }
      else
      {
         Object id = invocation.getId();
         if(trace)
         {
            log.trace("Begin invoke, id=" + id);
         }

         if(tx != null)
         {
            ctx = EntityContainer.getEntityInvocationRegistry().getContext(
                  container,
                  id,
                  tx);
         }

         if(ctx == null)
         {
            ctx = (EntityEnterpriseContext)container.getInstancePool().get();
            ctx.setCacheKey(id);
            ctx.setId(id);
            container.activateEntity(ctx);
         }
      }

      // Pass it to the method invocation
      invocation.setEnterpriseContext(ctx);

      // Set the current security information
      ctx.setPrincipal(invocation.getPrincipal());

      // Associate transaction, in the new design the lock already has the 
      // transaction from the previous interceptor
      ctx.setTransaction(tx);

      return getNext().invoke(invocation);
   }
}
