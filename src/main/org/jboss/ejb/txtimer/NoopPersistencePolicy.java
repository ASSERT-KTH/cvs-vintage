/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.txtimer;

// $Id: NoopPersistencePolicy.java,v 1.3 2004/09/10 14:37:16 tdiesler Exp $

import org.jboss.logging.Logger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This service implements a PersistencePolicy that does not persist the timer.
 *
 * @author Thomas.Diesler@jboss.org
 * @jmx.mbean name="jboss.ejb:service=EJBTimerService,persistencePolicy=noop"
 * extends="org.jboss.ejb.txtimer.PersistencePolicy"
 * @since 09-Sep-2004
 */
public class NoopPersistencePolicy implements NoopPersistencePolicyMBean
{
   // logging support
   private static Logger log = Logger.getLogger(NoopPersistencePolicy.class);


   /**
    * Creates the timer in  persistent storage.
    *
    * @param timedObjectId The timed object id
    * @param firstEvent    The point in time at which the first txtimer expiration must occur.
    * @param firstEvent    The point in time at which the first txtimer expiration must occur.
    * @param periode       The number of milliseconds that must elapse between txtimer expiration notifications.
    */
   public void insertTimer(TimedObjectId timedObjectId, Date firstEvent, long periode, Serializable info)
   {
      log.debug("Noop on createTimer");
   }

   /**
    * Removes the timer from persistent storage.
    *
    * @param timedObjectId The timed object id
    * @param firstEvent    The point in time at which the first txtimer expiration must occur.
    */
   public void deleteTimer(TimedObjectId timedObjectId, Date firstEvent)
   {
      log.debug("Noop on destroyTimer");
   }

   /**
    * Restore the persistet timers
    */
   public void restoreTimers()
   {
      log.debug("Noop on restoreTimers");
   }

   /**
    * Return a List of TimerHandle objects.
    */
   public List listTimerHandles()
   {
      log.debug("Noop on getTimerHandles");
      return new ArrayList();
   }

   /**
    * Delete all persisted timers
    */
   public void deleteAllTimers()
   {
      log.debug("Noop on deleteAllTimers");
   }
}
