/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.util;

/**
 *   <description> 
 *      
 *   @see <related>
 *   @author Rickard Öberg (rickard.oberg@telkel.com)
 *   @version $Revision: 1.2 $
 */
public interface ServiceMBean
	extends Service
{
   // Constants -----------------------------------------------------
    
   // Static --------------------------------------------------------

   // Public --------------------------------------------------------
   public String getName();

   public int getState();
   
   public String getStateString();
}
