/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.invocation.pooled.server;

/**
 * MBean interface.
 */
public interface PooledInvokerMBean extends org.jboss.system.ServiceMBean {

   /**
    * Getter for property numAcceptThreads
    * @return Value of property numAcceptThreads
    */
  int getNumAcceptThreads() ;

   /**
    * Setter for property numAcceptThreads
    * @param size New value of property numAcceptThreads.
    */
  void setNumAcceptThreads(int size) ;

   /**
    * Getter for property maxPoolSize;
    * @return Value of property maxPoolSize.
    */
  int getMaxPoolSize() ;

   /**
    * Setter for property maxPoolSize.
    * @param maxPoolSize New value of property serverBindPort.
    */
  void setMaxPoolSize(int maxPoolSize) ;

   /**
    * Getter for property maxPoolSize;
    * @return Value of property maxPoolSize.
    */
  int getClientMaxPoolSize() ;

   /**
    * Setter for property maxPoolSize.
    * @param clientMaxPoolSize New value of property serverBindPort.
    */
  void setClientMaxPoolSize(int clientMaxPoolSize) ;

   /**
    * Getter for property timeout
    * @return Value of property timeout
    */
  int getSocketTimeout() ;

   /**
    * Setter for property timeout
    * @param time New value of property timeout
    */
  void setSocketTimeout(int time) ;

  int getCurrentClientPoolSize() ;

  int getCurrentThreadPoolSize() ;

   /**
    * Getter for property serverBindPort.
    * @return Value of property serverBindPort.
    */
  int getServerBindPort() ;

   /**
    * Setter for property serverBindPort.
    * @param serverBindPort New value of property serverBindPort.
    */
  void setServerBindPort(int serverBindPort) ;

  java.lang.String getClientConnectAddress() ;

  void setClientConnectAddress(java.lang.String clientConnectAddress) ;

  int getClientConnectPort() ;

  void setClientConnectPort(int clientConnectPort) ;

  int getBacklog() ;

  void setBacklog(int backlog) ;

  boolean isEnableTcpNoDelay() ;

  void setEnableTcpNoDelay(boolean enableTcpNoDelay) ;

  java.lang.String getServerBindAddress() ;

  void setServerBindAddress(java.lang.String serverBindAddress) ;

   /**
    * mbean get-set pair for field transactionManagerService Get the value of transactionManagerService
    * @return value of transactionManagerService
    */
  javax.management.ObjectName getTransactionManagerService() ;

   /**
    * Set the value of transactionManagerService
    * @param transactionManagerService Value to assign to transactionManagerService
    */
  void setTransactionManagerService(javax.management.ObjectName transactionManagerService) ;

  org.jboss.invocation.pooled.interfaces.PooledInvokerProxy getOptimizedInvokerProxy() ;

}
