/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.txtimer;

// $Id: DatabasePersistencePolicy.java,v 1.1 2004/09/09 22:04:29 tdiesler Exp $

import org.jboss.ejb.plugins.cmp.jdbc.JDBCUtil;
import org.jboss.logging.Logger;
import org.jboss.mx.util.ObjectNameFactory;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.system.server.Server;

import javax.management.InstanceNotFoundException;
import javax.management.Notification;
import javax.management.NotificationFilterSupport;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This service implements a PersistencePolicy that persistes the timer to a database.
 *
 * @author Thomas.Diesler@jboss.org
 * @jmx.mbean name="jboss.ejb:service=EJBTimerService,plugin=PersistencePolicy"
 * extends="org.jboss.system.Service, org.jboss.ejb.txtimer.PersistencePolicy"
 * @since 09-Sep-2004
 */
public class DatabasePersistencePolicy extends ServiceMBeanSupport implements NotificationListener, DatabasePersistencePolicyMBean
{
   // logging support
   private static Logger log = Logger.getLogger(DatabasePersistencePolicy.class);

   // The service attributes
   private ObjectName dataSourceTx;
   private ObjectName dataSourceNoTx;
   private String tableName;
   private String targetIdColumn;
   private String initialDateColumn;
   private String intervalColumn;
   private String createTableDDL;

   // The data source the timers will be persisted to
   private DataSource dsTx;
   // The data source for removal of stale timers outside an Tx
   private DataSource dsNoTx;
   // True when the table has been created
   private boolean tableCreated;

   /**
    * Initializes this service.
    */
   protected void startService() throws Exception
   {
      try
      {
         String dsJndiTx = (String)server.getAttribute(dataSourceTx, "BindName");
         dsTx = (DataSource)new InitialContext().lookup(dsJndiTx);
         String dsJndiNoTx = (String)server.getAttribute(dataSourceNoTx, "BindName");
         dsNoTx = (DataSource)new InitialContext().lookup(dsJndiNoTx);
      }
      catch (NamingException e)
      {
         throw new Exception("Failed to lookup data source: " + dataSourceTx);
      }

      // create the table if needed
      createTableIfNotExists();

      // await the server startup notification
      registerNotificationListener();
   }

   /**
    * Expects a notification from the server when it is done with startup
    *
    * @param notification the notification object
    * @param handback     the handback object given to the broadcaster upon listener registration
    */
   public void handleNotification(Notification notification, Object handback)
   {
      restoreTimers();
   }

   /**
    * Creates the timer in  persistent storage.
    *
    * @param timer             The Timer that is passed to ejbTimeout
    * @param initialExpiration The point in time at which the first txtimer expiration must occur.
    * @param intervalDuration  The number of milliseconds that must elapse between txtimer expiration notifications.
    */
   public void createTimer(TimerImpl timer, Date initialExpiration, long intervalDuration)
   {
      try
      {
         createTableIfNotExists();
         insertTimer(timer.getTimedObjectId().toString(), initialExpiration, intervalDuration);
      }
      catch (SQLException e)
      {
         RuntimeException ex = new IllegalStateException("Unable to persist timer");
         ex.initCause(e);
         throw ex;
      }
   }

   /**
    * Removes the timer from persistent storage.
    *
    * @param timer The Timer that is passed to ejbTimeout
    */
   public void destroyTimer(TimerImpl timer)
   {
      try
      {
         createTableIfNotExists();
         deleteTimer(timer.getTimedObjectId().toString(), timer.getFirstTime());
      }
      catch (SQLException e)
      {
         log.warn("Unable to delete timer", e);
      }
   }

   /**
    * Restore the persistet timers
    *
    * @jmx.managed-operation
    */
   public void restoreTimers()
   {
      try
      {
         createTableIfNotExists();

         List list = selectTimers();
         if (list.size() > 0)
         {
            log.info("Restoring " + list.size() + " timer(s)");
         }

         deleteTimers();
      }
      catch (SQLException e)
      {
         log.warn("Unable to restore timers", e);
      }
   }

   /**
    * Drop the table of persisted timers
    *
    * @jmx.managed-operation
    */
   public void clearTimers()
   {
      try
      {
         deleteTimers();
      }
      catch (SQLException e)
      {
         log.warn("Unable to clear timers", e);
      }
   }

   // MBean attributes *************************************************************************************************\

   /**
    * @jmx.managed-attribute
    */
   public ObjectName getDataSourceTx()
   {
      return dataSourceTx;
   }

   /**
    * @jmx.managed-attribute
    */
   public void setDataSourceTx(ObjectName dataSourceTx)
   {
      this.dataSourceTx = dataSourceTx;
   }

   /**
    * @jmx.managed-attribute
    */
   public ObjectName getDataSourceNoTx()
   {
      return dataSourceNoTx;
   }

   /**
    * @jmx.managed-attribute
    */
   public void setDataSourceNoTx(ObjectName dataSource)
   {
      this.dataSourceNoTx = dataSource;
   }

   /**
    * @jmx.managed-attribute
    */
   public String getInitialDateColumn()
   {
      return initialDateColumn;
   }

   /**
    * @jmx.managed-attribute
    */
   public void setInitialDateColumn(String initialDateColumn)
   {
      this.initialDateColumn = initialDateColumn;
   }

   /**
    * @jmx.managed-attribute
    */
   public String getIntervalColumn()
   {
      return intervalColumn;
   }

