/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.txtimer;

// $Id: DatabasePersistencePlugin.java,v 1.2 2004/11/20 08:31:50 starksm Exp $

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * An implementation of of this interface provides database specific JDBC access that is
 * not portable accros RDBMS systems.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 23-Sep-2004
 */
public interface DatabasePersistencePlugin
{
   /** Initialize the plugin */
   void init(MBeanServer server, ObjectName dataSource) throws SQLException;

   /** Create the timers table if it does not exist already */
   void createTableIfNotExists() throws SQLException;

   /** Insert a timer object */
   void insertTimer(String timerId, TimedObjectId timedObjectId, Date initialExpiration, long intervalDuration, Serializable info) throws SQLException;

   /** Select a list of currently persisted timer handles
    * @return List<TimerHandleImpl>
    */
   List selectTimers() throws SQLException;

   /** Delete a timer. */
   void deleteTimer(String timerId, TimedObjectId timedObjectId) throws SQLException;

   /** Clear all persisted timers */
   void clearTimers() throws SQLException;

   /** Get the timer table name */
   String getTableName();

   /** Get the timer ID column name */
   String getColumnTimerID();

   /** Get the target ID column name */
   String getColumnTargetID();

   /** Get the initial date column name */
   String getColumnInitialDate();

   /** Get the timer interval column name */
   String getColumnTimerInterval();

   /** Get the instance PK column name */
   String getColumnInstancePK();

   /** Get the info column name */
   String getColumnInfo();
}

