/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.jrmp.interfaces;

import java.io.*;
import java.net.*;
import java.rmi.server.*;
import java.security.*;

import org.jboss.logging.Logger;

/**
 *	<b>NOT IN USE</b>?
 *
 *  @deprecated     This this class in use at all?
 *  
 *	@see <related>
 *	@author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 *	@version $Revision: 1.8 $
 */
public class SecureSocketFactory
   extends RMISocketFactory
   implements java.io.Serializable
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
    
   // Static --------------------------------------------------------
   static ThreadLocal principals = new ThreadLocal();
   
   static void setPrincipal(Principal p) { principals.set(p); }
   public static Principal getPrincipal() { return (Principal)principals.get(); }
   
   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------
   public ServerSocket createServerSocket(int port)
      throws IOException
   {
      return new AuthenticatedServerSocket(port);
   }

   public Socket createSocket(String host, int port)
      throws IOException
   {
      return new AuthenticatedSocket(host, port);
   }
   
   public boolean equals(Object obj)
   {
		System.out.println(obj instanceof SecureSocketFactory);
      return obj instanceof SecureSocketFactory;
   }
    
   public int hashCode()
   {
      return getClass().getName().hashCode();
   }
	
   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
    
   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
   class AuthenticatedServerSocket
      extends ServerSocket
   {
      AuthenticatedServerSocket(int port)
         throws IOException
      {
         super(port);
      }
      
      public Socket accept()
         throws IOException
      {
         Socket s = new SecureSocket();
         
         super.implAccept(s);
         
         return s;
      }
      
   }
   
   class SecureSocket
      extends Socket
   {
      public InputStream getInputStream()
         throws IOException
      {
         InputStream in = super.getInputStream();
         DataInputStream din = new DataInputStream(in);
         setPrincipal(new PrincipalImpl(din.readUTF()));
         
         Logger.debug("Connected user:"+getPrincipal());
         
         return in;
      }
   }
   
   class AuthenticatedSocket
      extends Socket
   {
      AuthenticatedSocket(String host, int port)
         throws IOException
      {
         super(host, port);
         System.out.println("Create socket to "+host+" "+port);
			
			try
			{
				throw new Exception();
			} catch (Exception e)
			{
				e.printStackTrace();
			}
         
         DataOutputStream out = new DataOutputStream(getOutputStream());
         out.writeUTF(System.getProperty("user.name"));
      }
   }
   
   static class PrincipalImpl
      implements Principal
   {
      String name;
      
      PrincipalImpl(String name)
      {
         this.name = name;
      }
      
      public String getName() { return name; }
      
      public int hashCode() { return name.hashCode(); }
      
      public boolean equals(Object obj)
      {
         try
         {
            return ((PrincipalImpl)obj).getName().equals(name);
         } catch (Exception e)
         {
            return false;
         }
      }
      
      public String toString() { return name; }
   }
}
