/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.txtimer;

// $Id: TimeoutRetryPolicy.java,v 1.1 2004/04/13 10:10:40 tdiesler Exp $

import javax.ejb.Timer;

/**
 * Invokes the ejbTimeout method on the TimedObject with the given id.
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 07-Apr-2004
 */
public interface TimeoutRetryPolicy
{
   /** default object name: jboss:service=EJBTimerServiceRetryPolicy */
   public static final javax.management.ObjectName OBJECT_NAME = org.jboss.mx.util.ObjectNameFactory.create("jboss:service=EJBTimerServiceRetryPolicy");

   /**
    * Invokes the ejbTimeout method on the TimedObject with the given id.
    * @param invoker the invoker for the TimedObject
    * @param timer the Timer that is passed to ejbTimeout
    */
   void retryTimeout(TimedObjectInvoker invoker, Timer timer);
}
