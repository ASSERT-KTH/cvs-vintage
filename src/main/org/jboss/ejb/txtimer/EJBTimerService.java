/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.txtimer;

// $Id: EJBTimerService.java,v 1.5 2004/04/13 15:37:57 tdiesler Exp $

import javax.ejb.TimerService;
import javax.ejb.Timer;

/**
 * A service that implements this interface provides an Tx aware EJBTimerService.
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 07-Apr-2004
 */
public interface EJBTimerService
{
   /** default object name: jboss:service=EJBTimerServiceTx */
   public static final javax.management.ObjectName OBJECT_NAME = org.jboss.mx.util.ObjectNameFactory.create("jboss:service=EJBTimerServiceTx");

   /**
    * Create a TimerService for a given TimedObjectId
    * @param timedObjectId combined TimedObjectId
    * @param timedObjectInvoker a TimedObjectInvoker
    * @return the TimerService
    */
   TimerService createTimerService(TimedObjectId timedObjectId, TimedObjectInvoker timedObjectInvoker) throws IllegalStateException;

   /**
    * Get the TimerService for a given TimedObjectId
    * @param timedObjectId combined TimedObjectId
    * @return The TimerService, or null if it does not exist
    */
   TimerService getTimerService(TimedObjectId timedObjectId) throws IllegalStateException;

   /**
    * Invokes the ejbTimeout method on a given TimedObjectId
    * @param timedObjectId combined TimedObjectId
    * @param timer the Timer that is passed to ejbTimeout
    */
   void callTimeout(TimedObjectId timedObjectId, Timer timer) throws Exception;

   /**
    * Invokes the ejbTimeout method a given TimedObjectId
    * @param timedObjectId combined TimedObjectId
    * @param timer the Timer that is passed to ejbTimeout
    */
   void retryTimeout(TimedObjectId timedObjectId, Timer timer);

   /**
    * Remove the TimerService for a given TimedObjectId
    * @param timedObjectId combined TimedObjectId
    */
   void removeTimerService(TimedObjectId timedObjectId) throws IllegalStateException;

}
