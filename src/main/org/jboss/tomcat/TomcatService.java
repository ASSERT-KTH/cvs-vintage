/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
 
package org.jboss.tomcat;

import java.io.File;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.File;
import java.net.URL;
import java.lang.reflect.Method;

import javax.management.*;

import org.jboss.logging.Logger;
import org.jboss.logging.Log;
import org.jboss.util.ServiceMBeanSupport;

/**
 *   <description> 
 *      
 *   @see <related>
 *   @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>.
 *   @version $Revision: 1.7 $
 */
public class TomcatService
   extends ServiceMBeanSupport
   implements TomcatServiceMBean, MBeanRegistration
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   //Tomcat server;
//   Process proc;
   Thread runner;
   
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   public TomcatService()
   {
   }
   
   // Public --------------------------------------------------------
   public ObjectName getObjectName(MBeanServer server, ObjectName name)
      throws javax.management.MalformedObjectNameException
   {
      return new ObjectName(OBJECT_NAME);
   }
   
   public String getName()
   {
      return "Tomcat";
   }
   
   public void startService()
      throws Exception
   {
      final Log log = this.log;
      
      // Save CL since Tomcat does not reset it properly when it is done
      ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
      
      try
      {
         Class tomcatClass;
         
         log.log("Testing if Tomcat is present....");
         try{
             tomcatClass = Class.forName("org.apache.tomcat.startup.Tomcat");
             log.log("OK");
         }catch(Exception e)
         {
              log.log("failed");
              log.log("Tomcat wasn't found. Be sure to have your CLASSPATH correctly set");
              //Logger.exception(e);
              return;
         } 
         
         Class tomcatArgsClasses[] = new Class[1];
         String args[] = new String[0];
         tomcatArgsClasses[0] = args.getClass();
         Method mainMethod = tomcatClass.getMethod("main", tomcatArgsClasses);
         
         Object tomcatArgs[] = new Object[1];
         tomcatArgs[0] = args;
         
         Logger.log("Starting Tomcat...");
         mainMethod.invoke(null,tomcatArgs); 
         
      } catch (Exception e)
      {
         log.error("Tomcat failed");
         log.exception(e);
      } finally
      {
         Thread.currentThread().setContextClassLoader(oldCl);
      } 
      
   }
   
   public void stopService()
   {
      if (runner != null)
      {
         runner.stop();
         runner = null;
      }
   }
   // Protected -----------------------------------------------------
}
