/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.naming;

/**
 * MBean interface.
 */
public interface NamingServiceMBean extends org.jboss.system.ServiceMBean, org.jnp.server.MainMBean {

   //default object name
   public static final javax.management.ObjectName OBJECT_NAME = org.jboss.mx.util.ObjectNameFactory.create("jboss:service=Naming");

   /**
    * Set the thread pool used for the bootstrap lookups
    * @param poolMBean    */
  void setLookupPool(org.jboss.util.threadpool.BasicThreadPoolMBean poolMBean) ;

   /**
    * Get the call by value flag for jndi lookups.
    * @return true if all lookups are unmarshalled using the caller's TCL, false if in VM lookups return the value by reference.    */
  boolean getCallByValue() ;

   /**
    * Set the call by value flag for jndi lookups.
    * @param flag - true if all lookups are unmarshalled using the caller's TCL, false if in VM lookups return the value by reference.    */
  void setCallByValue(boolean flag) ;

   /**
    * Expose the Naming service interface mapping as a read-only attribute
    * @return A Map<Long hash, Method> of the Naming interface    */
  java.util.Map getMethodMap() ;

   /**
    * Expose the Naming service via JMX to invokers.
    * @param invocation A pointer to the invocation object
    * @return Return value of method invocation.
    * @throws Exception Failed to invoke method.    */
  java.lang.Object invoke(org.jboss.invocation.Invocation invocation) throws java.lang.Exception;

}
