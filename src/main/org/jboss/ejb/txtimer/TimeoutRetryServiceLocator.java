/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.txtimer;

// $Id: TimeoutRetryServiceLocator.java,v 1.1 2004/04/13 10:10:40 tdiesler Exp $

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
public class TimeoutRetryServiceLocator
{
   // logging support
   private static Logger log = Logger.getLogger(TimeoutRetryServiceLocator.class);

   private static TimeoutRetryPolicy retryPolicy;

   /**
    * Locates the EJBTimerService, first as MBean, then as singleton
    */
   public static TimeoutRetryPolicy getTimeoutRetryPolicy()
   {
      try
      {
         // First try the MBean server
         MBeanServer server = MBeanServerLocator.locateJBoss();
         if (server != null && server.isRegistered(TimeoutRetryPolicy.OBJECT_NAME))
            retryPolicy = new MBeanDelegate(server);
      }
      catch (Exception ignore)
      {
      }

      // This path can be used for standalone test cases
      if (retryPolicy == null)
         retryPolicy = new FixedDelayRetryPolicy();

      return retryPolicy;
   }

   /**
    * Delegates method calls to the EJBTimerService to the MBean server
    */
   public static class MBeanDelegate implements TimeoutRetryPolicy
   {
      private MBeanServer server;

      public MBeanDelegate(MBeanServer server)
      {
         this.server = server;
      }

      /**
       * Invokes the ejbTimeout method on the TimedObject with the given id.
       * @param invoker the invoker for the TimedObject
       * @param timer the Timer that is passed to ejbTimeout
       */
      public void retryTimeout(TimedObjectInvoker invoker, Timer timer)
      {
         try
         {
            server.invoke(TimeoutRetryPolicy.OBJECT_NAME,
                    "retryTimeout",
                    new Object[]{invoker, timer},
                    new String[]{TimedObjectInvoker.class.getName(), Timer.class.getName()});
         }
         catch (Exception e)
         {
            log.error("Cannot retry timeout", e);
         }
      }
   }
}
