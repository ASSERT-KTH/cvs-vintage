/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.txtimer;

// $Id: EJBTimerServiceTxLocator.java,v 1.3 2004/04/09 22:47:01 tdiesler Exp $

import org.jboss.logging.Logger;
import org.jboss.mx.util.MBeanServerLocator;

import javax.ejb.Timer;
import javax.ejb.TimerService;
import javax.management.MBeanServer;

/**
 * The EJBTimerServiceTxLocator locates EJBTimerServiceTx.
 * <p/>
 * It first checks if the EJBTimerServiceTx is registerd with the MBeanServer,
 * if not it creates a singleton and uses that.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 07-Apr-2004
 */
public class EJBTimerServiceTxLocator
{
   // logging support
   private static Logger log = Logger.getLogger(EJBTimerServiceTxLocator.class);

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
         if (server != null && server.isRegistered(EJBTimerServiceTxMBean.OBJECT_NAME))
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
       * @param id The combined TimedObjectId
       * @param timedObjectInvoker a TimedObjectInvoker
       * @return the TimerService
       */
      public TimerService createTimerService(TimedObjectId id, TimedObjectInvoker timedObjectInvoker)
              throws IllegalStateException
      {
         try
         {
            TimerService timerService = (TimerService) server.invoke(EJBTimerServiceTxMBean.OBJECT_NAME,
                    "createTimerService",
                    new Object[]{id, timedObjectInvoker},
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
       * @param id The combined TimedObjectId
       * @return The TimerService, or null if it does not exist
       */
      public TimerService getTimerService(TimedObjectId id)
              throws IllegalStateException
      {
         try
         {
            TimerService timerService = (TimerService) server.invoke(EJBTimerServiceTxMBean.OBJECT_NAME,
                    "getTimerService",
                    new Object[]{id},
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
       * Remove the TimerService for a given TimedObjectId
       *
       * @param id The combined TimedObjectId
       */
      public void removeTimerService(TimedObjectId id)
              throws IllegalStateException
      {
         try
         {
            server.invoke(EJBTimerServiceTxMBean.OBJECT_NAME,
                    "removeTimerService",
                    new Object[]{id},
                    new String[]{TimedObjectId.class.getName()});
         }
         catch (Exception e)
         {
            log.error("Cannot removeTimerService", e);
         }
      }

      /**
       * Invokes the ejbTimeout method on the TimedObject with the given id.
       *
       * @param id The combined TimedObjectId
       * @param timer         the Timer that is passed to ejbTimeout
       */
      public void invokeTimedObject(TimedObjectId id, Timer timer)
              throws Exception
      {
         server.invoke(EJBTimerServiceTxMBean.OBJECT_NAME,
                 "invokeTimedObject",
                 new Object[]{id, timer},
                 new String[]{TimedObjectId.class.getName(), Timer.class.getName()});
      }
   }

}
