/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.naming;

import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Properties;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.StringRefAddr;

import org.jnp.server.Main;

import org.jboss.logging.Log;
import org.jboss.system.ServiceMBeanSupport;

/** A JBoss service that starts the jnp JNDI server.
 *      
 *   @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 *   @author <a href="mailto:Scott_Stark@displayscape.com">Scott Stark</a>.
 *   @version $Revision: 1.16 $
 *
 * Revisions:
 * 20010622 scott.stark: Report IntialContext env for problem tracing
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
      String categoryName = category.getName();
      naming = new Main(categoryName);
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

   public String getBindAddress()
   {
      return naming.getBindAddress();
   }
   public void setBindAddress(String host) throws UnknownHostException
   {
      naming.setBindAddress(host);
   }

   public int getBacklog()
   {
      return naming.getBacklog();
   }
   public void setBacklog(int backlog)
   {
      naming.setBacklog(backlog);
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
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      InputStream is = loader.getResourceAsStream("jndi.properties");
      Properties props = new Properties();
      props.load(is);

      Enumeration keys = props.propertyNames();
      while( keys.hasMoreElements() )
      {
         String key = (String) keys.nextElement();
         String value = props.getProperty(key);
         category.debug("System.setProperty, key="+key+", value="+value);
         System.setProperty(key, value);
      }
   }

   public void startService()
      throws Exception
   {
      naming.start();
      /* Create a default InitialContext and dump out its env to show what properties
         were used in its creation. If we find a Context.PROVIDER_URL property
         issue a warning as this means JNDI lookups are going through RMI.
      */
      InitialContext iniCtx = new InitialContext();
      Hashtable env = iniCtx.getEnvironment();
      Enumeration keys = env.keys();
      category.info("InitialContext Environment:");
      String providerURL = null;
      while( keys.hasMoreElements() )
      {
         String key = (String) keys.nextElement();
         String value = (String) env.get(key);
         category.info("key="+key+", value="+value);
         if( key.equals(Context.PROVIDER_URL) )
            providerURL = value;
      }
      // Warn if there was a Context.PROVIDER_URL
      if( providerURL != null )
         category.warn("Saw Context.PROVIDER_URL in server jndi.properties, url="+providerURL);

      // Create "java:comp/env"
      RefAddr refAddr = new StringRefAddr("nns", "ENC");
      Reference envRef = new Reference("javax.naming.Context", refAddr, ENCFactory.class.getName(), null);
      Context ctx = (Context)iniCtx.lookup("java:");
      ctx.rebind("comp", envRef);
      category.info("Naming started on port "+naming.getPort());
   }

   public void stopService()
   {
      naming.stop();
      category.info("JNP server stopped");
   }

   // Protected -----------------------------------------------------
}


