/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins;

import java.security.Principal;
import org.jboss.ejb.EnterpriseContext;
import org.jboss.ejb.StatelessSessionEnterpriseContext;

/**
 *	<description> 
 *      
 *	@see <related>
 *	@author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:andreas.schaefer@madplanet.com">Andreas Schaefer</a>
 * @author <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>
 *	@version $Revision: 1.11 $
 *      
 * <p><b>Revisions:</b>
 * <p><b>20010718 andreas schaefer:</b>
 * <ul>
 * <li>- Added Statistics Gathering
 * </ul>
*  <p><b>20010920 Sacha Labourey:</b>
*  <ul>
*  <li>- Activate pooling for SLSB
*  </ul>
 */
public class StatelessSessionInstancePool
   extends AbstractInstancePool
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------
   
   // Z implementation ----------------------------------------------
   public void create()
      throws Exception
   {
      super.create();
      // for SLSB, we *do* pool
      this.reclaim = true;
   }
    
   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
   protected EnterpriseContext create(Object instance, Principal callerPrincipal)
      throws Exception
   {
      mInstantiate.add();
      return new StatelessSessionEnterpriseContext(instance, getContainer(), callerPrincipal);
   }
    
   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------

}

