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

import javax.management.*;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.hsql.Server;

import org.jboss.logging.Log;
import org.jboss.util.ServiceMBeanSupport;

/**
 *   <description> 
 *      
 *   @see <related>
 *   @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 *   @version $Revision: 1.4 $
 */
public class InstantDBDatabase
   extends ServiceMBeanSupport
   implements InstantDBDatabaseMBean, MBeanRegistration
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   String poolName;
   
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   public InstantDBDatabase()
   {
      this("InstantDB");
   }
   
   public InstantDBDatabase(String poolName)
   {
      this.poolName = poolName;
   }
   
   // Public --------------------------------------------------------
   public ObjectName getObjectName(MBeanServer server, ObjectName name)
      throws javax.management.MalformedObjectNameException
   {
      return new ObjectName(OBJECT_NAME);
   }
   
   public String getName()
   {
      return "InstantDB";
   }
   
   public void initService()
      throws Exception
   {
      // Test database
      DataSource ds = (DataSource)new InitialContext().lookup(poolName);
      Connection con = ds.getConnection();
      con.close();
      log.log("Database started");
   }
   
   public void stopService()
   {
   }
   // Protected -----------------------------------------------------
}
