/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.txtimer;

// $Id: TimerImpl.java,v 1.1 2004/04/08 15:03:54 tdiesler Exp $

import org.jboss.logging.Logger;

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
 * The Timer contains information about a txtimer that was created
 * through the EJB Timer Service
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 07-Apr-2004
 */
public class TimerImpl implements javax.ejb.Timer, Synchronization
{
   // logging support
   private static Logger log = Logger.getLogger(TimerImpl.class);

   // The initial txtimer properties
   private String timedObjectId;
   private Date createDate;
   private long periode;
   private Serializable info;

   private long nextExpire;
   private boolean startedTx;
   private boolean started;
   private boolean canceledTx;
   private boolean canceled;
   private boolean expired;
   private Timer utilTimer;
   private int hashCode;

   /**
    * Schedules the txtimer for execution at the specified time with a specified periode.
    */
   TimerImpl(String timedObjectId, Date firstTime, long periode, Serializable info)
   {
      this.timedObjectId = timedObjectId;
      this.createDate = new Date();
      this.periode = periode;
      this.info = info;

      nextExpire = firstTime.getTime();

      TimerServiceImpl timerService = getTimerService(timedObjectId);
      timerService.addTimer(this);
      registerTimerWithTx();
      startInTx();
   }

   public String getTimedObjectId()
   {
      return timedObjectId;
   }

   public Date getCreateDate()
   {
      return createDate;
   }

   public long getPeriode()
   {
      return periode;
   }

   public long getNextExpire()
   {
      return nextExpire;
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
      registerTimerWithTx();
      cancelInTx();
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
    * Hash code based on objectId, createDate, periode
    */
   public int hashCode()
   {
      if (hashCode == 0)
      {
         String hash = "[" + timedObjectId + "," + createDate + "," + periode + "]";
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
      return "[id=" + timedObjectId + ",remaining=" + remaining + ",periode=" + periode + "]";
   }

   /**
    * Register the txtimer with the current transaction
    */
   private void registerTimerWithTx()
   {
      TimerServiceImpl timerService = getTimerService(timedObjectId);
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

   private void startInTx()
   {
      TimerServiceImpl timerService = getTimerService(timedObjectId);

      utilTimer = new Timer();
      if (periode > 0)
         utilTimer.schedule(new TimerTaskImpl(this), new Date(nextExpire), periode);
      else
         utilTimer.schedule(new TimerTaskImpl(this), new Date(nextExpire));

      if (timerService.getTransaction() != null)
         startedTx = true;
      else
         started = true;
   }

   private boolean isStartedInTx()
   {
      return started || startedTx;
   }

   private boolean isStarted()
   {
      return started;
   }

   private void cancelInTx()
   {
      TimerServiceImpl timerService = getTimerService(timedObjectId);
      if (timerService.getTransaction() != null)
      {
         log.debug("cancelInTx: " + this);
         canceledTx = true;
      }
      else
      {
         setCancel(true);
      }
   }

   private void setCancel(boolean cancel)
   {
      if (cancel)
      {
         log.debug("cancel: " + this);
         TimerServiceImpl timerService = getTimerService(timedObjectId);
         timerService.removeTimer(this);
         utilTimer.cancel();
      }
      canceled = cancel;
   }

   private boolean isCanceledInTx()
   {
      return canceled || canceledTx;
   }

   private boolean isCanceled()
   {
      return canceled;
   }

   /**
    * Get the TimerService accociated with this Timer
    */
   private TimerServiceImpl getTimerService(String timedObjectId)
   {
      EJBTimerService ejbTimerService = EJBTimerServiceTxLocator.getEjbTimerService();
      TimerServiceImpl timerService = (TimerServiceImpl)ejbTimerService.getTimerService(timedObjectId);
      if (timerService == null)
         throw new NoSuchObjectLocalException("Cannot find TimerService: " + timedObjectId);

      return timerService;
   }

   /**
    * Throws NoSuchObjectLocalException if the txtimer was canceled or has expired
    */
   private void assertTimedOut()
   {
      if (expired)
         throw new NoSuchObjectLocalException("Timer has expired");
      if (isCanceledInTx())
         throw new NoSuchObjectLocalException("Timer was canceled");
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
         setCancel(isCanceledInTx());
         started = isStartedInTx();
      }

      if (status == Status.STATUS_ROLLEDBACK)
      {
         log.debug("rollback: " + this);

         if (isCanceledInTx() && isCanceled() == false)
            canceledTx = false;

         if (isStartedInTx() && isStarted() == false)
            startedTx = false;

         if (isStartedInTx() == false)
            setCancel(true);
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

         if (isStartedInTx() == true && periode > 0)
            nextExpire += periode;

         if (isStartedInTx() == true && isCanceledInTx() == false)
         {
            EJBTimerService ejbTimerService = EJBTimerServiceTxLocator.getEjbTimerService();
            ejbTimerService.invokeTimedObject(timedObjectId, timer);
         }

         if (periode == 0)
         {
            expired = true;
            setCancel(true);
         }
      }
   }
}
