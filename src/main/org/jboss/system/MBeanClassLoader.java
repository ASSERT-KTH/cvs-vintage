/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.system;

import java.net.URL;
import java.io.InputStream;

import javax.management.ObjectName;

/**
 * The pupose of MBeanCL is to load the classes on behalf of an MBean.
 * 
 * @author <a href="marc.fleury@jboss.org">Marc Fleury</a>
 * @version $Revision: 1.6 $
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
	
   /** The bootstrap interface to the log4j system */
   private static BootstrapLogger log = 
      BootstrapLogger.getLogger(MBeanClassLoader.class);

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
		
      if (libraries == null)
      {
         libraries = ServiceLibraries.getLibraries();
      }
   }

   /**
    * Returns the object name of the MBean for which this class loader
    * is for.
    *
    * @return    MBean object name.
    */
   public ObjectName getObjectName()
   {
      return objectName;
   }
	
   /**
    * We intercept the load class to know exactly the dependencies
    * of the underlying jar.
    */
   public Class loadClass(String name, boolean resolve)
      throws ClassNotFoundException
   {
      if (name.endsWith("CHANGEME"))
      {
         log.debug("MCL LOAD " + this.hashCode() +
                            " in loadClass " + name);
      }

      return libraries.loadClass(name, resolve, this);
		
   }
	
   public Class loadClass(String name) 
      throws ClassNotFoundException
   {
      return loadClass(name, true);
   }
	
   public URL getResource(String name)
   {
      if (name.endsWith("CHANGEME"))
      {
         log.debug("MCL GETRESOURCE " + name +
                            " in SCL " + this.hashCode());
      }
      return libraries.getResource(name, this);
   }
	
   public InputStream getResourceAsStream(String name)
   {
      try
      {
         URL url = getResource(name);
         if (url != null)
         {
            return url.openStream();
         }		
      }
      catch (Exception ignore) {}
      return null;
   }

   public URL[] getURLs()
   {
     return libraries.getURLs();
   }
}
