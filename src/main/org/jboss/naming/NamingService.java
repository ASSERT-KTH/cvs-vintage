/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.naming;

import javax.management.*;
import javax.naming.*;

import org.jnp.server.Main;

import org.jboss.logging.Log;
import org.jboss.util.ServiceMBeanSupport;

/**
 *   <description> 
 *      
 *   @see <related>
 *   @author Rickard Öberg (rickard.oberg@telkel.com)
 *   @version $Revision: 1.2 $
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
      naming.start();
      log.log("Naming started on port "+naming.getPort());
      
      // Create "java:comp/env"
      RefAddr refAddr = new StringRefAddr("nns", "ENC");
      Reference envRef = new Reference("javax.naming.Context", refAddr, ENCFactory.class.getName(), null);
      Context ctx = (Context)new InitialContext().lookup("java:");
      ctx.rebind("comp", envRef);
   }
   
   public void destroyService()
   {
      naming.stop();
   }

   // Protected -----------------------------------------------------
}

