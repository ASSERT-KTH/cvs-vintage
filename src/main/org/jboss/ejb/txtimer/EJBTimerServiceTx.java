/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.txtimer;

// $Id: EJBTimerServiceTx.java,v 1.2 2004/04/08 21:54:27 tdiesler Exp $

import javax.ejb.Timer;
import javax.ejb.TimerService;
import java.util.HashMap;

/**
 * The EJBTimerService manages the TimerService for
 * an associated TimedObject.
 *
 * @jmx:mbean name="jboss:service=EJBTimerServiceTx"
 *    extends="org.jboss.ejb.txtimer.EJBTimerService"
 *
 * @author Thomas.Diesler@jboss.org
 * @since 07-Apr-2004
 */
public class EJBTimerServiceTx implements EJBTimerServiceTxMBean
{

   // maps the timedObjectId to ServiceEntry objects
   private HashMap timerServiceMap = new HashMap();

   /**
    * Create a TimerService for a given TimedObjectId
    * @param timedObjectId the id of the TimedObject
    * @param timedObjectInvoker a TimedObjectInvoker
    * @return the TimerService
    *
    * @jmx:managed-operation
    */
   public TimerService createTimerService(String timedObjectId, TimedObjectInvoker timedObjectInvoker)
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
    * @param timedObjectId The id of the TimedObject
    * @return The TimerService, or null if it does not exist
    *
    * @jmx:managed-operation
    */
   public TimerService getTimerService(String timedObjectId)
   {
      ServiceEntry serviceEntry = (ServiceEntry) timerServiceMap.get(timedObjectId);
      if (serviceEntry != null)
         return serviceEntry.timerService;
      else
         return null;
   }

   /**
    * Remove the TimerService for a given TimedObjectId
    * @param timedObjectId the id of the TimedObject
    *
    * @jmx:managed-operation
    */
   public void removeTimerService(String timedObjectId)
   {
      timerServiceMap.remove(timedObjectId);
   }

   /**
    * Invokes the ejbTimeout method on the TimedObject with the given id.
    * @param timedObjectId The id of the TimedObject
    * @param timer the Timer that is passed to ejbTimeout
    *
    * @jmx:managed-operation
    */
   public void invokeTimedObject(String timedObjectId, Timer timer) throws Exception
   {
      ServiceEntry serviceEntry = (ServiceEntry) timerServiceMap.get(timedObjectId);
      if (serviceEntry != null)
      {
         if (serviceEntry.timedObjectInvoker != null)
            serviceEntry.timedObjectInvoker.invokeTimedObject(timedObjectId, timer);
      }
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
