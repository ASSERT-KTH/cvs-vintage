/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
 
package org.jboss.jdbc;

import java.io.PrintWriter;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.lang.reflect.Method;
import java.util.Stack;
import java.util.Hashtable;
import java.util.HashMap;

import javax.management.*;
import javax.naming.spi.ObjectFactory;
import javax.naming.Referenceable;
import javax.naming.Reference;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.jboss.logging.Log;
import org.jboss.util.ServiceMBeanSupport;

/**
 *   <description> 
 *      
 *   @see <related>
 *   @author Rickard Öberg (rickard.oberg@telkel.com)
 *   @version $Revision: 1.1 $
 */
public class DataSourceImpl
   extends ServiceMBeanSupport
   implements DataSource, ObjectFactory, Referenceable, DataSourceImplMBean, MBeanRegistration
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   Stack pool = new Stack();
   
   int maxSize = 5;
   
   String url;
   String jndiName;
   String driverName;
   String userName;
   String password;
   
   // Static --------------------------------------------------------
   static HashMap pools = new HashMap();

   // Constructors --------------------------------------------------
   public DataSourceImpl()
   {
      // Instance being used as ObjectFactory
   }
   
   public DataSourceImpl(String url, String jndiName, String driverName, String userName, String password)
   {
      this.url = url;
      this.jndiName = jndiName;
      this.driverName = driverName;
      this.userName = userName;
      this.password = password;
      
      pools.put(jndiName, this);
   }
   
   // Public --------------------------------------------------------
   public ObjectName getObjectName(MBeanServer server, ObjectName name)
      throws javax.management.MalformedObjectNameException
   {
      return new ObjectName(OBJECT_NAME+",name="+jndiName);
   }
   
   public String getName()
   {
      return "DataSource";
   }
   
   public void startService()
      throws Exception
   {
      // Bind in JNDI
      new InitialContext().bind(jndiName, this);
      
      log.log("Connection pool for "+url+" bound to "+jndiName);
      
      // Test database
      getConnection().close();
   }
   
   public void stopService()
   {
      // Unbind from JNDI
      try
      {
         new InitialContext().unbind(jndiName);
         log.log("Connection pool for "+url+" removed from JNDI");
      } catch (NamingException e)
      {
         log.error("Could not unbind connection pool "+jndiName);
         log.exception(e);
      }
   }
   
   // DataSource implementation -------------------------------------
   public synchronized Connection getConnection()
      throws SQLException
   {
      return getConnection(userName, password);
   }
   
   public synchronized Connection getConnection(String user,
                                            String password)
                                               throws SQLException
   {
      if (pool.empty())
      {
         Connection con = DriverManager.getConnection(url,user,password);
//DEBUG         log.debug("Connection to "+url+" created");
         // Create proxy around it
         return (Connection) Proxy.newProxyInstance(null,
                                          new Class[] { Connection.class },
                                          new ConnectionProxy(con));
      } else
      {
//DEBUG         log.debug("Connection to "+url+" taken from pool");
         return (Connection)pool.pop();
      }
   }
   
   public PrintWriter getLogWriter()
      throws SQLException
   {
      return null;
   }
   
   public void setLogWriter(PrintWriter out)
      throws SQLException
   {
      // TODO
   }

   public void setLoginTimeout(int seconds)
      throws SQLException
   {
      // TODO
   }
   
   public int getLoginTimeout()
      throws SQLException
   {
      return 30;
   }
   
   // Referenceable implementation ----------------------------------
   public Reference getReference()
   {
      return new Reference(getClass().getName(), getClass().getName(), null);
   }
   
   // ObjectFactory implementation ----------------------------------
   public Object getObjectInstance(Object obj,
                                Name name,
                                Context nameCtx,
                                Hashtable environment)
                         throws Exception
   {
      return pools.get(name.toString());
   }
   
   // Protected -----------------------------------------------------
   synchronized void release(Connection con)
   {
      if (pool.size() < maxSize)
      {
         pool.push(con);
//DEBUG         log.debug("Connection to "+url+" put into pool(size="+pool.size()+")");
      } else
      {
         // Pool is full
//DEBUG         log.debug("Connection to "+url+" closed");
         ((ConnectionProxy)Proxy.getInvocationHandler(con)).close();
      }
   }
   
   class ConnectionProxy
      implements InvocationHandler
   {
      Connection con;
      ConnectionProxy(Connection con)
      {
         this.con = con;
      }
      public Object invoke(Object proxy,
                           Method method,
                           Object[] args)
         throws Throwable
      {
         if (method.getName().equals("close"))
         {
            if (con == null)
               throw new SQLException("Connection is already closed");
               
            release((Connection)proxy);
            return null;
         } else
         {
            if (con == null)
               throw new SQLException("Connection is closed");
            else
            {
               // Invoke underlying connection
               if (args == null) args = new Object[0];
               return method.invoke(con, args);
            }
         }
      }
      
      void close()
      {
         try
         {
            con.close();
         } catch (Throwable e) { e.printStackTrace(); }
         con = null;
      }
   }
}
