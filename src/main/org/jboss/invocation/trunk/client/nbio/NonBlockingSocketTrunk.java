/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.invocation.trunk.client.nbio;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import org.jboss.invocation.trunk.client.CommTrunkRamp;
import org.jboss.invocation.trunk.client.Compression;
import org.jboss.invocation.trunk.client.ICommTrunk;
import org.jboss.invocation.trunk.client.TrunkResponse;
import org.jboss.invocation.trunk.client.TrunkRequest;
import org.jboss.logging.Logger;

import EDU.oswego.cs.dl.util.concurrent.LinkedQueue;

/**
 * The Non Blocking implementation of the ICommTrunk interface.  This guy is 
 * the one that understands how to work the NBIO sockets.
 *
 * @author    <a href="mailto:hiram.chirino@jboss.org">Hiram Chirino</a>
 */
public class NonBlockingSocketTrunk implements ICommTrunk
{
   static final private Logger log = Logger.getLogger(NonBlockingSocketTrunk.class);

   Socket socket;

   /**
    * This trunk will power a thread that will pull request messages
    * off the trunkRamp.
    */
   private CommTrunkRamp trunkRamp;

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
   private static final byte REQUEST_MESSAGE_COMPRESSED = 2;
   private static final byte RESPONSE_MESSAGE_COMPRESSED = 3;

   SocketChannel client;
   Selector selector;
   SelectionKey selectionKey;

   /**
    * Holds message that need to be sent down the socket.
    */
   LinkedQueue outputQueue = new LinkedQueue();

   /**
    * We only enabled the OP_WRITE selection key if we have something waiting to 
    * be written.  isSendingOutput lets us know if we have the OP_WRITE selection 
    * key registered.
    */
   boolean isSendingOutput = false;
   /** used to guard access to the isSendingOutput attribute */
   Object isSendingOutputMutex = new Object();

