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
 * @version $Revision: 1.3 $
 * 
 * <p><b>20010830 marc fleury:</b>
 * <ul>
 *   <li>Initial import
 * </ul>
 */
public class URLClassLoader
   extends java.net.URLClassLoader
   implements URLClassLoaderMBean
{
   /** One URL per classLoader in our case */
   private URL url = null;
   /** An SCL can also be loading on behalf of an MBean */
   private ObjectName mbean = null;
	
   /** All SCL are just in orbit around a basic ServiceLibraries */
   private static ServiceLibraries libraries;
	
	/**
	* One url per SCL
	*
	* @param String application
	* @param ClassLoader parent
	*/
	public URLClassLoader( String pUrl )
	{
      super( new URL[] {} );
      try {
         URL lUrl = new URL( pUrl );
         addURL( lUrl );
         this.url = lUrl;
      }
      catch( Exception e ) {
			System.out.println("[GPA] WARNING: URL "+url+" is not valid");
      }
      
		try {
			
			url.openStream();
			
			
			if (libraries == null) libraries = ServiceLibraries.getLibraries();
			
/*
			//Reload the library if necessary
			if (reload) 
					libraries.removeClassLoader(this) ;
*/

			// A URL enabled SCL must register itself with the libraries to be queried
			libraries.addClassLoader(this);
		}
		catch(Exception e) { 
			System.out.println("[GPA] WARNING: URL "+url+" could not be opened");
		}
	}
	
   /**
    * One url per SCL
    *
    * @param String application
    * @param ClassLoader parent
    */
	
   public URLClassLoader(URL[] urls)
   {
		
      super(urls);
		
      this.url = urls[0];
		
      try {
         url.openStream();
			
         if (libraries == null) {
            libraries = ServiceLibraries.getLibraries();
         }
			
         /*
         //Reload the library if necessary
         if (reload) 
         libraries.removeClassLoader(this) ;
         */

         // A URL enabled SCL must register itself with the libraries to
         // be queried
         libraries.addClassLoader(this);
      }
      catch(Exception e) { 
         System.out.println("[GPA] WARNING: URL "+url+" could not be opened");
      }
   }

   public URL getURL() {
      return url;
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
	
   public Class loadClass(String name) 
      throws ClassNotFoundException
   {
      return loadClass(name, true);
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
	
   public URL getResource(String name) {
      if (name.endsWith("CHANGEME")) {
         System.out.println("UCL GETRESOURCE "+name+ " in UCL " +
                            this.hashCode());
      }
      
      URL resource = super.getResource(name);
		
      if (resource == null) {
         resource = libraries.getResource(name, this);
      }
		
      if (resource == null) {
         System.out.println("Did not find the UCL resource "+name);
      }
      return resource;
   }
	
   public URL getResourceLocally(String name) {
      return super.getResource(name);
   }
	
   public InputStream getResourceAsStream(String name) {
      try {
         URL url = getResource(name);
			
         if (url != null) {
            return url.openStream();
         }
      } catch (Exception ignore) {}
      
      return null;
   }
	
   public int hashCode() 
   {
      return url.hashCode();
   }
	
   public boolean equals(Object other) 
   {
      if (other instanceof URLClassLoader) 
      {
         return ((URLClassLoader) other).getURL().equals(url);
      }
      return false;
   }
}
