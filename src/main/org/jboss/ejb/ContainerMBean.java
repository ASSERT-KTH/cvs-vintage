/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb;

/**
 * MBean interface.
 * @see EJBDeployer
 */
public interface ContainerMBean extends org.jboss.system.ServiceMBean {

   /**
    * Gets the application deployment unit for this container. All the bean containers within the same application unit share the same instance.
    */
  org.jboss.ejb.EjbModule getEjbModule() ;

   /**
    * Gets the number of create invocations that have been made
    */
  long getCreateCount() ;

   /**
    * Gets the number of remove invocations that have been made
    */
  long getRemoveCount() ;

   /**
    * Gets the invocation statistics collection
    */
  org.jboss.invocation.InvocationStatistics getInvokeStats() ;

   /**
    * Get the components environment context
    * @return Environment Context    */
  javax.naming.Context getEnvContext() throws javax.naming.NamingException;

   /**
    * Returns the metadata of this container.
    * @return metaData;    */
  org.jboss.metadata.BeanMetaData getBeanMetaData() ;

   /**
    * Creates the single Timer Servic for this container if not already created
    * @param pKey Bean id
    * @return Container Timer Service
    * @throws IllegalStateException If the type of EJB is not allowed to use the timer service
    * @see javax.ejb.EJBContext#getTimerService
    */
  javax.ejb.TimerService getTimerService(java.lang.Object pKey) throws java.lang.IllegalStateException;

   /**
    * Removes Timer Servic for this container
    * @param pKey Bean id
    * @throws IllegalStateException If the type of EJB is not allowed to use the timer service
    */
  void removeTimerService(java.lang.Object pKey) throws java.lang.IllegalStateException;

   /**
    * The detached invoker operation.
    * @param mi - the method invocation context
    * @return the value of the ejb invocation
    * @throws Exception on error    */
  java.lang.Object invoke(org.jboss.invocation.Invocation mi) throws java.lang.Exception;

}
