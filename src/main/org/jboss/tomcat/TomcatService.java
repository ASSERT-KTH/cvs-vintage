/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
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

import org.jboss.logging.Log;
import org.jboss.util.ServiceMBeanSupport;

/**
 *   <description> 
 *      
 *   @see <related>
 *   @author Rickard Öberg (rickard.oberg@telkel.com)
 *   @version $Revision: 1.3 $
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
      runner = new Thread(new Runnable()
      {
         public void run()
         {
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
                    //e.printStackTrace();
                    return;
               } 
               
               Class tomcatArgsClasses[] = new Class[1];
               String args[] = new String[0];
               tomcatArgsClasses[0] = args.getClass();
               Method mainMethod = tomcatClass.getMethod("main", tomcatArgsClasses);
               
               Object tomcatArgs[] = new Object[1];
               tomcatArgs[0] = args;
               
               System.out.println("Starting Tomcat...");
               mainMethod.invoke(null,tomcatArgs); 
               
            } catch (Exception e)
            {
               log.error("Tomcat failed");
               log.exception(e);
            }
         }
      });
      
      runner.start();
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
