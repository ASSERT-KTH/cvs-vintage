/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.txtimer;

// $Id: TimerImpl.java,v 1.8 2004/04/22 17:44:22 tdiesler Exp $

import org.jboss.logging.Logger;
import org.jboss.ejb.AllowedOperationsAssociation;

import javax.ejb.EJBException;
import javax.ejb.NoSuchObjectLocalException;
import javax.ejb.TimerHandle;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.Transaction;
import java.io.Serializable;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * An implementation of an EJB Timer.
 * <p/>
 * Internally it uses a java.util.Timer and maintains its state in
 * a Tx manner.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 07-Apr-2004
 */
public class TimerImpl implements javax.ejb.Timer, Synchronization
{
   // logging support
   private static Logger log = Logger.getLogger(TimerImpl.class);

   /**
    * Timer states and their allowed transitions
    * <p/>
    * CREATED  - on create
    * CREATED -> STARTED_IN_TX - when strated with Tx
    * CREATED -> ACTIVE  - when started without Tx
    * STARTED_IN_TX -> ACTIVE - on Tx commit
    * STARTED_IN_TX -> CANCELED - on Tx rollback
    * ACTIVE -> CANCELED_IN_TX - on cancel() with Tx
    * ACTIVE -> CANCELED - on cancel() without Tx
    * CANCELED_IN_TX -> CANCELED - on Tx commit
    * CANCELED_IN_TX -> ACTIVE - on Tx rollback
    * ACTIVE -> IN_TIMEOUT - on TimerTask run
    * IN_TIMEOUT -> ACTIVE - on Tx commit if periode > 0
    * IN_TIMEOUT -> EXPIRED -> on Tx commit if periode == 0
    * IN_TIMEOUT -> RETRY_TIMEOUT -> on Tx rollback
    * RETRY_TIMEOUT -> ACTIVE -> on Tx commit/rollback if periode > 0
    * RETRY_TIMEOUT -> EXPIRED -> on Tx commit/rollback if periode == 0
    */
   private static final int CREATED = 0;
   private static final int STARTED_IN_TX = 1;
   private static final int ACTIVE = 2;
   private static final int CANCELED_IN_TX = 3;
   private static final int CANCELED = 4;
   private static final int EXPIRED = 5;
   private static final int IN_TIMEOUT = 6;
   private static final int RETRY_TIMEOUT = 7;

   private static final String[] TIMER_STATES = {"created", "started_in_tx", "active", "canceled_in_tx",
                                                 "canceled", "expired", "in_timeout", "retry_timeout"};

   // The initial txtimer properties
   private TimedObjectId timedObjectId;
   private TimedObjectInvoker timedObjectInvoker;
   private Date firstTime;
   private Date createDate;
   private long periode;
   private Serializable info;

   private long nextExpire;
   private int timerState;
   private Timer utilTimer;
   private int hashCode;

   /**
    * Schedules the txtimer for execution at the specified time with a specified periode.
    */
   TimerImpl(TimedObjectId timedObjectId, TimedObjectInvoker timedObjectInvoker, Serializable info)
   {
      this.timedObjectId = timedObjectId;
      this.timedObjectInvoker = timedObjectInvoker;
      this.createDate = new Date();
      this.info = info;

      setTimerState(CREATED);
   }

   void startTimer(Date firstTime, long periode)
   {
      this.firstTime = firstTime;
      this.periode = periode;

      nextExpire = firstTime.getTime();

      TimerServiceImpl timerService = getTimerService();
      timerService.addTimer(this);
      registerTimerWithTx();
      startInTx();
   }

   public TimedObjectId getTimedObjectId()
   {
      return timedObjectId;
   }

   public Date getCreateDate()
   {
      return createDate;
   }

   public Date getFirstTime()
   {
      return firstTime;
   }

   public long getPeriode()
   {
      return periode;
   }

   public long getNextExpire()
   {
      return nextExpire;
   }

   public Serializable getInfoInternal()
   {
      return info;
   }

   public boolean isActive()
   {
      return !isCanceled() && !isExpired();
   }

   public boolean isCanceled()
   {
      return timerState == CANCELED_IN_TX || timerState == CANCELED;
   }

   public boolean isExpired()
   {
      return timerState == EXPIRED;
   }

   /**
    * Cause the txtimer and all its associated expiration notifications to be cancelled.
    *
    * @throws IllegalStateException  If this method is invoked while the instance is in
    *                                a state that does not allow access to this method.
    * @throws javax.ejb.NoSuchObjectLocalException
    *                                If invoked on a txtimer that has expired or has been cancelled.
    * @throws javax.ejb.EJBException If this method could not complete due to a system-level failure.
    */
   public void cancel() throws IllegalStateException, NoSuchObjectLocalException, EJBException
   {
      assertTimedOut();
      assertAllowedOperation("Timer.cancel");
      registerTimerWithTx();
      cancelInTx();
   }

