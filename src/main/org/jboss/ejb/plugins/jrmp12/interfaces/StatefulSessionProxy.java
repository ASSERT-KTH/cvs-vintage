/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.jrmp12.interfaces;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationHandler;
import java.rmi.MarshalledObject;

import javax.naming.Name;

/**
 *	<description> 
 *      
 *	@see <related>
 *	@author Rickard Öberg (rickard.oberg@telkel.com)
 *	@version $Revision: 1.1 $
 */
public class StatefulSessionProxy
   implements InvocationHandler, java.io.Serializable
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   String name;
   ContainerRemote container;
   Object id;
   
   boolean optimize = false;
   
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

   // Inner classes -------------------------------------------------
}

