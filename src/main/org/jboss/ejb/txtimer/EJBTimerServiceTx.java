/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.txtimer;

// $Id: EJBTimerServiceTx.java,v 1.4 2004/04/13 10:10:39 tdiesler Exp $

import org.jboss.logging.Logger;
import org.jboss.mx.util.MBeanServerLocator;

import javax.ejb.Timer;
import javax.ejb.TimerService;
import javax.management.ObjectName;
import javax.management.MBeanServer;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Collection;
import java.util.Collections;
import java.text.ParseException;
import java.io.Serializable;

/**
 * The EJBTimerService manages the TimerService for
 * an associated TimedObject.
 *
 * @jmx.mbean
 *    name="jboss:service=EJBTimerServiceTx"
 *    extends="org.jboss.ejb.txtimer.EJBTimerService"
 *
 * @author Thomas.Diesler@jboss.org
 * @since 07-Apr-2004
 */
public class EJBTimerServiceTx implements EJBTimerServiceTxMBean
{
   // logging support
   private static Logger log = Logger.getLogger(EJBTimerServiceTx.class);

   // maps the timedObjectId to ServiceEntry objects
   private Map timerServiceMap = Collections.synchronizedMap(new HashMap());

   // the object name of the retry policy
   private ObjectName retryPolicyName;

   /**
    * Get the object name of the retry policy.
    * @jmx.managed-attribute
    */
   public ObjectName getRetryPolicy()
   {
      return retryPolicyName;
   }

   /**
    * Set the object name of the retry policy.
    * @jmx.managed-attribute
    */
   public void setRetryPolicy(ObjectName retryPolicyName)
   {
      this.retryPolicyName = retryPolicyName;
   }

   /**
    * Create a TimerService for a given TimedObjectId
    * @jmx.managed-operation
    * @param timedObjectId The combined TimedObjectId
    * @param timedObjectInvoker a TimedObjectInvoker
    * @return the TimerService
    */
   public TimerService createTimerService(TimedObjectId timedObjectId, TimedObjectInvoker timedObjectInvoker)
   {
      ServiceEntry serviceEntry = (ServiceEntry) timerServiceMap.get(timedObjectId);
      if (serviceEntry == null)
      {
         TimerServiceImpl timerService = new TimerServiceImpl(timedObjectId);
         log.debug("createTimerService: " + timerService);
         serviceEntry = new ServiceEntry(timerService, timedObjectInvoker);
         timerServiceMap.put(timedObjectId, serviceEntry);
      }
      return serviceEntry.timerService;

   }

   /**
    * Get the TimerService for a given TimedObjectId
    * @jmx.managed-operation
    * @param timedObjectId The combined TimedObjectId
    * @return The TimerService, or null if it does not exist
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
    * Invokes the ejbTimeout method on a given TimedObjectId
    * @jmx.managed-operation
    * @param timedObjectId The combined TimedObjectId
    * @param timer the Timer that is passed to ejbTimeout
    */
   public void callTimeout(TimedObjectId timedObjectId, Timer timer) throws Exception
   {
      ServiceEntry serviceEntry = (ServiceEntry) timerServiceMap.get(timedObjectId);
      if (serviceEntry != null)
         serviceEntry.timedObjectInvoker.callTimeout(timer);
   }

   /**
    * Invokes the ejbTimeout method a given TimedObjectId
    * @jmx.managed-operation
    * @param timedObjectId The combined TimedObjectId
    * @param timer the Timer that is passed to ejbTimeout
    */
   public void retryTimeout(TimedObjectId timedObjectId, Timer timer)
   {
      ServiceEntry serviceEntry = (ServiceEntry) timerServiceMap.get(timedObjectId);
      if (serviceEntry != null)
      {
         try
         {
            // Invoke retry policy, through the MBeanServer
            TimedObjectInvoker invoker = serviceEntry.timedObjectInvoker;
            if (retryPolicyName != null)
            {
               MBeanServer server = MBeanServerLocator.locateJBoss();
               server.invoke(retryPolicyName,
                       "retryTimeout",
                       new Object[]{invoker, timer},
                       new String[]{TimedObjectInvoker.class.getName(), Timer.class.getName()});
            }

            // If the retry policy is not set, fall back to the FixedDelayRetryPolicy
            else
            {
               TimeoutRetryPolicy retryPolicy = new FixedDelayRetryPolicy();
               retryPolicy.retryTimeout(invoker, timer);
            }
         }
         catch (Exception e)
         {
            log.error("Retry timeout failed: " + e);
         }
      }
   }

   /**
    * Remove the TimerService for a given TimedObjectId
    * If the instance pk is left to null, it removes all timer services for the container id
    * @jmx.managed-operation
    * @param timedObjectId The combined TimedObjectId
    */
   public void removeTimerService(TimedObjectId timedObjectId)
   {

      // remove a single timer service
      if (timedObjectId.getInstancePk() != null)
      {
         TimerServiceImpl timerService = (TimerServiceImpl) getTimerService(timedObjectId);
         if (timerService != null)
         {
            log.debug("removeTimerService: " + timerService);
            timerService.killAllTimers();
            timerServiceMap.remove(timedObjectId);
         }
      }
      // remove all timers with the given containerId
      else
      {
         String containerId = timedObjectId.getContainerId();
         Iterator it = timerServiceMap.entrySet().iterator();
         while (it.hasNext())
         {
            Map.Entry entry = (Map.Entry) it.next();
            TimedObjectId key = (TimedObjectId) entry.getKey();
            ServiceEntry serviceEntry = (ServiceEntry) entry.getValue();
            TimerServiceImpl timerService = serviceEntry.timerService;
            if (containerId.equals(key.getContainerId()))
            {
               log.debug("removeTimerService: " + timerService);
               timerService.killAllTimers();
               it.remove();
            }
         }
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
         TimerServiceImpl timerService = (TimerServiceImpl) serviceEntry.timerService;
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

   /**
    * An entry in the timer service map.
    */
   private static class ServiceEntry
   {
      TimerServiceImpl timerService;
      TimedObjectInvoker timedObjectInvoker;

      public ServiceEntry(TimerServiceImpl timerService, TimedObjectInvoker timedObjectInvoker)
      {
         this.timerService = timerService;
         this.timedObjectInvoker = timedObjectInvoker;
      }
   }
}
