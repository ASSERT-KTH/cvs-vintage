/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.security.plugins;

import java.io.InputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.Properties;
import java.util.Iterator;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.jboss.system.ServiceMBeanSupport;

/** The SecurityConfigMBean implementation. 
 
 @author Scott.Stark@jboss.org
 @version $Revision: 1.2 $
 */
public class SecurityConfig extends ServiceMBeanSupport implements SecurityConfigMBean
{
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   private MBeanServer server;
   private String authConf = "auth.conf";

   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   public SecurityConfig()
   {
   }
   
   public String getName()
   {
      return "SecurityIntialization";
   }

   /** Get the resource path to the JAAS login configuration file to use.
    */
   public String getAuthConf()
   {
      return authConf;
   }
   
   /** Set the resource path to the JAAS login configuration file to use.
    The default is "auth.conf".
    */
   public void setAuthConf(String authConf)
   {
      this.authConf = authConf;
   }
   
   // Public --------------------------------------------------------
   /** Start the service by locating the AuthConf resource in the classpath
    using the current thread context ClassLoader and set the
    "java.security.auth.login.config" system property to  location of the
    resource if this property has not already been set.
    @exception Exception thrown if the property cannot be set.
    */
   public void startService() throws Exception
   {
      // Set the JAAS login config file if not already set
      if( System.getProperty("java.security.auth.login.config") == null )
      {
         ClassLoader loader = Thread.currentThread().getContextClassLoader();
         URL loginConfig = loader.getResource("auth.conf");
         if( loginConfig != null )
         {
            System.setProperty("java.security.auth.login.config", loginConfig.toExternalForm());
            if (log.isInfoEnabled())
               log.info("Using JAAS LoginConfig: "+loginConfig.toExternalForm());
         }
         else
         {
            log.warn("No auth.conf resource found");
         }
      }
   }

}
