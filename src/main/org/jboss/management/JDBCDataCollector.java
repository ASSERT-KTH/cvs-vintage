/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.management;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import org.jboss.management.JBossJDBC;
import org.jboss.management.JBossJDBCConnectionPool;
import org.jboss.management.JBossJDBCDriver;

import management.JDBC;
import management.JDBCConnectionPool;
import management.JDBCDriver;
import management.StatisticsProvider;
import management.Stats;

/**
 * JDBC Data Collector
 *
 * @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @author <a href="mailto:andreas.schaefer@madplanet.com">Andreas Schaefer</a>
 * @version $Revision: 1.3 $
 **/
public class JDBCDataCollector
   implements DataCollector
{

   // -------------------------------------------------------------------------
   // Constants
   // -------------------------------------------------------------------------  

   // -------------------------------------------------------------------------
   // Members
   // -------------------------------------------------------------------------  

   // -------------------------------------------------------------------------
   // Constructors
   // -------------------------------------------------------------------------  

   /**
    * Default (no-args) Constructor
    **/
   public JDBCDataCollector() {
   }

   // -------------------------------------------------------------------------
   // Methods
   // -------------------------------------------------------------------------  

   public Collection refresh( MBeanServer pServer ) {
      Collection lReturn = new ArrayList();
      try {
         ArrayList lPools = new ArrayList();
         // Lookup all the JDBC Connection Pool
         Iterator i = pServer.queryMBeans( null, null ).iterator();
         while( i.hasNext() ) {
            ObjectInstance lBean = (ObjectInstance) i.next();
            if( "org.jboss.jdbc.XADataSourceLoader".equals( lBean.getClassName() ) ) {
               ObjectName lBeanName = lBean.getObjectName();
               // Data Sourec Found, no get the information
               JDBCDriver lDriver = new JBossJDBCDriver(
                  (String) pServer.getAttribute( lBeanName, "URL" )
               );
               JDBCConnectionPool lPool = new JBossJDBCConnectionPool(
                  (String) pServer.getAttribute( lBeanName, "PoolName" ),
                  lDriver
               );
               lPools.add( lPool );
            }
         }
         // Add the Pools to the J2EE Server
         lReturn.add(
            new JBossJDBC(
               "JDBC",
               (JDBCConnectionPool[]) lPools.toArray( new JDBCConnectionPool[ 0 ] )
            )
         );
      }
      catch( Exception e ) {
         e.printStackTrace();
      }
      return lReturn;
   }

  public Stats getStatistics( StatisticsProvider pProvider, MBeanServer pServer )
  {
    return null;
  }
  
  public void resetStatistics( StatisticsProvider pProvider, MBeanServer pServer )
  {
  }
}
