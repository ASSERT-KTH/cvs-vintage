/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.txtimer;

// $Id: TimeoutRetryPolicy.java,v 1.3 2004/04/14 13:18:40 tdiesler Exp $

import javax.ejb.Timer;

/**
 * An implementation can retry the invocation of the ejbTimeout method on a TimedObject.
 * <p/>
 * The TimeoutRetryPolicy is stateless and has no knowledge of the TimedObjectId.
 * It does the invokation through the given TimedObjectInvoker.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 07-Apr-2004
 */
public interface TimeoutRetryPolicy
{
   /**
    * default object name: jboss:service=EJBTimerServiceRetryPolicy
    */
   public static final javax.management.ObjectName OBJECT_NAME = org.jboss.mx.util.ObjectNameFactory.create("jboss:service=EJBTimerServiceRetryPolicy");

   /**
    * Invokes the ejbTimeout method on the TimedObject with the given id.
    *
    * @param invoker the invoker for the TimedObject
    * @param timer   the Timer that is passed to ejbTimeout
    */
   void retryTimeout(TimedObjectInvoker invoker, Timer timer);
}
