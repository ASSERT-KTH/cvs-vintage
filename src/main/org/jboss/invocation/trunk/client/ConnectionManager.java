/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.invocation.trunk.client;



import EDU.oswego.cs.dl.util.concurrent.ClockDaemon;
import EDU.oswego.cs.dl.util.concurrent.ThreadFactory;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import javax.resource.spi.work.WorkManager;
import org.jboss.invocation.trunk.client.bio.BlockingClient;
import org.jboss.invocation.ServerID;
import org.jboss.logging.Logger;

import org.jboss.system.ServiceMBeanSupport;
import javax.management.ObjectName;

/**
 * This is an mbeanthat lives on a client which is used to find existing 
 * connections that have been established to a server.
 * 
 * This class also powers a clean-up thread which shuts down clients that have been
 * been inactive for 1 min.
 * 
 * This class is the one that makes the decision if the client used
 * will be Blocking or Non Blocking.  (currently based on jre version)
 * 
 * @author <a href="mailto:hiram.chirino@jboss.org">Hiram Chirino</a>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version
 *
 * @jmx.mbean extends="org.jboss.system.ServiceMBean"
 */
public class ConnectionManager
   extends ServiceMBeanSupport
   implements ConnectionManagerMBean
{
   private final static Logger log = Logger.getLogger(ConnectionManager.class);

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


   private ObjectName workManagerName;

   private WorkManager workManager;

   /**
    * We check the connections here.
    */
   class CheckTask implements Runnable
   {
      public void run()
      {
      	 synchronized(requestConnections) {
			Iterator i = requestConnections.values().iterator();
			while (i.hasNext())
			{
			   AbstractClient c = (AbstractClient) i.next();
			   c.checkExpired(expirationPeriod);
			}
      	 }
      }
   }

   /**
    * The thread group used by all the 
    */
   public static ThreadGroup oiThreadGroup = new ThreadGroup("Optimized Invoker");

   public ConnectionManager()
   {
   }

   protected void startService() throws Exception
   {

      workManager = (WorkManager)getServer().getAttribute(workManagerName, "WorkManager");
     
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
            Thread t = new Thread(oiThreadGroup, r, "Connection Cleaner");
            t.setDaemon(true);
            return t;
         }
      });

   }

   protected void stopService() throws Exception
   {
      workManager = null;
   }


   /**
    * Get the WorkManagerName value.
    * @return the WorkManagerName value.
    *
    * @jmx.managed-attribute
    */
   public ObjectName getWorkManagerName() {
      return workManagerName;
   }

   /**
    * Set the WorkManagerName value.
    * @param newWorkManagerName The new WorkManagerName value.
    *
    * @jmx.managed-attribute
    */
   public void setWorkManagerName(ObjectName workManagerName) {
      this.workManagerName = workManagerName;
   }

     
   /**
    * Describe <code>getInstance</code> method here.
    *
    * @return a <code>ConnectionManager</code> value
    *
    * @jmx.managed-attribute
    */
   public ConnectionManager getConnectionManager()
   {
      return this;
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


   AbstractClient connect(ServerID serverID) throws IOException
   {

      boolean tracing = log.isTraceEnabled();
      synchronized (requestConnections)
      {

		 AbstractClient connection = (AbstractClient) requestConnections.get(serverID);
         if (connection != null)
         {
            if (tracing)
               log.trace("Allready connected to that server, Reusing connection: " + connection);
            return connection;
         }

         if (tracing)
            log.trace("Establishing a new connection to: " + serverID);

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
         c.setWorkManager(workManager);
         c.connect(serverID, threadGroup);
         c.start();

         if (tracing)
            log.trace("Connection established: " + c);

         if (requestConnections.size() == 0)
            startCheckThread();

		 requestConnections.put(serverID, c);
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
		 requestConnections.remove(connection.getServerID());
         if (requestConnections.size() == 0)
            stopCheckThread();
      }
   }

   public void handleRequest(AbstractClient connection, TrunkRequest request)
   {
   }

}
