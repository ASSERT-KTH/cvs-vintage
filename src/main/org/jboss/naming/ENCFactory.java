/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.naming;

import java.util.Hashtable;
import javax.naming.*;
import javax.naming.spi.*;

import org.jnp.server.NamingServer;
import org.jnp.interfaces.NamingContext;

/**
 *   Implementation of "java:" namespace factory. The context is associated
 *   with the thread, so the root context must be set before this is used in a thread
 *   
 *   SA FIXME: the java: namespace should be global.  the java:comp/env subcontext should 
 *   be threadlocal
 *     
 *   @see <related>
 *   @author <a href="mailto:rickard.oberg@telkel.com">Rickard Oberg</a>
 *   @version $Revision: 1.4 $
 */
public class ENCFactory
   implements ObjectFactory
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   static Hashtable encs = new Hashtable();
    
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------

   // ObjectFactory implementation ----------------------------------
   public Object getObjectInstance(Object obj,
                                Name name,
                                Context nameCtx,
                                Hashtable environment)
                         throws Exception
   {
      synchronized (encs)
      {
         // Get naming for this component
         NamingServer srv = (NamingServer)encs.get(Thread.currentThread().getContextClassLoader());
      
         // If this is the first time we see this name
         if (srv == null)
         {
            srv = new NamingServer();
            encs.put(Thread.currentThread().getContextClassLoader(), srv);
         }
         return new NamingContext(environment, null, srv);
      }
   }
    
   // Y overrides ---------------------------------------------------

   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
    
   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}
