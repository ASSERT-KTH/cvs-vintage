/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.jmx.connector.invoker;

/**
 * MBean interface.
 */
public interface InvokerAdaptorServiceMBean extends org.jboss.system.ServiceMBean {

   //default object name
   public static final javax.management.ObjectName OBJECT_NAME = org.jboss.mx.util.ObjectNameFactory.create("jboss.jmx:type=adaptor,protocol=INVOKER");

  java.lang.Class[] getExportedInterfaces() ;

  void setExportedInterfaces(java.lang.Class[] exportedInterfaces) ;

   /**
    * Expose the service interface mapping as a read-only attribute
    * @return A Map<Long hash, Method> of the MBeanServer    */
  java.util.Map getMethodMap() ;

   /**
    * Expose the MBeanServer service via JMX to invokers.
    * @param invocation A pointer to the invocation object
    * @return Return value of method invocation.
    * @throws Exception Failed to invoke method.    */
  java.lang.Object invoke(org.jboss.invocation.Invocation invocation) throws java.lang.Exception;

}
