/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.invocation.trunk.server.bio;


import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import javax.resource.spi.work.WorkManager;
import org.jboss.invocation.trunk.client.CommTrunkRamp;
import org.jboss.invocation.trunk.client.bio.BlockingSocketTrunk;
import org.jboss.invocation.trunk.server.IServer;
import org.jboss.invocation.trunk.server.TrunkInvoker;
import org.jboss.logging.Logger;

/**
 * Provides a Blocking implemenation of the IServer interface.
 * 
 * Sets up a blocking ServerSocket to accept blocking client connections.
 * @author    <a href="mailto:hiram.chirino@jboss.org">Hiram Chirino</a>
 */
public final class BlockingServer implements java.lang.Runnable, IServer
{

   /**
    * logger instance.
    */
   final static private Logger log = Logger.getLogger(BlockingServer.class);

   /**
    * The default timeout for the server socket. This is
    * set so the socket will periodically return to check
    * the running flag.
    */
   private final static int SO_TIMEOUT = 5000;

   /**
    * The listening socket that receives incomming connections
    * for servicing.
    */
   private ServerSocket serverSocket;

   /**
    * The running flag that all worker and server
    * threads check to determine if the service should
    * be stopped.
    */
   private volatile boolean running;

   /**
    * Should the connections use the TCP no delay option.
    */
   boolean enableTcpNoDelay;

   /**
    * The thread group that will hold al the threads that
    * are servicing this protocol's sockets.
    */
   ThreadGroup threadGroup = new ThreadGroup("Server Sockets");

   TrunkInvoker optimizedInvoker;

   private WorkManager workManager;

   public ServerSocket bind(
      TrunkInvoker optimizedInvoker,
      InetAddress address,
      int port,
      int connectBackLog,
      boolean enableTcpNoDelay,
      WorkManager workManager)
      throws IOException
   {
      this.optimizedInvoker = optimizedInvoker;
      this.enableTcpNoDelay = enableTcpNoDelay;
      this.workManager = workManager;

      serverSocket = new ServerSocket(port, connectBackLog, address);
      log.debug("bound blocking to address " + address + ", port: " + port);
      serverSocket.setSoTimeout(SO_TIMEOUT);
      return serverSocket;
   }

   public void start() throws IOException
   {
      running = true;
      new Thread(threadGroup, this, "OI Service Listener").start();
   }

   public void stop() throws IOException
   {
      running = false;
   }

   /**
    * Main processing method for the OILServerILService object
    */
   public void run()
   {
      try
      {
         while (running)
         {
            Socket socket = null;
            try
            {
               socket = serverSocket.accept();
               if (log.isTraceEnabled())
                  log.trace("Accepted connection: " + socket);
            }
            catch (java.io.InterruptedIOException e)
            {
               // It's ok, this is due to the SO_TIME_OUT
               continue;
            }

            // it's possible that the service is no longer
            // running but it got a connection, no point in
            // starting up a thread!
            //
            if (!running)
            {
               if (socket != null)
               {
                  try
                  {
                     socket.close();
                  }
                  catch (Exception ignore)
                  {
                  }
               }
               return;
            }

            try
            {

               if (log.isTraceEnabled())
                  log.trace("Initializing RequestListener for socket: " + socket);

               socket.setTcpNoDelay(enableTcpNoDelay);

               BlockingSocketTrunk trunk = new BlockingSocketTrunk(socket, threadGroup);
               CommTrunkRamp trunkRamp = new CommTrunkRamp(trunk, workManager);
               trunk.setCommTrunkRamp(trunkRamp);
               trunkRamp.setTrunkListener(optimizedInvoker);
               trunk.start();

            }
            catch (IOException ie)
            {
               log.debug("Client connection could not be accepted: ", ie);
            }
         }
      }
      catch (SocketException e)
      {
         // There is no easy way (other than string comparison) to
         // determine if the socket exception is caused by connection
         // reset by peer. In this case, it's okay to ignore both
         // SocketException and IOException.
         log.warn("SocketException occured (Connection reset by peer?). Cannot initialize the OILServerILService.");
      }
      catch (IOException e)
      {
         log.warn("IOException occured. Cannot initialize the OILServerILService.");
      }
      finally
      {
         try
         {
            serverSocket.close();
         }
         catch (Exception e)
         {
            log.debug("error closing server socket", e);
         }
         return;
      }
   }

}
// vim:expandtab:tabstop=3:shiftwidth=3
