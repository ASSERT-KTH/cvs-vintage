/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.txtimer;

// $Id: PersistencePolicy.java,v 1.1 2004/09/09 22:04:29 tdiesler Exp $

import org.jboss.mx.util.ObjectNameFactory;

import javax.ejb.Timer;
import javax.ejb.TimerHandle;
import java.util.Date;

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
   /** Inserts a timer into persistent storage.
    *
    * @param timer             The Timer that is passed to ejbTimeout
    * @param initialExpiration The point in time at which the first txtimer expiration must occur.
    * @param intervalDuration  The number of milliseconds that must elapse between txtimer expiration notifications.
    */
   void createTimer(TimerImpl timer, Date initialExpiration, long intervalDuration);

   /** Deletes a timer from persistent storage.
    *
    * @param timer The Timer that is passed to ejbTimeout
    */
   void destroyTimer(TimerImpl timer);

   /** Restore the persistet timers
    */
   void restoreTimers();
}
