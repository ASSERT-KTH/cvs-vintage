/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.txtimer;

// $Id: OracleDatabasePersistencePlugin.java,v 1.2 2004/09/22 09:52:24 tdiesler Exp $

import org.jboss.ejb.plugins.cmp.jdbc.JDBCUtil;
import org.jboss.logging.Logger;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This DatabasePersistencePlugin uses getBinaryStream/setBinaryStream to persist the
 * serializable objects associated with the timer.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 23-Sep-2004
 */
public class OracleDatabasePersistencePlugin extends GeneralPurposeDatabasePersistencePlugin
{
   // logging support
   private static Logger log = Logger.getLogger(OracleDatabasePersistencePlugin.class);

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

         byte[] pkArr = serialize(timedObjectId.getInstancePk());
         if (pkArr != null)
         {
            InputStream is = new ByteArrayInputStream(pkArr);
            st.setBinaryStream(5, is, pkArr.length);
         }
         else
         {
            st.setBytes(5, null);
         }

         byte[] infoArr = serialize(info);
         if (infoArr != null)
         {
            InputStream is = new ByteArrayInputStream(infoArr);
            st.setBinaryStream(6, is, infoArr.length);
         }
         else
         {
            st.setBytes(6, null);
         }

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

            InputStream isPk = rs.getBinaryStream(INSTANCEPK);
            Serializable pKey = (Serializable)deserialize(isPk);
            InputStream isInfo = rs.getBinaryStream(INFO);
            Serializable info = (Serializable)deserialize(isInfo);

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
}

