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
import java.rmi.RemoteException;

import org.jboss.invocation.Invocation;
import org.jboss.invocation.Invoker;
import org.jboss.logging.Logger;

/**
 * This is the proxy object ot the TrunkInvoker that lives on the sever.
 * This object will use the ConnectionManager to create a Client to connect to the
 * server and then use that client to send Invocations to the server.
 *
 * @author <a href="mailto:hiram.chirino@jboss.org">Hiram Chirino</a>
 */
public final class TrunkInvokerProxy implements java.io.Serializable, Invoker
{
   private final static Logger log = Logger.getLogger(TrunkInvokerProxy.class);

   private ServerAddress serverAddress;
   private transient AbstractClient connection;

   public TrunkInvokerProxy(ServerAddress serverAddress)
   {
      this.serverAddress = serverAddress;
   }

   public String getServerHostName() throws Exception
   {
      TrunkRequest request = new TrunkRequest();
      request.setOpServerHostName();
      return (String) issue(request);
   }

   public Object invoke(Invocation invocation) throws Exception
   {
      TrunkRequest request = new TrunkRequest();
      request.setOpInvoke(invocation);
      return issue(request);
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
            connection = ConnectionManager.getInstance().connect(serverAddress);
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
