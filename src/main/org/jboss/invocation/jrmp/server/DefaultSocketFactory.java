package org.jboss.invocation.jrmp.server;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.rmi.server.RMIServerSocketFactory;

/**
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 1.1 $
 */
public class DefaultSocketFactory
   implements RMIServerSocketFactory, Serializable
{
   private transient InetAddress bindAddress;

   public String getBindAddress()
   {
      String address = null;
      if( bindAddress != null )
         address = bindAddress.getHostAddress();
      return address;
   }
   public void setBindAddress(String host) throws UnknownHostException
   {
      bindAddress = InetAddress.getByName(host);
   }

    /**
     * Create a server socket on the specified port (port 0 indicates
     * an anonymous port).
     * @param  port the port number
     * @return the server socket on the specified port
     * @exception IOException if an I/O error occurs during server socket
     * creation
     * @since 1.2
     */
    public ServerSocket createServerSocket(int port) throws IOException
    {
        ServerSocket activeSocket = new ServerSocket(port, 50, bindAddress);
        return activeSocket;
    }

    public boolean equals(Object obj)
    {
        return obj instanceof DefaultSocketFactory;
    }
    public int hashCode()
    {
        return getClass().getName().hashCode();
    }
}
