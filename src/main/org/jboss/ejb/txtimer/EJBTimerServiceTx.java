/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.txtimer;

// $Id: EJBTimerServiceTx.java,v 1.3 2004/04/09 22:47:01 tdiesler Exp $

import javax.ejb.Timer;
import javax.ejb.TimerService;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Collection;
import java.text.ParseException;
import java.io.Serializable;

/**
 * The EJBTimerService manages the TimerService for
 * an associated TimedObject.
 *
 * @author Thomas.Diesler@jboss.org
 * @jmx.mbean name="jboss:service=EJBTimerServiceTx"
 * extends="org.jboss.ejb.txtimer.EJBTimerService"
 * @since 07-Apr-2004
 */
public class EJBTimerServiceTx implements EJBTimerServiceTxMBean
{
   // maps the timedObjectId to ServiceEntry objects
   private HashMap timerServiceMap = new HashMap();

   /**
    * Create a TimerService for a given TimedObjectId
    *
    * @param timedObjectId The combined TimedObjectId
    * @param timedObjectInvoker a TimedObjectInvoker
    * @return the TimerService
    * @jmx.managed-operation
    */
   public TimerService createTimerService(TimedObjectId timedObjectId, TimedObjectInvoker timedObjectInvoker)
   {
      TimerService timerService = (TimerService) timerServiceMap.get(timedObjectId);
      if (timerService == null)
      {
         timerService = new TimerServiceImpl(timedObjectId);
         timerServiceMap.put(timedObjectId, new ServiceEntry(timerService, timedObjectInvoker));
      }
      return timerService;

   }

   /**
    * Get the TimerService for a given TimedObjectId
    *
    * @param timedObjectId The combined TimedObjectId
    * @return The TimerService, or null if it does not exist
    * @jmx.managed-operation
    */
   public TimerService getTimerService(TimedObjectId timedObjectId)
   {
      ServiceEntry serviceEntry = (ServiceEntry) timerServiceMap.get(timedObjectId);
      if (serviceEntry != null)
         return serviceEntry.timerService;
      else
         return null;
   }

   /**
    * Remove the TimerService for a given TimedObjectId
    *
    * @param timedObjectId The combined TimedObjectId
    * @jmx.managed-operation
    */
   public void removeTimerService(TimedObjectId timedObjectId)
   {
      timerServiceMap.remove(timedObjectId);
   }

   /**
    * Invokes the ejbTimeout method on the TimedObject with the given id.
    *
    * @param timedObjectId The combined TimedObjectId
    * @param timer The Timer that is passed to ejbTimeout
    * @jmx.managed-operation
    */
   public void invokeTimedObject(TimedObjectId timedObjectId, Timer timer) throws Exception
   {
      ServiceEntry serviceEntry = (ServiceEntry) timerServiceMap.get(timedObjectId);
      if (serviceEntry != null)
      {
         if (serviceEntry.timedObjectInvoker != null)
            serviceEntry.timedObjectInvoker.invokeTimedObject(timedObjectId, timer);
      }
   }

   /**
    * List the timers registered with all TimerService objects
    *
    * @jmx.managed-operation
    */
   public String listTimers()
   {
      StringBuffer retBuffer = new StringBuffer();
      Iterator it = timerServiceMap.entrySet().iterator();
      while (it.hasNext())
      {
         Map.Entry entry = (Map.Entry) it.next();
         TimedObjectId timedObjectId = (TimedObjectId) entry.getKey();
         retBuffer.append(timedObjectId + "\n");

         ServiceEntry serviceEntry = (ServiceEntry) entry.getValue();
         TimerServiceImpl timerService = (TimerServiceImpl)serviceEntry.timerService;
         Collection col = timerService.getAllTimers();
         for (Iterator iterator = col.iterator(); iterator.hasNext();)
         {
            TimerImpl timer = (TimerImpl) iterator.next();
            TimerHandleImpl handle = new TimerHandleImpl(timer);
            retBuffer.append("   handle: " + handle + "\n");
            retBuffer.append("      " + timer + "\n");
         }
      }
      return retBuffer.toString();
   }

   private static class ServiceEntry
   {
      TimerService timerService;
      TimedObjectInvoker timedObjectInvoker;

      public ServiceEntry(TimerService timerService, TimedObjectInvoker timedObjectInvoker)
      {
         this.timerService = timerService;
         this.timedObjectInvoker = timedObjectInvoker;
      }
   }
}
