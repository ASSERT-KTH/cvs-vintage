/**
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins;

import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.Transaction;

import org.jboss.ejb.BeanLock;
import org.jboss.ejb.BeanLockManager;
import org.jboss.ejb.Container;
import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.InstancePool;
import org.jboss.ejb.entity.EntitySynchronizationInterceptor;
import org.jboss.metadata.ConfigurationMetaData;

/**
 * @todo remove when multi instance is mergged as simple flas on 
 * the enditiy container
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.9 $
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