   public NonBlockingSocketTrunk(SocketChannel client, Selector selector) throws IOException
   {
      this.client = client;
      this.selector = selector;

      // Put the client channel in non-blocking mode.
      client.configureBlocking(false);

      // Now register the client channel with the Selector object
      selectionKey = client.register(selector, SelectionKey.OP_READ, new ServiceSelectionAction());

      this.connectionId = connectionCounter++;
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
         
         byte messageType = RESPONSE_MESSAGE;
         byte data[] = response.serialize();
         byte compressed[] = Compression.compress(data);
         if( compressed != null ) {
            messageType = RESPONSE_MESSAGE_COMPRESSED;
            data = compressed;
         }         
         
         ByteBuffer buff = ByteBuffer.allocate(data.length + 1 + 4);

         buff.putInt(data.length + 1);
         buff.put(messageType);
         buff.put(data);
         buff.flip();
         try
         {
            outputQueue.put(buff);
            synchronized (isSendingOutputMutex)
            {
               if (!isSendingOutput)
               {
                  selectionKey.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                  selector.wakeup();
                  isSendingOutput = true;
               }
            }
         }
         catch (InterruptedException e)
         {
            throw new IOException("Operation interrupted: " + e);
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
   public void sendRequest(TrunkRequest request) throws IOException
   {
      if (log.isTraceEnabled())
         log.trace("Sending request: " + request);

      try
      {
         byte messageType = REQUEST_MESSAGE;
         byte data[] = request.serialize();
         byte compressed[] = Compression.compress(data);
         if( compressed != null ) {
            messageType = REQUEST_MESSAGE_COMPRESSED;
            data = compressed;
         }         
         
         ByteBuffer buff = ByteBuffer.allocate(data.length + 1 + 4);
         buff.putInt(data.length + 1);
         buff.put(messageType);
         buff.put(data);
         buff.flip();
         try
         {
            synchronized (isSendingOutputMutex)
            {
               outputQueue.put(buff);
               if (!isSendingOutput)
               {
                  selectionKey.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                  selector.wakeup();
                  isSendingOutput = true;
               }
            }
         }
         catch (InterruptedException e)
         {
            throw new IOException("Operation interrupted: " + e);
         }
      }
      catch (IOException e)
      {
         state = STATE_CONNECTION_ERROR;
         throw e;
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
         client.close();
      }
      catch (IOException e)
      {
         log.debug("Exception occured while closing resources: ", e);
      }
      finally
      {
         try
         {
            client.close(); // Make our best effort to close the socket.
         }
         catch (Throwable ignore)
         {
         }
         state = STATE_DESTROYED;
      }
   }

   public CommTrunkRamp getCommTrunkRamp()
   {
      return trunkRamp;
   }

   public void setCommTrunkRamp(CommTrunkRamp ramp)
   {
      trunkRamp = ramp;
   }

   public boolean isConnected()
   {
      return (state == STATE_CONNECTED);
   }

   class ServiceSelectionAction implements SelectionAction
   {

      public void service(SelectionKey selection)
      {
         if (selection.isWritable())
            serviceWrite(selection);
         if (selection.isReadable())
            serviceRead(selection);
      }

      boolean writeOnNewFrame = true;
      ByteBuffer writeBuffer;

      public void serviceWrite(SelectionKey selection)
      {

         boolean tracing = log.isTraceEnabled();

         if (tracing)
            log.trace("WriteDataAction triggered.");

         // Get the client channel that has data to read
         SocketChannel client = (SocketChannel) selection.channel();

         // Now read bytes from the client into a buffer
         int byteswritten;
         try
         {

            do
            {
               if (writeOnNewFrame)
               {
                  if (tracing)
                     log.trace("We are on a new frame, looking for the next item to send.");

                  // Do we have more items to send  
                  writeBuffer = (ByteBuffer) outputQueue.poll(0);
                  if (writeBuffer == null)
                  {
                     synchronized (isSendingOutputMutex)
                     {
                        // The first check could be wrong since it was not synchronized,
                        // we do that since we want to avoid the contention overhead when
                        // we have alot of messsages to send.
                        writeBuffer = (ByteBuffer) outputQueue.poll(0);
                        if (writeBuffer == null)
                        {
                           if (tracing)
                              log.trace("No more items to send.");
                           selection.interestOps(SelectionKey.OP_READ);
                           isSendingOutput = false;
                           return;
                        }
                     }
                  }
                  if (tracing)
                     log.trace("Sending another item.");
                  writeOnNewFrame = false;
               }

               if (tracing)
                  log.trace("Writing item data.");
               byteswritten = client.write(writeBuffer);
               if (writeBuffer.position() == writeBuffer.limit())
               {
                  if (tracing)
                     log.trace("Item write completed.");
                  writeOnNewFrame = true;
               }
            }
            while (byteswritten > 0);
            if (tracing)
               log.trace("Output buffer must be full, no more data could be written.");

         }
         catch (InterruptedException e)
         {
            log.debug("Communications error, closing connection: ", e);
            selection.cancel();
            try
            {
               client.close();
            }
            catch (IOException ignore)
            {
            }
         }
         catch (IOException e)
         {
            // means the client has disconnected, so de-register the
            // selection key and close the channel.
            log.debug("Communications error, closing connection: ", e);
            selection.cancel();
            try
            {
               client.close();
            }
            catch (IOException ignore)
            {
            }
         }
      }

      boolean readOnNewFrame = true;
      ByteBuffer readSizebuffer = ByteBuffer.allocate(4);
      ByteBuffer readBuffer;

      public void serviceRead(SelectionKey selection)
      {

         boolean tracing = log.isTraceEnabled();

         if (tracing)
            log.trace("ReadDataAction triggered.");

         // Get the client channel that has data to read
         SocketChannel client = (SocketChannel) selection.channel();

         if (tracing)
            log.trace("Client was: " + client);

         // Now read bytes from the client into a buffer
         int bytesread;
         try
         {
            do
            {
               if (readOnNewFrame)
               {
                  if (tracing)
                     log.trace("Reading frame size data");

                  bytesread = client.read(readSizebuffer);
                  if (readSizebuffer.position() == 4)
                  {
                     readSizebuffer.flip();
                     int frameSize = readSizebuffer.getInt();
                     readSizebuffer.clear();
                     readBuffer = ByteBuffer.allocate(frameSize);
                     readOnNewFrame = false;
                  }
               }
               else
               {
                  if (tracing)
                     log.trace("Reading frame data");

                  bytesread = client.read(readBuffer);
                  if (readBuffer.position() == readBuffer.capacity())
                  {
                     readBuffer.flip();
                     readOnNewFrame = true;

                     byte code = readBuffer.get();
                     
                     // get the rest of the buffer.
                     byte data[] = new byte[readBuffer.limit() - 1];
                     readBuffer.get(data);
                     
                     if( code == REQUEST_MESSAGE_COMPRESSED || code == RESPONSE_MESSAGE_COMPRESSED )
                        data = Compression.uncompress(data);            
            
                     if (code == REQUEST_MESSAGE || code == REQUEST_MESSAGE_COMPRESSED)
                     {
                        try
                        {
                           if (tracing)
                              log.trace("Frame read finished delivering new Request");

                           TrunkRequest newRequest = new TrunkRequest();
                           newRequest.deserialize(data);
                           trunkRamp.deliverTrunkRequest(newRequest);
                        }
                        catch (ClassNotFoundException e)
                        {
                           log.info("Communications error:", e);
                        }
                     }
                     else
                     {
                        try
                        {
                           if (tracing)
                              log.trace("Frame read finished placing Response input queue");

                           TrunkResponse response = new TrunkResponse();
                           response.deserialize(data);
                           trunkRamp.deliverTrunkResponse(response);
                        }
                        catch (ClassNotFoundException e)
                        {
                           log.info("Communications error:", e);
                        }
                     }
                  }
               }
            }
            while (bytesread > 0);
            if (tracing)
               log.trace("No more data available to be read.");

         }
         catch (InterruptedException e)
         {
            log.debug("Communications error, closing connection: ", e);
            bytesread = -1;
         }
         catch (IOException e)
         {
            log.debug("Communications error, closing connection: ", e);
            bytesread = -1;
         }

         // If read() returns -1, it indicates end-of-stream, which
         // means the client has disconnected, so de-register the
         // selection key and close the channel.
         if (bytesread == -1)
         {
            selection.cancel();
            try
            {
               client.close();
            }
            catch (IOException ignore)
            {
            }
            if (log.isTraceEnabled())
               log.trace("Remote connection closed.");
         }
      }
   }

}
