/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.txtimer;

// $Id: GeneralPurposeDatabasePersistencePlugin.java,v 1.1 2004/09/22 09:33:42 tdiesler Exp $

import org.jboss.ejb.plugins.cmp.jdbc.JDBCUtil;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCTypeMappingMetaData;
import org.jboss.logging.Logger;
import org.jboss.mx.util.ObjectNameFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.InputStream;
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
 * This DatabasePersistencePlugin uses getBytes/setBytes to persist the
 * serializable objects associated with the timer.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 23-Sep-2004
 */
public class GeneralPurposeDatabasePersistencePlugin implements DatabasePersistencePlugin
{
   // logging support
   private static Logger log = Logger.getLogger(GeneralPurposeDatabasePersistencePlugin.class);

   // The service attributes
   protected ObjectName dataSourceName;
   protected String tableName;

   // The mbean server
   protected MBeanServer server;
   // The data source the timers will be persisted to
   protected DataSource ds;
   // datasource meta data
   protected ObjectName metaDataName;

   /** Initialize the plugin */
   public void init(MBeanServer server, ObjectName dataSourceName, String tableName) throws SQLException
   {
      this.server = server;
      this.dataSourceName = dataSourceName;
      this.tableName = tableName;

      // Get the DataSource from JNDI
      try
      {
         String dsJndiTx = (String)server.getAttribute(dataSourceName, "BindName");
         ds = (DataSource)new InitialContext().lookup(dsJndiTx);
      }
      catch (Exception e)
      {
         throw new SQLException("Failed to lookup data source: " + dataSourceName);
      }

      // Get the DataSource meta data
      String dsName = dataSourceName.getKeyProperty("name");
      metaDataName = ObjectNameFactory.create("jboss.jdbc:datasource=" + dsName + ",service=metadata");
      if (this.server.isRegistered(metaDataName) == false)
         throw new IllegalStateException("Cannot find datasource meta data: " + metaDataName);
   }

   /** Create the timer table if it does not exist already */
   public void createTableIfNotExists()
           throws SQLException
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
            JDBCTypeMappingMetaData typeMapping = (JDBCTypeMappingMetaData)server.getAttribute(metaDataName, "TypeMappingMetaData");
            if (typeMapping == null)
               throw new IllegalStateException("Cannot obtain type mapping from: " + metaDataName);

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

   /** Insert a timer object */
   public void insertTimer(String timerId, TimedObjectId timedObjectId, Date initialExpiration, long intervalDuration, Serializable info)
           throws SQLException
   {
      Connection con = null;
      PreparedStatement st = null;
      try
      {
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

   /** Select a list of currently persisted timer handles
    * @return List<TimerHandleImpl>
    */
   public List selectTimers()
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

   /** Delete a timer. */
   public void deleteTimer(String timerId, TimedObjectId timedObjectId)
           throws SQLException
   {
      Connection con = null;
      PreparedStatement st = null;
      ResultSet rs = null;

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
         JDBCUtil.safeClose(rs);
         JDBCUtil.safeClose(st);
         JDBCUtil.safeClose(con);
      }
   }

   /** Clear all persisted timers */
   public void clearTimers()
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

   /** Serialize an object */
   protected byte[] serialize(Object obj)
   {

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

   /** Deserialize an object */
   protected Object deserialize(byte[] bytes)
   {

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

   /** Deserialize an object */
   protected Object deserialize(InputStream input)
   {

      if (input == null)
         return null;

      byte[] barr = new byte[1024];
      ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
      try
      {
         for (int b=0; (b = input.read(barr)) > 0;) {
            baos.write(barr, 0, b);
         }
         return deserialize(baos.toByteArray());
      }
      catch (Exception e)
      {
         log.error("Cannot deserialize", e);
         return null;
      }
   }
}

