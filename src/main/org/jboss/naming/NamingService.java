/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.naming;

import javax.management.*;
import javax.naming.*;

import org.jnp.server.Main;

import org.jboss.logging.Log;
import org.jboss.util.ServiceMBeanSupport;

/** A JBoss service that starts the jnp JNDI server.
 *      
 *   @author Rickard Öberg (rickard.oberg@telkel.com)
 *   @author Scott_Stark@displayscape.com
 *   @version $Revision: 1.11 $
 */
public class NamingService
   extends ServiceMBeanSupport
   implements NamingServiceMBean
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   Main naming;
   
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   public NamingService()
   {
      naming = new Main();
   }
   
   // Public --------------------------------------------------------
   public void setPort(int port)
   {
      naming.setPort(port);
   }
   
   public int getPort()
   {
      return naming.getPort();
   }
   
   public void setRmiPort(int port)
   {
      naming.setRmiPort(port);
   }
   
   public int getRmiPort()
   {
      return naming.getRmiPort();
   }

    public String getClientSocketFactory()
    {
        return naming.getClientSocketFactory();
    }
    public void setClientSocketFactory(String factoryClassName)
        throws ClassNotFoundException, InstantiationException, IllegalAccessException
    {
        naming.setClientSocketFactory(factoryClassName);
    }

    public String getServerSocketFactory()
    {
        return naming.getServerSocketFactory();
    }
    public void setServerSocketFactory(String factoryClassName)
        throws ClassNotFoundException, InstantiationException, IllegalAccessException
    {
        naming.setServerSocketFactory(factoryClassName);
    }

   public ObjectName getObjectName(MBeanServer server, ObjectName name)
      throws javax.management.MalformedObjectNameException
   {
      return new ObjectName(OBJECT_NAME);
   }
   
   public String getName()
   {
      return "Naming";
   }

   public void initService()
      throws Exception
   {
      // Read jndi.properties into system properties
      // RO: this is necessary because some components (=Tomcat servlets) use a 
      // buggy classloader that disallows finding the resource properly
      System.getProperties().load(Thread.currentThread().getContextClassLoader().getResourceAsStream("jndi.properties"));
   }
   
   public void startService()
      throws Exception
   {
      naming.start();
      // Create "java:comp/env"
      RefAddr refAddr = new StringRefAddr("nns", "ENC");
      Reference envRef = new Reference("javax.naming.Context", refAddr, ENCFactory.class.getName(), null);
      Context ctx = (Context)new InitialContext().lookup("java:");
      ctx.rebind("comp", envRef);
      log.log("Naming started on port "+naming.getPort());
   }

   public void stopService()
   {
      naming.stop();
      log.log("JNP server stopped");
   }

   // Protected -----------------------------------------------------
}

