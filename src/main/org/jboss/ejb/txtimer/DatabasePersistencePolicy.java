/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.txtimer;

// $Id: DatabasePersistencePolicy.java,v 1.3 2004/09/10 14:05:46 tdiesler Exp $

import org.jboss.ejb.ContainerMBean;
import org.jboss.ejb.plugins.cmp.jdbc.JDBCUtil;
import org.jboss.logging.Logger;
import org.jboss.mx.util.MBeanProxy;
import org.jboss.mx.util.ObjectNameFactory;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.system.server.Server;
import org.jboss.tm.TxManager;

import javax.ejb.TimerService;
import javax.management.InstanceNotFoundException;
import javax.management.Notification;
import javax.management.NotificationFilterSupport;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This service implements a PersistencePolicy that persistes the timer to a database.
 *
 * @author Thomas.Diesler@jboss.org
 * @jmx.mbean name="jboss.ejb:service=EJBTimerService,persistencePolicy=database"
 * extends="org.jboss.system.Service, org.jboss.ejb.txtimer.PersistencePolicy"
 * @since 09-Sep-2004
 */
public class DatabasePersistencePolicy extends ServiceMBeanSupport implements NotificationListener, DatabasePersistencePolicyMBean
{
   // logging support
   private static Logger log = Logger.getLogger(DatabasePersistencePolicy.class);

   // The service attributes
   private ObjectName dataSource;
   private String tableName;
   private String targetIdColumn;
   private String initialDateColumn;
   private String intervalColumn;
   private String instancePkColumn;
   private String infoColumn;
   private String createTableDDL;

   private TransactionManager tm;
   // The data source the timers will be persisted to
   private DataSource ds;
   // True when the table has been created
   private boolean tableCreated;

