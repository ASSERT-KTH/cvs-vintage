/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.system;

import java.net.URL;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jboss.system.URLClassLoader;

/** 
 * Service Libraries. The service libraries is a central repository of all
 * classes loaded by the ClassLoaders
 *
 * @author <a href="mailto:marc@jboss.org">Marc Fleury</a>
 * @author <a href="mailto:osh@sparre.dk">Ole Husgaard</a>
 * @version $Revision: 1.12 $ <p>
 *
 * <b>20010830 marc fleury:</b>
 * <ul>
 *   <li>initial import
 * </ul>
 *
 * <b>20010908 david jencks:</b>
 * <ul>
 *   <li>Modified to make undeploy work better.
 * </ul>
 *
 * <b>20011003 Ole Husgaard:</b>
 * <ul>
 *   <li>Changed synchronization to avoid deadlock with SUNs
 *       java.lang.Classloader implementation. Kudos to Dr. Christoph Jung
 *       and Sacha Labourey for identifying this problem.
 * </ul>
 */
public class ServiceLibraries
   implements ServiceLibrariesMBean, MBeanRegistration
{
   /** The bootstrap interface to the log4j system */
   private static BootstrapLogger log = 
      BootstrapLogger.getLogger(ServiceLibraries.class);

   private static ServiceLibraries libraries;

   /**
    * The classloaders we use for loading classes here.
    */
   private Set classLoaders;

   /**
    * Maps class names of the classes loaded here to the classes.
    */
   private Map classes;

   /**
    * Maps class loaders to the set of classes they loaded here.
    */
   private Map clToClassSetMap;

   /**
    * The version number of the {@link #clToClassSetMap} map.
    * If a lookup of a class detects a change in this while calling
    * the classloaders with locks removed, the {@link #clToClassSetMap}
    * and {@link #classes} fields should <em>only</em> be modified
    * if the classloader used for loading the class is still in the
    * {@link #classLoaders} set.
    */
   private long clToClassSetMapVersion = 0;

   /**
    * Maps resource names of resources looked up here to the URLs used to
    * load them.
    */
   private Map resources;

   /**
    * Maps class loaders to the set of resource names they looked up here.
    */
   private Map clToResourceSetMap;

   /**
    * The version number of the {@link #clToResourceSetMap} map.
    * If a lookup of a resource detects a change in this while
    * calling the classloaders with locks removed, the
    * {@link #clToResourceSetMap} and {@link #resources} fields should
    * <em>only</em> be modified if the classloader used for loading
    * the class is still in the {@link #classLoaders} set.
    */
   private long clToResourceSetMapVersion = 0;

   /**
    * Gets the Libraries attribute of the ServiceLibraries class
    *
    * @return The Libraries value
    */
   public static ServiceLibraries getLibraries()
   {
      if (libraries == null)
         libraries = new ServiceLibraries();

      return libraries;
   }

   // ServiceClassLoaderMBean implementation ------------------------

   /**
    * Gets the Name attribute of the ServiceLibraries object.
    *
    * @return The Name value
    */
   public String getName()
   {
      return "ServiceLibraries";
   }

   /**
    * Find a resource in the ServiceLibraries object.
    *
    * @param name   The name of the resource
    * @param scl    The asking class loader
    * @return       An URL for reading the resource, or <code>null</code> if the
    *               resource could not be found.
    */
   public URL getResource(String name, ClassLoader scl)
   {
      Set classLoaders2;
      long clToResourceSetMapVersion2;

      synchronized (this)
      {
         // Is it in the global map?
         if (resources.containsKey(name))
            return (URL)resources.get(name);

         // No, make copies of the classLoader reference to avoid working on
         // a later version of it.
         classLoaders2 = classLoaders;

         // Save the current version of the resource map, so we
         // can detect if it has changed.
         clToResourceSetMapVersion2 = clToResourceSetMapVersion;
      }

      URL resource = null;

      // First ask for the class to the asking class loader
      if (scl instanceof URLClassLoader)
         resource = ((URLClassLoader)scl).getResourceLocally(name);

      if (resource == null)
      {
         // If not start asking around to URL classloaders for it
         for (Iterator iter = classLoaders2.iterator(); iter.hasNext();)
         {
            URLClassLoader cl = (URLClassLoader)iter.next();

            if (!cl.equals(scl))
            { // already tried this one
               resource = cl.getResourceLocally(name);

               if (resource != null)
               {
                  synchronized (this)
                  {
                     // Did the version change?
                     if (clToResourceSetMapVersion2 != clToResourceSetMapVersion)
                     {
                        // Yes. Is the class loader we used still here?
                        if (!classLoaders.contains(cl))
                        {
                           // No, it was removed from under us.
                           // Don't change the maps, simply return the resource.
                           return resource;
                        }
                     }
                     // We can keep track
                     resources.put(name, resource);

                     // When we cycle the cl we also need to remove the classes it loaded
                     Set set = (Set)clToResourceSetMap.get(cl);
                     if (set == null)
                     {
                        set = new HashSet();
                        clToResourceSetMap.put(cl, set);
                     }

                     set.add(name);

                     return resource;
                  }
               } // if we found it
            }
         } // for all ClassLoaders
      } // If we reach here, all of the classloaders currently in the VM don't know about the resource

      return resource;
   }

   /**
    * Add a ClassLoader to the ServiceLibraries object.
    *
    * @param cl The class loader to be added.
    */
   public synchronized void addClassLoader(URLClassLoader cl)
   {
      // we allow for duplicate class loader definitions in the services.xml files
      // we should however only keep the first classloader declared
      boolean trace = log.isTraceEnabled();
      if (!classLoaders.contains(cl))
      {
         // We create a new copy of the classLoaders set.
         classLoaders = new HashSet(classLoaders);

         classLoaders.add(cl);
         if( trace )
         {
            log.trace("Libraries adding URLClassLoader " + cl.hashCode() +
               " key URL " + cl.getKeyURL().toString());
         }
      }
      else if( trace )
      {
         log.trace("Libraries skipping duplicate URLClassLoader for key URL " +
            cl.getKeyURL().toString());
      }
   }

   /**
    * Remove a ClassLoader from the ServiceLibraries object.
    *
    * @param cl The ClassLoader to be removed.
    */
   public synchronized void removeClassLoader(URLClassLoader cl)
   {
      boolean trace = log.isTraceEnabled();
      if( trace )
         log.trace("removing classloader " + cl);

      if (!classLoaders.contains(cl))
         return; // nothing to remove

      // We create a new copy of the classLoaders set.
      classLoaders = new HashSet(classLoaders);
      classLoaders.remove(cl);

      if (clToClassSetMap.containsKey(cl))
      {
         // We have a new version of the map
         ++clToClassSetMapVersion;

         Set clClasses = (Set)clToClassSetMap.remove(cl);

         for (Iterator iter = clClasses.iterator(); iter.hasNext();)
         {
            Object o = iter.next();
            Object o1 = classes.remove(o);
            if( trace )
               log.trace("removing class " + o + ", removed: " + o1);
         }
      }
      
      // Same procedure for resources
      if (clToResourceSetMap.containsKey(cl))
      {
         ++clToResourceSetMapVersion;

         Set clResources = (Set)clToResourceSetMap.remove(cl);

         for (Iterator iter = clResources.iterator(); iter.hasNext();)
         {
            Object o = iter.next();
            Object o1 = resources.remove(o);
            if( trace )
               log.trace("removing resource " + o + ", removed: " + o1);
         }
      }
   }

   /**
    * Load a class in the ServiceLibraries object.
    *
    * @param name The name of the class
    * @param resolve If <code>true</code>, the class will be resolved
    * @param scl The asking class loader
    * @return The loaded class.
    *         resource could not be found.
    * @throws ClassNotFoundException If the class could not be found.
    */
   public Class loadClass(String name, boolean resolve, ClassLoader scl)
          throws ClassNotFoundException
   {
      Class foundClass;
      Set classLoaders2;
      long clToClassSetMapVersion2;

      synchronized (this)
      {
         // Try the local map already
         foundClass = (Class)classes.get(name);

         if (foundClass != null)
            return foundClass;

         // Not found, make copies of the classLoader reference to avoid
         // working on a later version of it.
         classLoaders2 = classLoaders;

         // Save the current version of the class map, so we
         // can detect if it has changed.
         clToClassSetMapVersion2 = clToClassSetMapVersion;
      }

      // If not start asking around to URL classloaders for it
      // who will find it?
      URLClassLoader cl = null;

      if (scl instanceof URLClassLoader)
      {
         // First ask the asking classloader chances are the dependent class is in there
         try
         {
            foundClass = ((URLClassLoader)scl).loadClassLocally(name, resolve);

            //If we get here we know the scl is the right one
            cl = (URLClassLoader)scl;
         }
         catch (ClassNotFoundException ignored)
         {
         }
      }

      Iterator allLoaders = classLoaders2.iterator();
      while (allLoaders.hasNext() && (foundClass == null))
      {
         // next!
         cl = (URLClassLoader)allLoaders.next();

         if (!scl.equals(cl))
         {
            try
            {
               foundClass = cl.loadClassLocally(name, resolve);
            }
            catch (ClassNotFoundException ignored2)
            {
               //try next loader
            }
         }
      } //allLoaders

      if (foundClass != null)
      {
         synchronized (this)
         {
            // Did the version change?
            if (clToClassSetMapVersion2 != clToClassSetMapVersion)
            {
               // Yes. Is the class loader we used still here?
               if (!classLoaders.contains(cl))
               {
                  // No, it was removed from under us.
                  // Don't change the maps, simply return the class.
                  return foundClass;
               }
            }
            // We can keep track
            classes.put(name, foundClass);

            // When we cycle the cl we also need to remove the classes it loaded
            Set set = (Set)clToClassSetMap.get(cl);
            if (set == null)
            {
               set = new HashSet();
               clToClassSetMap.put(cl, set);
            }
            set.add(name);
         }

         return foundClass;
      }

      // If we reach here, all of the classloaders currently in the VM don't 
      // know about the class
      throw new ClassNotFoundException(name);
   }

   /** 
    * Pre-register this component.
    * 
    * @param server        The server which the component is registering with.
    * @param name          The configured name of the object.
    * @throws Exception    Pre-registration failed
    * @return              The name to register the object as.
    */   
   public ObjectName preRegister(MBeanServer server, ObjectName name)
      throws Exception
   {
      // this.server = server;

      classLoaders = new HashSet();
      classes = new HashMap();
      resources = new HashMap();
      clToResourceSetMap = new HashMap();
      clToClassSetMap = new HashMap();

      log.info("ClassLoaders and ServiceLibraries initialized");
      return name == null ? new ObjectName(OBJECT_NAME) : name;
   }

   /** 
    * Non-operation.
    */   
   public void preDeregister() throws Exception
   {
      // empty
   }

   /** 
    * Non-operation.
    */   
   public void postRegister(Boolean b)
   {
      // empty
   }

   /** 
    * Non-operation.
    */   
   public void postDeregister()
   {
      // empty
   }
}
