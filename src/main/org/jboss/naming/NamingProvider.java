/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.naming;

import javax.management.*;

import org.jnp.server.Main;

import org.jboss.logging.Log;
import org.jboss.util.ServiceMBeanSupport;

/**
 *   <description> 
 *      
 *   @see <related>
 *   @author Rickard Öberg (rickard.oberg@telkel.com)
 *   @version $Revision: 1.1 $
 */
public class NamingProvider
   extends ServiceMBeanSupport
   implements NamingProviderMBean
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   Main naming;
   
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   public NamingProvider()
   {
      naming = new Main();
   }
   
   // Public --------------------------------------------------------
   public ObjectName getObjectName(MBeanServer server, ObjectName name)
      throws javax.management.MalformedObjectNameException
   {
      return new ObjectName(OBJECT_NAME);
   }
   
   public String getName()
   {
      return "Naming";
   }
   
   public void startService()
      throws Exception
   {
      naming.start();
      log.log("Naming started on port "+naming.getPort());
   }
   
   public void stopService()
   {
      naming.stop();
   }

   // Protected -----------------------------------------------------
}

