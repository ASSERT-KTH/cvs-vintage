/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.invocation.trunk.client.bio;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import javax.resource.spi.work.WorkManager;
import org.jboss.invocation.trunk.client.AbstractClient;
import org.jboss.invocation.trunk.client.CommTrunkRamp;
import org.jboss.invocation.trunk.client.ICommTrunk;
import org.jboss.invocation.ServerID;
import org.jboss.logging.Logger;

/**
 * Provides a Blocking implemenation for clients running on < 1.4 JVMs.
 * 
 * Simply connects to the server with a regular Socket and sets up a blocking
 * ICommTrunk to handle the IO protocol.
 * 
 * @author    <a href="mailto:hiram.chirino@jboss.org">Hiram Chirino</a>
 */
public class BlockingClient extends AbstractClient
{
   private final static Logger log = Logger.getLogger(BlockingClient.class);

   private BlockingSocketTrunk trunk;
   private Socket socket;
   private ServerID serverID;

   public void connect(ServerID serverID, 
                       ThreadGroup threadGroup) throws IOException
   {
      this.serverID = serverID;
      boolean tracing = log.isTraceEnabled();
      if (tracing)
         log.trace("Connecting to : " + serverID);

      socket = new Socket(serverID.address, serverID.port);
      socket.setTcpNoDelay(serverID.enableTcpNoDelay);

      trunk = new BlockingSocketTrunk(socket, threadGroup);
      CommTrunkRamp ramp = new CommTrunkRamp(trunk, workManager);
      trunk.setCommTrunkRamp(ramp);
      ramp.setTrunkListener(this);
      
      if (tracing)
         log.trace("Connection established.");
      
   }

   public ServerID getServerID()
   {
      return serverID;
   }

   public ICommTrunk getCommTrunk()
   {
      return trunk;
   }

   public void start()
   {
      trunk.start();
   }

   public void stop()
   {
      trunk.stop();
   }
}
