/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.txtimer;

// $Id: EJBTimerServiceImpl.java,v 1.2 2004/09/09 22:04:29 tdiesler Exp $

import org.jboss.ejb.Container;
import org.jboss.logging.Logger;
import org.jboss.mx.util.MBeanServerLocator;
import org.jboss.mx.util.MBeanProxy;
import org.jboss.system.ServiceMBeanSupport;

import javax.ejb.Timer;
import javax.ejb.TimerService;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A service that implements this interface provides an Tx aware EJBTimerService.
 *
 * @author Thomas.Diesler@jboss.org
 * @jmx.mbean name="jboss.ejb:service=EJBTimerService"
 * extends="org.jboss.ejb.txtimer.EJBTimerService"
 * @since 07-Apr-2004
 */
public class EJBTimerServiceImpl
        extends ServiceMBeanSupport
        implements EJBTimerServiceImplMBean
{
   // logging support
   private static Logger log = Logger.getLogger(EJBTimerServiceImpl.class);

   // maps the timedObjectId to ServiceEntry objects
   private Map timerServiceMap = Collections.synchronizedMap(new HashMap());

   // The object name of the retry policy
   private ObjectName retryPolicyName;
   // The object name of the retry policy
   private ObjectName persistencePolicyName;

   // The TimedObjectInvoker class name
   private String timedObjectInvokerClassName;

   /** Get the object name of the retry policy.
    * @jmx.managed-attribute
    */
   public ObjectName getRetryPolicy()
   {
      return retryPolicyName;
   }

   /** Set the object name of the retry policy.
    * @jmx.managed-attribute
    */
   public void setRetryPolicy(ObjectName retryPolicyName)
   {
      this.retryPolicyName = retryPolicyName;
   }

   /** Get the object name of the persistence policy.
    * @jmx.managed-attribute
    */
   public ObjectName getPersistencePolicy()
   {
      return persistencePolicyName;
   }

   /** Set the object name of the persistence policy.
    * @jmx.managed-attribute
    */
   public void setPersistencePolicy(ObjectName persistencePolicyName)
   {
      this.persistencePolicyName = persistencePolicyName;
   }

   /**
    * Get the TimedObjectInvoker class name
    *
    * @jmx.managed-attribute
    */
   public String getTimedObjectInvokerClassName()
   {
      return timedObjectInvokerClassName;
   }

   /**
    * Set the TimedObjectInvoker class name
    *
    * @jmx.managed-attribute
    */
   public void setTimedObjectInvokerClassName(String timedObjectInvokerClassName)
   {
      this.timedObjectInvokerClassName = timedObjectInvokerClassName;
   }

   /**
    * Create a TimerService for a given TimedObjectId that lives in a JBoss Container.
    * The TimedObjectInvoker is constructed from the invokerClassName.
    *
    * @param containerId The string identifier for a class of TimedObjects
    * @param instancePk  The rimary key for an instance of a TimedObject, may be null
    * @param container   The Container that is associated with the TimerService
    * @return the TimerService
    * @jmx.managed-operation
    */
   public TimerService createTimerService(String containerId, Object instancePk, Container container)
   {
      TimedObjectInvoker invoker = null;
      try
      {
         TimedObjectId timedObjectId = new TimedObjectId(containerId, instancePk);
         Class invokerClass = getClass().getClassLoader().loadClass(timedObjectInvokerClassName);
         Constructor constr = invokerClass.getConstructor(new Class[]{TimedObjectId.class, Container.class});
         invoker = (TimedObjectInvoker)constr.newInstance(new Object[]{timedObjectId, container});
      }
      catch (Exception e)
      {
         log.error("Cannot create TimedObjectInvoker: " + timedObjectInvokerClassName, e);
         return null;
      }

      return createTimerService(containerId, instancePk, invoker);
   }

   /**
    * Create a TimerService for a given TimedObjectId that is invoked through the given invoker
    *
    * @param containerId The string identifier for a class of TimedObjects
    * @param instancePk  The rimary key for an instance of a TimedObject, may be null
    * @param invoker     The TimedObjectInvoker
    * @return the TimerService
    * @jmx.managed-operation
    */
   public TimerService createTimerService(String containerId, Object instancePk, TimedObjectInvoker invoker)
   {
      TimedObjectId timedObjectId = new TimedObjectId(containerId, instancePk);
      TimerServiceImpl timerService = (TimerServiceImpl)timerServiceMap.get(timedObjectId);
      if (timerService == null)
      {
         timerService = new TimerServiceImpl(timedObjectId, invoker);
         log.debug("createTimerService: " + timerService);
         timerServiceMap.put(timedObjectId, timerService);
      }
      return timerService;
   }

   /**
    * Get the TimerService for a given TimedObjectId
    *
    * @param containerId The string identifier for a class of TimedObjects
    * @param instancePk  The rimary key for an instance of a TimedObject, may be null
    * @return The TimerService, or null if it does not exist
    * @jmx.managed-operation
    */
   public TimerService getTimerService(String containerId, Object instancePk)
   {
      TimedObjectId timedObjectId = new TimedObjectId(containerId, instancePk);
      return (TimerServiceImpl)timerServiceMap.get(timedObjectId);
   }

   /**
    * Invokes the ejbTimeout method a given TimedObjectId
    *
    * @param containerId The string identifier for a class of TimedObjects
    * @param instancePk  The rimary key for an instance of a TimedObject, may be null
    * @param timer       the Timer that is passed to ejbTimeout
    * @jmx.managed-operation
    */
   public void retryTimeout(String containerId, Object instancePk, Timer timer)
   {
      TimedObjectId timedObjectId = new TimedObjectId(containerId, instancePk);
      TimerServiceImpl timerService = (TimerServiceImpl)timerServiceMap.get(timedObjectId);
      if (timerService != null)
      {
         try
         {
            // Invoke retry policy, through the MBeanServer
            TimedObjectInvoker invoker = timerService.getTimedObjectInvoker();
            MBeanServer server = MBeanServerLocator.locateJBoss();
            RetryPolicy retryPolicy = (RetryPolicy)MBeanProxy.get(RetryPolicy.class, getRetryPolicy(), server);
            retryPolicy.retryTimeout(invoker, timer);
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
    *
    * @param containerId The string identifier for a class of TimedObjects
    * @param instancePk  The rimary key for an instance of a TimedObject, may be null
    * @jmx.managed-operation
    */
   public void removeTimerService(String containerId, Object instancePk)
   {
      // remove a single timer service
      TimedObjectId timedObjectId = new TimedObjectId(containerId, instancePk);
      if (timedObjectId.getInstancePk() != null)
      {
         TimerServiceImpl timerService = (TimerServiceImpl)getTimerService(containerId, instancePk);
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
         Iterator it = timerServiceMap.entrySet().iterator();
         while (it.hasNext())
         {
            Map.Entry entry = (Map.Entry)it.next();
            TimedObjectId key = (TimedObjectId)entry.getKey();
            TimerServiceImpl timerService = (TimerServiceImpl)entry.getValue();
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
         Map.Entry entry = (Map.Entry)it.next();
         TimedObjectId timedObjectId = (TimedObjectId)entry.getKey();
         retBuffer.append(timedObjectId + "\n");

         TimerServiceImpl timerService = (TimerServiceImpl)entry.getValue();
         Collection col = timerService.getAllTimers();
         for (Iterator iterator = col.iterator(); iterator.hasNext();)
         {
            TimerImpl timer = (TimerImpl)iterator.next();
            TimerHandleImpl handle = new TimerHandleImpl(timer);
            retBuffer.append("   handle: " + handle + "\n");
            retBuffer.append("      " + timer + "\n");
         }
      }
      return retBuffer.toString();
   }
}
