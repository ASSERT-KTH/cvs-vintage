/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.txtimer;

// $Id: PersistencePolicy.java,v 1.3 2004/09/10 14:37:16 tdiesler Exp $

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Timers are persistent objects. In the event of a container crash, any single-event timers that have expired
 * during the intervening time before container restart must cause the ejbTimeout method to be invoked
 * upon restart. Any interval timers that have expired during the intervening time must cause the ejb-
 * Timeout method to be invoked at least once upon restart.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 09-Sep-2004
 */
public interface PersistencePolicy
{
   /**
    * Inserts a timer into persistent storage.
    *
    * @param timedObjectId The timed object id
    * @param firstEvent    The point in time at which the first txtimer expiration must occur.
    * @param periode       The number of milliseconds that must elapse between txtimer expiration notifications.
    * @param info          A serializable handback object.
    */
   void insertTimer(TimedObjectId timedObjectId, Date firstEvent, long periode, Serializable info);

   /**
    * Deletes a timer from persistent storage.
    *
    * @param timedObjectId The timed object id
    * @param firstEvent    The point in time at which the first txtimer expiration must occur.
    */
   void deleteTimer(TimedObjectId timedObjectId, Date firstEvent);

   /**
    * Clear the persisted timers
    */
   void deleteAllTimers();

   /**
    * Restore the persistet timers
    */
   void restoreTimers();

   /**
    * Return a List of TimerHandle objects.
    */
   List listTimerHandles();
}
