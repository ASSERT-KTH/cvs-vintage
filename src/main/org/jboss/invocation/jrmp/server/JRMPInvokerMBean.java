/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.invocation.jrmp.server;

/**
 * MBean interface.
 */
public interface JRMPInvokerMBean extends org.jboss.system.ServiceMBean {

  int getBacklog() ;

  void setBacklog(int back) ;

  boolean getEnableClassCaching() ;

  void setEnableClassCaching(boolean flag) ;

  void setRMIObjectPort(int rmiPort) ;

  int getRMIObjectPort() ;

  void setRMIClientSocketFactory(java.lang.String name) ;

  java.lang.String getRMIClientSocketFactory() ;

  void setRMIClientSocketFactoryBean(java.rmi.server.RMIClientSocketFactory bean) ;

  java.rmi.server.RMIClientSocketFactory getRMIClientSocketFactoryBean() ;

  void setRMIServerSocketFactory(java.lang.String name) ;

  java.lang.String getRMIServerSocketFactory() ;

  void setRMIServerSocketFactoryBean(java.rmi.server.RMIServerSocketFactory bean) ;

  java.rmi.server.RMIServerSocketFactory getRMIServerSocketFactoryBean() ;

  void setServerAddress(java.lang.String address) ;

  java.lang.String getServerAddress() ;

  void setSecurityDomain(java.lang.String domainName) ;

  java.lang.String getSecurityDomain() ;

}
