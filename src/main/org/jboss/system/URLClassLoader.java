/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.system;

import java.net.URL;
import java.io.InputStream;

/**
 * The URLClassLoader is associated with a given URL.
 * It can load jar and sar.
 * 
 * <p>The ServiceLibraries keeps track of the UCL and asks everyone for
 *    resources and classes.
 *
 * @author <a href="marc.fleury@jboss.org">Marc Fleury</a>
 * @author <a href="christoph.jung@jboss.org">Christoph G. Jung</a>
 * @version $Revision: 1.11 $
 * 
 * <p><b>20010830 marc fleury:</b>
 * <ul>
 *   <li>Initial import
 * </ul>
 * <p><b>20011009 cgj:</b>
 * <ul>
 *   <li>fixed default resolution behaviour
 * </ul>
 */
public class URLClassLoader
   extends java.net.URLClassLoader
   implements URLClassLoaderMBean
{
   /** 
    * This is just a key used for identifying the classloader,
    * nothing is actually loaded from it.  Classes and resources are 
    * loaded from local copies or unpacked local copies.
    *
    * One URL per classLoader in our case.
    */   
   private URL keyUrl;
   
   /** An SCL can also be loading on behalf of an MBean */
   //private ObjectName mbean = null; not used
   
   /** All SCL are just in orbit around a basic ServiceLibraries */
   private static ServiceLibraries libraries;
   
   /** The bootstrap interface to the log4j system */
   private static BootstrapLogger log = BootstrapLogger.getLogger(URLClassLoader.class);
   
   /**
    * One url per SCL
    */
   public URLClassLoader(final URL[] urls, final URL keyUrl)
   {
      super(urls);
      this.keyUrl = keyUrl;

      try
      {
         if (libraries == null)
         {
            libraries = ServiceLibraries.getLibraries();
         }
         
         // A URL enabled SCL must register itself with the libraries to be queried
         libraries.addClassLoader(this);
      }
      catch(Exception e)
      {
         // e.printStackTrace();
         log.warn("Could not open URL: " + keyUrl, e);
      }
   }
   
   /**
    * Create a self keyed loader based on the given URL.
    *
    * <p>Only the boot urls are keyed on themselves.
    *    Everything else is copied for loading but keyed on the
    *    original deployed url.
    *
    * <p>Only Server needs to access this constructor, so make it package
    *    private to avoid improper usage.
    */
   URLClassLoader(final URL url)
   {
      this (new URL[]{ url }, url);
   }
   
   public URL getKeyURL()
   {
      return keyUrl;
   }
   
   /**
    * We intercept the load class to know exactly the dependencies
    * of the underlying jar
    */
   public Class loadClass(String name, boolean resolve)
      throws ClassNotFoundException
   {
      return libraries.loadClass(name, resolve, this);	
   }
   
   public Class loadClassLocally(String name, boolean resolve)
      throws ClassNotFoundException
   {
      return super.loadClass(name, resolve);
   }
   
   public URL getResource(String name)
   {
      URL resource = super.getResource(name);
      
      if (resource == null)
      {
         resource = libraries.getResource(name, this);
      }
      
      /*
      if (resource == null)
      {
      if( log.isTraceEnabled() )
      log.trace("Did not find the UCL resource "+name);
      }
      */
      return resource;
   }
   
   public URL getResourceLocally(String name)
   {
      return super.getResource(name);
   }
   
   public InputStream getResourceAsStream(String name)
   {
      try
      {
         URL resourceUrl = getResource(name);
         
         if (resourceUrl != null)
         {
            return resourceUrl.openStream();
         }
      }
      catch (Exception ignore) {}
      
      return null;
   }
   
   public int hashCode() 
   {
      return keyUrl.hashCode();
   }
   
   public boolean equals(Object other) 
   {
      if (other instanceof URLClassLoader) 
      {
         return ((URLClassLoader) other).getKeyURL().equals(keyUrl);
      }
      return false;
   }
   
   public String toString()
   {
      StringBuffer tmp = new StringBuffer("JBoss URLClassloader: keyURL : ");
      tmp.append(getKeyURL());
      tmp.append(", URLS: ");
      URL[] urls = getURLs();
      tmp.append('[');
      for(int u = 0; u < urls.length; u ++)
      {
         tmp.append(urls[u]);
         tmp.append(',');
      }
      tmp.append(']');
      return tmp.toString();
   }
}
