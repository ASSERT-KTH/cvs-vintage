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

import javax.management.*;

import org.apache.tomcat.startup.Tomcat;

import org.jboss.logging.Log;
import org.jboss.util.ServiceMBeanSupport;

/**
 *   <description> 
 *      
 *   @see <related>
 *   @author Rickard Öberg (rickard.oberg@telkel.com)
 *   @version $Revision: 1.1 $
 */
public class TomcatService
   extends ServiceMBeanSupport
   implements TomcatServiceMBean, MBeanRegistration
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   Tomcat server;
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
               Tomcat.main(new String[0]);
               log.log("Tomcat done");
               
/*               proc = Runtime.getRuntime().exec("java -classpath ../lib/ext/hsql.jar org.hsql.Server");
               
               DataInputStream din = new DataInputStream(proc.getInputStream());
               String line;
               while((line = din.readLine()) != null)
               {
                  // Notify that something happened
                  synchronized(runner)
                  {
                     runner.notifyAll();
                  }
                  
                  if (!line.equals("Press [Ctrl]+[C] to abort"))
                     log.log(line);
               }
                  
               runner = null;
               proc = null;
*/               
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
