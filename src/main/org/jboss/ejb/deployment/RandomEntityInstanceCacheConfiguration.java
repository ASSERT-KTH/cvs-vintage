/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.deployment;

/**
 *	<description> 
 *      
 *	@see <related>
 *	@author Rickard Öberg (rickard.oberg@telkel.com)
 *	@version $Revision: 1.2 $
 */
public class RandomEntityInstanceCacheConfiguration
   extends NoPassivationEntityInstanceCacheConfiguration
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   int passivationInterval = 20;
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   // Public --------------------------------------------------------
   public void setPassivationInterval(int pi) { this.passivationInterval = pi; }
   public int getPassivationInterval() { return passivationInterval; }
   
   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
    
   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}

