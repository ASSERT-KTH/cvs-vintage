/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.system;

import java.io.File;
import java.net.URL;

/**
 * The JMX MBean interface for the <tt>ServerConfig</tt> component.
 *      
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @version $Revision: 1.1 $
 */
public interface ServerConfigMBean
{
   String getDomain();

   File getHomeDir();
   
   URL getInstallURL();
   
   URL getLibraryURL();
   
   String getConfigName();
   
   URL getConfigURL();
   
   URL getPatchURL();
   
   URL getSpineURL();

   /**
    * Enable or disable exiting the JVM when {@link #shutdown} is called.
    * If enabled, then shutdown calls {@link #exit}.  If disabled, then
    * only the shutdown hook will be run.
    *
    * @param flag    True to enable calling exit on shutdown.
    */
   void setExitOnShutdown(boolean flag);

    /**
     * Get the current value of the exit on shutdown flag.  Default value is
     * false, though it will be set to true when bootstrapped with 
     * {@link org.jboss.Main}.
     *
     * @return    The current value of the exit on shutdown flag.
     */
   boolean getExitOnShutdown();
}
