/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.system;

import java.io.File;

import java.net.URL;
import java.net.MalformedURLException;

/**
 * A container for the basic configuration elements required to create
 * a Server instance.
 *
 * <p>MalformedURLException are rethrown as RuntimeExceptions, so that
 *    code that needs to access these values does not have to directly 
 *    worry about problems with lazy construction of final URL values.
 *    Should use nested version when JDK 1.4 is standardized.
 *      
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @version $Revision: 1.4 $
 */
public class ServerConfig
   implements ServerConfigMBean
{
   /** The local home directory for the server. */
   private File homeDir;

   /** The directory for server temporay files. */
   private File tempDir;

   /** The directory for local data files. */
   private File dataDir;
   
   /** The base installation URL, used to construct other URLs. */
   private URL installURL;

   /** The configuration name to use. */
   private String configName = "default";
   
   /** The URL where configuration files will be found. */
   private URL configURL;

   /** The base URL where spine libraies will be loaded from. */
   private URL spineURL;

   /** The base URL where library files are. */
   private URL libraryURL;
   
   /** The base URL where patch files will be loaded from. */
   private URL patchURL;
   
   /** Exit on shutdown flag. */
   private boolean exitOnShutdown = false;

   /** 
    * Construct a new <tt>ServerConfig</tt> instance.
    *
    * @param dir   The local home directory.
    * @param url   The base install URL.
    */
   public ServerConfig(final File dir, final URL url) 
      throws MalformedURLException 
   {
      setHomeDir(dir);
      setInstallURL(url);
   }
   
   /** 
    * Construct a new <tt>ServerConfig</tt> instance.
    *
    * @param dir   The local home and installation directory.
    */
   public ServerConfig(final File dir) 
      throws MalformedURLException 
   {
      this(dir, dir.toURL());
   }

   public void setConfigName(final String name) {
      if (name == null)
         throw new IllegalArgumentException("name is null");
      
      configName = name;
   }
   
   public String getConfigName() {
      return configName;
   }

   public void setHomeDir(final File dir) {
      if (dir == null)
         throw new IllegalArgumentException("dir is null");

      // FIXME: check if this is really a directory      
      
      homeDir = dir;
   }

   public File getHomeDir() {
      return homeDir;
   }

   public void setTempDir(final File dir) {
      if (dir == null)
         throw new IllegalArgumentException("dir is null");
      
      // FIXME: check if this is really a directory
      // FIXME: check if this is writable
      tempDir = dir;
   }
   
   /**
    * Get the directory where temporary files will be stored.
    *
    * @return    The directory where the server stores temporary files.
    */
   public File getTempDir() {
      if (tempDir == null) {
         tempDir = new File(homeDir, "tmp");
      }
      
      return tempDir;
   }

   public void setDataDir(final File dir) {
      if (dir == null)
         throw new IllegalArgumentException("dir is null");
      
      // FIXME: check if this is really a directory
      // FIXME: check if this is writable
      dataDir = dir;
   }

   /**
    * Get the directory where local data will be stored.
    *
    * @return    The directory where the server stores local data.
    */
   public File getDataDir() {
      if (dataDir == null) {
         dataDir = new File(homeDir, "db");
      }

      return dataDir;
   }
   
   public void setInstallURL(final URL url) {
      if (url == null)
         throw new IllegalArgumentException("url is null");

      // should probably ensure that installURL ends with '/' ?
      
      installURL = url;
   }
   
   public URL getInstallURL() {
      return installURL;
   }
   
   public void setLibraryURL(final URL url) {
      if (url == null)
         throw new IllegalArgumentException("url is null");
      
      libraryURL = url;
   }
   
   public URL getLibraryURL() {
      if (libraryURL == null) {
         try {
            libraryURL = new URL(installURL, "lib/ext/");
         }
         catch (MalformedURLException e) {
            throw new RuntimeException(e.toString());
         }
      }
      return libraryURL;
   }

   public void setConfigURL(final URL url) {
      if (url == null)
         throw new IllegalArgumentException("url is null");
      
      configURL = url;
   }
   
   public URL getConfigURL() {
      if (configURL == null) {
         try {
            configURL = new URL(installURL, "conf/" + configName + "/");
         }
         catch (MalformedURLException e) {
            throw new RuntimeException(e.toString());
         }
      }
      return configURL;
   }
   
   public void setPatchURL(final URL url) {
      if (url == null)
         throw new IllegalArgumentException("url is null");
      
      patchURL = url;
   }
   
   public URL getPatchURL() {
      return patchURL;
   }
   
   public void setSpineURL(final URL url) {
      if (url == null)
         throw new IllegalArgumentException("url is null");
      
      spineURL = url;
   }
   
   public URL getSpineURL() {
      if (spineURL == null) {
         try { 
            spineURL = new URL(installURL, "lib/");
         }
         catch (MalformedURLException e) {
            throw new RuntimeException(e.toString());
         }
      }
      return spineURL;
   }

   /**
    * Enable or disable exiting the JVM when {@link #shutdown} is called.
    * If enabled, then shutdown calls {@link #exit}.  If disabled, then
    * only the shutdown hook will be run.
    *
    * @param flag    True to enable calling exit on shutdown.
    */
   public void setExitOnShutdown(final boolean flag) {
      exitOnShutdown = flag;
   }

    /**
     * Get the current value of the exit on shutdown flag.  Default value is
     * false, though it will be set to true when bootstrapped with 
     * {@link org.jboss.Main}.
     *
     * @return    The current value of the exit on shutdown flag.
     */
   public boolean getExitOnShutdown() {
      return exitOnShutdown;
   }
}
