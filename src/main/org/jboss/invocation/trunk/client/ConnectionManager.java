/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.invocation.trunk.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import org.jboss.invocation.trunk.client.bio.BlockingClient;
import org.jboss.logging.Logger;

import EDU.oswego.cs.dl.util.concurrent.ClockDaemon;
import EDU.oswego.cs.dl.util.concurrent.ThreadFactory;

/**
 * This is a singleton class on that lives on a client which is used find existing 
 * connections that have been established to a sever.
 * 
 * This class also powers a clean-up thread which shuts down clients that have been
 * been inactive for 1 min.
 * 
 * This class is the one that makes the decicion if the client used will be Blocking or Non Blocking.
 * (currently based on jre version)
 * 
 * @author <a href="mailto:hiram.chirino@jboss.org">Hiram Chirino</a>
 */
public class ConnectionManager
{
   private final static Logger log = Logger.getLogger(ConnectionManager.class);
   private static ConnectionManager instance = new ConnectionManager();

   /** 
    * A map of all the connections that we have established due to 
    * a request this vm has made.
    * This field uses copy on write synchronization.
    */
   HashMap requestConnections = new HashMap();

   /**
    * Manages the thread that checks to see if connections have expired.
    */
   protected ClockDaemon clockDaemon = new ClockDaemon();

   /**
    * How often to check for expired connections
    */
   protected long checkPeriod = 1000 * 60;

   /**
    * How long before a connection expires
    */
   protected long expirationPeriod = 1000 * 60;

   /**
    * 
    */
   Object checkTaskId;

   private Class clientClass;

   /**
    * The thread group that will hold al the threads that
    * are servicing this protocol's sockets.
    */
   public ThreadGroup threadGroup = new ThreadGroup("Client Sockets");

   /**
    * We check the connections here.
    */
   class CheckTask implements Runnable
   {
      public void run()
      {
         Iterator i = requestConnections.values().iterator();
         while (i.hasNext())
         {
            AbstractClient c = (AbstractClient) i.next();
            c.checkExpired(expirationPeriod);
         }
      }
   }

   /**
    * The thread group used by all the 
    */
   public static ThreadGroup oiThreadGroup = new ThreadGroup("Optimized Invoker");

   protected ConnectionManager()
   {
      clientClass = BlockingClient.class;
      
      // Try to use the NonBlockingClient if possible
      if( "true".equals( System.getProperty("org.jboss.invocation.trunk.enable_nbio", "true") ) ) {
         try
         {
            clientClass = Class.forName("org.jboss.invocation.trunk.client.nbio.NonBlockingClient");
            log.debug("Using the Non Blocking version of the client");
         }
         catch (Throwable e)
         {
            if (log.isTraceEnabled())
               log.trace("Cannot used NBIO: " + e);
            log.debug("Using the Blocking version of the client");
         }
      }
      
      log.debug("Setting the clockDaemon's thread factory");
      clockDaemon.setThreadFactory(new ThreadFactory()
      {
         public Thread newThread(Runnable r)
         {
            Thread t = new Thread(oiThreadGroup, r, "OI Connection Cleaner");
            t.setDaemon(true);
            return t;
         }
      });

   }

   private void startCheckThread()
   {
      log.trace("Starting the Check Thread..");
      checkTaskId = clockDaemon.executePeriodically(this.checkPeriod, new CheckTask(), true);
   }

   private void stopCheckThread()
   {
      log.trace("Stopping the Check Thread..");
      clockDaemon.cancel(checkTaskId);
      clockDaemon.shutDown();
   }

   public static ConnectionManager getInstance()
   {
      return instance;
   }

   AbstractClient connect(ServerAddress serverAddress) throws IOException
   {

      boolean tracing = log.isTraceEnabled();
      /* most of the time this will find a connection */
      AbstractClient connection = (AbstractClient) requestConnections.get(serverAddress);
      if (connection != null)
      {
         if (tracing)
            log.trace("Allready connected to that server, Reusing connection: " + connection);
         return connection;
      }

      synchronized (requestConnections)
      {

         connection = (AbstractClient) requestConnections.get(serverAddress);
         if (connection != null)
         {
            if (tracing)
               log.trace("Allready connected to that server, Reusing connection: " + connection);
            return connection;
         }

         if (tracing)
            log.trace("Establishing a new connection to: " + serverAddress);

         AbstractClient c = null;
         try
         {
            c = (AbstractClient) clientClass.newInstance();
         }
         catch (Throwable e)
         {
            throw new IOException("Client could not be initialized: " + e);
         }

         c.setConnectionManager(this);
         c.connect(serverAddress, threadGroup);
         c.start();

         if (tracing)
            log.trace("Connection established: " + c);

         if (requestConnections.size() == 0)
            startCheckThread();

         HashMap t = (HashMap) requestConnections.clone();
         t.put(serverAddress, c);
         requestConnections = t;
         return c;

      }

   }

   public void connectionClosed(AbstractClient connection, Exception reason)
   {
      if (log.isTraceEnabled())
         log.trace("Connection closed: " + connection, reason);
      // A connection was closed.. timeout or failure.
      // Remove form out map of connections.
      synchronized (requestConnections)
      {
         HashMap t = (HashMap) requestConnections.clone();
         t.remove(connection.getServerAddress());
         requestConnections = t;

         if (t.size() == 0)
            stopCheckThread();
      }
   }

   public void handleRequest(AbstractClient connection, TunkRequest request)
   {
   }

}
