/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.txtimer;

// $Id: EJBTimerServiceLocator.java,v 1.5 2004/09/10 14:05:46 tdiesler Exp $

import org.jboss.ejb.Container;
import org.jboss.logging.Logger;
import org.jboss.mx.util.MBeanServerLocator;
import org.jboss.mx.util.MBeanProxy;
import org.jboss.mx.util.MBeanProxyCreationException;

import javax.ejb.Timer;
import javax.ejb.TimerService;
import javax.management.MBeanServer;
import javax.management.ObjectName;

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
      private EJBTimerService ejbTimerService;

      public MBeanDelegate(MBeanServer server)
      {
         try
         {
            ejbTimerService = (EJBTimerService)MBeanProxy.get(EJBTimerService.class, EJBTimerService.OBJECT_NAME, server);
         }
         catch (MBeanProxyCreationException e)
         {
            throw new IllegalStateException("Cannot create EJBTimerService proxy");
         }
      }

      public TimerService createTimerService(ObjectName containerId, Object instancePk, Container container)
              throws IllegalStateException
      {
         try
         {
            TimerService timerService = (TimerService)ejbTimerService.createTimerService(containerId, instancePk, container);
            return timerService;
         }
         catch (Exception e)
         {
            log.error("Cannot createTimerService", e);
            return null;
         }
      }

      public TimerService createTimerService(ObjectName containerId, Object instancePk, TimedObjectInvoker invoker)
              throws IllegalStateException
      {
         try
         {
            TimerService timerService = (TimerService)ejbTimerService.createTimerService(containerId, instancePk, invoker);
            return timerService;
         }
         catch (Exception e)
         {
            log.error("Cannot createTimerService", e);
            return null;
         }
      }

      public TimerService getTimerService(ObjectName containerId, Object instancePk)
              throws IllegalStateException
      {
         try
         {
            TimerService timerService = (TimerService)ejbTimerService.getTimerService(containerId, instancePk);
            return timerService;
         }
         catch (Exception e)
         {
            log.error("Cannot getTimerService", e);
            return null;
         }
      }

      public void retryTimeout(ObjectName containerId, Object instancePk, Timer timer)
      {
         try
         {
            ejbTimerService.retryTimeout(containerId, instancePk, timer);
         }
         catch (Exception e)
         {
            log.error("Cannot callTimeout", e);
         }
      }

      public void removeTimerService(ObjectName containerId, Object instancePk)
              throws IllegalStateException
      {
         try
         {
            ejbTimerService.removeTimerService(containerId, instancePk);
         }
         catch (Exception e)
         {
            log.error("Cannot removeTimerService", e);
         }
      }
   }
}
