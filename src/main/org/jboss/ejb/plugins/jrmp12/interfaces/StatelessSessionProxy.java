/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.jrmp12.interfaces;

import java.lang.reflect.Member;
import java.lang.reflect.Method;

import java.rmi.MarshalledObject;

import javax.ejb.EJBObject;
import javax.naming.Name;

import org.jboss.ejb.jrmp12.interfaces.proxy.InvocationHandler;
import org.jboss.ejb.jrmp12.server.JRMPContainerInvoker;

/**
 *	<description> 
 *      
 *	@see <related>
 *	@author Rickard Öberg (rickard.oberg@telkel.com)
 *	@version $Revision: 1.1 $
 */
public class StatelessSessionProxy
   implements InvocationHandler, java.io.Serializable
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   String name;
   ContainerRemote container;
   long containerStartup = ContainerRemote.startup;
   
   boolean optimize = false;
   
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
   public Object invoke(Object dummy, Method method, Object[] args)
      throws Throwable
   {
      Method m = (Method)method;
      
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

   public Class[] getTargetTypes()
   {
      return null;
   }
   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
    
   // Private -------------------------------------------------------
   private void readObject(java.io.ObjectInputStream in)
      throws java.io.IOException, ClassNotFoundException
   {
      in.defaultReadObject();
      
      // TODO: doesn't work yet
      
//      System.out.println("Optimize-check:"+containerStartup+"="+ContainerRemote.startup);
      
      if (containerStartup == ContainerRemote.startup)
      {
//         System.out.println("Optimize session locally");
         // VM-local optimization
         container = JRMPContainerInvoker.getLocal(name);
      }
      
   }

   private boolean isLocal()
   {
      return containerStartup == ContainerRemote.startup;
   }
   // Inner classes -------------------------------------------------
}

