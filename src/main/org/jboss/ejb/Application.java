/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb;

import java.net.URL;
import java.util.Collection;
import java.util.HashMap;

/**
 *   An Application represents a collection of beans that are deployed as a unit.
 *	  The beans may use the Application to access other beans within the same deployment unit 
 *      
 *   @see Container
 *   @see ContainerFactory
 *   @author Rickard Öberg (rickard.oberg@telkel.com)
 *   @version $Revision: 1.3 $
 */
public class Application
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   HashMap containers = new HashMap();
   String name = "";
   URL url;
   
   // Static --------------------------------------------------------

   // Public --------------------------------------------------------


	/**
	 *	Add a container to this application. This is called by the ContainerFactory
	 *
	 * @param   con  
	 */
   public void addContainer(Container con)
   {
      containers.put(con.getMetaData().getEjbName(), con);
   }
   

	/**
	 *	Remove a container from this application
	 *
	 * @param   con  
	 */
   public void removeContainer(Container con)
   {
      containers.remove(con.getMetaData().getEjbName());
   }
   

	/**
	 *	Get a container from this Application that corresponds to a given name
	 *
	 * @param   name  
	 * @return     
	 */
   public Container getContainer(String name)
   {
      return (Container)containers.get(name);
   }
   

	/**
	 *	Get all containers in this Application
	 *
	 * @return     
	 */
   public Collection getContainers()
   {
      return containers.values();
   }
   

	/**
	 *	Get the name of this Application. 
	 *
	 * @return     
	 */
   public String getName()
   {
      return name;
   }
   

	/**
	 *	Set the name of this Application
	 *
	 * @param   name  
	 */
   public void setName(String name)
   {
      this.name = name;
   }
   

	/**
	 *	Get the URL from which this Application was deployed
	 *
	 * @return     
	 */
   public URL getURL()
   {
      return url;
   }
   

	/**
	 *	Set the URL that was used to deploy this Application
	 *
	 * @param   url  
	 */
   public void setURL(URL url)
   {
		if (url == null)
			throw new IllegalArgumentException("Null URL");
	
      this.url = url;
      if (name.equals(""))
         name = url.toString();
   }
}
