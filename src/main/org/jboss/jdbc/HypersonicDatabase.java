/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
 
package org.jboss.jdbc;

import java.io.File;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import javax.management.*;

import org.hsql.Server;

import org.jboss.logging.Log;
import org.jboss.util.ServiceMBeanSupport;

/**
 *   Integration with Hypersonic SQL (http://hsql.oron.ch/). Starts a Hypersonic database in-VM.
 *
 *   @see HypersonicDatabaseMBean
 *   @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 *   @author <a href="mailto:Scott_Stark@displayscape.com">Scott Stark</a>.
 *   @author Peter Fagerlund pf@iprobot.se @see stopService()
 *   @version $Revision: 1.10 $
 */
public class HypersonicDatabase
   extends ServiceMBeanSupport
   implements HypersonicDatabaseMBean, MBeanRegistration
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   Thread runner;
   Process proc;
   MBeanServer server;
   
   String name = "jboss"; // Database name will be appended to "<db.properties location>/hypersonic/"
   int port = 1476; // Default port
   boolean silent = false;
   boolean trace = true;
   
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   public HypersonicDatabase()
   {
   }
   
   // Public --------------------------------------------------------
   // Settings
   public void setDatabase(String name)
   {
      this.name = name;
   }
   
   public String getDatabase()
   {
      return name;
   }

   public void setPort(int port)
   {
      this.port = port;
   }
   
   public int getPort()
   {
      return port;
   }

   public void setSilent(boolean silent)
   {
      this.silent = silent;
   }
   
   public boolean getSilent()
   {
      return silent;
   }
   
   public void setTrace(boolean trace)
   {
      this.trace = trace;
   }
   
   public boolean getTrace()
   {
      return trace;
   }
   
   public ObjectName getObjectName(MBeanServer server, ObjectName name)
      throws javax.management.MalformedObjectNameException
   {
      this.server = server;
      return name == null ? new ObjectName(OBJECT_NAME) : name;
   }
   
   public String getName()
   {
      return "Hypersonic";
   }
   
   public void startService()
      throws Exception
   {
      // Start DB in new thread, or else it will block us
      runner = new Thread(new Runnable()
      {
         public void run()
         {
            // Get DB directory
            URL dbLocator = this.getClass().getResource("/db.properties");
            File dbDir = new File(dbLocator.getFile()).getParentFile();
            File dbName = new File(dbDir, "hypersonic/"+name);
            
            // Create startup arguments
            String[] args = new String[]
            {
               "-database", dbName.toString(),
               "-port", port+"",
               "-silent", silent+"",
               "-trace", trace+""
            };
            
            // Start server
            org.hsql.Server.main(args);
         }
      });

      // Wait for startup message
      runner.start();
      log.log("Database started");
   }

    /**
    *    @author Peter Fagerlund pf@iprobot.com
    *    
    *    We now close the connection clean by calling the
    *    serverSocket throught jdbc. The MBeanServer calls 
    *    this method at closing time
    */
    public void stopService()
    {
        Connection connection;
        Statement statement;

        String cmd              = "SHUTDOWN";
        String jdbcDriver       = "org.hsql.jdbcDriver";
        String dbStrVersion_1_4 = "jdbc:HypersonicSQL:hsql://localhost:"+port;
        String dbStrVersion_1_6 = "jdbc:hsqldb:hsql://localhost:"+port;
        String user             = "sa";
        String password         = "";

        try
        {
            new org.hsql.jdbcDriver();
            Class.forName(jdbcDriver).newInstance();
            connection=DriverManager.getConnection(dbStrVersion_1_4, user, password);
            statement=connection.createStatement();
            statement.executeQuery(cmd);

            log.log("Database closed clean");
            return;

        } catch(ClassNotFoundException cnfe)
        {
            log.log("ClassNotFound " + cnfe.getMessage());
        } catch(IllegalAccessException iae)
        {
            log.log("Illegal Access  " + iae.getMessage());
        } catch(InstantiationException ie)
        {
            log.log("Instantiation " + ie.getMessage());
        } catch(SQLException sqle)
        {
            log.log("SQL " + sqle.getMessage());
        }
        log.log("Database unable to close clean");
    }

    // Protected -----------------------------------------------------
}
