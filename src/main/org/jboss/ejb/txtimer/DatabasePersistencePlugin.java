/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.txtimer;

// $Id: DatabasePersistencePlugin.java,v 1.1 2004/09/22 09:33:42 tdiesler Exp $

import javax.management.ObjectName;
import javax.management.MBeanServer;
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
   // Column names
   static final String TIMERID = "TIMERID";
   static final String TARGETID = "TARGETID";
   static final String INITIALDATE = "INITIALDATE";
   static final String INTERVAL = "INTERVAL";
   static final String INSTANCEPK = "INSTANCEPK";
   static final String INFO = "INFO";

   /** Initialize the plugin */
   void init(MBeanServer server, ObjectName dataSource, String tableName) throws SQLException;

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
}

