/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.jrmp.interfaces;

import java.io.IOException;
import java.security.Principal;

import javax.transaction.TransactionManager;

import org.jboss.ejb.MethodInvocation;
import org.jboss.ejb.plugins.jrmp.server.JRMPContainerInvoker;
import org.jboss.tm.TxManager;

import java.util.HashMap;
import org.jboss.system.SecurityAssociation;


/**
 *      <description> 
 *      
 *      @see <related>
 *      @author Rickard Öberg (rickard.oberg@telkel.com)
 *      @version $Revision: 1.8 $
 */
public abstract class GenericProxy
   implements java.io.Externalizable
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   String name;
   ContainerRemote container;
   long containerStartup = ContainerRemote.startup;
   
   boolean optimize = false;
   
   // Static --------------------------------------------------------
   static TransactionManager tm;

   static HashMap invokers = new HashMap(); // Prevent DGC
   public static ContainerRemote getLocal(String jndiName) { return (ContainerRemote)invokers.get(jndiName); }
   public static void addLocal(String jndiName, ContainerRemote invoker) { invokers.put(jndiName, invoker); }
   public static void removeLocal(String jndiName) { invokers.remove(jndiName); }

	public static void setTransactionManager(TransactionManager txMan)
	{
		if (tm == null)
		   tm = txMan;
	}

  public Principal getPrincipal()
  {
    return SecurityAssociation.getPrincipal();
  }

  public Object getCredential()
  {
    return SecurityAssociation.getCredential();
  }

   // Constructors --------------------------------------------------
   public GenericProxy()
   {
      // For externalization to work
   }
   
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
    
   public void writeExternal(java.io.ObjectOutput out)
      throws IOException
   {
   	  out.writeUTF(name);
   	  out.writeObject(isLocal() ? container : null);
        out.writeLong(containerStartup);
        out.writeBoolean(optimize);
   }
   
   public void readExternal(java.io.ObjectInput in)
      throws IOException, ClassNotFoundException
   {
   	name = in.readUTF();
   	container = (ContainerRemote)in.readObject();
   	containerStartup = in.readLong();
   	optimize = in.readBoolean();
      
      if (isLocal())
      {
         // VM-local optimization; still follows RMI-semantics though
         container = getLocal(name);
      }
   }
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
}

