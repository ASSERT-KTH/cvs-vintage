/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins;

import java.rmi.RemoteException;

import org.jboss.ejb.Container;
import org.jboss.ejb.EnterpriseContext;
import org.jboss.ejb.StatelessSessionEnterpriseContext;

/**
 *	<description> 
 *      
 *	@see <related>
 *	@author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:andreas.schaefer@madplanet.com">Andreas Schaefer</a>
 *	@version $Revision: 1.7 $
 *      
 * <p><b>Revisions:</b>
 * <p><b>20010718 andreas schaefer:</b>
 * <ul>
 * <li>- Added Statistics Gathering
 * </ul>
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
   public void init()
      throws Exception
   {
   }
    
   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
   protected EnterpriseContext create(Object instance)
      throws Exception
   {
      mInstantiate.add();
      return new StatelessSessionEnterpriseContext(instance, getContainer());
   }
    
   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------

}

