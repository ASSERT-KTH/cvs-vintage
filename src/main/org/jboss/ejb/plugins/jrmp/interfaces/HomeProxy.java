/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.jrmp.interfaces;

import javax.naming.Name;
import java.lang.reflect.Method;
import java.rmi.MarshalledObject;

import javax.ejb.EJBHome;
import javax.ejb.EJBObject;
import javax.ejb.Handle;
import javax.ejb.HomeHandle;

import org.jboss.ejb.plugins.jrmp.server.JRMPContainerInvoker;

/**
 *      <description> 
 *      
 *      @see <related>
 *      @author Rickard Öberg (rickard.oberg@telkel.com)
 *		@author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 *      @version $Revision: 1.11 $
 */
public class HomeProxy
   extends GenericProxy
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   
   // Static --------------------------------------------------------
   static Method getEJBMetaData;
   static Method getHomeHandle;
   static Method removeByHandle;
   static Method removeByPrimaryKey;
   static Method removeObject;
   static Method toStr;
   static Method eq;
   static Method hash;
   
   static
   {
      try
      {
         // EJB methods
		 getEJBMetaData = EJBHome.class.getMethod("getEJBMetaData", new Class[0]);
         getHomeHandle = EJBHome.class.getMethod("getHomeHandle", new Class[0]);
         removeByHandle = EJBHome.class.getMethod("remove", new Class[] {Handle.class});
		 removeByPrimaryKey = EJBHome.class.getMethod("remove", new Class[] {Object.class});
		 // Get the "remove" method from the EJBObject
		 removeObject = EJBObject.class.getMethod("remove", new Class[0]);
		 
		 // Object methods
	     toStr = Object.class.getMethod("toString", new Class[0]);
         eq = Object.class.getMethod("equals", new Class[] { Object.class });
	     hash = Object.class.getMethod("hashCode", new Class[0]);
      } catch (Exception e)
      {
         e.printStackTrace();
      }
   }


   // Constructors --------------------------------------------------
   public HomeProxy(String name, ContainerRemote container, boolean optimize)
   {
		super(name, container, optimize);
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
      
      // Implement local methods
      if (m.equals(toStr))
      {
         return name+"Home";
      }
      else if (m.equals(eq))
      {
		  // equality of the proxy home is based on names...
         return new Boolean(invoke(proxy,toStr, args).equals(name+"Home"));
      }
      
	  else if (m.equals(hash))
      {
      	return new Integer(this.hashCode());
      }
      
	  // Implement local EJB calls
	   else if (m.equals(getHomeHandle))
      {
         return new HomeHandleImpl(name);
      }
     
	 /* MF FIXME
	   We could implement the EJBMetaData on the client
	 
	  else if (m.equals(getEJBMetaData))
      {
         return id;
      }
	  */
	  
	  else if (m.equals(removeByHandle))
      {
		  // First get the EJBObject
		  EJBObject object = ((Handle) args[0]).getEJBObject();
		  
		  // remove the object from here
		  object.remove();
      	
		  // Return Void
		  return Void.TYPE;
	  }
	  
	  // The trick is simple we trick the container in believe it is a remove() on the instance
	  else if (m.equals(removeByPrimaryKey))
      {
		  if (optimize && isLocal())
	      {
	         return container.invoke(
			 						// The first argument is the id
									args[0], 
									// Pass the "removeMethod"
									removeObject, 
									// this is a remove() on the object
									new Object[0],
									// Tx stuff
									tm != null ? tm.getTransaction() : null,
									// Security attributes
									getPrincipal(), getCredential());
	      } else
	      {

			// Build a method invocation that carries the identity of the target object
		  	RemoteMethodInvocation rmi = new RemoteMethodInvocation(
		  														// The first argument is the id
																args[0], 
																// Pass the "removeMethod"
																removeObject, 
																// this is a remove() on the object
																new Object[0]);

			 // Set the transaction context
			 rmi.setTransaction(tm != null? tm.getTransaction() : null);
           	 
			 // Set the security stuff
			 // MF fixme this will need to use "thread local" and therefore same construct as above
			 // rmi.setPrincipal(sm != null? sm.getPrincipal() : null);
           	 // rmi.setCredential(sm != null? sm.getCredential() : null);
           	 // is the credential thread local? (don't think so... but...)
			 rmi.setPrincipal( getPrincipal() );
           	 rmi.setCredential( getCredential() );
			 
			 // Invoke on the remote server, enforce marshalling
	         return container.invokeHome(new MarshalledObject(rmi));
	      }
      }
      
	  // If not taken care of, go on and call the container
	  else
      {
	      // Delegate to container
	      // Optimize if calling another bean in same EJB-application
	      if (optimize && isLocal())
	      {
	         return container.invokeHome( // The method and arguments for the invocation
			 						      m, args,
									      // Transaction attributes
									      tm != null ? tm.getTransaction() : null,
									      // Security attributes
									      getPrincipal(), getCredential());
	      } else
	      {
			 // Create a new MethodInvocation for distribution
	         RemoteMethodInvocation rmi = new RemoteMethodInvocation(null, m, args);
	         
			 // Set the transaction context
			 rmi.setTransaction(tm != null? tm.getTransaction() : null);
           	 
			 // Set the security stuff
			 // MF fixme this will need to use "thread local" and therefore same construct as above
			 // rmi.setPrincipal(sm != null? sm.getPrincipal() : null);
           	 // rmi.setCredential(sm != null? sm.getCredential() : null);
           	 // is the credential thread local? (don't think so... but...)
			 rmi.setPrincipal( getPrincipal() );
           	 rmi.setCredential( getCredential() );
			 
			 // Invoke on the remote server, enforce marshalling
	         return container.invokeHome(new MarshalledObject(rmi));
	      }
      }
   }

   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
    
   // Private -------------------------------------------------------
        
   // Inner classes -------------------------------------------------
}
