/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.txtimer;

// $Id: NoopPersistencePolicy.java,v 1.1 2004/09/09 22:04:29 tdiesler Exp $

import org.jboss.logging.Logger;

import javax.ejb.Timer;
import java.util.Date;

/**
 * This service implements a PersistencePolicy that does not persist the timer.
 *
 * @author Thomas.Diesler@jboss.org
 * @jmx.mbean name="jboss.ejb:service=EJBTimerService,plugin=PersistencePolicy"
 * extends="org.jboss.ejb.txtimer.PersistencePolicy"
 * @since 09-Sep-2004
 */
public class NoopPersistencePolicy implements NoopPersistencePolicyMBean
{
   // logging support
   private static Logger log = Logger.getLogger(NoopPersistencePolicy.class);


   /** Creates the timer in  persistent storage.
    *
    * @param timer             The Timer that is passed to ejbTimeout
    * @param initialExpiration The point in time at which the first txtimer expiration must occur.
    * @param intervalDuration  The number of milliseconds that must elapse between txtimer expiration notifications.
    */
   public void createTimer(TimerImpl timer, Date initialExpiration, long intervalDuration)
   {
      log.debug("Noop on createTimer");
   }

   /** Removes the timer from persistent storage.
    *
    * @param timer The Timer that is passed to ejbTimeout
    */
   public void destroyTimer(TimerImpl timer)
   {
      log.debug("Noop on destroyTimer");
   }

   /** Restore the persistet timers
    */
   public void restoreTimers()
   {
      log.debug("Noop on restoreTimers");
   }
}
