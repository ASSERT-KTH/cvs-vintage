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
 *	@version $Revision: 1.2 $
 */
public class EntityEnterpriseContext
   extends EnterpriseContext
{
   // Attributes ----------------------------------------------------
   EJBObject ejbObject;
	
	// True if this instance has been invoked since it was synchronized with DB
	// If true, then we have to store it to synch back to DB
   boolean invoked = false;
	
	// True if this instances' state is synchronized with the DB
   boolean synched = false;
   
   // Set to the tx currently using this context. May be null
   Transaction tx;
	
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
		return ejbObject; 
	}
   
   public void setTransaction(Transaction tx) 
	{ 
		this.tx = tx; 
	}
	
   public Transaction getTransaction() 
	{ 
		return tx; 
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
		this.invoked = invoked; 
	}
	
   public boolean isInvoked() 
	{ 
		return invoked; 
	}

   public void setSynchronized(boolean synched) 
	{ 
		this.synched = synched; 
	}
	
   public boolean isSynchronized() 
	{ 
		return synched; 
	}
   
   // Inner classes -------------------------------------------------
   protected class EntityContextImpl
      extends EJBContextImpl
      implements EntityContext
   {
      public EJBObject getEJBObject()
      {
         return ejbObject;
      }
      
      public Object getPrimaryKey()
      {
         return id;
      }
   }
}

