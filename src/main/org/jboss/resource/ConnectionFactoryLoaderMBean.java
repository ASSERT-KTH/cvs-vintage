/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.resource;

import org.jboss.util.ServiceMBean;

/**
 *   MBean that binds a connection factory for a deployed resource
 *   adapter using a specific configuration. Note that it is entirely
 *   possible that the resource adapter this refers to doesn't exist
 *   until later (i.e. until it is deployed). If so, we wait until the
 *   resource adapter deployer notifies us of the deployment.
 *      
 *   @see RARDeployer
 *   @author Toby Allsopp (toby.allsopp@peace.com)
 *   @version $Revision: 1.2 $
 */
public interface ConnectionFactoryLoaderMBean
   extends ServiceMBean
{
   String OBJECT_NAME = ":service=ConnectionFactoryLoader";
   String DEPLOYMENT_NOTIFICATION = "org.jboss.resource.deployment";
   String DEPLOY_NOTIFICATION = ".deploy";
   String UNDEPLOY_NOTIFICATION = ".undeploy";

   /**
    * The name of the resource adapter. This is the value from the
    * <code>display-name</code> element in its deployment descriptor
    * becase I can't see a better name to use.
    */
   String getResourceAdapterName();
   void setResourceAdapterName(String resourceAdapterName);

   /**
    * The name under which to bind this connection factory in
    * JNDI. The name will be prepended with "java:/" to ensure that it
    * is only visible in the local JVM.
    */
   String getFactoryName();
   void setFactoryName(String factoryName);

   /**
    * A string in a format parseable by {@link
    * java.util.Properties#load} that defines the properties to set on
    * the <code>ManagedConnectionFactory</code> for this connection
    * factory instance.
    */
   String getProperties();
   void setProperties(String properties);

   /**
    * The name of the MBean responsible for deploying the resource
    * adapter. This is so we can listen for notification of the
    * resource adapter being deployed.
    */
   String getRARDeployerName();
   void setRARDeployerName(String rarDeployerName);

   /**
    * The name under which the transaction manager is bound in
    * JNDI. This has a sensible default.
    */
   String getTransactionManagerName();
   void setTransactionManagerName(String transactionManagerName);

   /**
    * The name of the connection manager factory to use. This is the
    * same name specified in the corresponding
    * <code>ConnectionManagerFactoryLoader</code> MBean.
    */
   String getConnectionManagerFactoryName();
   void setConnectionManagerFactoryName(String connectionManagerFactoryName);

   /**
    * The properties to set on the connection manager.
    */
   String getConnectionManagerProperties();
   void setConnectionManagerProperties(String properties);

   /**
    * The name of the class implementing {@link
    * org.jboss.resource.security.PrincipalMapping} that is to be used
    * for mapping caller principals to resource principals for this
    * connection factory.
    */
   String getPrincipalMappingClass();
   void setPrincipalMappingClass(String className);

   /**
    * A string in a format parseable by {@link
    * java.util.Properties#load} that defines the properties to set on
    * the <code>PrincipalMapping</code> for this connection factory
    * instance.
    */
   String getPrincipalMappingProperties();
   void setPrincipalMappingProperties(String properties);
}
