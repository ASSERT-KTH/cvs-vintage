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
//import java.lang.reflect.InvocationHandler;
//import java.lang.reflect.Proxy;
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
import javax.naming.NameNotFoundException;
import javax.sql.DataSource;

import org.jboss.logging.Log;
import org.jboss.logging.Logger;
import org.jboss.util.ServiceMBeanSupport;

import org.jboss.proxy.Proxy;
import org.jboss.proxy.Proxies;
import org.jboss.proxy.InvocationHandler;

/**
 *   <description> 
 *      
 *   @see <related>
 *   @author Rickard Öberg (rickard.oberg@telkel.com)
 *   @version $Revision: 1.8 $
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
   
   public void initService()
      throws Exception
   {
      // Bind in JNDI
      bind(new InitialContext(), jndiName, this);
      
 		log.debug("Connection pool for "+url+" bound to "+jndiName);
      
      // Test database
      getConnection().close();
   }
   
   public void stopService()
   {
      // Unbind from JNDI
      try
      {
         new InitialContext().unbind(jndiName);
//DEBUG         log.debug("Connection pool for "+url+" removed from JNDI");
      } catch (NamingException e)
      {
			// Ignore
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
         // Create proxy around it
         ConnectionProxy proxyHandler = new ConnectionProxy(con, this);
         Connection conProxy = (Connection) Proxy.newProxyInstance(getClass().getClassLoader(),
                                          new Class[] { org.jboss.jdbc.Connection.class },
                                          proxyHandler);
         // Set proxy (TODO: Should not be needed!)
         proxyHandler.setProxy(conProxy);      
//DEBUG			log.debug("Connection to "+url+" created:"+conProxy);
			return conProxy;
      } else
      {
//DEBUG         log.debug("Connection to "+url+" pool size:"+pool.size());
         Connection con = (Connection)pool.pop();
//DEBUG         log.debug("Connection to "+url+" pool size:"+pool.size());
//DEBUG         log.debug("Connection to "+url+" taken from pool:"+con);
			return con;
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
//DEBUG         log.debug("Connection to "+url+" put into pool(size="+pool.size()+","+con+")");
      } else
      {
         // Pool is full
//DEBUG         log.debug("Connection to "+url+" closed");
         ((ConnectionProxy)Proxies.getInvocationHandler(con, (Class[])null)).close();
      }
   }
   
	// Private -------------------------------------------------------
   private void bind(Context ctx, String name, Object val)
      throws NamingException
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
}

class ConnectionProxy
   implements InvocationHandler
{
   Connection con;
	DataSourceImpl ds;
   Connection conProxy;
	
   ConnectionProxy(Connection con, DataSourceImpl ds)
   {
      this.con = con;
		this.ds = ds;
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
            
// TODO: Should work, but doesn't!(?)
//         ds.release((Connection)Proxies.getTarget(this));
         ds.release(this.conProxy);
//			ds.release((Connection)Proxy.newProxyInstance(getClass().getClassLoader(),
//                                          new Class[] { org.jboss.jdbc.Connection.class },
//                                          new ConnectionProxy(con, ds)));
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
   
   public void setProxy(Connection con)
   {
      this.conProxy = con;
   }
   
   // Package protected  --------------------------------------------
   void close()
   {
      try
      {
         con.close();
      } catch (Throwable e) { Logger.exception(e); }
      con = null;
   }
}

