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
 * A ClassLoader which loades classes from a single URL
 * in conjunction with the {@link ServiceLibraries}.
 * 
 * <p>The ServiceLibraries keeps track of the UCL and asks everyone for
 *    resources and classes.
 *
 * @author <a href="marc.fleury@jboss.org">Marc Fleury</a>
 * @author <a href="christoph.jung@jboss.org">Christoph G. Jung</a>
 * @author <a href="scott.stark@jboss.org">Scott Stark/a>
 * @version <tt>$Revision: 1.9 $</tt>
 * 
 * <p><b>20010830 marc fleury:</b>
 * <ul>
 *   <li>Initial import
 * </ul>
 *
 * <p><b>20011009 cgj:</b>
 * <ul>
 *   <li>fixed default resolution behaviour
 * </ul>
 */
public class UnifiedClassLoader
   extends java.net.URLClassLoader
   implements UnifiedClassLoaderMBean
{
   /** The bootstrap interface to the log4j system */
   private static BootstrapLogger log = BootstrapLogger.getLogger(UnifiedClassLoader.class);
   
   /** All SCL are just in orbit around a basic ServiceLibraries */
   private static ServiceLibraries libraries;

   /** One URL per ClassLoader in our case */   
   private URL url;
   
   /**
    * Construct a <tt>UnifiedClassLoader<tt>.
    *
    * <p>A UCL only loads classes from a single URL;
    *
    * @param url   The single URL to load classes from.
    */
   public UnifiedClassLoader(final URL url)
   {
      super(new URL[] {url}, UnifiedClassLoader.class.getClassLoader());
      
      if (log.isDebugEnabled()) log.debug("New UCL with url " + url);
      this.url = url;

      // Initialize our reference to SL
      if (libraries == null)
      {
         libraries = ServiceLibraries.getLibraries();
      }
         
      // Must register itself with the libraries to be queried
      libraries.addClassLoader(this);
   }

   /**
    * We intercept the load class to know exactly the dependencies
    * of the underlying jar.
    *
    * <p>Forwards request to {@link ServiceLibraries}.
    */
   public Class loadClass(String name, boolean resolve)
      throws ClassNotFoundException
   {
      return libraries.loadClass(name, resolve, this);  
   }

   /**
    * Provides the same functionality as {@link URLClassLoader#loadClass}.
    */
   public Class loadClassLocally(String name, boolean resolve)
      throws ClassNotFoundException
   {
      return super.loadClass(name, resolve);
   }

   /**
    * Attempts to load the resource from its URL and if not found
    * forwards to the request to {@link ServiceLibraries}.
    */
   public URL getResource(String name)
   {
      URL resource = super.getResource(name);
      
      if (resource == null)
      {
         resource = libraries.getResource(name, this);
      }
      
      // if (resource == null)
      // {
      //    if (log.isTraceEnabled())
      //       log.trace("Did not find the UCL resource: " + name);
      // }

      return resource;
   }

   /**
    * Provides the same functionality as {@link URLClassLoader#getResource}.
    */
   public URL getResourceLocally(String name)
   {
      return super.getResource(name);
   }

   /**
    * Returns the single URL for this UCL.
    */
   public URL getURL() {
      return url;
   }

   /**
    * Uses the hash code from it's URL.
    */
   public int hashCode() 
   {
      return url.hashCode();
   }

   /**
    * Return an empty URL array to force the RMI marshalling subsystem to
    * use the <tt>java.server.codebase</tt> property as the annotated codebase.
    *
    * <p>Do not remove this method without discussing it on the dev list.
    *
    * @return Empty URL[]
    */
   public URL[] getURLs()
   {
      return EMPTY_URL_ARRAY;
   }

   /** The value returned by {@link getURLs}. */
   private static final URL[] EMPTY_URL_ARRAY = {};

   /**
    * This method simply invokes the super.getURLs() method to access the
    * list of URLs that make up the UnifiedClassLoader classpath.
    */
   public URL[] getClasspath()
   {
      return super.getURLs();
   }

   /**
    * This UCL is equal to another UCL when the two URLs are equal.
    */
   public boolean equals(Object other) 
   {
      if (other instanceof UnifiedClassLoader) 
      {
         return ((UnifiedClassLoader) other).getURL().equals(url);
      }
      return false;
   }

   /**
    * Retruns a string representaion of this UCL.
    */
   public String toString()
   {
      return super.toString() + "{ url=" + getURL() + " }";
   }
}

