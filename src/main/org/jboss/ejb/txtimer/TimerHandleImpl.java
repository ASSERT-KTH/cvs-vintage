/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.txtimer;

// $Id: TimerHandleImpl.java,v 1.6 2004/04/14 13:18:40 tdiesler Exp $

import javax.ejb.EJBException;
import javax.ejb.NoSuchObjectLocalException;
import javax.ejb.Timer;
import javax.ejb.TimerHandle;
import javax.management.MalformedObjectNameException;
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
   private Date createDate;
   private long periode;
   private long nextExpire;
   private Serializable info;
   private int hashCode;

   /**
    * Construct a handle from a timer
    */
   TimerHandleImpl(TimerImpl timer)
   {
      timedObjectId = timer.getTimedObjectId();
      firstTime = timer.getFirstTime();
      createDate = timer.getCreateDate();
      periode = timer.getPeriode();
      nextExpire = timer.getNextExpire();
      info = timer.getInfoInternal();
   }

   /**
    * Construct a handle from external form
    */
   public TimerHandleImpl(String externalForm)
   {
      if (externalForm.startsWith("[") == false || externalForm.endsWith("]") == false)
         throw new IllegalArgumentException("Square brackets expected arround: " + externalForm);

      try
      {
         // take first and last char off
         String inStr = externalForm.substring(1, externalForm.length() - 1);

         int pkIndex = inStr.indexOf("pk=");
         if (pkIndex < 0)
            throw new IllegalArgumentException("Cannot find 'pk=' in: " + inStr);

         String idStr = inStr.substring(0, pkIndex);
         String pkAndRestStr = inStr.substring(pkIndex);

         idStr = new ObjectName(idStr).toString();

         StringTokenizer st = new StringTokenizer(pkAndRestStr, ",=");
         if (st.countTokens() % 2 != 0)
            throw new IllegalArgumentException("Cannot parse: " + pkAndRestStr);

         SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);

         periode = -1;

         String pk = null;
         while (st.hasMoreTokens())
         {
            String key = st.nextToken();
            String value = st.nextToken();
            if (key.equals("pk"))
               pk = value;
            if (key.equals("created"))
               createDate = sdf.parse(value);
            if (key.equals("first"))
               firstTime = sdf.parse(value);
            if (key.equals("periode"))
               periode = new Integer(value).intValue();
         }

         if (createDate == null || firstTime == null || periode < 0)
            throw new IllegalArgumentException("Cannot parse: " + externalForm);

         timedObjectId = new TimedObjectId(idStr, pk);
      }
      catch (MalformedObjectNameException e)
      {
         throw new IllegalArgumentException("id is not a valid object name: " + externalForm);
      }
      catch (ParseException e)
      {
         throw new IllegalArgumentException("Cannot parse date/time in: " + externalForm);
      }

   }

   /**
    * Returns the external representation of the handle.
    * "[id=timedObjectId,created=createDate,first=firstTime,periode=periode]"
    */
   public String toExternalForm()
   {
      SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
      String created = sdf.format(createDate);
      String firstEvent = sdf.format(firstTime);
      String id = timedObjectId.getContainerId();
      Object pk = timedObjectId.getInstancePk();
      return "[id=" + id + ",pk=" + pk + ",created=" + created + ",first=" + firstEvent + ",periode=" + periode + "]";
   }

   TimedObjectId getTimedObjectId()
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

      EJBTimerService ejbTimerService = EJBTimerServiceLocator.getEjbTimerService();
      String containerId = timedObjectId.getContainerId();
      Object instancePk = timedObjectId.getInstancePk();
      TimerServiceImpl timerService = (TimerServiceImpl) ejbTimerService.getTimerService(containerId, instancePk);
      if (timerService == null)
         throw new NoSuchObjectLocalException("Timer not available: " + timedObjectId);

      TimerImpl timer = (TimerImpl) timerService.getTimer(this);
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
