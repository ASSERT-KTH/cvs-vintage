/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.txtimer;

// $Id: DatabasePersistencePolicy.java,v 1.6 2004/09/21 12:18:44 tdiesler Exp $

import org.jboss.ejb.ContainerMBean;
import org.jboss.ejb.plugins.cmp.jdbc.JDBCUtil;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCTypeMappingMetaData;
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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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

   // Column names
   private static final String TIMERID = "TIMERID";
   private static final String TARGETID = "TARGETID";
   private static final String INITIALDATE = "INITIALDATE";
   private static final String INTERVAL = "INTERVAL";
   private static final String INSTANCEPK = "INSTANCEPK";
   private static final String INFO = "INFO";

   // The service attributes
   private ObjectName dataSource;
   private String tableName;

   private TransactionManager tm;
   // The data source the timers will be persisted to
   private DataSource ds;
   // True when the table has been created
   private boolean tableCreated;
   // datasource meta data
   private ObjectName dataSourceMetaData;

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

      // Get the DataSource from JNDI
      try
      {
         String dsJndiTx = (String)server.getAttribute(dataSource, "BindName");
         ds = (DataSource)new InitialContext().lookup(dsJndiTx);
      }
      catch (NamingException e)
      {
         throw new Exception("Failed to lookup data source: " + dataSource);
      }

      // Get the DataSource meta data
      String dsName = dataSource.getKeyProperty("name");
      dataSourceMetaData = ObjectName.getInstance("jboss.jdbc:datasource=" + dsName + ",service=metadata");
      if (server.isRegistered(dataSourceMetaData) == false)
         throw new IllegalStateException("Canno find datasource meta data: " + dataSourceMetaData);

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
    * @param timerId          The timer id
    * @param timedObjectId    The timed object id
    * @param firstEvent       The point in time at which the first txtimer expiration must occur.
    * @param intervalDuration The number of milliseconds that must elapse between txtimer expiration notifications.
    * @param info             A serializable handback object.
    */
   public void insertTimer(String timerId, TimedObjectId timedObjectId, Date firstEvent, long intervalDuration, Serializable info)
   {
      try
      {
         createTableIfNotExists();
         doInsertTimer(timerId, timedObjectId, firstEvent, intervalDuration, info);
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
    * @param timerId The timer id
    */
   public void deleteTimer(String timerId, TimedObjectId timedObjectId)
   {
      try
      {
         createTableIfNotExists();
         doDeleteTimer(timerId, timedObjectId);
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

            // delete all timers
            doDeleteAllTimers();

            // recreate the timers
            for (int i = 0; i < list.size(); i++)
            {
               TimerHandleImpl handle = (TimerHandleImpl)list.get(i);

               try
               {
                  TimedObjectId targetId = handle.getTimedObjectId();
                  ObjectName containerName = targetId.getContainerId();
                  ContainerMBean container = (ContainerMBean)MBeanProxy.get(ContainerMBean.class, containerName, server);
                  TimerService timerService = container.getTimerService(targetId.getInstancePk());
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
         doDeleteAllTimers();
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
               JDBCTypeMappingMetaData typeMapping = (JDBCTypeMappingMetaData)server.getAttribute(dataSourceMetaData, "TypeMappingMetaData");
               String dateType = typeMapping.getTypeMappingMetaData(Timestamp.class).getSqlType();
               String objectType = typeMapping.getTypeMappingMetaData(Object.class).getSqlType();
               String longType = typeMapping.getTypeMappingMetaData(Long.class).getSqlType();

               String createTableDDL = "create table " + tableName + " (" +
                       "  " + TIMERID + " varchar(80) not null," +
                       "  " + TARGETID + " varchar(80) not null," +
                       "  " + INITIALDATE + " " + dateType + " not null," +
                       "  " + INTERVAL + " " + longType + "," +
                       "  " + INSTANCEPK + " " + objectType + "," +
                       "  " + INFO + " " + objectType + "," +
                       "  constraint " + tableName + "_PK primary key (" + TIMERID + "," + TARGETID + ")" +
                       ")";

               log.debug("Executing DDL: " + createTableDDL);

               st = con.createStatement();
               st.executeUpdate(createTableDDL);
            }

            tableCreated = true;
         }
         catch (SQLException e)
         {
            throw e;
         }
         catch (Exception e)
         {
            log.error("Cannot create timer table", e);
         }
         finally
         {
            JDBCUtil.safeClose(rs);
            JDBCUtil.safeClose(st);
            JDBCUtil.safeClose(con);
         }
      }
   }

   private void doInsertTimer(String timerId, TimedObjectId timedObjectId, Date initialExpiration, long intervalDuration, Serializable info)
           throws SQLException
   {
      Connection con = null;
      PreparedStatement st = null;
      try
      {
         // Use the Tx data source
         con = ds.getConnection();

         String sql = "insert into " + tableName + " " +
                 "(" + TIMERID + "," + TARGETID + "," + INITIALDATE + "," + INTERVAL + "," + INSTANCEPK + "," + INFO + ") " +
                 "values (?,?,?,?,?,?)";
         st = con.prepareStatement(sql);

         st.setString(1, timerId);
         st.setString(2, timedObjectId.toString());
         st.setTimestamp(3, new Timestamp(initialExpiration.getTime()));
         st.setLong(4, intervalDuration);
         st.setBytes(5, serialize(timedObjectId.getInstancePk()));
         st.setBytes(6, serialize(info));

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
            String timerId = rs.getString(TIMERID);
            TimedObjectId targetId = TimedObjectId.parse(rs.getString(TARGETID));
            Date initialDate = rs.getTimestamp(INITIALDATE);
            long interval = rs.getLong(INTERVAL);
            Serializable pKey = (Serializable)deserialize(rs.getBytes(INSTANCEPK));
            Serializable info = (Serializable)deserialize(rs.getBytes(INFO));

            targetId = new TimedObjectId(targetId.getContainerId(), pKey);
            TimerHandleImpl handle = new TimerHandleImpl(timerId, targetId, initialDate, interval, info);
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

   private void doDeleteTimer(String timerId, TimedObjectId timedObjectId)
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

         String sql = "delete from " + tableName + " where " + TIMERID + "=? and " + TARGETID + "=?";
         st = con.prepareStatement(sql);

         st.setString(1, timerId);
         st.setString(2, timedObjectId.toString());

         int rows = st.executeUpdate();
         if (rows != 1)
            log.warn("Unable to remove timer for: " + timerId);
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

   private void doDeleteAllTimers()
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


   private byte[] serialize(Object obj) {

      if (obj == null)
         return null;

      ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
      try
      {
         ObjectOutputStream oos = new ObjectOutputStream(baos);
         oos.writeObject(obj);
         oos.close();
      }
      catch (IOException e)
      {
         log.error("Cannot serialize: " + obj, e);
      }
      return baos.toByteArray();
   }

   private Object deserialize(byte[] bytes) {

      if (bytes == null)
         return null;

      ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
      try
      {
         ObjectInputStream oos = new ObjectInputStream(bais);
         return oos.readObject();
      }
      catch (Exception e)
      {
         log.error("Cannot deserialize", e);
         return null;
      }
   }
}