   /**
    * Kill the timer, and remove it from the timer service
    */
   public void killTimer()
   {
      log.debug("killTimer: " + this);
      if (timerState != EXPIRED)
         setTimerState(CANCELED);
      TimerServiceImpl timerService = getTimerService();
      timerService.removeTimer(this);
      utilTimer.cancel();
   }

   /**
    * Get the number of milliseconds that will elapse before the next scheduled txtimer expiration.
    *
    * @return Number of milliseconds that will elapse before the next scheduled txtimer expiration.
    * @throws IllegalStateException  If this method is invoked while the instance is in
    *                                a state that does not allow access to this method.
    * @throws javax.ejb.NoSuchObjectLocalException
    *                                If invoked on a txtimer that has expired or has been cancelled.
    * @throws javax.ejb.EJBException If this method could not complete due to a system-level failure.
    */
   public long getTimeRemaining() throws IllegalStateException, NoSuchObjectLocalException, EJBException
   {
      assertTimedOut();
      assertAllowedOperation("Timer.getTimeRemaining");
      return nextExpire - System.currentTimeMillis();
   }

   /**
    * Get the point in time at which the next txtimer expiration is scheduled to occur.
    *
    * @return Get the point in time at which the next txtimer expiration is scheduled to occur.
    * @throws IllegalStateException  If this method is invoked while the instance is in
    *                                a state that does not allow access to this method.
    * @throws javax.ejb.NoSuchObjectLocalException
    *                                If invoked on a txtimer that has expired or has been cancelled.
    * @throws javax.ejb.EJBException If this method could not complete due to a system-level failure.
    */
   public Date getNextTimeout() throws IllegalStateException, NoSuchObjectLocalException, EJBException
   {
      assertTimedOut();
      assertAllowedOperation("Timer.getNextTimeout");
      return new Date(nextExpire);
   }

   /**
    * Get the information associated with the txtimer at the time of creation.
    *
    * @return The Serializable object that was passed in at txtimer creation, or null if the
    *         info argument passed in at txtimer creation was null.
    * @throws IllegalStateException  If this method is invoked while the instance is in
    *                                a state that does not allow access to this method.
    * @throws javax.ejb.NoSuchObjectLocalException
    *                                If invoked on a txtimer that has expired or has been cancelled.
    * @throws javax.ejb.EJBException If this method could not complete due to a system-level failure.
    */
   public Serializable getInfo() throws IllegalStateException, NoSuchObjectLocalException, EJBException
   {
      assertTimedOut();
      assertAllowedOperation("Timer.getInfo");
      return info;
   }

   /**
    * Get a serializable handle to the txtimer. This handle can be used at a later time to
    * re-obtain the txtimer reference.
    *
    * @return Handle of the Timer
    * @throws IllegalStateException  If this method is invoked while the instance is in
    *                                a state that does not allow access to this method.
    * @throws javax.ejb.NoSuchObjectLocalException
    *                                If invoked on a txtimer that has expired or has been cancelled.
    * @throws javax.ejb.EJBException If this method could not complete due to a system-level failure.
    */
   public TimerHandle getHandle() throws IllegalStateException, NoSuchObjectLocalException, EJBException
   {
      assertTimedOut();
      assertAllowedOperation("Timer.getHandle");
      return new TimerHandleImpl(this);
   }

   /**
    * Return true if objectId, createDate, periode are equal
    */
   public boolean equals(Object obj)
   {
      if (obj == this) return true;
      if (obj instanceof TimerImpl)
      {
         TimerImpl other = (TimerImpl) obj;
         return hashCode() == other.hashCode();
      }
      return false;
   }

   /**
    * Hash code based on the Timers invariant properties
    */
   public int hashCode()
   {
      if (hashCode == 0)
      {
         String hash = "[" + timedObjectId + "," + createDate + "," + firstTime + "," + periode + "]";
         hashCode = hash.hashCode();
      }
      return hashCode;
   }

   /**
    * Returns a string representation of the object.
    */
   public String toString()
   {
      long remaining = nextExpire - System.currentTimeMillis();
      String retStr = "[" + timedObjectId + ",remaining=" + remaining + ",periode=" + periode +
              "," + TIMER_STATES[timerState] + "]";
      return retStr;
   }

   /**
    * Register the txtimer with the current transaction
    */
   private void registerTimerWithTx()
   {
      TimerServiceImpl timerService = getTimerService();
      Transaction tx = timerService.getTransaction();
      if (tx != null)
      {
         try
         {
            tx.registerSynchronization(this);
         }
         catch (Exception e)
         {
            log.error("Cannot register txtimer with Tx: " + this);
         }
      }
   }

   private void setTimerState(int state)
   {
      log.debug("setTimerState: " + TIMER_STATES[state]);
      timerState = state;

      // get rid of the expired timer
      if (timerState == EXPIRED)
         killTimer();
   }

   private void startInTx()
   {
      TimerServiceImpl timerService = getTimerService();

      utilTimer = new Timer();
      if (periode > 0)
         utilTimer.schedule(new TimerTaskImpl(this), new Date(nextExpire), periode);
      else
         utilTimer.schedule(new TimerTaskImpl(this), new Date(nextExpire));

      if (timerService.getTransaction() != null)
         setTimerState(STARTED_IN_TX);
      else
         setTimerState(ACTIVE);
   }

