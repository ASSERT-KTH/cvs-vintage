/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.timer;

import java.io.Serializable;

import javax.ejb.EJBException;
import javax.ejb.NoSuchObjectLocalException;
import javax.ejb.Timer;
import javax.ejb.TimerHandle;

/**
 * The TimerHandle interface is implemented by all EJB timer handles.
 **/
public class ContainerTimerHandle
    implements TimerHandle
{
   
   /**
    * Obtain a reference to the timer represented by this handle.
    *
    * @return Timer which this handle represents
    *
    * @throws IllegalStateException If this method is invoked while the instance is in
    *                               a state that does not allow access to this method.
    * @throws NoSuchObjectLocalException If invoked on a timer that has expired or has been cancelled.
    * @throws EJBException If this method could not complete due to a system-level failure.
    **/
   public Timer getTimer()
      throws
         IllegalStateException,
         NoSuchObjectLocalException,
         EJBException
   {
      //AS TODO: Create an implementation
      return null;
   }
}
