/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
 
package org.jboss.web;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.InetAddress;
import java.util.Properties;
import java.util.Enumeration;

import javax.management.*;

import org.jboss.logging.Log;
import org.jboss.util.ServiceMBeanSupport;

/**
 *   <description> 
 *      
 *   @see <related>
 *   @author Rickard Öberg (rickard.oberg@telkel.com)
 *   @version $Revision: 1.3 $
 */
public class WebService
   extends ServiceMBeanSupport
   implements WebServiceMBean
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   WebServer server;
	
	Log log = Log.createLog(getName());
   
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   public WebService()
   {
      this.server = new WebServer();

      // Load the file mime.types into the mapping list
		Properties mimeTypes = new Properties();
      try
      {
         mimeTypes.load(getClass().getResourceAsStream("mime.types"));
			
			Enumeration keys = mimeTypes.keys();
			while (keys.hasMoreElements())
			{
				String extension = (String)keys.nextElement();
				String type = mimeTypes.getProperty(extension);
				server.addMimeType(extension, type);
			}
      } catch (IOException e)
      {
         e.printStackTrace(System.err);
      }
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
      // Set codebase
      String host = System.getProperty("java.rmi.server.hostname");
      if (host ==null) host = InetAddress.getLocalHost().getHostName(); 
      
      String codebase = "http://"+host+":"+getPort()+"/";
      System.setProperty("java.rmi.server.codebase", codebase);
      log.log("Codebase set to "+codebase);
		
      log.log("Started webserver on port "+server.getPort());
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
	
	public void setPort(int port)
	{
		server.setPort(port);
	}
	
   public int getPort()
   {
   	return server.getPort();
   }
   // Protected -----------------------------------------------------
}


