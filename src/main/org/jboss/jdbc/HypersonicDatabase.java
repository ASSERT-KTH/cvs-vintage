/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
 
package org.jboss.jdbc;

import java.io.File;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.File;
import java.net.URL;

import javax.management.*;

import org.hsql.Server;

import org.jboss.logging.Log;
import org.jboss.util.ServiceMBeanSupport;

/**
 *   <description> 
 *      
 *   @see <related>
 *   @author Rickard Öberg (rickard.oberg@telkel.com)
 *   @version $Revision: 1.2 $
 */
public class HypersonicDatabase
   extends ServiceMBeanSupport
   implements HypersonicDatabaseMBean, MBeanRegistration
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   Thread runner;
   Process proc;
   
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   public HypersonicDatabase()
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
      return "Hypersonic";
   }
   
   public void initService()
      throws Exception
   {
      final Log log = this.log;
      runner = new Thread(new Runnable()
      {
         public void run()
         {
            try
            {
               proc = Runtime.getRuntime().exec("java -classpath ../lib/ext/hsql.jar org.hsql.Server");
               
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
            } catch (IOException e)
            {
               log.error("Hypersonic database failed");
            }
         }
      });
      
      synchronized (runner)
      {
         runner.start();
         runner.wait(30000); // Wait for database to start; timeout = not started
      }
      log.log("Database started");
   }
   
   public void stopService()
   {
      if (runner != null)
      {
         runner.stop();
         proc.destroy();
         runner = null;
         proc = null;
      }
   }
   // Protected -----------------------------------------------------
}
