/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.system;

import java.io.File;
import java.util.Set;
import java.net.URL;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Collections;
import java.util.StringTokenizer;
import javax.management.ObjectName;
import javax.management.MBeanServer;
import javax.management.MBeanRegistration;

import org.jboss.system.URLClassLoader;

// import org.jboss.logging.Log;

/**
 * The service libraries is a central repository of all classes
 * loaded by the ClassLoaders.
 * 
 * @author <a href="mailto:marc@jboss.org">Marc Fleury</a>
 * @version $Revision: 1.2 $
 *
 * <p><b>20010830 marc fleury:</b>
 * <ul>
 *   <li>Initial import
 * </ul>
 */
public class ServiceLibraries
   implements ServiceLibrariesMBean, MBeanRegistration
{
   // Constants -----------------------------------------------------
	
   // Attributes ----------------------------------------------------
	
   // The classloaders
   private Set classLoaders;
	
   // The classes kept in this library
   private Map classes, resources,
      // A given classloader loads a set of class
      clToClassSetMap, clToResourceSetMap;
	
   // JBoss logger version move to log4j if needed
   //Log log = Log.createLog("VM-ClassLoader");
	
   // Static --------------------------------------------------------

   private static ServiceLibraries libraries;

   // Constructors --------------------------------------------------
	
   // Public --------------------------------------------------------
	
   public static ServiceLibraries getLibraries() 
   {
      if (libraries == null) {
         libraries = new ServiceLibraries();
      }
			
      return libraries;
   }
	
   // ServiceClassLoaderMBean implementation ------------------------
	
   public String getName()
   {
      return "Service Libraries";
   }
	
   public void addClassLoader(URLClassLoader cl) 
   { 
      synchronized(classLoaders) {
			
         // we allow for duplicate class loader definitions in the
         // services.xml files
         // we should however only keep the first classloader declared
			
         if (!classLoaders.contains(cl))
         {
            classLoaders.add(cl);
            System.out.println("Libraries adding URLClassLoader " +
                               cl.hashCode() + " URL " +
                               ((URLClassLoader) cl).getURL().toString()); 
         }
         else 
         {
            System.out.println("Libraries skipping duplicate URLClassLoader " +
                               "for URL " +
                               ((URLClassLoader)cl).getURL().toString());
         }
      }
   }
	
   public void removeClassLoader(URLClassLoader cl)
   {
      synchronized(classLoaders) {
         classLoaders.remove(cl);
			
         Set classes = (Set) clToClassSetMap.remove(cl);
         Iterator iterator = classes.iterator();
         while( iterator.hasNext()) 
         {
            classes.remove(iterator.next());
         }
			
         Set resources = (Set) clToResourceSetMap.remove(cl);
         Iterator iterator2 = resources.iterator();
         while( iterator2.hasNext())
         {
            resources.remove(iterator2.next());
         }
      }
   }
	
	
   public Class loadClass(String name, boolean resolve, ClassLoader scl) 
      throws ClassNotFoundException
   {
      // Try the local map already 
      Class foundClass = (Class) classes.get(name);	
		
      if (foundClass != null) return foundClass;
			
      // If not start asking around to URL classloaders for it
		
      // who will find it?
      URLClassLoader cl = null;
		
      if (scl instanceof URLClassLoader) 
      {
         // First ask the asking classloader chances are the dependent
         // class is in there
         try { 
            foundClass = ((URLClassLoader) scl).loadClassLocally(name,resolve);
				
            // If we get here we know the scl is the right one
            cl = (URLClassLoader) scl;
         }
         catch (ClassNotFoundException ignored) {}
      }
		
      synchronized(classLoaders) 
      {
         Iterator allLoaders = classLoaders.iterator();
         while (allLoaders.hasNext() && (foundClass == null)) 
         {
            // next!
            cl = (URLClassLoader) allLoaders.next();
				
            if (!scl.equals(cl))
            {
               try {
						
                  foundClass = cl.loadClassLocally(name, resolve);
               }
               catch (ClassNotFoundException ignored2) {
                  // try next loader 
               }
            }
         } // allLoaders 
			
         if (foundClass != null) 
         {
            // We can keep track  
            classes.put(name, foundClass);
				
            // When we cycle the cl we also need to remove the classes it
            // loaded
            Set set = (Set) clToClassSetMap.get(cl);
            if (set == null) {
               set = new HashSet();
               clToClassSetMap.put(cl, set);
            }
            set.add(foundClass);
				
            return foundClass; 
         }
      } // Synchronization
		
      // If we reach here, all of the classloaders currently in
      // the VM don't know about the class
      throw new ClassNotFoundException(name);
   }
	
   public URL getResource(String name, ClassLoader scl) 
   {
      // Is it in the global map?
      if (resources.containsKey(name)) 
      {
         return (URL) resources.get(name);	
      }
		
      URL resource = null;
		
      // First ask for the class to the asking class loader  
      if (scl instanceof URLClassLoader) 
      {
         resource = ((URLClassLoader) scl).getResourceLocally(name);
      }
		
      if (resource == null)
      {
         // If not start asking around to URL classloaders for it
         int i=1;
         synchronized(classLoaders) 
         {
            Iterator allLoaders = classLoaders.iterator();
            while (allLoaders.hasNext()) 
            {
               URLClassLoader cl = (URLClassLoader) allLoaders.next();
					
               if (!cl.equals(scl))
               {
                  resource = cl.getResourceLocally(name);
						
                  if (resource != null) 
                  {
                     // We can keep track  
                     resources.put(name, resource);
							
                     // When we cycle the cl we also need to remove the
                     // classes it loaded
                     Set set = (Set) clToResourceSetMap.get(cl);
                     if (set == null) {
                        set = new HashSet();
                        clToResourceSetMap.put(cl, set);
                     }
                     set.add(resource);
                     return resource;
                  }
                  
                  // Just cycle through the class loaders until you find it 
               }
            } // allLoaders 
         } // Synchronization
      }
      
      // If we reach here, all of the classloaders currently in the
      // VM don't know about the resource
      return resource;
   }
	
   // The name of the system MLet 
   //   ObjectName mlet = new ObjectName(server.getDefaultDomain(), "service", "MLet");
	
   public ObjectName preRegister(MBeanServer server, ObjectName name)
      throws Exception
   {
      //this.server = server;
		
      classLoaders = Collections.synchronizedSet(new HashSet());
      classes = Collections.synchronizedMap(new HashMap());
      resources = Collections.synchronizedMap(new HashMap());
      clToResourceSetMap = Collections.synchronizedMap(new HashMap());
      clToClassSetMap = Collections.synchronizedMap(new HashMap());

      System.out.println
         ("[GPA] Microkernel ClassLoaders and Libraries initialized");
      
      return name==null ? new ObjectName(OBJECT_NAME) : name;
   }
	
	
   public void preDeregister()
      throws Exception
   {
   }
	
   public void postRegister(Boolean b) {}

   public void postDeregister()
   {
   }
	
   // Package protected ---------------------------------------------
	
   // Protected -----------------------------------------------------
	
   // Private -------------------------------------------------------
	
   // Inner classes -------------------------------------------------
}                  
