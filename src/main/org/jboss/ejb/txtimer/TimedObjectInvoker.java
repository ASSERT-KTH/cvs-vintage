/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.txtimer;

// $Id: TimedObjectInvoker.java,v 1.6 2004/04/14 13:18:40 tdiesler Exp $

import javax.ejb.Timer;

/**
 * An implementation can invoke the ejbTimeout method on a TimedObject.
 * <p/>
 * The TimedObjectInvoker has knowledge of the TimedObjectId, it
 * knows which object to invoke.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 07-Apr-2004
 */
public interface TimedObjectInvoker
{
   /**
    * Invokes the ejbTimeout method on the TimedObject with the given id.
    *
    * @param timer the Timer that is passed to ejbTimeout
    */
   void callTimeout(Timer timer) throws Exception;

}
