/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.txtimer;

// $Id: EJBTimerService.java,v 1.8 2004/09/10 14:05:46 tdiesler Exp $

import org.jboss.ejb.Container;
import org.jboss.mx.util.ObjectNameFactory;

import javax.ejb.Timer;
import javax.ejb.TimerService;
import javax.management.ObjectName;

/**
 * A service that implements this interface provides an Tx aware EJBTimerService.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 07-Apr-2004
 */
public interface EJBTimerService
{
   /**
    * default object name
    */
   public static final javax.management.ObjectName OBJECT_NAME = ObjectNameFactory.create("jboss.ejb:service=EJBTimerService");

   /**
    * Create a TimerService for a given TimedObjectId that lives in a JBoss Container.
    * The TimedObjectInvoker is constructed from the invokerClassName.
    *
    * @param containerId The string identifier for a class of TimedObjects
    * @param pKey        The rimary key for an instance of a TimedObject, may be null
    * @param container   The Container that is associated with the TimerService
    * @return the TimerService
    */
   TimerService createTimerService(ObjectName containerId, Object pKey, Container container) throws IllegalStateException;

   /**
    * Create a TimerService for a given TimedObjectId that is invoked through the given invoker
    *
    * @param containerId The string identifier for a class of TimedObjects
    * @param pKey        The rimary key for an instance of a TimedObject, may be null
    * @param invoker     The TimedObjectInvoker
    * @return the TimerService
    */
   TimerService createTimerService(ObjectName containerId, Object pKey, TimedObjectInvoker invoker) throws IllegalStateException;

   /**
    * Get the TimerService for a given TimedObjectId
    *
    * @param containerId The string identifier for a class of TimedObjects
    * @param pKey        The rimary key for an instance of a TimedObject, may be null
    * @return The TimerService, or null if it does not exist
    */
   TimerService getTimerService(ObjectName containerId, Object pKey) throws IllegalStateException;

   /**
    * Invokes the ejbTimeout method a given TimedObjectId
    *
    * @param containerId The string identifier for a class of TimedObjects
    * @param pKey        The rimary key for an instance of a TimedObject, may be null
    * @param timer       the Timer that is passed to ejbTimeout
    */
   void retryTimeout(ObjectName containerId, Object pKey, Timer timer);

   /**
    * Remove the TimerService for a given TimedObjectId
    *
    * @param containerId The string identifier for a class of TimedObjects
    * @param pKey        The rimary key for an instance of a TimedObject, may be null
    */
   void removeTimerService(ObjectName containerId, Object pKey) throws IllegalStateException;

}
