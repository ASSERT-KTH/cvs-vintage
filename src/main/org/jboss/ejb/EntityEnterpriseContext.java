/*
* jBoss, the OpenSource EJB server
*
* Distributable under GPL license.
* See terms of license at gnu.org.
*/
package org.jboss.ejb;

import java.rmi.RemoteException;

import javax.ejb.EJBHome;
import javax.ejb.EJBObject;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;

import javax.transaction.Transaction;

/**
*	The EntityEnterpriseContext is used to associate EntityBean instances with metadata about it.
*      
*	@see EnterpriseContext
*	@author Rickard Öberg (rickard.oberg@telkel.com)
*   @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
*	@version $Revision: 1.5 $
*/
public class EntityEnterpriseContext
extends EnterpriseContext
{
	// Attributes ----------------------------------------------------
	EJBObject ejbObject;
	
	// True if this instance has been invoked since it was synchronized with DB
	// If true, then we have to store it to synch back to DB
	boolean invoked = false;
	
	// True if this instances' state is valid 
	// when a bean is called the state is not synchronized with the DB
	// but "valid" as long as the transaction runs
	boolean valid = false;
	
	// The instance cache may attach any metadata it wishes to this context here
	Object cacheCtx;
	
	// The persistence manager may attach any metadata it wishes to this context here
	Object persistenceCtx;
	
	// Constructors --------------------------------------------------
	public EntityEnterpriseContext(Object instance, Container con)
	throws RemoteException
	{
		super(instance, con);
		((EntityBean)instance).setEntityContext(new EntityContextImpl());
	}
	
	// Public --------------------------------------------------------
	public void discard()
	throws RemoteException
	{
		((EntityBean)instance).unsetEntityContext();
	}
	
	public void setEJBObject(EJBObject eo) 
	{ 
		ejbObject = eo; 
	}
	
	public EJBObject getEJBObject() 
	{ 
		// Context can have no EJBObject (created by finds) in which case we need to wire it at call time
		
		return ejbObject; 
	}
	
	
	public void setPersistenceContext(Object ctx) 
	{ 
		this.persistenceCtx = ctx; 
	}
	
	public Object getPersistenceContext() 
	{ 
		return persistenceCtx; 
	}
	
	public void setCacheContext(Object ctx) 
	{ 
		this.cacheCtx = ctx; 
	}
	
	public Object getCacheContext() 
	{ 
		return cacheCtx; 
	}
	
	public void setInvoked(boolean invoked) 
	{ 
		/*
		System.out.println("&&&&&&&&&&& in setInvoked("+invoked+")");
		Exception e = new Exception();
		e.printStackTrace();
		*/
		this.invoked = invoked; 
	}
	
	public boolean isInvoked() 
	{ 
		return invoked; 
	}
	
	public void setValid(boolean valid) 
	{ 
		/*System.out.println("&&&&&&&&&&& in setSynchronized("+synched+")");
		Exception e = new Exception();
		e.printStackTrace();
		*/
		this.valid = valid; 
	}
	
	public boolean isValid() 
	{ 
		return valid; 
	}
	
	// Inner classes -------------------------------------------------
	protected class EntityContextImpl
	extends EJBContextImpl
	implements EntityContext
	{
		public EJBObject getEJBObject()
		{
			if (ejbObject == null) {
			
				try {
					
					ejbObject = ((EntityContainer)con).getContainerInvoker().getEntityEJBObject(id); 
				}
			 	catch (RemoteException re) {
					// ...
					throw new IllegalStateException();
				}
			}
			
			return ejbObject;
		}
		
		public Object getPrimaryKey()
		{
			return id;
		}
	}
}

