/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.txtimer;

// $Id: FixedDelayRetryPolicy.java,v 1.5 2004/09/10 14:05:46 tdiesler Exp $

import org.jboss.logging.Logger;
import org.jboss.system.ServiceMBeanSupport;

import javax.ejb.Timer;

/**
 * This service implements a RetryPolicy that retries
 * the call to ejbTimeout after a fixed delay.
 *
 * @author Thomas.Diesler@jboss.org
 * @jmx.mbean name="jboss.ejb:service=EJBTimerService,retryPolicy=fixedDelay"
 * extends="org.jboss.system.Service, org.jboss.ejb.txtimer.RetryPolicy"
 * @since 07-Apr-2004
 */
public class FixedDelayRetryPolicy extends ServiceMBeanSupport implements FixedDelayRetryPolicyMBean
{
   // logging support
   private static Logger log = Logger.getLogger(FixedDelayRetryPolicy.class);

   // the delay before retry
   private long delay = 100;

   /**
    * Get the delay for retry
    *
    * @return delay in ms
    * @jmx.managed-attribute
    */
   public long getDelay()
   {
      return this.delay;
   }

   /**
    * Set the delay for retry
    *
    * @param delay in ms
    * @jmx.managed-attribute
    */
   public void setDelay(long delay)
   {
      this.delay = delay;
   }

   /**
    * Invokes the ejbTimeout method on the TimedObject with the given id.
    *
    * @param invoker The invoker for the TimedObject
    * @param timer   the Timer that is passed to ejbTimeout
    * @jmx.managed-operation
    */
   public void retryTimeout(TimedObjectInvoker invoker, Timer timer)
   {
      // check if the delay is appropriate
      if (timer instanceof TimerImpl)
      {
         TimerImpl txTimer = (TimerImpl) timer;

         long periode = txTimer.getPeriode();
         if (0 < periode && periode / 2 < delay)
            log.warn("A delay of " + delay + " ms might not be appropriate for a timer periode of " + periode + " ms");
      }

      new RetryThread(invoker, timer).start();
   }

   /**
    * The thread that does the actual invocation,
    * after a short delay.
    */
   private class RetryThread extends Thread
   {
      private TimedObjectInvoker invoker;
      private Timer timer;

      public RetryThread(TimedObjectInvoker invoker, Timer timer)
      {
         this.invoker = invoker;
         this.timer = timer;
      }

      public void run()
      {
         try
         {
            Thread.sleep(delay);
            log.debug("Retry ejbTimeout: " + timer);
            invoker.callTimeout(timer);
         }
         catch (Exception ignore)
         {
            ignore.printStackTrace();
         }
      }
   }
}
