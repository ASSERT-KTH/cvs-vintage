/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.system;

import java.net.URL;
import java.io.InputStream;

import java.util.Map;
import java.util.HashMap;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.MBeanServerFactory;
import javax.management.MBeanException;
import javax.management.InstanceNotFoundException;
import javax.management.ReflectionException;
import javax.management.MalformedObjectNameException;
import javax.management.loading.MLet;

/**
 * The URLClassLoader is associated with a given URL.
 * It can load jar and sar (or jsr).
 * 
 * <p>The ServiceLibraries keeps track of the UCL and asks everyone for
 *    resources and classes.
 *
 * @author <a href="marc.fleury@jboss.org">Marc Fleury</a>
 * @author <a href="christoph.jung@jboss.org">Christoph G. Jung</a>
 * @version $Revision: 1.7 $
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
   /** One URL per classLoader in our case 
    * This is just a key used for identifying the classloader,
    * nothing is actually loaded from it.  Classes and resources are 
    * loaded from local copies or unpacked local copies.
    */   
   private URL keyUrl = null;

   /** An SCL can also be loading on behalf of an MBean */
    //private ObjectName mbean = null; not used
	
   /** All SCL are just in orbit around a basic ServiceLibraries */
   private static ServiceLibraries libraries;
	
	
   /**
    * One url per SCL
    *
    * @param String application
    * @param ClassLoader parent
    */
   public URLClassLoader(URL[] urls, URL keyUrl)
   {
      super(urls);
      this.keyUrl = keyUrl;
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
         System.out.println("[GPA] WARNING: URL "+keyUrl+" could not be opened");
      }
   }

   public URL getKeyURL()
   {
      return keyUrl;
   }
	
   /**
    * loadClass
    *
    * We intercept the load class to know exactly the dependencies
    * of the underlying jar
    */
	
   public Class loadClass(String name, boolean resolve)
      throws ClassNotFoundException
   {
      if (name.endsWith("CHANGEME"))
      {
         System.out.println("UCL LOAD "+this.hashCode()+" in loadClass "+name);
      }
		
      return libraries.loadClass(name, resolve, this);	
   }
	
	
   public Class loadClassLocally (String name, boolean resolve)
      throws ClassNotFoundException
   {
      if (name.endsWith("CHANGEME")) 
      {
         System.out.println("UCL LOAD LOCALLY "+ this.hashCode() +
                            " in loadClass "+name);
      }
		
      return super.loadClass(name, resolve);
   }
	
   public URL getResource(String name)
   {
      if (name.endsWith("CHANGEME"))
      {

         System.out.println("UCL GETRESOURCE "+name+ " in UCL " +
                            this.hashCode());
      }
      
      URL resource = super.getResource(name);
		
      if (resource == null)
      {
         resource = libraries.getResource(name, this);
      }
		
      if (resource == null)
      {
         System.out.println("Did not find the UCL resource "+name);
      }
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
