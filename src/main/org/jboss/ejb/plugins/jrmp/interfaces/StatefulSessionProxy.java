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
 *      <description> 
 *      
 *      @see <related>
 *      @author Rickard Öberg (rickard.oberg@telkel.com)
 *      @version $Revision: 1.6 $
 */
public abstract class StatefulSessionProxy
   extends GenericProxy
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   protected Object id;
   
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   public StatefulSessionProxy(String name, ContainerRemote container, Object id, boolean optimize)
   {
		super(name, container, optimize);
		
      this.id = id;
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
                
				
	System.out.println("In creating Home "+m.getDeclaringClass()+m.getName()+m.getParameterTypes().length);
	   
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

