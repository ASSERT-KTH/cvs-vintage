/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.logging;

/**
 * MBean interface.
 */
public interface Log4jSocketServerMBean extends org.jboss.system.ServiceMBean {

  void setPort(int port) ;

  int getPort() ;

  void setBacklog(int backlog) ;

  int getBacklog() ;

  void setBindAddress(java.net.InetAddress addr) ;

  java.net.InetAddress getBindAddress() ;

  void setListenerEnabled(boolean enabled) ;

  boolean setListenerEnabled() ;

  void setLoggerRepositoryFactoryType(java.lang.Class type) throws java.lang.InstantiationException, java.lang.IllegalAccessException, java.lang.ClassCastException;

  java.lang.Class getLoggerRepositoryFactoryType() ;

  org.apache.log4j.spi.LoggerRepository getLoggerRepository(java.net.InetAddress addr) ;

}
