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
import javax.resource.spi.work.WorkManager;
import org.jboss.invocation.ServerID;
import org.jboss.logging.Logger;

/**
 * The base class for client connections to the server.  This class is sub-classed to provide
 * Blocking and Non Blocking client implemenations.
 * 
 * This class allows you to assign ITrunkListeners to receive Invocations from the server.
 * It also keeps track of the when the connection was last used so that a connection can 
 * be closed after a long period of inactivity.
 * 
 * @author    <a href="mailto:hiram.chirino@jboss.org">Hiram Chirino</a>
 */
public abstract class AbstractClient implements ITrunkListener
{
   private final static Logger log = Logger.getLogger(AbstractClient.class);

   /** 
    * The connection manager that is pooling
    * this connection.
    */
   ConnectionManager connectionManager;

   /**
    * Used to determine when a connection should be closed
    * due to inactivity.
    */
   private long lastUsed = System.currentTimeMillis();
   private int keepOpenRequests = 0;
   private boolean isValid = true;

   /**
    * Clients can setup Request listeners.
    */
   HashMap requestListeners = new HashMap();
   int requestListenerCounter = 0;

   protected WorkManager workManager;

   synchronized public void keepOpen(boolean enabled)
   {
      keepOpenRequests = enabled ? keepOpenRequests + 1 : keepOpenRequests - 1;
   }

   boolean isValid()
   {
      return isValid;
   }

   synchronized void checkExpired(long expirationPeriod)
   {
      if (keepOpenRequests > 0)
         return;
      if ((lastUsed + expirationPeriod) > System.currentTimeMillis())
         return;

      // The connection has expired.. close it up.
      stop();
      isValid = false;
      connectionManager.connectionClosed(this, new Exception("This connection has expired due to inactivity."));
   }

   public void exceptionEvent(ICommTrunk trunk, Exception e)
   {
      stop();
      isValid = false;
      connectionManager.connectionClosed(this, e);
      Iterator i = requestListeners.values().iterator();
      while (i.hasNext())
      {
         ITrunkListener rl = (ITrunkListener) i.next();
         rl.exceptionEvent(trunk, e);
      }
   }

   /**
    * The <code>requestEvent</code> method looks up the correct trunk
    * listener from the table and forwards the request to it.  Where
    * does the id come from?
    *
    * @param trunk an <code>ICommTrunk</code> value
    * @param request a <code>TrunkRequest</code> value
    */
   public void requestEvent(ICommTrunk trunk, TrunkRequest request)
   {
      if (log.isTraceEnabled()) {
	 log.trace("requestEvent, trunk: " + trunk + ", request " + request);
      } // end of if ()
      
      //currently this does nothing
      connectionManager.handleRequest(this, request);
      lastUsed = System.currentTimeMillis();

      Integer rlID = (Integer) request.invocation.getObjectName();
      if (rlID == null)
      {
         log.debug("ObjectName not set in the Invocation.");
         return;
      }
      ITrunkListener rl = (ITrunkListener) requestListeners.get(rlID);
      rl.requestEvent(trunk, request);
   }

   public TrunkResponse synchRequest(TrunkRequest request)
      throws IOException, InterruptedException, ClassNotFoundException
   {
      lastUsed = System.currentTimeMillis();
      try
      {
         return getCommTrunk().getCommTrunkRamp().synchRequest(request);
      }
      catch (IOException e)
      {
         exceptionEvent(getCommTrunk(), e);
         throw e;
      }
      catch (InterruptedException e)
      {
         exceptionEvent(getCommTrunk(), e);
         throw e;
      }
      catch (ClassNotFoundException e)
      {
         exceptionEvent(getCommTrunk(), e);
         throw e;
      }
      finally
      {
         lastUsed = System.currentTimeMillis();
      }
   }

   public void sendResponse(TrunkResponse response) throws IOException
   {
      lastUsed = System.currentTimeMillis();
      try
      {
         getCommTrunk().sendResponse(response);
      }
      catch (IOException e)
      {
         exceptionEvent(getCommTrunk(), e);
         throw e;
      }
      finally
      {
         lastUsed = System.currentTimeMillis();
      }
   }

   public Integer addRequestListener(ITrunkListener rl)
   {
      Integer requestListenerID = new Integer(requestListenerCounter++);
      synchronized (requestListeners)
      {
         HashMap t = (HashMap) requestListeners.clone();
         t.put(requestListenerID, rl);
         requestListeners = t;
      }
      log.debug("added request listener: "  + rl + " at id " + requestListenerID);
      return requestListenerID;
   }

   public void removeRequestListener(Integer requestListenerID)
   {
      log.debug("removing request listener: at id " + requestListenerID);
      synchronized (requestListeners)
      {
         HashMap t = (HashMap) requestListeners.clone();
         t.remove(requestListenerID);
         requestListeners = t;
      }
   }

   public ConnectionManager getConnectionManager()
   {
      return connectionManager;
   }

   public void setConnectionManager(ConnectionManager connectionManager)
   {
      this.connectionManager = connectionManager;
   }

   
   
   /**
    * mbean get-set pair for field workManager
    * Get the value of workManager
    * @return value of workManager
    *
    * @jmx:managed-attribute
    */
   public WorkManager getWorkManager()
   {
      return workManager;
   }
   
   
   /**
    * Set the value of workManager
    * @param workManager  Value to assign to workManager
    *
    * @jmx:managed-attribute
    */
   public void setWorkManager(WorkManager workManager)
   {
      this.workManager = workManager;
   }
   
   


   /**
    * Established a connection to the server.
    */
   abstract public void connect(ServerID serverID, ThreadGroup threadGroup) throws IOException;

   /**
    * Used to start the current connection with the server
    */
   abstract public void start();

   /**
    * Used to stop the current connection with the server
    */
   abstract public void stop();

   /**
    * Used to get the comm trunk that is used by this connection.
    */
   abstract protected ICommTrunk getCommTrunk();

   /**
    * Used to get the comm trunk that is used by this connection.
    */
   abstract public ServerID getServerID();

}
