/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.txtimer;

/**
 * MBean interface.
 * @since 09-Sep-2004
 */
public interface DatabasePersistencePolicyMBean extends org.jboss.system.Service, org.jboss.ejb.txtimer.PersistencePolicy {

   //default object name
   public static final javax.management.ObjectName OBJECT_NAME = org.jboss.mx.util.ObjectNameFactory.create("jboss.ejb:service=EJBTimerService,persistencePolicy=database");

   /**
    * Re-read the current persistent timers list, clear the db of timers, and restore the timers.
    */
  void resetAndRestoreTimers() throws java.sql.SQLException;

  javax.management.ObjectName getDataSource() ;

  void setDataSource(javax.management.ObjectName dataSource) ;

  java.lang.String getDatabasePersistencePlugin() ;

  void setDatabasePersistencePlugin(java.lang.String dbpPluginClass) ;

}
