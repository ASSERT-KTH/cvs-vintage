/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.jrmp.interfaces;

import java.lang.reflect.Method;
import java.rmi.MarshalledObject;

import javax.naming.Name;

import org.jboss.ejb.plugins.jrmp.server.JRMPContainerInvoker;

/**
 *	<description> 
 *      
 *	@see <related>
 *	@author Rickard Öberg (rickard.oberg@telkel.com)
 *	@version $Revision: 1.2 $
 */
public abstract class StatefulSessionProxy
   implements java.io.Serializable
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   protected String name;
   protected ContainerRemote container;
   protected Object id;
   protected long containerStartup = ContainerRemote.startup;
   
   protected boolean optimize = false;
   
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   public StatefulSessionProxy(String name, ContainerRemote container, Object id, boolean optimize)
   {
      this.name = name;
      this.container = container;
      this.id = id;
      this.optimize = optimize;
   }
   
   // Public --------------------------------------------------------

   // InvocationHandler implementation ------------------------------
   public Object invoke(Object proxy, Method m, Object[] args)
      throws Throwable
   {
      // Normalize args to always be an array
      // Isn't this a bug in the proxy call??
      if (args == null)
         args = new Object[0];

      // Optimize if calling another bean in same EJB-application
//      if (optimize &&
//          this.getClass().getClassLoader() == Thread.currentThread().getContextClassLoader())
//             return container.invoke(id, m, args, null, null);
                
      Object result = container.invoke(new MarshalledObject(new MethodInvocation(id, m, args)), null, null);
      if (result instanceof MarshalledObject)
         return ((MarshalledObject)result).get();
      else
         return result;
   }

   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
    
   // Private -------------------------------------------------------
   private void readObject(java.io.ObjectInputStream in)
      throws java.io.IOException, ClassNotFoundException
   {
      in.defaultReadObject();
      
      if (containerStartup == ContainerRemote.startup)
      {
         // VM-local optimization
         container = MethodInvocation.getLocal(name);;
      }
      
   }

   private boolean isLocal()
   {
      return containerStartup == ContainerRemote.startup;
   }

   // Inner classes -------------------------------------------------
}

