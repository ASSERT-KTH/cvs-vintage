/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/
package org.jboss.invocation;

import java.io.Serializable;

/**
 * This class encapsulates all the required information for a client to 
 * establish a connection with the server.
 * 
 * It also attempts to provide a fast hash() function since this object
 * is used as a key in a hashmap mainted by the ConnectionManager. 
 *
 * @author <a href="mailto:hiram.chirino@jboss.org">Hiram Chirino</a>
 */
public class ServerID implements Serializable
{
   /**
    * Address of host ot connect to
    */
   public final String address;

   /**
    * Port the service is listening on
    */
   public final int port;

   /**
    * If the TcpNoDelay option should be used on the socket.
    */
   public final boolean enableTcpNoDelay;

   public final int timeoutMillis;

   /**
    * This object is used as a key in a hashmap,
    * so we precompute the hascode for faster lookups.
    */
   private final int hashCode;

   public ServerID(String address, int port, boolean enableTcpNoDelay, int timeoutMillis)
   {
      this.address = address;
      this.port = port;
      this.enableTcpNoDelay = enableTcpNoDelay;
      this.timeoutMillis = timeoutMillis;
      this.hashCode = address.hashCode() + port;
   }

   public String toObjectNameClause()
   {
      return "address="+ address + ",port=" + port;
   }

   public String toString()
   {
      return "[address:" + address + ",port:" + port 
	 + ",enableTcpNoDelay:" + enableTcpNoDelay + ",timeoutMillis:" + timeoutMillis + "]";
   }

   public boolean equals(Object obj)
   {
      if (obj == this) {
	 return true;
      } // end of if ()
      
      try
      {
         ServerID o = (ServerID) obj;
         if (o.hashCode != hashCode)
            return false;
         if (port != port)
            return false;
         if (!o.address.equals(address))
            return false;
         if (o.enableTcpNoDelay != enableTcpNoDelay)
            return false;
         if (o.timeoutMillis != timeoutMillis)
            return false;
         return true;
      }
      catch (Throwable e)
      {
         return false;
      }
   }

   public int hashCode()
   {
      return hashCode;
   }

}
