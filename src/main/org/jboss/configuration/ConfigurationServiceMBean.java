/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.configuration;

/**
 *   <description> 
 *      
 *   @see <related>
 *   @author Rickard Öberg (rickard.oberg@telkel.com)
 *   @version $Revision: 1.5 $
 */
public interface ConfigurationServiceMBean
{
   // Constants -----------------------------------------------------
   public static final String OBJECT_NAME = ":service=Configuration";
    
   // Public --------------------------------------------------------

    /**
     * Get the attribute value auto-trim flag.
     *
     * @return  True if attribute values are auto-trimmed.
     */
    boolean getAutoTrim();
   
	public void load(org.w3c.dom.Document conf)
		throws Exception;
		
	public String save()
		throws Exception;
      
   public void loadConfiguration()
      throws Exception;
      
   public void saveConfiguration()
      throws Exception;
}
