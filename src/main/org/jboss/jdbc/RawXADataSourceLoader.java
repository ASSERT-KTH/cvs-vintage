/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.jdbc;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;

import javax.sql.XADataSource;

import org.jboss.logging.Log;
import org.jboss.logging.LogWriter;
import org.jboss.util.ServiceMBeanSupport;

/**
 * Service that loads a JDBC 2 std. extension-compliant
 * <code>XADataSource</code> and makes it available through JNDI.
 *
 * @version $Revision: 1.4 $
 * @author <a href="mailto:ammulder@alumni.princeton.edu">Aaron Mulder</a>
 *   @author <a href="mailto:toby.allsopp@peace.com">Toby Allsopp</a>
 */
public class RawXADataSourceLoader 
   extends ServiceMBeanSupport
   implements RawXADataSourceLoaderMBean, ObjectFactory
{
   // Settings
   String name;
   String dataSourceClass;
   String properties;
   boolean loggingEnabled;
   
   static XADataSource vendorSource = null;

   public RawXADataSourceLoader()
   {
   }

   public RawXADataSourceLoader(String poolName, String xaDataSourceClass)
   {
      setPoolName(poolName);
      setDataSourceClass(xaDataSourceClass);
   }
   public void setPoolName(String name)
   {
      this.name = name.trim();
      log = Log.createLog(name);
   }
   
   public String getPoolName()
   {
      return name;
   }
   
   public void setDataSourceClass(String clazz)
   {
      dataSourceClass = clazz.trim();
   }
   
   public String getDataSourceClass()
   {
      return dataSourceClass;
   }
   
   public void setProperties(String properties)
   {
      this.properties = properties;
   }
   
   public String getProperties()
   {
      return properties;
   }
   
   public void setLoggingEnabled(boolean enabled)
   {
      this.loggingEnabled = enabled;
   }
   
   public boolean getLoggingEnabled()
   {
      return loggingEnabled;
   }
   
   // ServiceMBeanSupport implementation ----------------------------

   public ObjectName getObjectName(MBeanServer server, ObjectName objectName) 
      throws javax.management.MalformedObjectNameException
   {
      return (objectName == null) 
         ? new ObjectName(OBJECT_NAME+",name="+getPoolName()) 
         : objectName;
   }

   public String getName()
   {
      return name;
   }

   public void startService() throws Exception
   {
      Class cls = Class.forName(dataSourceClass);
      vendorSource = (XADataSource)cls.newInstance();
   
      cls = vendorSource.getClass();
   
      if(properties != null && properties.length() > 0)
      {
         Properties props = new Properties();
         try
         {
            props.load(
               new ByteArrayInputStream(properties.getBytes("ISO-8859-1")));
         }
         catch (IOException ioe)
         {
            log.error("Couldn't convert properties string '" + properties +
                      "' to Properties");
            log.exception(ioe);
         }
         for (Iterator i = props.entrySet().iterator(); i.hasNext();)
         {
            Map.Entry entry = (Map.Entry) i.next();
            String attributeName = (String) entry.getKey();
            String attributeValue = (String) entry.getValue();
            log.debug("Setting attribute '" + attributeName + "' to '" +
                      attributeValue + "'");
            try
            {
               Method setAttribute =
                  cls.getMethod("set" + attributeName,
                                new Class[] { String.class });
               setAttribute.invoke(vendorSource,
                                   new Object[] { attributeValue });
            }
            catch (NoSuchMethodException e)
            {
               log.warning("No setter method for attribute '" + attributeName +
                           "' - skipping");
            }
         }
      }
   
      PrintWriter writer = loggingEnabled ? new LogWriter(log) : null;
      vendorSource.setLogWriter(writer);

      // Test database
      vendorSource.getXAConnection().close();
      
      // Bind in JNDI
      bind(new InitialContext(), "java:/"+getPoolName(),
           new Reference(vendorSource.getClass().getName(),
                         getClass().getName(), null));

      log.log("XA Data source "+getPoolName()+" bound to java:/"+getPoolName());
   }

   public void stopService()
   {
      // Unbind from JNDI
      try {
         String name = getPoolName();
         new InitialContext().unbind("java:/"+name);
         log.log("XA Data source "+name+" removed from JNDI");
         log.log("XA Data source "+name+" shut down");
      } catch (NamingException e)
      {
         // Ignore
      }
   }

   // ObjectFactory implementation ----------------------------------

   public Object getObjectInstance(Object obj, Name name, Context nameCtx,
                                   Hashtable environment)
   {
      // Return the XA data source
      return vendorSource;
   }

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

   private static Properties parseProperties(String string)
   {
      Properties props = new Properties();
      
      StringTokenizer tokens = new StringTokenizer(string, ";=");
      
      while (tokens.hasMoreTokens())
      {
         String key = tokens.nextToken();
         String value = tokens.nextToken();
         props.put(key, value);
      }
      return props;
   }
}
