/*
 * JBoss, the OpenSource J2EE webOS
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

/** The WebService implementation. It configures a WebServer instance to
 perform dynamic class and resource loading.

 *   @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>.
 *   @author <a href="mailto:Scott_Stark@displayscape.com">Scott Stark</a>.
 *   @version $Revision: 1.8 $
 */
public class WebService
   extends ServiceMBeanSupport
   implements WebServiceMBean
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   WebServer server;
	
   Log log = Log.createLog(this.getClass().getName() + "#" + getName());
   
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

   /** Start the web server for dynamic downloading of classes and resources.
    This sets the system java.rmi.server.hostname property to the local hostname
    if it has not been set. The system java.rmi.server.codebase is also set to
    "http://"+java.rmi.server.hostname+":"+getPort()+"/" if the 
    java.rmi.server.codebase has not been set.
    */
   public void startService()
      throws Exception
   {
      server.start();
      // Set the rmi host and codebase if they are not already set
      String host = System.getProperty("java.rmi.server.hostname");
      if( host == null )
          host = InetAddress.getLocalHost().getHostName();
      
      String codebase = System.getProperty("java.rmi.server.codebase");
      if( codebase == null )
      {
        codebase = "http://"+host+":"+getPort()+"/";
        System.setProperty("java.rmi.server.codebase", codebase);
      }
      log.log("Codebase set to "+codebase);
      log.log("Started webserver on port "+server.getPort());
   }
   
   public void stopService()
   {
      server.stop();
   }

   public URL addClassLoader(ClassLoader cl)
   {
      return server.addClassLoader(cl);
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
 
   /** A flag indicating if the server should attempt to download classes from
    * thread context class loader when a request arrives that does not have a
    * class loader key prefix.
    */
   public boolean getDownloadServerClasses()
   {
      return server.getDownloadServerClasses();
   }
   public void setDownloadServerClasses(boolean flag)
   {
      server.setDownloadServerClasses(flag);
   }
   
   // Protected -----------------------------------------------------
}


