/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
 
package org.jboss.jdbc;

import java.io.*;
import java.util.StringTokenizer;
import java.sql.*;

import javax.management.*;

import org.jboss.logging.Log;

/**
 *   <description> 
 *      
 *   @see <related>
 *   @author Rickard Öberg (rickard.oberg@telkel.com)
 *   @version $Revision: 1.1 $
 */
public class JdbcProvider
   implements JdbcProviderMBean, MBeanRegistration
{
   // Constants -----------------------------------------------------
   public static final String OBJECT_NAME = ":service=JdbcProvider";
    
   // Attributes ----------------------------------------------------
   
   Log log = new Log("JDBC");
   
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------
   public void start()
      throws Exception
   {
   }
   
   public void stop()
   {
   }

   
   public ObjectName preRegister(MBeanServer server, ObjectName name)
      throws java.lang.Exception
   {
      Log.setLog(log);
      
      StringTokenizer drivers = new StringTokenizer(System.getProperty("jdbc.drivers"), ",");
      while (drivers.hasMoreTokens())
      {
         String driver = drivers.nextToken();
         try
         {
            Class.forName(driver);
            log.log("Loaded JDBC-driver:"+driver);
         } catch (Exception e)
         {
            log.error("Could not load driver:"+driver);
         }
      }
         
      Log.unsetLog();
      
      return new ObjectName(OBJECT_NAME);
   }
   
   public void postRegister(java.lang.Boolean registrationDone)
   {
   }
   
   public void preDeregister()
      throws java.lang.Exception
   {
      Log.setLog(log);
      try
      {
         stop();
      } finally
      {
         Log.unsetLog();
      }
   }
   
   public void postDeregister()
   {
   }
   // Protected -----------------------------------------------------
}
