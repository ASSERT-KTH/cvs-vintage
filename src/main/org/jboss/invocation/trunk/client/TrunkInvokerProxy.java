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
import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import java.rmi.RemoteException;
import javax.management.ObjectName;
import javax.transaction.Transaction;
import javax.transaction.xa.XAResource;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.Invoker;
import org.jboss.logging.Logger;
import org.jboss.proxy.ProxyXAResource;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.invocation.ServerID;

/**
 * This is the proxy object of the TrunkInvoker that lives on the server.
 * This object will use the ConnectionManager to create a Client to connect to the
 * server and then use that client to send Invocations to the server.
 *
 * @author <a href="mailto:hiram.chirino@jboss.org">Hiram Chirino</a>
 *
 * @jmx.mbean extends="org.jboss.system.ServiceMBean"
 */
public final class TrunkInvokerProxy 
   extends ServiceMBeanSupport
   implements java.io.Serializable, Invoker, TrunkInvokerProxyMBean
{
   private final static Logger log = Logger.getLogger(TrunkInvokerProxy.class);

   static final int DEFAULT_TX_TIMEOUT = 6;//seconds?

   private ServerID serverID;
   private transient AbstractClient connection;

   private ObjectName connectionManagerName;

   private ObjectName xaResourceFactoryName;

   private transient ProxyXAResource proxyXAResource;

   private transient ConnectionManager connectionManager;

   public TrunkInvokerProxy(ServerID serverID)
   {
      this.serverID = serverID;
   }

   //this is how it sets itself up in a new vm:

   /**
    * The <code>readResolve</code> method uses the ClientSetup class
    * to create an mbean server if necessary and set up the needed
    * mbeans.  If an mbean with the correct name is already
    * registered, it is returned instead of this instance.  Thus the
    * TrunkInvokerProxy is a singleton.
    *
    * @return an <code>Object</code> value
    * @exception ObjectStreamException if an error occurs
    */
   private Object readResolve() throws ObjectStreamException
   {
      try
      {
         return ClientSetup.setUpClient(this);
      }
      catch (Exception e)
      {
         getLog().fatal("Could not set up mbean server or mbeans in client", e);
         throw new InvalidObjectException("Problem setting up mbean server or mbeans in client: " + e);
      }
   }

   public ServerID getServerID()
   {
      return serverID;
   }

   /**
    * Get this instance.
    * @return the This value.
    *
    * @jmx.managed-attribute
    */
   public TrunkInvokerProxy getTrunkInvokerProxy() {
      return this;
   }
   
   /**
    * Get the XaResourceFactoryName value.
    * @return the XaResourceFactoryName value.
    *
    * @jmx.managed-attribute
    */
   public ObjectName getXAResourceFactoryName() {
      return xaResourceFactoryName;
   }

   /**
    * Set the xaResourceFactoryName value.
    * @param newXaResourceFactoryName The new XaResourceFactoryName value.
    *
    * @jmx.managed-attribute
    */
   public void setXAResourceFactoryName(ObjectName xaResourceFactoryName) {
      this.xaResourceFactoryName = xaResourceFactoryName;
   }

   
   /**
    * Get the ConnectionManagerName value.
    * @return the ConnectionManagerName value.
    *
    * @jmx.managed-attribute
    */
   public ObjectName getConnectionManagerName() {
      return connectionManagerName;
   }

   /**
    * Set the ConnectionManagerName value.
    * @param newConnectionManagerName The new ConnectionManagerName value.
    *
    * @jmx.managed-attribute
    */
   public void setConnectionManagerName(ObjectName connectionManagerName) {
      this.connectionManagerName = connectionManagerName;
   }


   protected void startService() throws Exception
   {
      connectionManager = (ConnectionManager)getServer().getAttribute(connectionManagerName, "ConnectionManager");
      proxyXAResource = (ProxyXAResource)getServer().getAttribute(xaResourceFactoryName, "XAResource");
   }

   protected void stopService() throws Exception
   {
      connectionManager = null;
      proxyXAResource = null;
   }


   /**
    * The <code>invoke</code> method sends the invocation over the
    * wire. The tx conversion should be in an invoker
    * interceptor/aspect.
    *
    * @param invocation an <code>Invocation</code> value
    * @return an <code>Object</code> value
    * @exception Exception if an error occurs
    */
   public Object invoke(Invocation invocation) throws Exception
   {
      boolean trace = log.isTraceEnabled();
      if (trace) {
	 log.trace("Invoking, invocation: " + invocation);
      } // end of if ()
      

      Transaction tx = invocation.getTransaction();
      if (tx == null) 
      {
         TrunkRequest request = new TrunkRequest();
         request.setOpInvoke(invocation);
	 if (trace) {
	    log.trace("No tx, request: " + request);
	 }
         return issue(request);
      }
      else
      {
	 proxyXAResource.setInvocation(invocation);
         tx.enlistResource(proxyXAResource);
	 //dont' try to send the tx
	 invocation.setTransaction(null);
         TrunkRequest request = new TrunkRequest();
	 if (trace) {
	    log.trace("Tx found. request: " + request);
	 }
         request.setOpInvoke(invocation);
         try 
         {
            return issue(request);            
         }
         finally
         {
	    if (trace) {
	       log.trace("Returned from invocation");
	    }
	    //restore the tx.
	    invocation.setTransaction(tx);
            tx.delistResource(proxyXAResource, XAResource.TMSUSPEND);
         } // end of try-catch
      } // end of else
      
   }

   public Object issue(TrunkRequest request) throws Exception
   {
      checkConnection();
      TrunkResponse response;
      try
      {
         response = connection.synchRequest(request);
      }
      catch (IOException e)
      {
         throw new RemoteException("Connection to the server failed.", e);
      }
      return response.evalThrowsException();
   }

   public void sendResponse(TrunkResponse response) throws IOException
   {
      connection.sendResponse(response);
   }

   protected void checkConnection() throws RemoteException
   {
      if (connection == null || !connection.isValid())
      {
         try
         {
            connection = connectionManager.connect(serverID);
            if (log.isTraceEnabled())
               log.trace("I will use this connection for requests: " + connection);
         }
         catch (IOException e)
         {
            throw new RemoteException("Could not establish a connection to the server.", e);
         }
      }
   }

   public Integer addRequestListener(ITrunkListener rl) throws RemoteException
   {
      checkConnection();
      return connection.addRequestListener(rl);
   }

   public void removeRequestListener(Integer requestListenerID) throws RemoteException
   {
      checkConnection();
      connection.removeRequestListener(requestListenerID);
   }
}
