/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.txtimer;

/**
 * MBean interface.
 * @since 07-Apr-2004
 */
public interface FixedDelayRetryPolicyMBean extends org.jboss.system.Service, org.jboss.ejb.txtimer.RetryPolicy {

   //default object name
   public static final javax.management.ObjectName OBJECT_NAME = org.jboss.mx.util.ObjectNameFactory.create("jboss.ejb:service=EJBTimerService,retryPolicy=fixedDelay");

   /**
    * Get the delay for retry
    * @return delay in ms
    */
  long getDelay() ;

   /**
    * Set the delay for retry
    * @param delay in ms
    */
  void setDelay(long delay) ;

   /**
    * Invokes the ejbTimeout method on the TimedObject with the given id.
    * @param invoker The invoker for the TimedObject
    * @param timer the Timer that is passed to ejbTimeout
    */
  void retryTimeout(org.jboss.ejb.txtimer.TimedObjectInvoker invoker,javax.ejb.Timer timer) ;

}
