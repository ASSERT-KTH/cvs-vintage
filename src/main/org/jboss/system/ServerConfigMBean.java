/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.system;

import java.io.File;

import java.net.URL;

import javax.management.ObjectName;

import org.jboss.util.ObjectNameFactory;

/**
 * The JMX MBean interface for the <tt>ServerConfig</tt> component.
 *      
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @version $Revision: 1.4 $
 */
public interface ServerConfigMBean
{
   /** The default object name. */
   ObjectName OBJECT_NAME =
      ObjectNameFactory.create("jboss.system", 
                               "service", 
                               "ServerConfig");

   /**
    * Get the configuration name of the server.
    *
    * @return    The configuration name of the server.
    */
   String getConfigName();

   /**
    * Get the local home directory which the server is running from.
    *
    * @return    The local server home directory.
    */
   File getHomeDir();

   /**
    * Get the directory where temporary files will be stored.
    *
    * @return    The directory where the server stores temporary files.
    */
   File getTempDir();

   /**
    * Get the directory where local data will be stored.
    *
    * @return    The directory where the server stores local data.
    */
   File getDataDir();
   
   /**
    * Get the installation URL for the server.
    *
    * @return    The installation URL for the server.
    */
   URL getInstallURL();
   
   /**
    * Get the library URL for the server.
    *
    * @return    The library URL for the server.
    */
   URL getLibraryURL();
   
   /**
    * Get the configuration URL for the server.
    *
    * @return    The configuration URL for the server.
    */
   URL getConfigURL();
   
   /**
    * Get the patch URL for the server.
    *
    * @return    The patch URL for the server.
    */
   URL getPatchURL();
   
   /**
    * Get the spine URL for the server.
    *
    * @return    The spine URL for the server.
    */
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
