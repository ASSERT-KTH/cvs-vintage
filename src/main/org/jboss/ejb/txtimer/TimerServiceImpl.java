package org.jboss.ejb.txtimer;

// $Id: TimerServiceImpl.java,v 1.9 2004/09/10 14:05:46 tdiesler Exp $

import org.jboss.logging.Logger;
import org.jboss.tm.TxManager;
import org.jboss.mx.util.MBeanServerLocator;
import org.jboss.mx.util.MBeanProxy;

import javax.ejb.EJBException;
import javax.ejb.Timer;
import javax.ejb.TimerHandle;
import javax.ejb.TimerService;
import javax.naming.InitialContext;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * The TimerService provides enterprise bean components with access to the
 * container-provided Timer Service. The EJB Timer Service allows entity beans, stateless
 * session beans, and message-driven beans to be registered for timer callback events at
 * a specified time, after a specified elapsed time, or after a specified interval.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 07-Apr-2004
 */
public class TimerServiceImpl implements TimerService
{
   // logging support
   private static Logger log = Logger.getLogger(TimerServiceImpl.class);

   private TransactionManager transactionManager;
   private PersistencePolicy persistencePolicy;

   private TimedObjectId timedObjectId;
   private TimedObjectInvoker timedObjectInvoker;

   // Maps TimerHandles to Timer objects
   private Map timers = new HashMap();

   /**
    * Create a Timer service for the given TimedObject
    */
   public TimerServiceImpl(TimedObjectId timedObjectId, TimedObjectInvoker timedObjectInvoker)
   {
      this.timedObjectId = timedObjectId;
      this.timedObjectInvoker = timedObjectInvoker;

      // Get the Tx manager
      try
      {
         InitialContext iniCtx = new InitialContext();
         transactionManager = (TransactionManager) iniCtx.lookup("java:/TransactionManager");
      }
      catch (Exception e)
      {
         log.warn("Cannot obtain TransactionManager from JNDI: " + e.toString());
         transactionManager = TxManager.getInstance();
      }

      // Get a proxy to the persistence policy
      try
      {
         MBeanServer server = MBeanServerLocator.locateJBoss();
         ObjectName persistencePolicyName = (ObjectName)server.getAttribute(EJBTimerService.OBJECT_NAME, "PersistencePolicy");
         persistencePolicy = (PersistencePolicy)MBeanProxy.get(PersistencePolicy.class, persistencePolicyName, server);
      }
      catch (Exception e)
      {
         log.warn("Cannot obtain the implementation of a PersistencePolicy: " + e.toString());
         persistencePolicy = new NoopPersistencePolicy();
      }
   }

   /**
    * Create a single-action txtimer that expires after a specified duration.
    *
    * @param duration The number of milliseconds that must elapse before the txtimer expires.
    * @param info     Application information to be delivered along with the txtimer expiration
    *                 notification. This can be null.
    * @return The newly created Timer.
    * @throws IllegalArgumentException If duration is negative
    * @throws IllegalStateException    If this method is invoked while the instance is in
    *                                  a state that does not allow access to this method.
    * @throws javax.ejb.EJBException   If this method could not complete due to a system-level failure.
    */
   public Timer createTimer(long duration, Serializable info) throws IllegalArgumentException, IllegalStateException, EJBException
   {
      if (duration < 0)
         throw new IllegalArgumentException("duration is negative");

      return createTimer(new Date(System.currentTimeMillis() + duration), 0, info);
   }

   /**
    * Create an interval txtimer whose first expiration occurs after a specified duration,
    * and whose subsequent expirations occur after a specified interval.
    *
    * @param initialDuration  The number of milliseconds that must elapse before the first
    *                         txtimer expiration notification.
    * @param intervalDuration The number of milliseconds that must elapse between txtimer
    *                         expiration notifications. Expiration notifications are
    *                         scheduled relative to the time of the first expiration. If
    *                         expiration is delayed(e.g. due to the interleaving of other
    *                         method calls on the bean) two or more expiration notifications
    *                         may occur in close succession to "catch up".
    * @param info             Application information to be delivered along with the txtimer expiration
    *                         notification. This can be null.
    * @return The newly created Timer.
    * @throws IllegalArgumentException If initialDuration is negative, or intervalDuration
    *                                  is negative.
    * @throws IllegalStateException    If this method is invoked while the instance is in
    *                                  a state that does not allow access to this method.
    * @throws javax.ejb.EJBException   If this method could not complete due to a system-level failure.
    */
   public Timer createTimer(long initialDuration, long intervalDuration, Serializable info) throws IllegalArgumentException, IllegalStateException, EJBException
   {
      if (initialDuration < 0)
         throw new IllegalArgumentException("initial duration is negative");
      if (intervalDuration < 0)
         throw new IllegalArgumentException("interval duration is negative");

      return createTimer(new Date(System.currentTimeMillis() + initialDuration), intervalDuration, info);
   }

