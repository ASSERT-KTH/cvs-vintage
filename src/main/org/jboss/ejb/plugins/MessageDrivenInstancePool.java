/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins;

import java.rmi.RemoteException;

import org.jboss.ejb.Container;
import org.jboss.ejb.EnterpriseContext;
import org.jboss.ejb.MessageDrivenEnterpriseContext;

/**
 *	<description> 
 *      Stolen from StatelessSessionInstancePool
 *	@see <related>
 *	@author Rickard Öberg (rickard.oberg@telkel.com)
 *      @author Peter Antman (peter.antman@tim.se)
 *	@version $Revision: 1.1 $
 */
public class MessageDrivenInstancePool
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
      return new MessageDrivenEnterpriseContext(instance, getContainer());
   }
    
   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------

}
