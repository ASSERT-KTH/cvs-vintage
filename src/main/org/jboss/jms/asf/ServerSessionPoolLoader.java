/*
 * Copyright (c) 2000 Peter Antman Tim <peter.antman@tim.se>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.jboss.jms.asf;

import javax.management.ObjectName;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.NameNotFoundException;

import org.apache.log4j.Category;

import org.jboss.system.ServiceMBeanSupport;

/**
 * A loader for <tt>ServerSessionPools</tt>.
 *
 * <p>Created: Wed Nov 29 16:14:46 2000
 *
 * @author <a href="mailto:peter.antman@tim.se">Peter Antman</a>.
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @version $Revision: 1.6 $
 */
public class ServerSessionPoolLoader 
   extends ServiceMBeanSupport
   implements ServerSessionPoolLoaderMBean
{
   /** Instance logger. */
   private final Category log = Category.getInstance(this.getClass());

   /** The factory used to create server session pools. */
   private ServerSessionPoolFactory poolFactory;

   /** The name of the pool. */
   private String name;

   /** The type of pool factory to use. */
   private String poolFactoryClass;

   /**
    * Set the pool name.
    *
    * @param name    The pool name.
    */
   public void setPoolName(final String name)
   {
      this.name = name;
   }
   
   /**
    * Get the pool name.
    *
    * @return    The pool name.
    */
   public String getPoolName()
   {
      return name;
   }

   /**
    * Set the classname of pool factory to use.
    *
    * @param classname    The name of the pool factory class.
    */
   public void setPoolFactoryClass(final String classname)
   {
      this.poolFactoryClass = classname;
   }

   /**
    * Get the classname of pool factory to use.
    *
    * @return    The name of the pool factory class.
    */
   public String getPoolFactoryClass()
   {
      return poolFactoryClass;
   }

   /**
    * Get the JMX object name for this MBean.
    *
    * @param server    The server which this bean is loaded.
    * @param name      The user specified name.
    *
    * @throws MalformedObjectNameException
    */
   public ObjectName getObjectName(final MBeanServer server,
                                   final ObjectName name)
      throws MalformedObjectNameException
   {
      return (name == null) ? new ObjectName(OBJECT_NAME) : name;
   }

   /**
    * Get the name of this service.
    *
    * @return   The pool name.
    */
   public String getName()
   {
      return name;
   }


   /**
    * Start the service.
    *
    * <p>Bind the pool factory into JNDI.
    *
    * @throws Exception
    */
   protected void startService() throws Exception
   {
      Class cls = Class.forName(poolFactoryClass);
      poolFactory = (ServerSessionPoolFactory)cls.newInstance();
      poolFactory.setName(name);

      if (log.isDebugEnabled())
         log.debug("initialized with pool factory: " + poolFactory);
      InitialContext ctx = new InitialContext();
      String name = poolFactory.getName();
      String jndiname = "java:/" + name;
      try {
         org.jboss.naming.Util.bind(ctx, jndiname, poolFactory);
         log.info("pool factory " + name + " bound to "  + jndiname);
      }
      finally {
         ctx.close();
      }
   }

   /**
    * Stop the service.
    *
    * <p>Unbind from JNDI.
    */
   protected void stopService()
   {
      // Unbind from JNDI
      InitialContext ctx = null;
      try {
         ctx = new InitialContext();
         String name = poolFactory.getName();
         String jndiname = "java:/" + name;
         
         ctx.unbind(jndiname);
         log.info("pool factory " + name + " unbound from " + jndiname);
      }
      catch (NamingException ignore) {}
      finally {
         if (ctx != null) {
            try {
               ctx.close();
            }
            catch (NamingException ignore) {}
         }
      }
   }
}
