/*
* jBoss, the OpenSource EJB server
*
* Distributable under GPL license.
* See terms of license at gnu.org.
*/
package org.jboss.ejb.plugins.jrmp.interfaces;

import java.lang.reflect.Method;

import java.rmi.MarshalledObject;
import javax.naming.InitialContext;
import javax.naming.Name;

import javax.ejb.EJBObject;
import javax.ejb.EJBHome;

import org.jboss.ejb.plugins.jrmp.server.JRMPContainerInvoker;

/**
*      <description> 
*      
*      @see <related>
*      @author Rickard Öberg (rickard.oberg@telkel.com)
* 	   @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
*      @version $Revision: 1.11 $
*/
public class StatelessSessionProxy
   extends GenericProxy
{
	// Constants -----------------------------------------------------
	
	// Attributes ----------------------------------------------------
	
	// Static --------------------------------------------------------
	
	static Method getPrimaryKey;
	static Method getHandle;
	static Method getEJBHome;
	static Method isIdentical;
	static Method toStr;
	static Method eq;
	static Method hash;
	
	static
	{
		try
		{
			// EJBObject methods
			getPrimaryKey = EJBObject.class.getMethod("getPrimaryKey", new Class[0]);
			getHandle = EJBObject.class.getMethod("getHandle", new Class[0]);
			getEJBHome = EJBObject.class.getMethod("getEJBHome", new Class[0]);
			isIdentical = EJBObject.class.getMethod("isIdentical", new Class[] { EJBObject.class });
			
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
   public StatelessSessionProxy()
   {
      // For externalization to work
   }
   
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
		
		// Implement local methods
		if (m.equals(toStr))
		{
			return name+":Stateless";
		}
		else if (m.equals(eq))
		{
			return invoke(proxy, isIdentical, args);
		}
		
		else if (m.equals(hash))
		{
			// We base the stateless hash on the hash of the proxy...
			// MF XXX: it could be that we want to return the hash of the name?
			return new Integer(this.hashCode());
		}
		
		// Implement local EJB calls
		else if (m.equals(getHandle))
		{
			return new StatelessHandleImpl(name);
		}
		
		else if (m.equals(getPrimaryKey))
		{
			
			// MF FIXME 
			// The spec says that SSB PrimaryKeys should not be returned and the call should throw an exception
			// However we need to expose the field *somehow* so we can check for "isIdentical"
			// For now we use a non-spec compliant implementation and just return the key as is
			// See jboss1.0 for the PKHolder and the hack to be spec-compliant and yet solve the problem
			
			// This should be the following call 
			//throw new RemoteException("Session Beans do not expose their keys, RTFS");
			
			// This is how it can be solved with a PKHolder (extends RemoteException)
			// throw new PKHolder("RTFS", name);
			
			// This is non-spec compliant but will do for now
			// We can consider the name of the container to be the primary key, since all stateless beans
			// are equal within a home
			return name;
		}
		
	
	   else if (m.equals(getEJBHome))
       { 
           return (EJBHome) new InitialContext().lookup(name);
       }
	   
		else if (m.equals(isIdentical))                 
		{
			// All stateless beans are identical within a home, if the names are equal we are equal
			return new Boolean(((EJBObject)args[0]).getPrimaryKey().equals(name));
		}
		
		// If not taken care of, go on and call the container
		else
		{
			// Delegate to container
			// Optimize if calling another bean in same EJB-application
			if (optimize && isLocal())
			{
				return container.invoke( // The entity id, method and arguments for the invocation
					null, m, args,
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
				return container.invoke(new MarshalledObject(rmi)).get();
			}
		}
	}


// Package protected ---------------------------------------------

// Protected -----------------------------------------------------

// Private -------------------------------------------------------

// Inner classes -------------------------------------------------
}
