/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins;

import org.jboss.ejb.EnterpriseContext;
import org.jboss.ejb.MessageDrivenEnterpriseContext;

/**
 * A message driven bean instance pool.
 *
 * @version <tt>$Revision: 1.13 $<//t>
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:peter.antman@tim.se">Peter Antman</a>.
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
 * <li>- Activate pooling for MDB
 * </ul>
 */
public class MessageDrivenInstancePool
   extends AbstractInstancePool
{
   protected void createService() throws Exception
   {
      super.createService();

      // for MDB, we *do* pool
      this.reclaim = true;
   }
   
   protected EnterpriseContext create(Object instance)
      throws Exception
   {
      return new MessageDrivenEnterpriseContext(instance, getContainer());
   }
}






