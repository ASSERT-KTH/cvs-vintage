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
 * It can load jar and sar (or jsr).
 * 
 * <p>The ServiceLibraries keeps track of the UCL and asks everyone for
 *    resources and classes.
 *
 * @author <a href="marc.fleury@jboss.org">Marc Fleury</a>
 * @author <a href="christoph.jung@jboss.org">Christoph G. Jung</a>
 * @author <a href="scott.stark@jboss.org">Scott Stark/a>
 * @version $Revision: 1.6 $
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
   /** One URL per classLoader in our case */   
   private URL url;
   
   /** An SCL can also be loading on behalf of an MBean */
   //private ObjectName mbean = null; not used
   
   /** All SCL are just in orbit around a basic ServiceLibraries */
   private static ServiceLibraries libraries;
   
   /** The bootstrap interface to the log4j system */
   private static BootstrapLogger log = BootstrapLogger.getLogger(UnifiedClassLoader.class);
   
   /**
    * One url per SCL
    *
    * @param String application
    * @param ClassLoader parent
    */
   public UnifiedClassLoader(URL url)
   {
      super(new URL[] {url});
      
      if (log.isDebugEnabled()) log.debug("New UCL with url "+url);
      
      this.url = url;
      try
      {
         
         if (libraries == null)
         {
            libraries = ServiceLibraries.getLibraries();
         }
         
         // A URL enabled SCL must register itself with the libraries to
         // be queried
         libraries.addClassLoader(this);
      }
      catch(Exception e)
      {
	 //
	 // FIXME: Should not mask exceptionsd like this, if it is an error, then
	 //        propagate it to caller tyo handle
	 //
	 log.warn("URL "+url+" could not be opened", e);
      }
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
   
   
   public Class loadClassLocally (String name, boolean resolve)
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
      catch (Exception ignore)
      {
      }
      
      return null;
   }
   
   public URL getURL() {return url;}
   
   public int hashCode() 
   {
      return url.hashCode();
   }

   /**
    * This method simply invokes the super.getURLs() method to access the
    * list of URLs that make up the UnifiedClassLoader classpath.
    */
   public URL[] getClasspath()
   {
      return super.getURLs();
   }

   public boolean equals(Object other) 
   {
      if (other instanceof UnifiedClassLoader) 
      {
         return ((UnifiedClassLoader) other).getURL().equals(url);
      }
      return false;
   }
   
   public String toString()
   {
      StringBuffer tmp = new StringBuffer("JBoss UnifiedClassloader: keyURL : ");
      tmp.append(getURL());
      
      tmp.append(']');
      return tmp.toString();
   }
}
