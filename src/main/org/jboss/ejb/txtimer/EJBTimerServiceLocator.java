/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.txtimer;

// $Id: EJBTimerServiceLocator.java,v 1.4 2004/04/14 13:44:46 tdiesler Exp $

import org.jboss.ejb.Container;
import org.jboss.logging.Logger;
import org.jboss.mx.util.MBeanServerLocator;

import javax.ejb.Timer;
import javax.ejb.TimerService;
import javax.management.MBeanServer;

/**
 * Locates the EJBTimerService, either as MBean or as local instance.
 * <p/>
 * It first checks if the EJBTimerServiceImpl is registerd with the MBeanServer,
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
         ejbTimerService = new EJBTimerServiceImpl();

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

      public TimerService createTimerService(String containerId, Object instancePk, Container container)
              throws IllegalStateException
      {
         try
         {
            TimerService timerService = (TimerService) server.invoke(EJBTimerService.OBJECT_NAME,
                    "createTimerService",
                    new Object[]{containerId, instancePk, container},
                    new String[]{String.class.getName(), Object.class.getName(), Container.class.getName()});
            return timerService;
         }
         catch (Exception e)
         {
            log.error("Cannot createTimerService", e);
            return null;
         }
      }

      public TimerService createTimerService(String containerId, Object instancePk, TimedObjectInvoker invoker)
              throws IllegalStateException
      {
         try
         {
            TimerService timerService = (TimerService) server.invoke(EJBTimerService.OBJECT_NAME,
                    "createTimerService",
                    new Object[]{containerId, instancePk, invoker},
                    new String[]{String.class.getName(), Object.class.getName(), TimedObjectInvoker.class.getName()});
            return timerService;
         }
         catch (Exception e)
         {
            log.error("Cannot createTimerService", e);
            return null;
         }
      }

      public TimerService getTimerService(String containerId, Object instancePk)
              throws IllegalStateException
      {
         try
         {
            TimerService timerService = (TimerService) server.invoke(EJBTimerService.OBJECT_NAME,
                    "getTimerService",
                    new Object[]{containerId, instancePk},
                    new String[]{String.class.getName(), Object.class.getName()});
            return timerService;
         }
         catch (Exception e)
         {
            log.error("Cannot getTimerService", e);
            return null;
         }
      }

      public void retryTimeout(String containerId, Object instancePk, Timer timer)
      {
         try
         {
            server.invoke(EJBTimerService.OBJECT_NAME,
                    "retryTimeout",
                    new Object[]{containerId, instancePk, timer},
                    new String[]{String.class.getName(), Object.class.getName(), Timer.class.getName()});
         }
         catch (Exception e)
         {
            log.error("Cannot callTimeout", e);
         }
      }

      public void removeTimerService(String containerId, Object instancePk)
              throws IllegalStateException
      {
         try
         {
            server.invoke(EJBTimerService.OBJECT_NAME,
                    "removeTimerService",
                    new Object[]{containerId, instancePk},
                    new String[]{String.class.getName(), Object.class.getName()});
         }
         catch (Exception e)
         {
            log.error("Cannot removeTimerService", e);
         }
      }
   }
}
