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
public interface EJBTimerServiceImplMBean extends org.jboss.ejb.txtimer.EJBTimerService {

   //default object name
   public static final javax.management.ObjectName OBJECT_NAME = org.jboss.mx.util.ObjectNameFactory.create("jboss.ejb:service=EJBTimerService");

   /**
    * Get the object name of the retry policy.
    */
  javax.management.ObjectName getRetryPolicy() ;

   /**
    * Set the object name of the retry policy.
    */
  void setRetryPolicy(javax.management.ObjectName retryPolicyName) ;

   /**
    * Get the object name of the persistence policy.
    */
  javax.management.ObjectName getPersistencePolicy() ;

   /**
    * Set the object name of the persistence policy.
    */
  void setPersistencePolicy(javax.management.ObjectName persistencePolicyName) ;

   /**
    * Get the TimerIdGenerator class name
    */
  java.lang.String getTimerIdGeneratorClassName() ;

   /**
    * Get the TimerIdGenerator class name
    */
  void setTimerIdGeneratorClassName(java.lang.String timerIdGeneratorClassName) ;

   /**
    * Get the TimedObjectInvoker class name
    */
  java.lang.String getTimedObjectInvokerClassName() ;

   /**
    * Set the TimedObjectInvoker class name
    */
  void setTimedObjectInvokerClassName(java.lang.String timedObjectInvokerClassName) ;

   /**
    * List the timers registered with all TimerService objects
    */
  java.lang.String listTimers() ;

}
