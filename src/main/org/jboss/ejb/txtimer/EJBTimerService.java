/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.txtimer;

// $Id: EJBTimerService.java,v 1.3 2004/04/09 22:47:01 tdiesler Exp $

import javax.ejb.TimerService;

/**
 * The EJBTimerService interface manages the TimerService for
 * an associated TimedObject.
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 07-Apr-2004
 */
public interface EJBTimerService extends TimedObjectInvoker
{
   /**
    * Create a TimerService for a given TimedObjectId
    * @param id The combined TimedObjectId
    * @param timedObjectInvoker a TimedObjectInvoker
    * @return the TimerService
    */
   TimerService createTimerService(TimedObjectId id, TimedObjectInvoker timedObjectInvoker) throws IllegalStateException;

   /**
    * Get the TimerService for a given TimedObjectId
    * @param id The combined TimedObjectId
    * @return The TimerService, or null if it does not exist
    */
   TimerService getTimerService(TimedObjectId id) throws IllegalStateException;

   /**
    * Remove the TimerService for a given TimedObjectId
    * @param id The combined TimedObjectId
    */
   void removeTimerService(TimedObjectId id) throws IllegalStateException;

}
