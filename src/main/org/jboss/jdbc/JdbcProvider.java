/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
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
 *   @author Rickard �berg (rickard.oberg@telkel.com)
 *   @version $Revision: 1.3 $
 */
public class JdbcProvider
   extends org.jboss.util.ServiceMBeanSupport
   implements JdbcProviderMBean
{
   // Constants -----------------------------------------------------
   public static final String OBJECT_NAME = ":service=JdbcProvider";
    
   // Attributes ----------------------------------------------------
   String driverList = System.getProperty("jdbc.drivers");
   
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------
   public void setDrivers(String driverList)
   {
      this.driverList = driverList;
      System.setProperty("jdbc.drivers", driverList);
   }
   
   public String getDrivers()
   {
      return driverList;
   }
   
   // ServiceMBeanSupport overrides ---------------------------------
   public String getName()
   {
      return "Transaction manager";
    }
   
   protected ObjectName getObjectName(MBeanServer server, ObjectName name)
      throws javax.management.MalformedObjectNameException
   {
      return name == null ? new ObjectName(OBJECT_NAME) : name;
   }
    
   protected void initService()
      throws Exception
   {
      StringTokenizer drivers = new StringTokenizer(driverList, ",");
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
   }
}

