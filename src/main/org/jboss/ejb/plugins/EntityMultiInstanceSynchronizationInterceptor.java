/**
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins;


import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.InstancePool;
import org.jboss.ejb.entity.EntitySynchronizationInterceptor;

/**
 * @todo remove when multi instance is mergged as simple flas on
 * the enditiy container
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.10 $
 */
public class EntityMultiInstanceSynchronizationInterceptor
   extends EntitySynchronizationInterceptor
{
   public void create() throws Exception
   {
      super.create();

      // DAIN: if not an EntityInstancePool should we throw an exception?
      InstancePool pool = getContainer().getInstancePool();
      if(pool instanceof EntityInstancePool)
      {
         ((EntityInstancePool)pool).setReclaim(true);
      }

      // Dain: remove this when Multi instance is merged as a simple flag
      // on the container
      ((EntityContainer)getContainer()).setMultiInstance(true);
   }
}
