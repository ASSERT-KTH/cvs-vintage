/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.resource;

import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;

import javax.transaction.TransactionManager;

import org.jboss.logging.Log;
import org.jboss.logging.LogWriter;
import org.jboss.util.ServiceMBeanSupport;

/**
 *   Binds a <code>ConnectionManagerFactory</code> instance into JNDI
 *   so that <code>ConnectionFactoryLoader</code>s can get at it.
 *
 *   @author Toby Allsopp (toby.allsopp@peace.com)
 *   @version $Revision: 1.1 $
 */
public class ConnectionManagerFactoryLoader
   extends ServiceMBeanSupport
   implements ConnectionManagerFactoryLoaderMBean, ObjectFactory
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   private String factoryName;
   private String factoryClass;
   private String properties;
   private String tmName = "java:/TransactionManager";

   /** The JNDI name to which this connection manager factory is bound */
   private String bindName;

   private ConnectionManagerFactory cmf;

   /** Maps factory name to <code>ConnectionManagerFactory</code>
       instance for JNDI lookups */
   private static Map cmfs = new HashMap();
    
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------

   // ConnectionManagerFactoryLoaderMBean implementation ------------

   public String getFactoryName() { return factoryName; }
   public void setFactoryName(String name) { this.factoryName = name; }

   public String getProperties() { return properties; }
   public void setProperties(String p) { this.properties = p; }

   public String getTransactionManagerName() { return tmName; }
   public void setTransactionManagerName(String n) { tmName = n; }

   public String getFactoryClass() { return factoryClass; }
   public void setFactoryClass(String c) { factoryClass = c; }

   // ServiceMBeanSupport overrides ---------------------------------

   public String getName() { return "ConnectionManagerFactoryLoader"; }

   protected ObjectName getObjectName(MBeanServer server, ObjectName name)
   {
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
      log = Log.createLog(factoryName);
   }

   public void startService() throws Exception
   {
      Context ctx = new InitialContext();

      Class cls = Class.forName(factoryClass);
      cmf = (ConnectionManagerFactory) cls.newInstance();
   
      PrintWriter writer = new LogWriter(log);
      cmf.setLogWriter(writer);
      TransactionManager tm = (TransactionManager) ctx.lookup(tmName);
      cmf.setTransactionManager(tm);
      if( properties != null )
      {
         Properties props = new Properties();
         props.load(
            new ByteArrayInputStream(properties.getBytes("IS0-8859-1")));
         cmf.setProperties(props);
      }

      // Bind in JNDI
      synchronized(cmfs)
      {
         cmfs.put(factoryName, cmf);
      }
      bind(ctx, "java:/" + factoryName,
           new Reference(cmf.getClass().getName(), getClass().getName(), null));

      log.log("Connection manager factory '" + factoryName + " bound to " +
              "'java:/" + factoryName + "'");
   }

   public void stopService()
   {
      // Unbind from JNDI
      try
      {
         new InitialContext().unbind("java:/" + factoryName);
         log.log("Connection manager factory '" + factoryName +
                 "' removed from JNDI");
      }
      catch (NamingException e) { }
   }

   // ObjectFactory implementation ----------------------------------

   public Object getObjectInstance(Object obj, Name name, Context nameCtx,
                                   Hashtable environment)
   {
      // Return the connection factory with the requested name
      Log.getLog().debug("ConnectionManagerLoader.getObjectInstance, name = '" +
                         name + "'");
      synchronized (cmfs)
      {
         return cmfs.get(name.toString());
      }
   }

   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
    
   // Private -------------------------------------------------------

   private void bind(Context ctx, String name, Object val)
      throws NamingException
   {
      // Bind val to name in ctx, and make sure that all intermediate
      // contexts exist
      Name n = ctx.getNameParser("").parse(name);
      while (n.size() > 1)
      {
         String ctxName = n.get(0);
         try
         {
            ctx = (Context)ctx.lookup(ctxName);
         } catch (NameNotFoundException e)
         {
            ctx = ctx.createSubcontext(ctxName);
         }
         n = n.getSuffix(1);
      }

      ctx.bind(n.get(0), val);
   }

   // Inner classes -------------------------------------------------

}
