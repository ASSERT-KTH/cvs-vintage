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
 *      <description> 
 *      
 *      @see <related>
 *      @author Rickard Öberg (rickard.oberg@telkel.com)
 *      @version $Revision: 1.6 $
 */
public class StatelessSessionProxy
   extends GenericProxy
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   
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
		super(name, container, optimize);
   }
   
   // Public --------------------------------------------------------

   // InvocationHandler implementation ------------------------------
   public final Object invoke(Object proxy, Method m, Object[] args)
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
            return container.invoke(null, m, args, 
            								tm != null ? tm.getTransaction() : null,
            								null);
         } else
         {
	         RemoteMethodInvocation rmi = new RemoteMethodInvocation(null, m, args);
	         if (tm != null)
	            rmi.setTransaction(tm.getTransaction());
					
	         return container.invoke(new MarshalledObject(rmi));
         }
      }
   }

   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
    
   // Private -------------------------------------------------------
	  
	// Inner classes -------------------------------------------------
}
