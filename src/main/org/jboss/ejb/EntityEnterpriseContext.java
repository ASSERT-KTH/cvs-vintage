/*
* JBoss, the OpenSource EJB server
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.ejb;

import java.rmi.RemoteException;

import javax.ejb.EJBContext;
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
*	@version $Revision: 1.14 $
*/
public class EntityEnterpriseContext
extends EnterpriseContext
{
    // Attributes ----------------------------------------------------
    EJBObject ejbObject;
    EntityContext ctx;

    // True if this instance has been invoked since it was synchronized with DB
    // If true, then we have to store it to synch back to DB
    boolean invoked = false;

    // True if this instances' state is valid
    // when a bean is called the state is not synchronized with the DB
    // but "valid" as long as the transaction runs
    boolean valid = false;

    // The instance cache may attach any metadata it wishes to this context here
//    Object cacheCtx;

    // The persistence manager may attach any metadata it wishes to this context here
    Object persistenceCtx;

	//The cacheKey for this context
	CacheKey key;

    // Constructors --------------------------------------------------
    public EntityEnterpriseContext(Object instance, Container con)
    throws RemoteException
    {
       super(instance, con);
       ctx = new EntityContextImpl();
       ((EntityBean)instance).setEntityContext(ctx);
    }

    // Public --------------------------------------------------------

	public void clear() {

		super.clear();
		this.invoked = false;
		this.valid = false;
		key = null;
		persistenceCtx = null;
		ejbObject = null;
	}

    public void discard()
    throws RemoteException
    {
       ((EntityBean)instance).unsetEntityContext();
    }

    public EJBContext getEJBContext()
    {
        return ctx;
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

	public void setCacheKey(Object key) {
		this.key = (CacheKey) key;
	}

	public CacheKey getCacheKey() {
		return key;
	}

    public void setPersistenceContext(Object ctx)
    {
       this.persistenceCtx = ctx;
    }

    public Object getPersistenceContext()
    {
       return persistenceCtx;
    }
/*
    public void setCacheContext(Object ctx)
    {
       this.cacheCtx = ctx;
    }

    public Object getCacheContext()
    {
       return cacheCtx;
    }
*/
    public void setInvoked(boolean invoked)
    {

       this.invoked = invoked;
    }

    public boolean isInvoked()
    {
       return invoked;
    }

    public void setValid(boolean valid)
    {

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

			  // Create a new CacheKey
			  Object cacheKey = ((EntityCache) ((EntityContainer) con).getInstanceCache()).createCacheKey( id );

			  ejbObject = ((EntityContainer)con).getContainerInvoker().getEntityEJBObject(cacheKey);
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

