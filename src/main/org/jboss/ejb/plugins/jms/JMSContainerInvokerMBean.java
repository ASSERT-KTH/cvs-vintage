/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.jms;

/**
 * MBean interface.
 */
public interface JMSContainerInvokerMBean extends org.jboss.system.ServiceMBean {

  int getMinPoolSize() ;

  void setMinPoolSize(int minPoolSize) ;

  int getMaxPoolSize() ;

  void setMaxPoolSize(int maxPoolSize) ;

  long getKeepAliveMillis() ;

  void setKeepAliveMillis(long keepAlive) ;

  int getMaxMessages() ;

  void setMaxMessages(int maxMessages) ;

  org.jboss.metadata.MessageDrivenMetaData getMetaData() ;

  boolean getDeliveryActive() ;

  boolean getCreateJBossMQDestination() ;

  void startDelivery() throws java.lang.Exception;

  void stopDelivery() throws java.lang.Exception;

}
