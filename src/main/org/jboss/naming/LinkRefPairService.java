/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.naming;

import javax.naming.InitialContext;

import org.jboss.deployment.DeploymentException;
import org.jboss.system.ServiceMBeanSupport;

/**
 * An mbean used to construct a link ref pair
 * 
 * @author Adrian Brock (adrian@jboss.com)
 * @version $Revision: 1.1 $
 */
public class LinkRefPairService extends ServiceMBeanSupport
   implements LinkRefPairServiceMBean
{
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------

   /** The jndi binding */
   private String jndiName;

   /** The remote jndi binding */
   private String remoteJndiName;

   /** The local jndi binding */
   private String localJndiName;
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------
   
   // LinkRefPairServiceMBean implementation ------------------------

   public String getJndiName()
   {
      return jndiName;
   }

   public String getLocalJndiName()
   {
      return localJndiName;
   }

   public String getRemoteJndiName()
   {
      return remoteJndiName;
   }

   public void setJndiName(String jndiName)
   {
      this.jndiName = jndiName;
   }

   public void setLocalJndiName(String jndiName)
   {
      this.localJndiName = jndiName;
   }
   
   public void setRemoteJndiName(String jndiName)
   {
      this.remoteJndiName = jndiName;
   }
   
   // ServiceMBeanSupport overrides ---------------------------------
   
   protected void startService() throws Exception
   {
      if (jndiName == null)
         throw new DeploymentException("The jndiName is null for LinkRefPair " + getServiceName());
      if (remoteJndiName == null)
         throw new DeploymentException("The remoteJndiName is null for LinkRefPair " + getServiceName());
      if (localJndiName == null)
         throw new DeploymentException("The localJndiName is null for LinkRefPair " + getServiceName());

      LinkRefPair pair = new LinkRefPair(remoteJndiName, localJndiName);
      InitialContext ctx = new InitialContext();
      try
      {
         Util.bind(ctx, jndiName, pair);
      }
      finally
      {
         ctx.close();
      }
   }
   
   protected void stopService() throws Exception
   {
      LinkRefPair pair = new LinkRefPair(remoteJndiName, localJndiName);
      InitialContext ctx = new InitialContext();
      try
      {
         Util.unbind(ctx, jndiName);
      }
      finally
      {
         ctx.close();
      }
   }
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
}
