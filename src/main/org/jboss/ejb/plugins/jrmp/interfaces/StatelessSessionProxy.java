/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.jrmp.interfaces;

import java.lang.reflect.Method;

import java.rmi.MarshalledObject;

import javax.ejb.EJBObject;
import javax.naming.Name;

import org.jboss.ejb.plugins.jrmp.server.JRMPContainerInvoker;

/**
 *	<description> 
 *      
 *	@see <related>
 *	@author Rickard Öberg (rickard.oberg@telkel.com)
 *	@version $Revision: 1.2 $
 */
public abstract class StatelessSessionProxy
   implements java.io.Serializable
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   protected String name;
   protected ContainerRemote container;
   protected long containerStartup = ContainerRemote.startup;
   
   protected boolean optimize = false;
   
   // Static --------------------------------------------------------
   static Method getHandle;
   static Method toStr;
   
   static
   {
      try
      {
         getHandle = EJBObject.class.getMethod("getHandle", new Class[0]);
         toStr = Object.class.getMethod("toString", new Class[0]);
      } catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   // Constructors --------------------------------------------------
   public StatelessSessionProxy(String name, ContainerRemote container, boolean optimize)
   {
      this.name = name;
      this.container = container;
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
      
      if (m.equals(getHandle))
      {
         return new StatelessHandleImpl(name);
      }
      else if (m.equals(toStr))
      {
         return name;
      } else
      {
         // Delegate to container
         // Optimize if calling another bean in same EJB-application
         if (optimize && isLocal())
         {
            return container.invoke(null, m, args, null, null);
         }
                
         Object result = container.invoke(new MarshalledObject(new MethodInvocation(m, args)), null, null);
         if (result instanceof MarshalledObject)
            return ((MarshalledObject)result).get();
         else
            return result;
      }
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

