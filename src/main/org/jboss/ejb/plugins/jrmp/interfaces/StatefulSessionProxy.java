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
 *      @version $Revision: 1.7 $
 */
public class StatefulSessionProxy
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
   public final Object invoke(Object proxy, Method m, Object[] args)
      throws Throwable
   {
	   // Normalize args to always be an array
	   // Isn't this a bug in the proxy call??
	   if (args == null)
	      args = new Object[0];
	   
	   // Delegate to container
	   // Optimize if calling another bean in same EJB-application
	   if (optimize && isLocal())
	   {
	      return container.invoke(id, m, args, 
	      								tm != null ? tm.getTransaction() : null,
	      								null);
	   } else
	   {
	      RemoteMethodInvocation rmi = new RemoteMethodInvocation(id, m, args);
	      if (tm != null)
	         rmi.setTransaction(tm.getTransaction());
	      return ((MarshalledObject)container.invoke(new MarshalledObject(rmi))).get();
	   }
   }

   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
    
   // Private -------------------------------------------------------
	
   // Inner classes -------------------------------------------------
}

