/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.txtimer;

// $Id: RetryPolicy.java,v 1.1 2004/09/09 22:04:29 tdiesler Exp $

import org.jboss.mx.util.ObjectNameFactory;

import javax.ejb.Timer;

/**
 * An implementation can retry the invocation of the ejbTimeout method on a TimedObject.
 * <p/>
 * The RetryPolicy is stateless and has no knowledge of the TimedObjectId.
 * It does the invocation through the given TimedObjectInvoker.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 07-Apr-2004
 */
public interface RetryPolicy
{
   /** Invokes the ejbTimeout method on the TimedObject with the given id.
    *
    * @param invoker the invoker for the TimedObject
    * @param timer   the Timer that is passed to ejbTimeout
    */
   void retryTimeout(TimedObjectInvoker invoker, Timer timer);
}
