/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.jms.jndi;

/**
 * MBean interface.
 */
public interface JMSProviderLoaderMBean extends org.jboss.system.ServiceMBean {

  void setProviderName(java.lang.String name) ;

  java.lang.String getProviderName() ;

  void setProviderAdapterClass(java.lang.String clazz) ;

  java.lang.String getProviderAdapterClass() ;

  void setProperties(java.util.Properties properties) ;

  java.util.Properties getProperties() ;

  void setAdapterJNDIName(java.lang.String name) ;

  java.lang.String getAdapterJNDIName() ;

  void setFactoryRef(java.lang.String newFactoryRef) ;

  void setQueueFactoryRef(java.lang.String newQueueFactoryRef) ;

  void setTopicFactoryRef(java.lang.String newTopicFactoryRef) ;

  java.lang.String getFactoryRef() ;

  java.lang.String getQueueFactoryRef() ;

  java.lang.String getTopicFactoryRef() ;

}
