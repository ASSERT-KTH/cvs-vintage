/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.txtimer;

// $Id: TimerHandleImpl.java,v 1.8 2004/09/10 14:37:16 tdiesler Exp $

import javax.ejb.EJBException;
import javax.ejb.NoSuchObjectLocalException;
import javax.ejb.Timer;
import javax.ejb.TimerHandle;
import javax.management.ObjectName;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;

/**
 * An implementation of the TimerHandle
 *
 * @author Thomas.Diesler@jboss.org
 * @since 07-Apr-2004
 */
public class TimerHandleImpl implements TimerHandle
{
   /**
    * The date pattern used by this handle
    */
   public static final String DATE_PATTERN = "dd-MMM-yyyy HH:mm:ss.SSS";

   // The initial txtimer properties
   private TimedObjectId timedObjectId;
   private Date firstTime;
   private long periode;
   private Serializable info;
   private int hashCode;

   /**
    * Construct a handle from a timer
    */
   TimerHandleImpl(TimerImpl timer)
   {
      timedObjectId = timer.getTimedObjectId();
      firstTime = timer.getFirstTime();
      periode = timer.getPeriode();
      info = timer.getInfoInternal();
   }

   /**
    * Construct a handle from individual parameters
    */
   TimerHandleImpl(TimedObjectId timedObjectId, Date firstTime, long periode, Serializable info)
   {
      this.timedObjectId = timedObjectId;
      this.firstTime = firstTime;
      this.periode = periode;
      this.info = info;
   }

   /**
    * Construct a handle from external form
    */
   private TimerHandleImpl(String externalForm)
   {
      if (externalForm.startsWith("[") == false || externalForm.endsWith("]") == false)
         throw new IllegalArgumentException("Square brackets expected arround: " + externalForm);

      try
      {
         // take first and last char off
         String inStr = externalForm.substring(1, externalForm.length() - 1);

         if (inStr.startsWith("toid=") == false)
            throw new IllegalArgumentException("Cannot parse: " + externalForm);

         inStr = inStr.substring(5);
         int firstIndex = inStr.indexOf(",first=");
         String toidStr = inStr.substring(0, firstIndex);
         String restStr = inStr.substring(firstIndex + 1);

         timedObjectId = TimedObjectId.parse(toidStr);

         StringTokenizer st = new StringTokenizer(restStr, ",=");
         if (st.countTokens() % 2 != 0)
            throw new IllegalArgumentException("Cannot parse: " + externalForm);

         SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);

         periode = -1;

         while (st.hasMoreTokens())
         {
            String key = st.nextToken();
            String value = st.nextToken();
            if (key.equals("first"))
               firstTime = sdf.parse(value);
            if (key.equals("periode"))
               periode = new Long(value).longValue();
         }

         if (firstTime == null || periode < 0)
            throw new IllegalArgumentException("Cannot parse: " + externalForm);
      }
      catch (ParseException e)
      {
         throw new IllegalArgumentException("Cannot parse date/time in: " + externalForm);
      }
   }

   /**
    * Parse the handle from external form.
    * "[toid=timedObjectId,first=firstTime,periode=periode]"
    */
   public static TimerHandleImpl parse(String externalForm)
   {
      return new TimerHandleImpl(externalForm);
   }

   /**
    * Returns the external representation of the handle.
    * "[toid=timedObjectId,first=firstTime,periode=periode]"
    */
   public String toExternalForm()
   {
      SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
      String firstEvent = sdf.format(firstTime);
      return "[toid=" + timedObjectId + ",first=" + firstEvent + ",periode=" + periode + "]";
   }

   public TimedObjectId getTimedObjectId()
   {
      return timedObjectId;
   }

   public Date getFirstTime()
   {
      return firstTime;
   }

   public long getPeriode()
   {
      return periode;
   }

   public Serializable getInfo()
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

      EJBTimerService ejbTimerService = EJBTimerServiceLocator.getEjbTimerService();
      ObjectName containerId = timedObjectId.getContainerId();
      Object instancePk = timedObjectId.getInstancePk();
      TimerServiceImpl timerService = (TimerServiceImpl)ejbTimerService.getTimerService(containerId, instancePk);
      if (timerService == null)
         throw new NoSuchObjectLocalException("TimerService not available: " + timedObjectId);

      TimerImpl timer = (TimerImpl)timerService.getTimer(this);
      if (timer == null || timer.isActive() == false)
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
         TimerHandleImpl other = (TimerHandleImpl)obj;
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
         hashCode = toExternalForm().hashCode();
      }
      return hashCode;
   }

   /**
    * Returns a string representation of the object.
    */
   public String toString()
   {
      return toExternalForm();
   }
}