   /**
    * Create a single-action txtimer that expires at a given point in time.
    *
    * @param expiration The point in time at which the txtimer must expire.
    * @param info       Application information to be delivered along with the txtimer expiration
    *                   notification. This can be null.
    * @return The newly created Timer.
    * @throws IllegalArgumentException If expiration is null, or expiration.getTime() is negative.
    * @throws IllegalStateException    If this method is invoked while the instance is in
    *                                  a state that does not allow access to this method.
    * @throws javax.ejb.EJBException   If this method could not complete due to a system-level failure.
    */
   public Timer createTimer(Date expiration, Serializable info) throws IllegalArgumentException, IllegalStateException, EJBException
   {
      if (expiration == null)
         throw new IllegalArgumentException("expiration is null");

      return createTimer(expiration, 0, info);
   }

   /**
    * Create an interval txtimer whose first expiration occurs at a given point in time and
    * whose subsequent expirations occur after a specified interval.
    *
    * @param initialExpiration The point in time at which the first txtimer expiration must occur.
    * @param intervalDuration  The number of milliseconds that must elapse between txtimer
    *                          expiration notifications. Expiration notifications are
    *                          scheduled relative to the time of the first expiration. If
    *                          expiration is delayed(e.g. due to the interleaving of other
    *                          method calls on the bean) two or more expiration notifications
    *                          may occur in close succession to "catch up".
    * @param info              Application information to be delivered along with the txtimer expiration
    *                          notification. This can be null.
    * @return The newly created Timer.
    * @throws IllegalArgumentException If initialExpiration is null, or initialExpiration.getTime()
    *                                  is negative, or intervalDuration is negative.
    * @throws IllegalStateException    If this method is invoked while the instance is in
    *                                  a state that does not allow access to this method.
    * @throws javax.ejb.EJBException   If this method could not complete due to a system-level failure.
    */
   public Timer createTimer(Date initialExpiration, long intervalDuration, Serializable info) throws IllegalArgumentException, IllegalStateException, EJBException
   {
      if (initialExpiration == null)
         throw new IllegalArgumentException("initial expiration is null");
      if (intervalDuration < 0)
         throw new IllegalArgumentException("interval duration is negative");

      try
      {
         TimerImpl timer = new TimerImpl(timedObjectId, timedObjectInvoker, info);
         persistencePolicy.insertTimer(timedObjectId, initialExpiration, intervalDuration, info);
         timer.startTimer(initialExpiration, intervalDuration);
         return timer;
      }
      catch (Exception e)
      {
         log.error("Cannot create txtimer", e);
         return null;
      }
   }

   /**
    * Get all the active timers associated with this bean.
    *
    * @return A collection of javax.ejb.Timer objects.
    * @throws IllegalStateException  If this method is invoked while the instance is in
    *                                a state that does not allow access to this method.
    * @throws javax.ejb.EJBException If this method could not complete due to a system-level failure.
    */
   public Collection getTimers() throws IllegalStateException, EJBException
   {
      ArrayList activeTimers = new ArrayList();
      synchronized (timers)
      {
         Iterator it = timers.values().iterator();
         while (it.hasNext())
         {
            TimerImpl timer = (TimerImpl) it.next();
            if (timer.isActive())
               activeTimers.add(timer);
         }
      }
      return activeTimers;
   }

   /**
    * Get the list of all registerd timers, both active and inactive
    */
   public Collection getAllTimers()
   {
      synchronized (timers)
      {
         return new ArrayList(timers.values());
      }
   }

   /**
    * Get the Timer for the given timedObjectId
    */
   public Timer getTimer(TimerHandle handle)
   {
      TimerImpl timer = (TimerImpl) timers.get(handle);
      if (timer != null && timer.isActive())
         return timer;
      else
         return null;
   }

   /**
    * Kill the timer for the given handle
    */
   public void killTimer(TimerHandle handle)
   {
      TimerImpl timer = (TimerImpl) timers.get(handle);
      if (timer != null)
      {
         removeTimer(timer);
         timer.killTimer();
      }
   }

   /**
    * Kill all timers
    */
   public void killAllTimers()
   {
      synchronized (timers)
      {
         Iterator it = timers.values().iterator();
         while (it.hasNext())
         {
            TimerImpl timer = (TimerImpl) it.next();
            it.remove();
            timer.killTimer();
         }
      }
   }

   /**
    * Get the TimedObjectInvoker associated with this TimerService
    */
   public TimedObjectInvoker getTimedObjectInvoker()
   {
      return timedObjectInvoker;
   }

   /**
    * Get the current transaction
    */
   public Transaction getTransaction()
   {
      try
      {
         return transactionManager.getTransaction();
      }
      catch (SystemException e)
      {
         return null;
      }
   }

   /**
    * Add a txtimer to the list of active timers
    */
   void addTimer(TimerImpl txtimer)
   {
      synchronized (timers)
      {
         TimerHandle handle = new TimerHandleImpl(txtimer);
         timers.put(handle, txtimer);
      }
   }

   /**
    * Remove a txtimer from the list of active timers
    */
   void removeTimer(TimerImpl txtimer)
   {
      synchronized (timers)
      {
         persistencePolicy.deleteTimer(txtimer.getTimedObjectId(), txtimer.getFirstTime());
         timers.remove(new TimerHandleImpl(txtimer));
      }
   }
}
