/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.txtimer;

// $Id: TimedObjectInvoker.java,v 1.3 2004/04/09 22:47:01 tdiesler Exp $

import javax.ejb.Timer;

/**
 * Invokes the ejbTimeout method on the TimedObject with the given id.
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 07-Apr-2004
 */
public interface TimedObjectInvoker
{
   /**
    * Invokes the ejbTimeout method on the TimedObject with the given id.
    * @param id The combined TimedObjectId
    * @param timer the Timer that is passed to ejbTimeout
    */
   void invokeTimedObject(TimedObjectId id, Timer timer) throws Exception;
}
