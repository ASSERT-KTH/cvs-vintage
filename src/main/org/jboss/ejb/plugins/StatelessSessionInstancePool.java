/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins;

import org.jboss.ejb.EnterpriseContext;
import org.jboss.ejb.StatelessSessionEnterpriseContext;

/**
 * A stateless session bean instance pool.
 *      
 * @version <tt>$Revision: 1.13 $</tt>
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard �berg</a>
 * @author <a href="mailto:andreas.schaefer@madplanet.com">Andreas Schaefer</a>
 * @author <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>
 *      
 * <p><b>Revisions:</b>
 * <p><b>20010718 andreas schaefer:</b>
 * <ul>
 * <li>- Added Statistics Gathering
 * </ul>
 * <p><b>20010920 Sacha Labourey:</b>
 * <ul>
 * <li>- Activate pooling for SLSB
 * </ul>
 */
public class StatelessSessionInstancePool
   extends AbstractInstancePool
{
   protected void createService() throws Exception
   {
      super.createService();

      // for SLSB, we *do* pool
      this.reclaim = true;
   }

   protected EnterpriseContext create(Object instance)
      throws Exception
   {
      mInstantiate.add();
      return new StatelessSessionEnterpriseContext(instance, getContainer());
   }
}

