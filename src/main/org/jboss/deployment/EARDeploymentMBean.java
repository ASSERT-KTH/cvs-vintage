/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.deployment;

/**
 * MBean interface.
 * @see EARDeployer
 */
public interface EARDeploymentMBean extends org.jboss.system.ServiceMBean {

  java.lang.Object resolveMetaData(java.lang.Object key) ;

  void addMetaData(java.lang.Object key,java.lang.Object value) ;

  java.util.Map getMetaData() ;

}
