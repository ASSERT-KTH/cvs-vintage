/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.resource;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.NoSuchMethodException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;

import javax.resource.Referenceable;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ManagedConnectionFactory;

import javax.transaction.TransactionManager;

import org.jboss.logging.Log;
import org.jboss.logging.LogWriter;
import org.jboss.resource.security.PrincipalMapping;
import org.jboss.util.ServiceMBeanSupport;

/**
 *   Service that configures an instance of a deployed resource
 *   adapter and binds the resulting connection factory into JNDI.
 *
 *   <p> This service does nothing until it receives a notification
 *   from the RAR deployer that the resource adapter has been
 *   deployed.
 *      
 *   @see RARDeployer
 *   @author Toby Allsopp (toby.allsopp@peace.com)
 *   @version $Revision: 1.1 $
 */
public class ConnectionFactoryLoader
   extends ServiceMBeanSupport
   implements ConnectionFactoryLoaderMBean, NotificationListener, ObjectFactory
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   private MBeanServer server = null;

   private String resourceAdapterName = null;
   private String factoryName = null;
   private String properties = null;
   private String rarDeployerName = null;
   private String tmName = "java:/TransactionManager";

   // Principal mapping parameters
   private String princMapClass;
   private String princMapProps;

   // Pool strategy parameters
   private String poolStrategy;

   // ObjectPool configuration parameters
   private int minSize;
   private int maxSize;
   private boolean blocking;
   private boolean gcEnabled;
   private long gcInterval;
   private long gcMinIdleTime;
   private boolean idleTimeoutEnabled;
   private long idleTimeout;
   private float maxIdleTimeoutPercent;
   private boolean invalidateOnError;
   private boolean timestampUsed;

   private ObjectName rarDeployerObjectName = null;

   /** The JNDI name to which this connection factory is bound */
   private String bindName;

   private ConnectionManagerImpl cm = null;

   /** Maps factory name to <code>ConnectionFactory</code> instance
       for JNDI lookups */
   private static Map cfs = new HashMap();
    
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------

   // ConnectionFactoryLoaderMBean implementation -------------------

   public String getResourceAdapterName() { return resourceAdapterName; }
   public void setResourceAdapterName(String resourceAdapterName) {
      this.resourceAdapterName = resourceAdapterName;
   }

   public String getFactoryName() { return factoryName; }
   public void setFactoryName(String name) { this.factoryName = name; }

   public String getProperties() { return properties; }
   public void setProperties(String p) { this.properties = p; }

   public String getRARDeployerName() { return rarDeployerName; }
   public void setRARDeployerName(String rarDeployerName)
   {
      this.rarDeployerName = rarDeployerName;
   }

   public String getTransactionManagerName() { return tmName; }
   public void setTransactionManagerName(String n) { tmName = n; }

   // Pincipal mapping settings

   public String getPrincipalMappingClass() { return princMapClass; }
   public void setPrincipalMappingClass(String c) { princMapClass = c; }

   public String getPrincipalMappingProperties() { return princMapProps; }
   public void setPrincipalMappingProperties(String p) { princMapProps = p; }
    
   // Object pool settings

   public String getPoolStrategy() { return poolStrategy; }
   public void setPoolStrategy(String strategy) { poolStrategy = strategy; }

   public int getMinSize() { return minSize; }
   public void setMinSize(int minSize) { this.minSize = minSize; }
   
   public int getMaxSize() { return maxSize; }
   public void setMaxSize(int maxSize) { this.maxSize = maxSize; }
   
   public boolean getBlocking() { return blocking; }
   public void setBlocking(boolean blocking) { this.blocking = blocking; }
   
   public boolean getGCEnabled() { return gcEnabled; }
   public void setGCEnabled(boolean gcEnabled) { this.gcEnabled = gcEnabled; }
   
   public long getGCInterval() { return gcInterval; }
   public void setGCInterval(long interval) { this.gcInterval = interval; }
   
   public long getGCMinIdleTime() { return gcMinIdleTime; }
   public void setGCMinIdleTime(long idleMillis) { gcMinIdleTime = idleMillis; }
   
   public boolean getIdleTimeoutEnabled() { return idleTimeoutEnabled; }
   public void setIdleTimeoutEnabled(boolean e) { idleTimeoutEnabled = e; }
   
   public long getIdleTimeout() { return idleTimeout; }
   public void setIdleTimeout(long idleMillis) { idleTimeout = idleMillis; }
   
   public float getMaxIdleTimeoutPercent() { return maxIdleTimeoutPercent; }
   public void setMaxIdleTimeoutPercent(float p) { maxIdleTimeoutPercent = p; }
   
   public boolean getInvalidateOnError() { return invalidateOnError; }
   public void setInvalidateOnError(boolean i) { invalidateOnError = i; }
   
   public boolean getTimestampUsed() { return timestampUsed; }
   public void setTimestampUsed(boolean tstamp) { timestampUsed = tstamp; }

   // ServiceMBeanSupport overrides ---------------------------------

   public String getName() { return "ConnectionFactoryLoader"; }

   protected ObjectName getObjectName(MBeanServer server, ObjectName name)
   {
      this.server = server;
      if (name == null)
      {
         String nameStr = OBJECT_NAME + ",name=" + factoryName;
         try
         {
            name = new ObjectName(nameStr);
         }
         catch (MalformedObjectNameException mone)
         {
            log.error("The name '" + nameStr + "' is malformed");
            log.exception(mone);
         }
      }
      return name;
   }

   protected void initService() throws Exception
   {
      rarDeployerObjectName = new ObjectName(rarDeployerName);
      server.addNotificationListener(rarDeployerObjectName, this,
                                     new RAFilter(log), null);

      log = Log.createLog(factoryName);
   }

   // NotificationListener implementation ---------------------------

   public void handleNotification(Notification n, Object handback)
   {
      log.debug("Received notification '" + n + "'");

      // We know that this is relevent to us because of the filter
      String type = n.getType();
      try
      {
         if (type.endsWith(DEPLOY_NOTIFICATION))
            loadConnectionFactory((RARMetaData) n.getUserData());
         else if (type.endsWith(UNDEPLOY_NOTIFICATION))
            unloadConnectionFactory((RARMetaData) n.getUserData());
         else
            log.error("Unknown notification type: " + type);
      }
      catch (Exception e)
      {
         log.exception(e);
      }
   }
   
   // ObjectFactory implementation ----------------------------------

   public Object getObjectInstance(Object obj, Name name, Context nameCtx,
                                   Hashtable environment)
   {
      // Return the connection factory with the requested name
      Log.getLog().debug("ConnectionFactoryLoader.getObjectInstance, name = '" +
                         name + "'");
      synchronized (cfs)
      {
         return cfs.get(name.toString());
      }
   }

   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
    
   // Private -------------------------------------------------------

   /**
    * Does the actual work of configuring a connection factory.
    * Because this is invoked from a notification handler, it makes no
    * sense to propagate exceptions, so we handle all checked
    * exceptions in the body of this method.
    */
   private void loadConnectionFactory(RARMetaData metaData)
   {
      // This context is used in a few places. There is no point
      // continuing if JNDI isn't working.

      Context ctx;
      try
      {
         ctx = new InitialContext();
      }
      catch (NamingException ne)
      {
         log.error("Unable to obtain initial context");
         log.exception(ne);
         return;
      }

      // This is the class loader through which we should be able to
      // load the resource adapter's classes

      ClassLoader cl = metaData.getClassLoader();

      // Create the ManagedConnectionFactory instance

      Class mcfClass;
      ManagedConnectionFactory mcf;

      String mcfClassName = metaData.getManagedConnectionFactoryClass();
      try
      {
         mcfClass = cl.loadClass(mcfClassName);
      }
      catch (ClassNotFoundException cnfe)
      {
         log.error("Unable to load managed connection factory class '" +
                   mcfClassName + "'");
         log.exception(cnfe);
         return;
      }
      try
      {
         mcf = (ManagedConnectionFactory) mcfClass.newInstance();
      }
      catch (Exception e)
      {
         log.error("Unable to instantiate manageed connection factory class '" +
                   mcfClass + "'");
         log.exception(e);
         return;
      }

      // Set the properties on it

      Properties props = new Properties();
      try
      {
         props.load(
            new ByteArrayInputStream(properties.getBytes("ISO-8859-1")));
      }
      catch (IOException ioe)
      {
         // This shouldn't happen, so we try to carry on as if it didn't
         log.error("Problem converting properties string '" + properties +
                   "' to Properties");
         log.exception(ioe);
      }

      // the properties that the deployment descriptor says we need to
      // set
      Map ddProps = metaData.getProperties(); 
      for (Iterator i=ddProps.values().iterator(); i.hasNext(); )
      {
         RARMetaData.Property ddProp = (RARMetaData.Property) i.next();
         String value = (String) props.get(ddProp.name);
         if (value == null )
         {
            if (ddProp.value == null)
            {
               log.warning("Not setting config property '" + ddProp.name + "'");
               continue;
            }
            log.warning("Using default value '" + ddProp.value + "' for " +
                        "config property '" + ddProp.name + "'");
            value = ddProp.value;
         }

         Class clazz;
         Method setter;

         try {
            clazz = cl.loadClass(ddProp.type);
         }
         catch (ClassNotFoundException cnfe)
         {
            log.warning("Unable to find class '" + ddProp.type + "' for " +
                        "property '" + ddProp.name + "' - skipping property.");
            continue;
         }
         PropertyEditor pe = PropertyEditorManager.findEditor(clazz);
         if (pe == null)
         {
            log.warning("Unable to find a PropertyEditor for class '" +
                        clazz + "' of property '" + ddProp.name + "' - " +
                        "skipping property");
            continue;
         }
         try
         {
            pe.setAsText(value);
         }
         catch (IllegalArgumentException iae)
         {
            log.warning("Value '" + value + "' is not valid for property '" +
                        ddProp.name + "' of class '" + clazz + "' - skipping " +
                        "property");
            continue;
         }
         Object v = pe.getValue();
         try
         {
            setter = mcfClass.getMethod("set" + ddProp.name,
                                        new Class[] { clazz });
         }
         catch (NoSuchMethodException nsme)
         {
            log.warning("The class '" + mcfClass.toString() + "' has no " +
                        "setter for config property '" + ddProp.name + "'");
            continue;
         }
         try
         {
            setter.invoke(mcf, new Object[] { v });
         }
         catch (Exception e)
         {
            log.warning("Unable to invoke setter method '" + setter + "' " +
                        "on object '" + mcf + "'");
            log.exception(e);
         }
      }

      // Give it somewhere to tell people things

      PrintWriter logWriter = new LogWriter(log);
      try
      {
         mcf.setLogWriter(logWriter);
      }
      catch (ResourceException re)
      {
         log.warning("Unable to set log writer '" + logWriter + "' on " +
                     "managed connection factory");
         log.exception(re);
         log.exception(re.getLinkedException());
      }

      // Find the transaction manager

      TransactionManager tm = null;
      try
      {
         tm = (TransactionManager) ctx.lookup(tmName);
      }
      catch (NamingException ne)
      {
         log.error("Unable to locate the transaction manager at '" + tmName +
                   "'");
         log.exception(ne);
      }

      // Create the principal mapper

      PrincipalMapping principalMapping;
      try
      {
         principalMapping =
            (PrincipalMapping) Class.forName(princMapClass).newInstance();
      }
      catch (Exception e)
      {
         log.error("Unable to instantiate principal mapping class '" +
                   princMapClass + "'");
         log.exception(e);
         return;
      }

      principalMapping.setLog(log);
      principalMapping.setManagedConnectionFactory(mcf);
      principalMapping.setRARMetaData(metaData);
      principalMapping.setProperties(princMapProps);

      // Create the connection manager

      try
      {
         cm = new ConnectionManagerImpl(metaData, this, mcf, log, tm,
                                        principalMapping);
      }
      catch (Exception e)
      {
         log.error("Unable to create connection manager");
         log.exception(e);
         return;
      }

      // Create us a connection factory

      Object cf;

      try
      {
         cf = mcf.createConnectionFactory(cm);
      }
      catch (ResourceException re)
      {
         log.error("Unable to create connection factory");
         log.exception(re);
         return;
      }

      // Bind it into JNDI

      bindName = "java:/" + factoryName;
      log.debug("Binding object '" + cf + "' into JNDI at '" + bindName + "'");
      synchronized (cfs)
      {
         cfs.put(factoryName, cf);
      }
      ((Referenceable) cf).setReference(new Reference(cf.getClass().getName(),
                                                      getClass().getName(),
                                                      null));
      try
      {
         ctx.bind(bindName, cf);
         log.log("Bound connection factory for resource adapter '" +
                 resourceAdapterName + "' to JNDI name '" + bindName + "'");
      }
      catch (NamingException ne)
      {
         log.error("Unable to bind connection factory to JNDI name '" +
                   bindName + "'");
         log.exception(ne);
      }
   }

   /**
    * Does the actual work of tearing down a connection factory.
    */
   private void unloadConnectionFactory(RARMetaData metaData)
   {
      // Destroy any managed connections

      cm.shutdown();
      cfs.remove(factoryName);
      log.log("Connection factory '" + factoryName + "' shut down.");

      // Unbind from JNDI

      try
      {
         new InitialContext().unbind(bindName);
      }
      catch (NamingException ne)
      {
         log.error("Unable to unbind connection factory at JNDI location '" +
                   bindName + "'");
         log.exception(ne);
      }
   }

   // Inner classes -------------------------------------------------

   private class RAFilter implements NotificationFilter
   {
      private Log log;
      private RAFilter(Log log) { this.log = log; }

      public boolean isNotificationEnabled(Notification n)
      {
         log.debug("Evaluating notification type='" + n.getType() + "', " +
                   "message='" + n.getMessage() + "'");
         return
            n.getType().startsWith(DEPLOYMENT_NOTIFICATION) &&
            n.getMessage().equals(resourceAdapterName);
      }
   }
}
