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
 * @version $Revision: 1.1 $
 */
public class ServerConfig
{
   /** The local home directory for the server. */
   private File homeDir;
   
   /** The JMX domain for the base MBean server. */
   private String domain = "jboss";
   
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

   public void setDomain(final String domain) {
      if (domain == null)
         throw new IllegalArgumentException("domain is null");
      
      this.domain = domain;
   }
   
   public String getDomain() {
      return domain;
   }
   
   public void setHomeDir(final File dir) {
      if (dir == null)
         throw new IllegalArgumentException("dir is null");
      
      // check if this is really a directory ?
      
      homeDir = dir;
   }

   public File getHomeDir() {
      return homeDir;
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

   public void setConfigName(final String name) {
      if (name == null)
         throw new IllegalArgumentException("name is null");
      
      configName = name;
   }
   
   public String getConfigName() {
      return configName;
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
}
