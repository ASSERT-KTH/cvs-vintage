/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.deployment;

/**
 *   <description> 
 *      
 *   @see <related>
 *   @author Rickard Öberg (rickard.oberg@telkel.com)
 *   @version $Revision: 1.2 $
 */
public interface ResourceManager
{
   // Constants -----------------------------------------------------
    
   // Public --------------------------------------------------------
   public String getName();
   public String getType();
   public ResourceManagers getResourceManagers();
}
