/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.invocation.pooled.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.Socket;
import java.rmi.MarshalledObject;
import org.jboss.invocation.Invocation;
import org.jboss.logging.Logger;

/**
 * This Thread object hold a single Socket connection to a client
 * and is kept alive until a timeout happens, or it is aged out of the
 * PooledInvoker's LRU cache.
 *
 * This class will demarshal then delegate to PooledInvoker for invocation.
 *
 *
 * *NOTES* ObjectStreams were found to be better performing than the Custom marshalling
 * done by the TrunkInvoker.
 *
 * @author    <a href="mailto:bill@jboss.org">Bill Burke</a>
 */
public class ServerThread extends Thread
{
   final static private Logger log = Logger.getLogger(ServerThread.class);

   protected boolean running = true;
   protected ObjectInputStream in;
   protected ObjectOutputStream out;
   protected Socket socket;
   protected PooledInvoker invoker;
   protected LRUPool pool;

   public ServerThread(Socket socket, PooledInvoker invoker, LRUPool pool, int timeout) throws Exception
   {
      this.socket = socket;
      this.invoker = invoker;
      this.pool = pool;
      socket.setSoTimeout(timeout);
   }

   public void shutdown()
   {
      running = false;
   }

   public void run()
   {
      try
      {
         out = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
         out.flush();
         in = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
      }
      catch (Exception e)
      {
         log.error("Failed to initialize", e);
      }
      final byte alive = 1;
      while (running)
      {
         try
         {
            // Perform acknowledgement to convince client
            // that the socket is still active
            byte ACK = in.readByte();
            out.writeByte(ACK);
            out.flush();

            // Ok, now read invocation and invoke
            Invocation invocation = (Invocation)in.readObject();
            Object response = null;
            try
            {
               response = invoker.invoke(invocation);
            }
            catch (Exception ex)
            {
               response = ex;
            }
            out.writeObject(response);
            out.flush();
         }
         catch (InterruptedIOException iex)
         {
            log.debug("socket timed out");
            running = false;
         }
         catch (Exception ex)
         {
            running = false;
         }
      }

      // Ok, we've been shutdown.  Do appropriate cleanups.
      try
      {
         synchronized(pool)
         {
            pool.remove(this);
         }
      }
      catch (Exception ex)
      {
         log.error("Failed cleanup", ex);
      }
      try
      {
         in.close();
         out.close();
      }
      catch (Exception ex)
      {
      }
      try
      {
         socket.close();
      }
      catch (Exception ex)
      {
         log.error("Failed cleanup", ex);
      }
      socket = null;
      in = null;
      out = null;
      pool = null;
   }
}
