/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.configuration;

import org.w3c.dom.Document;

/**
 * The <em>JMX</em> admin interface for the {@link ConfigurationService}
 * MBean.
 *      
 * @author Rickard Öberg (rickard.oberg@telkel.com)
 * @version $Revision: 1.6 $
 */
public interface ConfigurationServiceMBean
{
    /** The default object name. */
    public static final String OBJECT_NAME = ":service=Configuration";

    /**
     * Get the attribute value auto-trim flag.
     *
     * @return  True if attribute values are auto-trimmed.
     */
    boolean getAutoTrim();

    /**
     * Parses the given configuration document and sets MBean attributes.
     *
     * @param configuration     The parsed configuration document.
     *
     * @throws Exception        Failed to load.
     */
    void load(Document configuration) throws Exception;
		
    /**
     * Builds a string that consists of the configuration elements of
     * the currently running MBeans registered in the server.
     *
     * @throws Exception    Failed to construct configuration.
     */
	String save() throws Exception;
		
    /**
     * Load the configuration from the configuration file,
     * installs and initailize configured MBeans and registeres the
     * beans as services.
     *
     * <p>This is a 2-step process:
     * <ol>
     *   <li>Load user conf. and create MBeans from that.
     *   <li>Apply user conf to created MBeans.
     * </ol>
     *
     * @throws Exception    ???
     */
   void loadConfiguration() throws Exception;

    /**
     * Saves the current configuration of each registered MBean to
     * the running state file file.  This will only occur if
     * a file of the that name exists in the classpath.
     *
     * @throws Exception    Failed to save configuration.
     */
   void saveConfiguration() throws Exception;
}
