/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.resource;

import javax.resource.spi.ManagedConnectionFactory;

import org.jboss.logging.Log;

/**
 *   There exists one instance of this class for each connection
 *   factory. This instance is notified by the connection manager when
 *   an application component is issued or closes a connectio handle
 *   obtained from the managed connection factory loaded by the
 *   connection factory.
 *
 *   @see ConnectionManagerFactory
 *   @author Toby Allsopp (toby.allsopp@peace.com)
 *   @version $Revision: 1.1 $
 */
public class JBossConnectionListenerImpl
   implements JBossConnectionListener
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   private ManagedConnectionFactory mcf;
   private JBossConnectionManager cm;
   private Log log;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   JBossConnectionListenerImpl(ManagedConnectionFactory mcf, Log log)
   {
      this.mcf = mcf;
      this.log = log;
   }

   // Public --------------------------------------------------------

   // JBossConnectionListener implementation ------------------------

   public void connectionHandleIssued(Object connection)
   {
      log.debug("Connection handle '" + connection +
                "' issued by connection manager '" + cm + "' from mcf '" +
                mcf + "'");
   }

   public void connectionHandleClosed(Object connection)
   {
      log.debug("Connection handle '" + connection +
                "' closed from connection manager '" + cm + "' from mcf '" +
                mcf + "'");
   }

   // Package protected ---------------------------------------------

   void setConnectionManager(JBossConnectionManager cm) { this.cm = cm; }

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}
