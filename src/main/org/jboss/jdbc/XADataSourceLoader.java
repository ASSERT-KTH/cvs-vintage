/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.jdbc;

import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;
import javax.management.ObjectName;
import javax.management.MBeanServer;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.sql.XADataSource;
import org.jboss.pool.jdbc.xa.XAPoolDataSource;
import org.jboss.logging.LogWriter;
import org.jboss.util.ServiceMBeanSupport;
import org.jboss.logging.Log;
import java.sql.Connection;

/**
 * Service that loads a JDBC 2 std. extension-compliant connection pool.  This
 * pool generates connections that are registered with the current Transaction
 * and support two-phase commit.  The constructors are called by the JMX engine
 * based on your MLET tags.
 * @version $Revision: 1.20 $
 * @author <a href="mailto:ammulder@alumni.princeton.edu">Aaron Mulder</a>
 * @author <a href="mailto:danch@nvisia.com">danch (Dan Christopherson)</a>
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * 
 * Revision:<br>
 * 20010701 danch added support for timeout in blocking.
 * 20010703 bill added support for transaction isolation and ps cache size.
 */
public class XADataSourceLoader
   extends ServiceMBeanSupport
   implements XADataSourceLoaderMBean
{
   // Settings
   String name;
   String dataSourceClass;
   String url;
   String userName;
   String password;
   String properties;
   boolean loggingEnabled;
   int minSize;
   int maxSize;
   boolean blocking;
   boolean gcEnabled;
   long gcInterval;
   long gcMinIdleTime;
   boolean idleTimeoutEnabled;
   long idleTimeout;
   float maxIdleTimeoutPercent;
   boolean invalidateOnError;
   boolean timestampUsed;
   int blockingTimeout;
   int transactionIsolation = -1; // use default of driver
   int psCacheSize = 10;

   XAPoolDataSource source;

   public XADataSourceLoader()
   {
   }

   public XADataSourceLoader(String poolName, String xaDataSourceClass)
   {
      setPoolName(poolName);
      setDataSourceClass(xaDataSourceClass);
   }
   public void setPoolName(String name)
   {
      this.name = name;
      log = Log.createLog(name);
   }

   public String getPoolName()
   {
      return name;
   }

   public void setDataSourceClass(String clazz)
   {
      dataSourceClass = clazz;
   }

   public String getDataSourceClass()
   {
      return dataSourceClass;
   }

   public void setURL(String url)
   {
      this.url = url;
   }

   public String getURL()
   {
      return url;
   }

   public void setJDBCUser(String userName)
   {
      this.userName = userName;
   }

   public String getJDBCUser()
   {
      return userName;
   }

   public void setPassword(String password)
   {
      this.password = password;
   }

   public String getPassword()
   {
      return password;
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

   public void setMinSize(int minSize)
   {
      this.minSize = minSize;
   }

   public int getMinSize()
   {
      return minSize;
   }

   public void setMaxSize(int maxSize)
   {
      this.maxSize = maxSize;
   }

   public int getMaxSize()
   {
      return maxSize;
   }

   public void setBlocking(boolean blocking)
   {
      this.blocking = blocking;
   }

   public boolean getBlocking()
   {
      return blocking;
   }

   public void setBlockingTimeout(int blockingTimeout) {
      this.blockingTimeout = blockingTimeout;
   }
   
   public int getBlockingTimeout() {
      return blockingTimeout;
   }
   
   public void setTransactionIsolation(String iso) 
   {
      if (iso.equals("TRANSACTION_NONE"))
      {
         this.transactionIsolation = Connection.TRANSACTION_NONE;
      }
      else if (iso.equals("TRANSACTION_READ_COMMITTED"))
      {
         this.transactionIsolation = Connection.TRANSACTION_READ_COMMITTED;
      }
      else if (iso.equals("TRANSACTION_READ_UNCOMMITTED"))
      {
         this.transactionIsolation = Connection.TRANSACTION_READ_UNCOMMITTED;
      }
      else if (iso.equals("TRANSACTION_REPEATABLE_READ"))
      {
         this.transactionIsolation = Connection.TRANSACTION_REPEATABLE_READ;
      }
      else if (iso.equals("TRANSACTION_SERIALIZABLE"))
      {
         this.transactionIsolation = Connection.TRANSACTION_SERIALIZABLE;
      }
      else
      {
         throw new IllegalArgumentException("Setting Isolation level to unknown state: " + iso);
      }
   }
   
   public String getTransactionIsolation() {
      switch (this.transactionIsolation)
      {
      case Connection.TRANSACTION_NONE:
         return "TRANSACTION_NONE";
      case Connection.TRANSACTION_READ_COMMITTED:
         return "TRANSACTION_READ_COMMITTED";
      case Connection.TRANSACTION_READ_UNCOMMITTED:
         return "TRANSACTION_READ_UNCOMMITTED";
      case Connection.TRANSACTION_REPEATABLE_READ:
         return "TRANSACTION_REPEATABLE_READ";
      case Connection.TRANSACTION_SERIALIZABLE:
         return "TRANSACTION_SERIALIZABLE";
      default:
         return "DEFAULT";
      }
   }
   
   public void setGCEnabled(boolean gcEnabled)
   {
      this.gcEnabled = gcEnabled;
   }

   public boolean getGCEnabled()
   {
      return gcEnabled;
   }

   public void setGCInterval(long interval)
   {
      this.gcInterval = interval;
   }

   public long getGCInterval()
   {
      return gcInterval;
   }

   public void setGCMinIdleTime(long idleMillis)
   {
      this.gcMinIdleTime = idleMillis;
   }

   public long getGCMinIdleTime()
   {
      return gcMinIdleTime;
   }

   public void setIdleTimeoutEnabled(boolean enabled)
   {
      this.idleTimeoutEnabled = enabled;
   }

   public boolean getIdleTimeoutEnabled()
   {
      return idleTimeoutEnabled;
   }

   public void setIdleTimeout(long idleMillis)
   {
      this.idleTimeout = idleMillis;
   }

   public long getIdleTimeout()
   {
      return idleTimeout;
   }

   public void setMaxIdleTimeoutPercent(float percent)
   {
      this.maxIdleTimeoutPercent = percent;
   }

   public float getMaxIdleTimeoutPercent()
   {
      return maxIdleTimeoutPercent;
   }

   public void setInvalidateOnError(boolean invalidate)
   {
      this.invalidateOnError = invalidate;
   }

   public boolean getInvalidateOnError()
   {
      return invalidateOnError;
   }

   public void setTimestampUsed(boolean timestamp)
   {
      this.timestampUsed = timestamp;
   }

   public boolean getTimestampUsed()
   {
      return timestampUsed;
   }

   public int getPSCacheSize()
   {
      return psCacheSize;
   }
   
   public void setPSCacheSize(int size)
   {
      psCacheSize = size;
   }

   // ServiceMBeanSupport implementation ----------------------------
   public ObjectName getObjectName(MBeanServer server, ObjectName objectName)
      throws javax.management.MalformedObjectNameException
   {
      return (objectName == null) ? new ObjectName(OBJECT_NAME+",name="+getSource().getPoolName()) : objectName;
   }

   public String getName()
   {
      return name;
   }

   public void startService() throws Exception
   {
      // Transfer settings
      getSource().setPoolName(name);

      XADataSource vendorSource = null;
      Class cls = Class.forName(dataSourceClass);
      vendorSource = (XADataSource)cls.newInstance();
      getSource().setDataSource(vendorSource);

      cls = vendorSource.getClass();
      if(url != null && url.length() > 0)
      {
         Method setURL = cls.getMethod("setURL", new Class[] { String.class });
         setURL.invoke(vendorSource, new Object[] { url });
      }

      cls = vendorSource.getClass();
      if(properties != null && properties.length() > 0)
      {
         Properties props = parseProperties(properties);
         Method setProperties = cls.getMethod("setProperties", new Class[] { Properties.class });
         setProperties.invoke(vendorSource, new Object[] { props });
      }

      if(userName != null && userName.length() > 0)
         getSource().setJDBCUser(userName);

      if(password != null && password.length() > 0)
         getSource().setJDBCPassword(password);

      PrintWriter writer = loggingEnabled ? new LogWriter(log) : null;
      getSource().setLogWriter(writer);
      getSource().getDataSource().setLogWriter(writer);
      getSource().setMinSize(minSize);
      getSource().setMaxSize(maxSize);
      getSource().setBlocking(blocking);
      getSource().setBlockingTimeout(blockingTimeout);
      getSource().setGCEnabled(gcEnabled);
      getSource().setGCInterval(gcInterval);
      getSource().setGCMinIdleTime(gcMinIdleTime);
      getSource().setIdleTimeoutEnabled(idleTimeoutEnabled);
      getSource().setIdleTimeout(idleTimeout);
      getSource().setMaxIdleTimeoutPercent(maxIdleTimeoutPercent);
      getSource().setInvalidateOnError(invalidateOnError);
      getSource().setTimestampUsed(timestampUsed);
      getSource().setTransactionIsolation(transactionIsolation);
      getSource().setPSCacheSize(psCacheSize);

      // Initialize pool
      Context ctx = null;
      Object mgr = null;
      getSource().setTransactionManagerJNDIName("java:/TransactionManager");
      try
      {
         ctx = new InitialContext();
         mgr = ctx.lookup("java:/TransactionManager");
      } catch(NamingException e)
      {
         throw new IllegalStateException("Cannot start XA Connection Pool; there is no TransactionManager in JNDI!");
      }
      getSource().initialize();

      // Bind in JNDI
      bind(new InitialContext(), "java:/"+getSource().getPoolName(), source);

      log.log("XA Connection pool "+getSource().getPoolName()+" bound to java:/"+getSource().getPoolName());

      // Test database
      getSource().getConnection().close();
   }

   public void stopService()
   {
      // Unbind from JNDI
      try {
         String name = getSource().getPoolName();
         new InitialContext().unbind("java:/"+name);
         log.log("XA Connection pool "+name+" removed from JNDI");
         getSource().close();
         log.log("XA Connection pool "+name+" shut down");
      } catch (NamingException e)
      {
         // Ignore
      }
   }

   // Private -------------------------------------------------------
   private XAPoolDataSource getSource()
   {
      if (source == null)
         source = new XAPoolDataSource();
      return source;
   }

   private void bind(Context ctx, String name, Object val) throws NamingException
   {
      // Bind val to name in ctx, and make sure that all intermediate contexts exist
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