   /**
    * Initializes this service.
    */
   protected void startService() throws Exception
   {
      // Get the Tx manager
      try
      {
         InitialContext iniCtx = new InitialContext();
         tm = (TransactionManager)iniCtx.lookup("java:/TransactionManager");
      }
      catch (Exception e)
      {
         log.warn("Cannot obtain TransactionManager from JNDI: " + e.toString());
         tm = TxManager.getInstance();
      }

      try
      {
         String dsJndiTx = (String)server.getAttribute(dataSource, "BindName");
         ds = (DataSource)new InitialContext().lookup(dsJndiTx);
      }
      catch (NamingException e)
      {
         throw new Exception("Failed to lookup data source: " + dataSource);
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
    * @param timedObjectId    The timed object id
    * @param firstEvent       The point in time at which the first txtimer expiration must occur.
    * @param intervalDuration The number of milliseconds that must elapse between txtimer expiration notifications.
    * @param info             A serializable handback object.
    */
   public void insertTimer(TimedObjectId timedObjectId, Date firstEvent, long intervalDuration, Serializable info)
   {
      try
      {
         createTableIfNotExists();
         doInsertTimer(timedObjectId, firstEvent, intervalDuration, info);
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
    * @param timedObjectId The timed object id
    * @param firstEvent    The point in time at which the first txtimer expiration must occur.
    */
   public void deleteTimer(TimedObjectId timedObjectId, Date firstEvent)
   {
      try
      {
         createTableIfNotExists();
         doDeleteTimer(timedObjectId, firstEvent);
      }
      catch (SQLException e)
      {
         log.warn("Unable to delete timer", e);
      }
   }

   /**
    * Return a List of TimerHandle objects.
    */
   public List listTimerHandles()
   {
      List list = new ArrayList();
      try
      {
         list.addAll(doSelectTimers());
      }
      catch (SQLException e)
      {
         log.warn("Unable to get timer handles", e);
      }
      return list;
   }

   /**
    * Restore the persistet timers
    */
   public void restoreTimers()
   {
      try
      {
         createTableIfNotExists();

         List list = doSelectTimers();
         if (list.size() > 0)
         {
            log.info("Restoring " + list.size() + " timer(s)");

            for (int i = 0; i < list.size(); i++)
            {
               TimerHandleImpl handle = (TimerHandleImpl)list.get(i);

               try
               {
                  TimedObjectId toid = handle.getTimedObjectId();
                  ObjectName containerName = toid.getContainerId();
                  ContainerMBean container = (ContainerMBean)MBeanProxy.get(ContainerMBean.class, containerName, server);
                  TimerService timerService = container.getTimerService(toid.getInstancePk());

                  doDeleteTimer(toid, handle.getFirstTime());

                  timerService.createTimer(handle.getFirstTime(), handle.getPeriode(), handle.getInfo());
               }
               catch (Exception e)
               {
                  log.warn("Unable to restore timer record: " + handle);
               }
            }
         }
      }
      catch (SQLException e)
      {
         log.warn("Unable to restore timers", e);
      }
   }

   /**
    * Delete all persisted timers
    */
   public void deleteAllTimers()
   {
      try
      {
         doDeleteTimers();
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
   public ObjectName getDataSource()
   {
      return dataSource;
   }

   /**
    * @jmx.managed-attribute
    */
   public void setDataSource(ObjectName dataSource)
   {
      this.dataSource = dataSource;
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
   public String getInfoColumn()
   {
      return infoColumn;
   }

   /**
    * @jmx.managed-attribute
    */
   public void setInfoColumn(String infoColumn)
   {
      this.infoColumn = infoColumn;
   }

   /**
    * @jmx.managed-attribute
    */
   public String getInstancePkColumn()
   {
      return instancePkColumn;
   }

   /**
    * @jmx.managed-attribute
    */
   public void setInstancePkColumn(String instancePkColumn)
   {
      this.instancePkColumn = instancePkColumn;
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
            con = ds.getConnection();
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

   private void doInsertTimer(TimedObjectId timedObjectId, Date initialExpiration, long intervalDuration, Serializable info)
           throws SQLException
   {
      Connection con = null;
      PreparedStatement st = null;
      try
      {
         // Use the Tx data source
         con = ds.getConnection();

         String sql = "insert into " + tableName + " " +
                 "(" + targetIdColumn + "," + initialDateColumn + "," + intervalColumn + "," + instancePkColumn + "," + infoColumn + ") " +
                 "values (?,?,?,?,?)";
         st = con.prepareStatement(sql);

         st.setString(1, timedObjectId.toString());
         st.setTimestamp(2, new Timestamp(initialExpiration.getTime()));
         st.setLong(3, intervalDuration);
         st.setObject(4, timedObjectId.getInstancePk());
         st.setObject(5, info);

         int rows = st.executeUpdate();
         if (rows != 1)
            log.error("Unable to insert timer for: " + timedObjectId);
      }
      finally
      {
         JDBCUtil.safeClose(st);
         JDBCUtil.safeClose(con);
      }
   }

   private List doSelectTimers()
           throws SQLException
   {
      Connection con = null;
      Statement st = null;
      ResultSet rs = null;
      try
      {
         con = ds.getConnection();

         List list = new ArrayList();

         st = con.createStatement();
         rs = st.executeQuery("select * from " + tableName);
         while (rs.next())
         {
            TimedObjectId toid = TimedObjectId.parse(rs.getString(targetIdColumn));
            Date initialDate = rs.getTimestamp(initialDateColumn);
            long interval = rs.getLong(intervalColumn);
            Serializable pKey = (Serializable)rs.getObject(instancePkColumn);
            Serializable info = (Serializable)rs.getObject(infoColumn);

            toid = new TimedObjectId(toid.getContainerId(), pKey);
            TimerHandleImpl handle = new TimerHandleImpl(toid, initialDate, interval, info);
            list.add(handle);
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

   private void doDeleteTimer(TimedObjectId timedObjectId, Date initialDate)
           throws SQLException
   {
      Connection con = null;
      PreparedStatement st = null;
      ResultSet rs = null;

      // suspend the Tx before we get the con, because you cannot get a connection on an already commited Tx
      Transaction threadTx = suspendTransaction();

      try
      {
         con = ds.getConnection();

         String sql = "delete from " + tableName + " where " + targetIdColumn + "=? and " + initialDateColumn + "=?";
         st = con.prepareStatement(sql);

         st.setString(1, timedObjectId.toString());
         st.setTimestamp(2, new Timestamp(initialDate.getTime()));

         int rows = st.executeUpdate();
         if (rows != 1)
            log.warn("Unable to remove timer for: " + timedObjectId);
      }
      finally
      {
         // resume the Tx
         resumeTransaction(threadTx);

         JDBCUtil.safeClose(rs);
         JDBCUtil.safeClose(st);
         JDBCUtil.safeClose(con);
      }
   }

   private Transaction suspendTransaction()
   {
      Transaction threadTx = null;
      try
      {
         threadTx = tm.suspend();
      }
      catch (SystemException e)
      {
         log.warn("Cannot suspend Tx: " + e.toString());
      }
      return threadTx;
   }

   private void resumeTransaction(Transaction threadTx)
   {
      try
      {
         if (threadTx != null)
            tm.resume(threadTx);
      }
      catch (Exception e)
      {
         log.warn("Cannot resume Tx: " + e.toString());
      }
   }

   private void doDeleteTimers()
           throws SQLException
   {
      Connection con = null;
      PreparedStatement st = null;
      ResultSet rs = null;
      try
      {
         con = ds.getConnection();
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
}

