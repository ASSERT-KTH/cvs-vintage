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
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import org.jboss.invocation.trunk.client.CommTrunkRamp;
import org.jboss.invocation.trunk.client.ICommTrunk;
import org.jboss.invocation.trunk.client.TrunkResponse;
import org.jboss.invocation.trunk.client.TunkRequest;
import org.jboss.logging.Logger;

/**
 * The Blocking implementation of the ICommTrunk interface.  This guy is 
 * the one that understands how to work the blocking sockets.
 *
 * @author    <a href="mailto:hiram.chirino@jboss.org">Hiram Chirino</a>
 */
public class BlockingSocketTrunk implements Runnable, ICommTrunk
{
   static final private Logger log = Logger.getLogger(BlockingSocketTrunk.class);

   Socket socket;
   /**
    * Messages will be read from the input stream
    */
   private DataInputStream in;

   /**
    * Messages will be writen to the output stream
    */
   private DataOutputStream out;

   /**
    * This trunk will power a thread that will pull request messages
    * off the trunkRamp.
    */
   private CommTrunkRamp trunkRamp;

   /** 
    * Reader thread.
    */
   private Thread worker;

   /**
    * The thread group that will hold al the threads that
    * are servicing this protocol's sockets.
    */
   ThreadGroup threadGroup;

   int connectionId;

   /**
    * Number of connection established so far
    */
   private static int connectionCounter = 0;

   /**
    * The state of the handler
    */
   private static final int STATE_CREATED = 0;
   private static final int STATE_CONNECTED = 1;
   private static final int STATE_DISCONNECTED = 2;
   private static final int STATE_CONNECTION_ERROR = 3;
   private static final int STATE_DESTROYED = 4;
   private int state = STATE_CREATED;

   private static final byte REQUEST_MESSAGE = 0;
   private static final byte RESPONSE_MESSAGE = 1;

   public BlockingSocketTrunk(Socket socket, ThreadGroup threadGroup) throws IOException
   {
      this.socket = socket;
      this.threadGroup = threadGroup;
      this.connectionId = connectionCounter++;

      socket.setSoTimeout(0);
      out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
      out.flush();
      in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

      state = STATE_CREATED;
   }

   /**
    * #Description of the Method
    *
    * @return               Description of the Returned Value
    * @exception Exception  Description of Exception
    */
   public void sendResponse(TrunkResponse response) throws IOException
   {
      if (log.isTraceEnabled())
         log.trace("Sending response: " + response);

      try
      {
         byte data[] = response.serialize();
         synchronized (out)
         {
            out.writeInt(data.length+1);
            out.writeByte(RESPONSE_MESSAGE);
            out.write(data);
            out.flush();
         }
      }
      catch (IOException e)
      {
         state = STATE_CONNECTION_ERROR;
         throw e;
      }
   }

   /**
    * #Description of the Method
    *
    * @return               Description of the Returned Value
    * @exception Exception  Description of Exception
    */
   public void sendRequest(TunkRequest request) throws IOException
   {
      if (log.isTraceEnabled())
         log.trace("Sending request: " + request);

      try
      {
         byte data[] = request.serialize();
         synchronized (out)
         {
            out.writeInt(data.length+1);
            out.writeByte(REQUEST_MESSAGE);
            out.write(data);
            out.flush();
         }
      }
      catch (IOException e)
      {
         state = STATE_CONNECTION_ERROR;
         throw e;
      }

   }

   public Object getNextMessage() throws IOException, InterruptedException, ClassNotFoundException
   {
      if (log.isTraceEnabled())
         log.trace("Waiting for message");
         
      int size = in.readInt();
      byte code = in.readByte();
      boolean tracing = log.isTraceEnabled();
      if (code == REQUEST_MESSAGE)
      {
         // Request received... pass it up
         if (tracing)
            log.trace("Reading Request Message");
         byte data[] = new byte[size-1];
         in.readFully(data);

         TunkRequest newRequest = new TunkRequest();
         newRequest.deserialize(data);
         return newRequest;

      }
      else
      {
         if (tracing)
            log.trace("Reading Response Message");
         byte data[] = new byte[size-1];
         in.readFully(data);

         TrunkResponse response = new TrunkResponse();
         response.deserialize(data);
         return response;
      }
   }

   public String toString()
   {
      return "BlockingSocketTrunk(" + connectionId + ") to " + socket.getInetAddress().getHostAddress();
   }

   ////////////////////////////////////////////////////////////////////////////
   // The following methods control the thread that is used to do the blocking
   // reads (looking for client requests) from the input socket.
   ////////////////////////////////////////////////////////////////////////////

   /**
    * Can only be started once.  Creates a new thread to handle client 
    * requests.
    */
   synchronized public void start() //throws java.lang.Exception
   {
      if (state != STATE_CREATED)
         return;

      if (log.isTraceEnabled())
         log.trace("Starting");

      state = STATE_CONNECTED;
      worker = new Thread(threadGroup, this, toString());

      worker.setDaemon(true);
      worker.start();
   }

   /**
    * Can only be stopped once.  Stops the previously started thread and closes
    * the sockets.
    */
   synchronized public void stop()
   {
      if (state == STATE_DESTROYED)
         return;

      if (log.isTraceEnabled())
         log.trace("Stopping");

      try
      {
         state = STATE_DISCONNECTED;
         if (worker.isAlive())
         {
            worker.interrupt();
            try
            {
               worker.join(1000);
            }
            catch (InterruptedException e)
            {
            }
         }

         in.close();
         out.close();
         socket.close();

      }
      catch (IOException e)
      {
         log.debug("Exception occured while closing resources: ", e);
      }
      finally
      {
         try
         {
            socket.close(); // Make our best effort to close the socket.
         }
         catch (Throwable ignore)
         {
         }
         state = STATE_DESTROYED;
      }
   }

   public boolean isConnected() {
      return ( state == STATE_CONNECTED );
   }

   /**
    * This thread makes sure we are processing requests from the client.
    */
   public void run()
   {
      try
      {
         trunkRamp.pumpRequest();
      }
      catch (Exception e)
      {
         if (state == STATE_CONNECTED)
         {
            if (log.isTraceEnabled())
               log.trace("Stopping due to unexcpected exception: ", e);
            state = STATE_CONNECTION_ERROR;
            trunkRamp.exceptionEvent(e);
         }
      }
      if (log.isTraceEnabled())
         log.trace("Stopped");
   }

   public CommTrunkRamp getCommTrunkRamp()
   {
      return trunkRamp;
   }

   public void setCommTrunkRamp(CommTrunkRamp ramp)
   {
      trunkRamp = ramp;
   }

}