   private void cancelInTx()
   {
      TimerServiceImpl timerService = getTimerService();
      if (timerService.getTransaction() != null)
         setTimerState(CANCELED_IN_TX);
      else
         killTimer();
   }

   /**
    * Get the TimerService accociated with this Timer
    */
   private TimerServiceImpl getTimerService()
   {
      EJBTimerService ejbTimerService = EJBTimerServiceLocator.getEjbTimerService();
      String containerId = timedObjectId.getContainerId();
      Object instancePk = timedObjectId.getInstancePk();
      TimerServiceImpl timerService = (TimerServiceImpl) ejbTimerService.getTimerService(containerId, instancePk);
      if (timerService == null)
         throw new NoSuchObjectLocalException("Cannot find TimerService: " + timedObjectId);

      return timerService;
   }

   /** Throws NoSuchObjectLocalException if the txtimer was canceled or has expired
    */
   private void assertTimedOut()
   {
      if (timerState == EXPIRED)
         throw new NoSuchObjectLocalException("Timer has expired");
      if (timerState == CANCELED_IN_TX || timerState == CANCELED)
         throw new NoSuchObjectLocalException("Timer was canceled");
   }

   /** Throws an IllegalStateException if the Timer method call is not allowed in the current context
    */
   private void assertAllowedOperation(String timerMethod)
   {
      AllowedOperationsAssociation.assertAllowedIn(timerMethod,
              AllowedOperationsAssociation.IN_BUSINESS_METHOD |
              AllowedOperationsAssociation.IN_EJB_TIMEOUT |
              AllowedOperationsAssociation.IN_SERVICE_ENDPOINT_METHOD |
              AllowedOperationsAssociation.IN_AFTER_BEGIN |
              AllowedOperationsAssociation.IN_BEFORE_COMPLETION |
              AllowedOperationsAssociation.IN_EJB_POST_CREATE |
              AllowedOperationsAssociation.IN_EJB_REMOVE |
              AllowedOperationsAssociation.IN_EJB_LOAD |
              AllowedOperationsAssociation.IN_EJB_STORE);
   }

   // Synchronization **************************************************************************************************

   /**
    * This method is invoked before the start of the commit or rollback
    * process. The method invocation is done in the context of the
    * transaction that is about to be committed or rolled back.
    */
   public void beforeCompletion()
   {
   }

   /**
    * This method is invoked after the transaction has committed or
    * rolled back.
    *
    * @param status The status of the completed transaction.
    */
   public void afterCompletion(int status)
   {
      if (status == Status.STATUS_COMMITTED)
      {
         log.debug("commit: " + this);

         if (timerState == STARTED_IN_TX)
            setTimerState(ACTIVE);
         else if (timerState == CANCELED_IN_TX)
            setTimerState(CANCELED);
         else if (timerState == IN_TIMEOUT || timerState == RETRY_TIMEOUT)
            setTimerState(periode == 0 ? EXPIRED : ACTIVE);
      }

      if (status == Status.STATUS_ROLLEDBACK)
      {
         log.debug("rollback: " + this);

         if (timerState == STARTED_IN_TX)
            killTimer();
         else if (timerState == CANCELED_IN_TX)
            setTimerState(ACTIVE);
         else if (timerState == IN_TIMEOUT)
         {
            setTimerState(RETRY_TIMEOUT);
            log.debug("retry: " + this);
            EJBTimerService ejbTimerService = EJBTimerServiceLocator.getEjbTimerService();
            String containerId = timedObjectId.getContainerId();
            Object instancePk = timedObjectId.getInstancePk();
            ejbTimerService.retryTimeout(containerId, instancePk, this);
         }
         else if (timerState == IN_TIMEOUT || timerState == RETRY_TIMEOUT)
            setTimerState(periode == 0 ? EXPIRED : ACTIVE);
      }
   }
   // TimerTask ********************************************************************************************************

   /**
    * The TimerTask's run method is invoked by the java.util.Timer
    */
   private class TimerTaskImpl extends TimerTask
   {
      private TimerImpl timer;

      public TimerTaskImpl(TimerImpl timer)
      {
         this.timer = timer;
      }

      /**
       * The action to be performed by this txtimer task.
       */
      public void run()
      {
         log.debug("run: " + timer);

         if (isActive() && periode > 0)
            nextExpire += periode;

         if (isActive())
         {
            try
            {
               setTimerState(IN_TIMEOUT);
               timedObjectInvoker.callTimeout(timer);
            }
            catch (Exception e)
            {
               log.error("Error invoking ejbTimeout: " + e.toString());
            }
            finally
            {
               if (timerState == IN_TIMEOUT)
               {
                  log.warn("Timer was not registered with Tx, reseting state: " + timer);
                  setTimerState(periode == 0 ? EXPIRED : ACTIVE);
               }
            }
         }
      }
   }
}
