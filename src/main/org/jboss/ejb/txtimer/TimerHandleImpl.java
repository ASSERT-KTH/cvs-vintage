/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.txtimer;

// $Id: TimerHandleImpl.java,v 1.2 2004/04/08 21:54:27 tdiesler Exp $

import javax.ejb.EJBException;
import javax.ejb.NoSuchObjectLocalException;
import javax.ejb.Timer;
import javax.ejb.TimerHandle;
import java.io.Serializable;
import java.util.Date;

/**
 * An implementation of the TimerHandle
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 07-Apr-2004
 */
public class TimerHandleImpl implements TimerHandle
{
   // The initial txtimer properties
   private String timedObjectId;
   private Date firstTime;
   private Date createDate;
   private long periode;
   private long nextExpire;
   private Serializable info;
   private int hashCode;

   TimerHandleImpl (TimerImpl timer)
   {
      timedObjectId = timer.getTimedObjectId();
      firstTime = timer.getFirstTime();
      createDate = timer.getCreateDate();
      periode = timer.getPeriode();
      nextExpire = timer.getNextExpire();
      info = timer.getInfoInternal();
   }

   String getTimedObjectId()
   {
      return timedObjectId;
   }

   Date getCreateDate()
   {
      return createDate;
   }

   long getPeriode()
   {
      return periode;
   }

   long getNextExpire()
   {
      return nextExpire;
   }

   Serializable getInfo()
   {
      return info;
   }

   /**
    * Obtain a reference to the txtimer represented by this handle.
    *
    * @return Timer which this handle represents
    * @throws IllegalStateException  If this method is invoked while the instance is in
    *                                a state that does not allow access to this method.
    * @throws javax.ejb.NoSuchObjectLocalException
    *                                If invoked on a txtimer that has expired or has been cancelled.
    * @throws javax.ejb.EJBException If this method could not complete due to a system-level failure.
    */
   public Timer getTimer() throws IllegalStateException, NoSuchObjectLocalException, EJBException
   {

      EJBTimerService ejbTimerService = EJBTimerServiceTxLocator.getEjbTimerService();
      TimerServiceImpl timerService = (TimerServiceImpl)ejbTimerService.getTimerService(timedObjectId);
      Timer timer = timerService.getTimer(this);

      if (timer == null)
         throw new NoSuchObjectLocalException("Timer not available: " + timedObjectId);

      return timer;
   }

   /**
    * Return true if objectId, createDate, periode are equal
    */
   public boolean equals(Object obj)
   {
      if (obj == this) return true;
      if (obj instanceof TimerHandleImpl)
      {
         TimerHandleImpl other = (TimerHandleImpl) obj;
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
      return "[id=" + timedObjectId + ",remaining=" + remaining + ",periode=" + periode + "]";
   }
}
