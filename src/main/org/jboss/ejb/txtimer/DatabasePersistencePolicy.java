/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.txtimer;

// $Id: DatabasePersistencePolicy.java,v 1.8 2004/11/20 08:31:50 starksm Exp $

import org.jboss.ejb.ContainerMBean;
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
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import java.io.Serializable;
import java.sql.SQLException;
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

   // The persistence plugin
   private DatabasePersistencePlugin dbpPlugin;

   // The service attributes
   private ObjectName dataSource;
   private String dbpPluginClassName;

   // The transaction manager, to suspend the current Tx during delete
   private TransactionManager tm;

   /**
    * Initializes this service.
    */
   public void startService() throws Exception
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

      // Get the persistence plugin
      if (dbpPluginClassName != null)
      {
         Class dbpPolicyClass = Thread.currentThread().getContextClassLoader().loadClass(dbpPluginClassName);
         dbpPlugin = (DatabasePersistencePlugin)dbpPolicyClass.newInstance();
      }
      else
      {
         dbpPlugin = new GeneralPurposeDatabasePersistencePlugin();
      }

      // init the plugin
      dbpPlugin.init(server, dataSource);

      // create the table if needed
      dbpPlugin.createTableIfNotExists();

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
         dbpPlugin.insertTimer(timerId, timedObjectId, firstEvent, intervalDuration, info);
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
      // suspend the Tx before we get the con, because you cannot get a connection on an already commited Tx
      Transaction threadTx = suspendTransaction();

      try
      {
         dbpPlugin.deleteTimer(timerId, timedObjectId);
      }
      catch (SQLException e)
      {
         log.warn("Unable to delete timer", e);
      }
      finally
      {
         // resume the Tx
         resumeTransaction(threadTx);
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
         list.addAll(dbpPlugin.selectTimers());
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
         List list = dbpPlugin.selectTimers();
         if (list.size() > 0)
         {
            log.info("Restoring " + list.size() + " timer(s)");

            // delete all timers
            dbpPlugin.clearTimers();

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
   public void clearTimers()
   {
      try
      {
         dbpPlugin.clearTimers();
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
   public String getDatabasePersistencePlugin()
   {
      return dbpPluginClassName;
   }

   /**
    * @jmx.managed-attribute
    */
   public void setDatabasePersistencePlugin(String dbpPluginClass)
   {
      this.dbpPluginClassName = dbpPluginClass;
   }
   // private **********************************************************************************************************

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

