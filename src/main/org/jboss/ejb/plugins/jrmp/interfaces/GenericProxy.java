/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.jrmp.interfaces;

import java.io.IOException;

import javax.transaction.TransactionManager;

import org.jboss.ejb.MethodInvocation;
import org.jboss.ejb.plugins.jrmp.server.JRMPContainerInvoker;
import org.jboss.tm.TxManager;

import java.util.HashMap;

/**
 *      <description> 
 *      
 *      @see <related>
 *      @author Rickard Öberg (rickard.oberg@telkel.com)
 *      @version $Revision: 1.3 $
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
   static TransactionManager tm;
	
   static HashMap invokers = new HashMap(); // Prevent DGC
   public static ContainerRemote getLocal(String jndiName) { return (ContainerRemote)invokers.get(jndiName); }
   public static void addLocal(String jndiName, ContainerRemote invoker) { invokers.put(jndiName, invoker); }
   public static void removeLocal(String jndiName) { invokers.remove(jndiName); }
	
	public static void setTransactionManager(TransactionManager txMan)
	{
		tm = txMan;
	}

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
   private void writeObject(java.io.ObjectOutputStream out)
      throws IOException
   {
      if (!isLocal())
      {
			ContainerRemote old = container;
         container = null;
	      out.defaultWriteObject();
	      container = old;
      } else
      {
	      out.defaultWriteObject();
      }
   }
	
   private void readObject(java.io.ObjectInputStream in)
      throws IOException, ClassNotFoundException
   {
      in.defaultReadObject();
      
      if (isLocal())
      {
         // VM-local optimization; still follows RMI-semantics though
         container = getLocal(name);
      }
   }
   
   // Inner classes -------------------------------------------------
}

