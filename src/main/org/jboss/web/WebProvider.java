/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
 
package org.jboss.web;

import java.io.File;
import java.net.URL;

import javax.management.*;

import com.dreambean.dynaserver.DynaServer;

import org.jboss.logging.Log;
import org.jboss.util.ServiceMBeanSupport;

/**
 *   <description> 
 *      
 *   @see <related>
 *   @author Rickard Öberg (rickard.oberg@telkel.com)
 *   @version $Revision: 1.2 $
 */
public class WebProvider
   extends ServiceMBeanSupport
   implements WebProviderMBean, MBeanRegistration
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   DynaServer server;
   
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   public WebProvider()
   {
      this.server = new DynaServer();
   }
   
   // Public --------------------------------------------------------
   public ObjectName getObjectName(MBeanServer server, ObjectName name)
      throws javax.management.MalformedObjectNameException
   {
      return new ObjectName(OBJECT_NAME);
   }
   
   public String getName()
   {
      return "Webserver";
   }
   
   public void startService()
      throws Exception
   {
      server.start();
      log.log("Started dynamic downloading service on port "+server.getPort());
   }
   
   public void stopService()
   {
      server.stop();
   }

   public void addClassLoader(ClassLoader cl)
   {
      server.addClassLoader(cl);
   }
   
   public void removeClassLoader(ClassLoader cl)
   {
      server.removeClassLoader(cl);
   }
   // Protected -----------------------------------------------------
}

