/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.web;

/**
 * MBean interface.
 */
public interface WebServiceMBean extends org.jboss.system.ServiceMBean {

   //default object name
   public static final javax.management.ObjectName OBJECT_NAME = org.jboss.mx.util.ObjectNameFactory.create("jboss:service=WebService");

  java.net.URL addClassLoader(java.lang.ClassLoader cl) ;

  void removeClassLoader(java.lang.ClassLoader cl) ;

   /**
    * Set the WebService listening port.
    * @param port The listening port; 0 for anonymous    */
  void setPort(int port) ;

   /**
    * Get the WebService listening port.
    */
  int getPort() ;

   /**
    * Get the name of the interface to use for the host portion of the RMI codebase URL.
    */
  void setHost(java.lang.String hostname) ;

   /**
    * Set the name of the interface to use for the host portion of the RMI codebase URL.
    */
  java.lang.String getHost() ;

   /**
    * Get the specific address the WebService listens on.
    * @return the interface name or IP address the WebService binds to.    */
  java.lang.String getBindAddress() ;

   /**
    * Set the specific address the WebService listens on. This can be used on a multi-homed host for a ServerSocket that will only accept connect requests to one of its addresses.
    * @param host the interface name or IP address to bind. If host is null, connections on any/all local addresses will be allowed.    */
  void setBindAddress(java.lang.String host) throws java.net.UnknownHostException;

   /**
    * Get the WebService listen queue backlog limit. The maximum queue length for incoming connection indications (a request to connect) is set to the backlog parameter. If a connection indication arrives when the queue is full, the connection is refused.
    * @return the queue backlog limit.    */
  int getBacklog() ;

   /**
    * Set the WebService listen queue backlog limit. The maximum queue length for incoming connection indications (a request to connect) is set to the backlog parameter. If a connection indication arrives when the queue is full, the connection is refused.
    * @param backlog, the queue backlog limit.    */
  void setBacklog(int backlog) ;

   /**
    * Set the thread pool used for the WebServer class loading.
    */
  void setThreadPool(org.jboss.util.threadpool.BasicThreadPoolMBean threadPool) ;

   /**
    * A flag indicating if the server should attempt to download classes from thread context class loader when a request arrives that does not have a class loader key prefix.
    */
  boolean getDownloadServerClasses() ;

  void setDownloadServerClasses(boolean flag) ;

}
