/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.jms.asf;

/**
 * MBean interface.
 */
public interface ServerSessionPoolLoaderMBean extends org.jboss.system.ServiceMBean {

   /**
    * Set the pool name.
    * @param name The pool name.
    */
  void setPoolName(java.lang.String name) ;

   /**
    * Get the pool name.
    * @return The pool name.
    */
  java.lang.String getPoolName() ;

   /**
    * Set the classname of pool factory to use.
    * @param classname The name of the pool factory class.
    */
  void setPoolFactoryClass(java.lang.String classname) ;

   /**
    * Get the classname of pool factory to use.
    * @return The name of the pool factory class.
    */
  java.lang.String getPoolFactoryClass() ;

   /**
    * mbean get-set pair for field xidFactory Get the value of xidFactory
    * @return value of xidFactory
    */
  javax.management.ObjectName getXidFactory() ;

   /**
    * Set the value of xidFactory
    * @param xidFactory Value to assign to xidFactory
    */
  void setXidFactory(javax.management.ObjectName xidFactory) ;

}
