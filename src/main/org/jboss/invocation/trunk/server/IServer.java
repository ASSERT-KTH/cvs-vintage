/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.invocation.trunk.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import javax.resource.spi.work.WorkManager;

/**
 * The TrunkInvoker abstracts the Blocking and Non-block implementations of the service via
 * the IServer interface.
 * 
 * @author    <a href="mailto:hiram.chirino@jboss.org">Hiram Chirino</a>
 */
public interface IServer
{
   /**
    * Initialized the server protocol at the given address/port.
    * 
    * @return the server socket that was created to accept client connections.
    */
   ServerSocket bind(TrunkInvoker oi, InetAddress address, int port, int connectBackLog, boolean enableTcpNoDelay, WorkManager workManager)
      throws IOException;

   /**
    * Start accepting client connections.
    */
   void start() throws IOException;

   /**
    * Stop accepting client connections.
    */
   void stop() throws IOException;

}
// vim:expandtab:tabstop=3:shiftwidth=3
