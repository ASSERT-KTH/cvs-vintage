/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb;

import java.net.URL;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;

import javax.ejb.EJBLocalHome;

import org.jboss.system.Service;

/**
 * An Application represents a collection of beans that are deployed as a
 * unit.
 *
 * <p>The beans may use the Application to access other beans within the same
 *    deployment unit.
 *      
 * @see Container
 * @see ContainerFactory
 * 
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @version $Revision: 1.19 $
 */
public class Application
   implements Service
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   
   /** Stores the containers for this application unit. */
   HashMap containers = new HashMap();
   HashMap localHomes = new HashMap();
   
   /** Class loader of this application. */
   ClassLoader classLoader = null;
   
   /** Name of this application. */
   String name = "";
   
   /** Url where this application was deployed from. */
   URL url;
   
   // Static --------------------------------------------------------

   // Public --------------------------------------------------------

   /**
    * Add a container to this application. This is called by the
    * ContainerFactory.
    *
    * @param   con  
    */
   public void addContainer(Container con)
   {
      containers.put(con.getBeanMetaData().getEjbName(), con);
      con.setApplication(this);
   }

   /**
    * Remove a container from this application.
    *
    * @param   con  
    */
   public void removeContainer(Container con)
   {
      containers.remove(con.getBeanMetaData().getEjbName());
   }
   
   public void addLocalHome(Container con, EJBLocalHome localHome)
   {
      localHomes.put(con.getBeanMetaData().getEjbName(), localHome);
   }
   
   public void removeLocalHome(Container con)
   {
      localHomes.remove(con.getBeanMetaData().getEjbName());
   }
   
   public EJBLocalHome getLocalHome(Container con)
   {
      return (EJBLocalHome)localHomes.get(con.getBeanMetaData().getEjbName());
   }

   /**
    * Get a container from this Application that corresponds to a given name
    *
    * @param   name  ejb-name name defined in ejb-jar.xml
    *
    * @return  container for the named bean, or null if the container was
    *          not found   
    */
   public Container getContainer(String name)
   {
      return (Container)containers.get(name);
   }

   /**
    * Get all containers in this Application.
    *
    * @return  a collection of containers for each enterprise bean in this 
    *          application unit.
    */
   public Collection getContainers()
   {
      return containers.values();
   }

   /**
    * Get the class loader of this Application. 
    *
    * @return     
    */
   public ClassLoader getClassLoader()
   {
      return classLoader;
   }

   /**
    * Set the class loader of this Application
    *
    * @param   name  
    */
   public void setClassLoader(ClassLoader cl)
   {
      this.classLoader = cl;
   }

   /**
    * Get the name of this Application. 
    *
    * @return    The name of this application.
    */
   public String getName()
   {
      return name;
   }

   /**
    * Set the name of this Application
    *
    * @param name    The name of this application.
    */
   public void setName(String name)
   {
      this.name = name;
   }
   
   /**
    * Get the URL from which this Application was deployed
    *
    * @return    The URL from which this Application was deployed.
    */
   public URL getURL()
   {
      return url;
   }

   /**
    * Set the URL that was used to deploy this Application
    *
    * @param url    The URL that was used to deploy this Application  
    */
   public void setURL(URL url)
   {
      if (url == null)
         throw new IllegalArgumentException("Null URL");
	
      this.url = url;
      
      // if name hasn't been set yet, use the url
      if (name.equals(""))
         name = url.toString();
   }
	
   // Service implementation ----------------------------------------
   public void init() throws Exception {throw new Exception("don't call init");}
   public void destroy(){}
   
   /**
    * Starts all the containers of this application.
    *
    * @exception Exception
    */
   public void start() throws Exception
   {
      Iterator enum = containers.values().iterator();
      while (enum.hasNext())
      {
         Container con = (Container)enum.next();
         con.start();        
      }
   }
	
   /**
    * Stops all the containers of this application.
    */
   public void stop()
   {
      Iterator enum = containers.values().iterator();
      while (enum.hasNext())
      {
         Container con = (Container)enum.next();
         con.stop();
      }
   }
	
}
