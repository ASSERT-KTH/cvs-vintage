/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.txtimer;

// $Id: EJBTimerService.java,v 1.2 2004/04/08 21:54:27 tdiesler Exp $

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
    * @param timedObjectId the id of the TimedObject
    * @param timedObjectInvoker a TimedObjectInvoker
    * @return the TimerService
    */
   TimerService createTimerService(String timedObjectId, TimedObjectInvoker timedObjectInvoker) throws IllegalStateException;

   /**
    * Get the TimerService for a given TimedObjectId
    * @param timedObjectId The id of the TimedObject
    * @return The TimerService, or null if it does not exist
    */
   TimerService getTimerService(String timedObjectId) throws IllegalStateException;

   /**
    * Remove the TimerService for a given TimedObjectId
    * @param timedObjectId the id of the TimedObject
    */
   void removeTimerService(String timedObjectId) throws IllegalStateException;

}