   /**
    * @jmx.managed-attribute
    */
   public void setIntervalColumn(String intervalColumn)
   {
      this.intervalColumn = intervalColumn;
   }

   /**
    * @jmx.managed-attribute
    */
   public String getTableName()
   {
      return tableName;
   }

   /**
    * @jmx.managed-attribute
    */
   public void setTableName(String tableName)
   {
      this.tableName = tableName;
   }

   /**
    * @jmx.managed-attribute
    */
   public String getTargetIdColumn()
   {
      return targetIdColumn;
   }

   /**
    * @jmx.managed-attribute
    */
   public void setTargetIdColumn(String targetIdColumn)
   {
      this.targetIdColumn = targetIdColumn;
   }

   /**
    * @jmx.managed-attribute
    */
   public String getCreateTableDDL()
   {
      return createTableDDL;
   }

   /**
    * @jmx.managed-attribute
    */
   public void setCreateTableDDL(String createTableDDL)
   {
      this.createTableDDL = createTableDDL;
   }

   // private **********************************************************************************************************

   private void createTableIfNotExists()
           throws SQLException
   {
      if (tableCreated == false)
      {
         Connection con = null;
         Statement st = null;
         ResultSet rs = null;
         try
         {
            con = dsNoTx.getConnection();
            final DatabaseMetaData dbMD = con.getMetaData();
            rs = dbMD.getTables(null, null, tableName, null);

            if (!rs.next())
            {
               log.debug("Executing DDL: " + createTableDDL);

               st = con.createStatement();
               st.executeUpdate(createTableDDL);
            }

            tableCreated = true;
         }
         finally
         {
            JDBCUtil.safeClose(rs);
            JDBCUtil.safeClose(st);
            JDBCUtil.safeClose(con);
         }
      }
   }

   private void insertTimer(String targetId, Date initialExpiration, long intervalDuration)
           throws SQLException
   {
      Connection con = null;
      PreparedStatement st = null;
      try
      {
         // Use the Tx data source
         con = dsTx.getConnection();

         String sql = "insert into " + tableName + " (" + targetIdColumn + "," + initialDateColumn + "," + intervalColumn + ") values (?,?,?)";
         st = con.prepareStatement(sql);

         st.setString(1, targetId);
         st.setDate(2, new java.sql.Date(initialExpiration.getTime()));
         st.setLong(3, intervalDuration);

         int rows = st.executeUpdate();
         if (rows != 1)
            log.error("Unable to insert timer for: " + targetId);
      }
      finally
      {
         JDBCUtil.safeClose(st);
         JDBCUtil.safeClose(con);
      }
   }

   private List selectTimers()
           throws SQLException
   {
      Connection con = null;
      Statement st = null;
      ResultSet rs = null;
      try
      {
         con = dsNoTx.getConnection();

         List list = new ArrayList();

         st = con.createStatement();
         rs = st.executeQuery("select * from " + tableName);
         while (rs.next())
         {
            String targetId = rs.getString(targetIdColumn);
            Date initialDate = rs.getDate(initialDateColumn);
            long interval = rs.getLong(intervalColumn);
            list.add(new TimerRecord(targetId, initialDate, interval));
         }

         return list;
      }
      finally
      {
         JDBCUtil.safeClose(rs);
         JDBCUtil.safeClose(st);
         JDBCUtil.safeClose(con);
      }
   }

   private void deleteTimer(String targetId, Date initialDate)
           throws SQLException
   {
      Connection con = null;
      PreparedStatement st = null;
      ResultSet rs = null;
      try
      {
         con = dsNoTx.getConnection();

         String sql = "delete from " + tableName + " where " + targetIdColumn + "=? and " + initialDateColumn + "=?";
         st = con.prepareStatement(sql);

         st.setString(1, targetId);
         st.setDate(2, new java.sql.Date(initialDate.getTime()));

         int rows = st.executeUpdate();
         if (rows != 1)
            log.warn("Unable to remove timer for: " + targetId);
      }
      finally
      {
         JDBCUtil.safeClose(rs);
         JDBCUtil.safeClose(st);
         JDBCUtil.safeClose(con);
      }
   }

   private void deleteTimers()
           throws SQLException
   {
      Connection con = null;
      PreparedStatement st = null;
      ResultSet rs = null;
      try
      {
         con = dsNoTx.getConnection();
         st = con.prepareStatement("delete from " + tableName);
         st.executeUpdate();
      }
      finally
      {
         JDBCUtil.safeClose(rs);
         JDBCUtil.safeClose(st);
         JDBCUtil.safeClose(con);
      }
   }

   /**
    * Register this service as a listener to the main jboss server.
    * We want the startup notification in order to restore our timers.
    */
   private void registerNotificationListener() throws InstanceNotFoundException
   {
      NotificationFilterSupport filter = new NotificationFilterSupport();
      filter.enableType(Server.START_NOTIFICATION_TYPE);
      server.addNotificationListener(ObjectNameFactory.create("jboss.system:type=Server"), this, filter, null);
   }

   /** Persisted timer data */
   class TimerRecord {
      String targetId;
      Date initialDate;
      long interval;
      public TimerRecord(String targetId, Date initialDate, long interval)
      {
         this.initialDate = initialDate;
         this.interval = interval;
         this.targetId = targetId;
      }
   }
}

