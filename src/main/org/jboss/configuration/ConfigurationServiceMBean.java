/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.configuration;

/**
 *   <description> 
 *      
 *   @see <related>
 *   @author Rickard Öberg (rickard.oberg@telkel.com)
 *   @version $Revision: 1.2 $
 */
public interface ConfigurationServiceMBean
{
   // Constants -----------------------------------------------------
   public static final String OBJECT_NAME = ":service=Configuration";
    
   // Public --------------------------------------------------------
	public void load(String cfg)
		throws Exception;
		
	public String save()
		throws Exception;
      
   public void loadConfiguration()
      throws Exception;
      
   public void storeConfiguration()
      throws Exception;
}
