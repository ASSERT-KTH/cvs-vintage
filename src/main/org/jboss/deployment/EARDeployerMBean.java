/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.deployment;

/**
 * MBean interface.
 */
public interface EARDeployerMBean extends org.jboss.deployment.SubDeployerMBean {

   //default object name
   public static final javax.management.ObjectName OBJECT_NAME = org.jboss.mx.util.ObjectNameFactory.create("jboss.j2ee:service=EARDeployer");

  boolean isIsolated() ;

  void setIsolated(boolean isolated) ;

  boolean isCallByValue() ;

  void setCallByValue(boolean callByValue) ;

}
