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
 *   <description> 
 *      
 *   @see <related>
 *   @author Rickard Öberg (rickard.oberg@telkel.com)
 *   @version $Revision: 1.1 $
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

   public void addContainer(Container con)
   {
      containers.put(con.getMetaData().getEjbName(), con);
   }
   
   public void removeContainer(Container con)
   {
      containers.remove(con.getMetaData().getEjbName());
   }
   
   public Container getContainer(String name)
   {
      return (Container)containers.get(name);
   }
   
   public Collection getContainers()
   {
      return containers.values();
   }
   
   public String getName()
   {
      return name;
   }
   
   public void setName(String name)
   {
      this.name = name;
   }
   
   public URL getURL()
   {
      return url;
   }
   
   public void setURL(URL url)
   {
      this.url = url;
      if (name.equals(""))
         name = url.toString();
   }
}
