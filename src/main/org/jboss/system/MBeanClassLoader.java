/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.system;

import java.net.URL;
import java.net.URLClassLoader;
import java.io.InputStream;

import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.MBeanServerFactory;
import javax.management.MBeanException;
import javax.management.InstanceNotFoundException;
import javax.management.ReflectionException;
import javax.management.MalformedObjectNameException;
import javax.management.loading.MLet;

/**
 * The pupose of MBeanCL is to load the classes on behalf of an MBean.
 * 
 * @author <a href="marc.fleury@jboss.org">Marc Fleury</a>
 * @version $Revision: 1.2 $
 *
 * <p><b>20010830 marc fleury:</b>
 * <ul>
 *   <li>Initial import
 * </ul>
 */
public class MBeanClassLoader
   extends ClassLoader
   implements MBeanClassLoaderMBean
{
   /** All SCL are just in orbit around a basic ServiceLibraries */
   private static ServiceLibraries libraries;
	
   private ObjectName objectName;
	
   /**
    * The SCL can be attached to an MBean in which case we pass the ObjectName.
    * 
    * <p>This SCL is not used for classloading from a URL, it is used to keep
    *    track of the dependencies.
    */
   public MBeanClassLoader(final ObjectName objectName) 
   {
      super();
      this.objectName = objectName;
		
      if (libraries == null) {
         libraries = ServiceLibraries.getLibraries();
      }
   }

   /**
    * Returns the object name of the MBean for which this class loader
    * is for.
    *
    * @return    MBean object name.
    */
   public ObjectName getObjectName() {
      return objectName;
   }
	
   /**
    * We intercept the load class to know exactly the dependencies
    * of the underlying jar.
    */
   public Class loadClass(String name, boolean resolve)
      throws ClassNotFoundException
   {
      if (name.endsWith("CHANGEME")) {
         System.out.println("MCL LOAD " + this.hashCode() +
                            " in loadClass " + name);
      }

      return libraries.loadClass(name, resolve, this);
		
   }
	
   public Class loadClass(String name) 
      throws ClassNotFoundException
   {
      return loadClass(name, true);
   }
	
   public URL getResource(String name) {
		
      if (name.endsWith("CHANGEME")) {
         System.out.println("MCL GETRESOURCE " + name +
                            " in SCL " + this.hashCode());
      }
      return libraries.getResource(name, this);
   }
	
   public InputStream getResourceAsStream(String name) {
      try {
         URL url = getResource(name);
         if (url != null) {
            return url.openStream();
         }		
      }
      catch (Exception ignore) {}
      return null;
   }
}
