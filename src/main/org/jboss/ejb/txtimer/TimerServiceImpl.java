package org.jboss.ejb.txtimer;

// $Id: TimerServiceImpl.java,v 1.1 2004/04/08 15:03:54 tdiesler Exp $

import org.jboss.logging.Logger;
import org.jboss.tm.TxManager;

import javax.ejb.EJBException;
import javax.ejb.NoSuchObjectLocalException;
import javax.ejb.Timer;
import javax.ejb.TimerHandle;
import javax.ejb.TimerService;
import javax.naming.InitialContext;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
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
 * session beans, and message-driven beans to be registered for txtimer callback events at
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

   private String timedObjectId;

   // maps TimerHandles to Timer objects
   private Map timers = new HashMap();

   /**
    * Create a Timer service for thr given TimedObject
    */
   public TimerServiceImpl(String timedObjectId)
   {
      this.timedObjectId = timedObjectId;
      try
      {
         InitialContext iniCtx = new InitialContext();
         transactionManager = (TransactionManager) iniCtx.lookup("TransactionManager");
      }
      catch (Exception e)
      {
         log.warn("Cannot obtain TransactionManager from JNDI: " + e.toString());
         transactionManager = TxManager.getInstance();
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
      try
      {
         TimerImpl timer = new TimerImpl(timedObjectId, initialExpiration, intervalDuration, info);
         log.debug("createTimer: " + timer);
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
            try
            {
               if (timer.getTimeRemaining() > 0)
                  activeTimers.add(timer);
            }
            catch (NoSuchObjectLocalException ignore)
            {
            }
         }
      }
      return activeTimers;
   }

   /**
    * Get the Timer for the given timedObjectId
    */
   public Timer getTimer(TimerHandle handle)
   {
      TimerImpl timer = (TimerImpl)timers.get(handle);
      if (timer != null && timer.getTimeRemaining() > 0)
         return timer;
      else
         return null;
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
         timers.put(txtimer.getHandle(), txtimer);
      }
   }

   /**
    * Remove a txtimer from the list of active timers
    */
   void removeTimer(TimerImpl txtimer)
   {
      synchronized (timers)
      {
         timers.remove(txtimer.getHandle());
      }
   }
}
