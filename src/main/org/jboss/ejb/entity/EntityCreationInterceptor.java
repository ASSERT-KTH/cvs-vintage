/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.entity;

import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.plugins.AbstractInterceptor;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationKey;
import org.jboss.invocation.InvocationType;
import org.jboss.invocation.InvocationResponse;
import org.jboss.invocation.PayloadKey;

/**
 * This interceptor does a double invocation for entity creation.  The first
 * invocation is for ejbCreate and the second for ejbPostCreate.  This makes
 * ejbPostCreate work like another business method.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.1 $
 */
public final class EntityCreationInterceptor extends AbstractInterceptor
{
   public InvocationResponse invoke(Invocation invocation) throws Exception
   {
      if(LifeCycleEvent.get(invocation) != LifeCycleEvent.CREATE)
      {
         // these are not the droids you're looking for...
         return getNext().invoke(invocation);
      }
      else
      {
         // invoke ejbCreate
         getNext().invoke(invocation);

         // now we are going to invoke ejbPostCreate
         LifeCycleEvent.set(invocation, LifeCycleEvent.POST_CREATE);

         if(invocation.getType().isLocal())
         {
            // Post create is not a home invocation
            invocation.setType(InvocationType.LOCAL);

            // invoke ejbPostCreate
            getNext().invoke(invocation);

            // reset type and lifecycle
            LifeCycleEvent.set(invocation, LifeCycleEvent.CREATE);
            invocation.setType(InvocationType.LOCALHOME);

            // return the actual entity 
            EntityEnterpriseContext ctx = 
               (EntityEnterpriseContext) invocation.getEnterpriseContext();
            return new InvocationResponse(ctx.getEJBLocalObject());
         } 
         else
         {
            // Post create is not a home invocation
            invocation.setType(InvocationType.REMOTE);

            // invoke ejbPostCreate
            getNext().invoke(invocation);

            // reset type and lifecycle
            LifeCycleEvent.set(invocation, LifeCycleEvent.CREATE);
            invocation.setType(InvocationType.HOME);

            // return the actual entity 
            EntityEnterpriseContext ctx = 
               (EntityEnterpriseContext) invocation.getEnterpriseContext();
            return new InvocationResponse(ctx.getEJBObject());
         }
      }
   }
}
