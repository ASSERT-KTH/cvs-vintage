/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.txtimer;

// $Id: EJBTimerServiceLocator.java,v 1.1 2004/04/13 10:10:39 tdiesler Exp $

import org.jboss.logging.Logger;
import org.jboss.mx.util.MBeanServerLocator;

import javax.ejb.Timer;
import javax.ejb.TimerService;
import javax.management.MBeanServer;

/**
 * The EJBTimerServiceLocator locates EJBTimerServiceTx.
 * <p/>
 * It first checks if the EJBTimerServiceTx is registerd with the MBeanServer,
 * if not it creates a singleton and uses that.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 07-Apr-2004
 */
public class EJBTimerServiceLocator
{
   // logging support
   private static Logger log = Logger.getLogger(EJBTimerServiceLocator.class);

   private static EJBTimerService ejbTimerService;

   /**
    * Locates the EJBTimerService, first as MBean, then as singleton
    */
   public static EJBTimerService getEjbTimerService()
   {
      try
      {
         // First try the MBean server
         MBeanServer server = MBeanServerLocator.locateJBoss();
         if (server != null && server.isRegistered(EJBTimerService.OBJECT_NAME))
            ejbTimerService = new MBeanDelegate(server);
      }
      catch (Exception ignore)
      {
      }

      // This path can be used for standalone test cases
      if (ejbTimerService == null)
         ejbTimerService = new EJBTimerServiceTx();

      return ejbTimerService;
   }

   /**
    * Delegates method calls to the EJBTimerService to the MBean server
    */
   public static class MBeanDelegate implements EJBTimerService
   {
      private MBeanServer server;

      public MBeanDelegate(MBeanServer server)
      {
         this.server = server;
      }

      /**
       * Create a TimerService for a given TimedObjectId
       *
       * @param timedObjectId The combined TimedObjectId
       * @param timedObjectInvoker a TimedObjectInvoker
       * @return the TimerService
       */
      public TimerService createTimerService(TimedObjectId timedObjectId, TimedObjectInvoker timedObjectInvoker)
              throws IllegalStateException
      {
         try
         {
            TimerService timerService = (TimerService) server.invoke(EJBTimerService.OBJECT_NAME,
                    "createTimerService",
                    new Object[]{timedObjectId, timedObjectInvoker},
                    new String[]{TimedObjectId.class.getName(), TimedObjectInvoker.class.getName()});
            return timerService;
         }
         catch (Exception e)
         {
            log.error("Cannot createTimerService", e);
            return null;
         }
      }

      /**
       * Get the TimerService for a given TimedObjectId
       *
       * @param timedObjectId The combined TimedObjectId
       * @return The TimerService, or null if it does not exist
       */
      public TimerService getTimerService(TimedObjectId timedObjectId)
              throws IllegalStateException
      {
         try
         {
            TimerService timerService = (TimerService) server.invoke(EJBTimerService.OBJECT_NAME,
                    "getTimerService",
                    new Object[]{timedObjectId},
                    new String[]{TimedObjectId.class.getName()});
            return timerService;
         }
         catch (Exception e)
         {
            log.error("Cannot getTimerService", e);
            return null;
         }
      }

      /**
       * Invokes the ejbTimeout method on a given TimedObjectId
       * @param timedObjectId The combined TimedObjectId
       * @param timer the Timer that is passed to ejbTimeout
       */
      public void callTimeout(TimedObjectId timedObjectId, Timer timer) throws Exception
      {
         try
         {
            server.invoke(EJBTimerService.OBJECT_NAME,
                    "callTimeout",
                    new Object[]{timedObjectId, timer},
                    new String[]{TimedObjectId.class.getName(), Timer.class.getName()});
         }
         catch (Exception e)
         {
            log.error("Cannot callTimeout", e);
         }
      }

      /**
       * Invokes the ejbTimeout method a given TimedObjectId
       * @param timedObjectId The combined TimedObjectId
       * @param timer the Timer that is passed to ejbTimeout
       */
      public void retryTimeout(TimedObjectId timedObjectId, Timer timer)
      {
         try
         {
            server.invoke(EJBTimerService.OBJECT_NAME,
                    "retryTimeout",
                    new Object[]{timedObjectId, timer},
                    new String[]{TimedObjectId.class.getName(), Timer.class.getName()});
         }
         catch (Exception e)
         {
            log.error("Cannot callTimeout", e);
         }
      }

      /**
       * Remove the TimerService for a given TimedObjectId
       *
       * @param timedObjectId The combined TimedObjectId
       */
      public void removeTimerService(TimedObjectId timedObjectId)
              throws IllegalStateException
      {
         try
         {
            server.invoke(EJBTimerService.OBJECT_NAME,
                    "removeTimerService",
                    new Object[]{timedObjectId},
                    new String[]{TimedObjectId.class.getName()});
         }
         catch (Exception e)
         {
            log.error("Cannot removeTimerService", e);
         }
      }

      /**
       * Invokes the ejbTimeout method on the TimedObject with the given id.
       * @param invoker the invoker for the TimedObject
       * @param timer the Timer that is passed to ejbTimeout
       */
      public void retryTimeout(TimedObjectInvoker invoker, Timer timer) throws Exception
      {

      }
   }
}
