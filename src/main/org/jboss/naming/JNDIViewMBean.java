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
public interface JNDIViewMBean extends org.jboss.system.ServiceMBean {

   //default object name
   public static final javax.management.ObjectName OBJECT_NAME = org.jboss.mx.util.ObjectNameFactory.create("jboss:type=JNDIView");

   /**
    * List deployed application java:comp namespaces, the java: namespace as well as the global InitialContext JNDI namespace.
    * @param verbose, if true, list the class of each object in addition to its name    */
  java.lang.String list(boolean verbose) ;

   /**
    * List deployed application java:comp namespaces, the java: namespace as well as the global InitialContext JNDI namespace in a XML Format.
    * @param verbose, if true, list the class of each object in addition to its name    */
  java.lang.String listXML() ;

}
