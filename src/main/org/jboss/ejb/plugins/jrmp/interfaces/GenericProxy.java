/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.jrmp.interfaces;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.rmi.server.RemoteObject;
import java.rmi.MarshalledObject;
import javax.ejb.EJBObject;
import javax.naming.Reference;
import javax.naming.BinaryRefAddr;
import javax.naming.Referenceable;
import javax.naming.Name;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import org.jboss.ejb.plugins.jrmp.server.JRMPContainerInvoker;

/**
 *      <description> 
 *      
 *      @see <related>
 *      @author Rickard Öberg (rickard.oberg@telkel.com)
 *      @version $Revision: 1.1 $
 */
public class GenericProxy
   implements java.io.Serializable
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   String name;
   ContainerRemote container;
   long containerStartup = ContainerRemote.startup;
   
   boolean optimize = false;
   
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   protected GenericProxy(String name, ContainerRemote container, boolean optimize)
   {
      this.name = name;
      this.container = container;
      this.optimize = optimize;
   }
   
   // Public --------------------------------------------------------

   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
   protected boolean isLocal()
   {
      return containerStartup == ContainerRemote.startup;
   }
    
   // Private -------------------------------------------------------
   private void readObject(java.io.ObjectInputStream in)
      throws IOException, ClassNotFoundException
   {
      in.defaultReadObject();
      
      if (isLocal())
      {
         // VM-local optimization; still follows RMI-semantics though
         container = MethodInvocation.getLocal(name);
      }
      
   }
   
   // Inner classes -------------------------------------------------
}

