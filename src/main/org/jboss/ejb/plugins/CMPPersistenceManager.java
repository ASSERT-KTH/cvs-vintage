/*
* jBoss, the OpenSource EJB server
*
* Distributable under GPL license.
* See terms of license at gnu.org.
*/                           
package org.jboss.ejb.plugins;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.util.Collection;                                
import java.util.Iterator;
import java.util.ArrayList;
import java.util.HashMap;

import javax.ejb.EntityBean;
import javax.ejb.CreateException;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;
import javax.ejb.EJBException;

import javax.transaction.Transaction;
import javax.transaction.Status;

import org.jboss.ejb.Container;
import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.EntityPersistenceManager;
import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.EntityInstanceCache;
import org.jboss.ejb.EntityPersistenceStore;

/**
*   The CMP Persistence Manager implements the semantics of the CMP
*  EJB 1.1 call back specification. 
*
*  This Manager works with a "EntityPersistenceStore" that takes care of the 
*  physical storing of instances (JAWS, JDBC O/R, FILE, Object).
*      
*   @see <related>
*   @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
*   @version $Revision: 1.12 $
*/
public class CMPPersistenceManager
implements EntityPersistenceManager {
    // Constants -----------------------------------------------------
    
    // Attributes ----------------------------------------------------
    EntityContainer con;
    // Physical persistence implementation
    EntityPersistenceStore store;
    
    // The EJB Methods, the reason for this class
    Method ejbLoad;
    Method ejbStore;
    Method ejbActivate;
    Method ejbPassivate;
    Method ejbRemove;
    
    HashMap createMethods = new HashMap();
    HashMap postCreateMethods = new HashMap();

    // Static --------------------------------------------------------
    
    // Constructors --------------------------------------------------
    
    // Public --------------------------------------------------------
    public void setContainer(Container c)   {
        con = (EntityContainer)c;
        if (store != null) store.setContainer(c);
    }
    
    
    public void setPersistenceStore(EntityPersistenceStore store) {
        this.store= store;
        
        //Give it the container
        if (con!= null) store.setContainer(con);
    }
    
    public void init()
    throws Exception {
        
        // The common EJB methods
        ejbLoad = EntityBean.class.getMethod("ejbLoad", new Class[0]);
        ejbStore = EntityBean.class.getMethod("ejbStore", new Class[0]);
        ejbActivate = EntityBean.class.getMethod("ejbActivate", new Class[0]);
        ejbPassivate = EntityBean.class.getMethod("ejbPassivate", new Class[0]);
        ejbRemove = EntityBean.class.getMethod("ejbRemove", new Class[0]);
        
		// Create cache of create methods
	    Method[] methods = con.getHomeClass().getMethods();
	    for (int i = 0; i < methods.length; i++)
	    {
	   		if (methods[i].getName().equals("create"))
			{
				createMethods.put(methods[i], con.getBeanClass().getMethod("ejbCreate", methods[i].getParameterTypes()));
				postCreateMethods.put(methods[i], con.getBeanClass().getMethod("ejbPostCreate", methods[i].getParameterTypes()));
			}
	    }
	   
        // Initialize the store
        // if the store performes database operations (ie: table creations) it
        // will need a transaction to do so
        con.getTransactionManager ().begin ();
        try
        {
           store.init();
           con.getTransactionManager ().commit ();
        } 
        catch (Exception _e)
        {
           con.getTransactionManager ().rollback ();
           store.destroy ();
           throw _e;
        }
        
    }
    
    public void start() 
    throws Exception {
        
        store.start();
    }
    
    public void stop() {
        store.stop();
    }
    
    public void destroy() {

      // same as inistalize...
      // maybe the store needs to drop tables and he
      // will need a transaction therefor
      try
      {
         con.getTransactionManager ().begin ();
         store.destroy();
         if (con.getTransactionManager().getStatus() == Status.STATUS_ACTIVE)
             con.getTransactionManager ().commit ();
         else
            con.getTransactionManager ().rollback ();
      }
      catch (Exception _e)
      {
      }
    }
    
    public void createEntity(Method m, Object[] args, EntityEnterpriseContext ctx)
    throws Exception {
        // Get methods
        Method createMethod = (Method)createMethods.get(m);
    	Method postCreateMethod = (Method)postCreateMethods.get(m);
            
        // Call ejbCreate on the target bean
        try {
            
	        createMethod.invoke(ctx.getInstance(), args);
		} catch (IllegalAccessException e)
		{
			// Throw this as a bean exception...(?)
			throw new EJBException(e);
		} catch (InvocationTargetException ite) 
		{
		 	Throwable e = ite.getTargetException();
			if (e instanceof EJBException)
			{
				// Rethrow exception
				throw (EJBException)e;
			} else if (e instanceof RuntimeException)
			{
				// Wrap runtime exceptions
				throw new EJBException((Exception)e);
			} else if (e instanceof Exception)
			{
            // Remote, Create, or custom app. exception
			   throw (Exception)e;
			} else
			{
			   throw (Error)e;
			}
      }
                 
        // Have the store persist the new instance, the return is the key
        Object id = store.createEntity(m, args, ctx);
        
        // Set the key on the target context
        ctx.setId(id);
        
        // Create a new CacheKey
           Object cacheKey = ((EntityInstanceCache) con.getInstanceCache()).createCacheKey( id );
    
        // Give it to the context
        ctx.setCacheKey(cacheKey);
     
     	// insert instance in cache, it is safe
     	((EntityInstanceCache) con.getInstanceCache()).insert(ctx);
     
        // Create EJBObject
        ctx.setEJBObject(con.getContainerInvoker().getEntityEJBObject(cacheKey));
            
		try 
		{
		   postCreateMethod.invoke(ctx.getInstance(), args);
        } catch (IllegalAccessException e)
		{
			// Throw this as a bean exception...(?)
			throw new EJBException(e);
		} catch (InvocationTargetException ite) 
		{
		 	Throwable e = ite.getTargetException();
			if (e instanceof EJBException)
			{
				// Rethrow exception
				throw (EJBException)e;
			} else if (e instanceof RuntimeException)
			{
				// Wrap runtime exceptions
				throw new EJBException((Exception)e);
			} else if (e instanceof Exception)
			{
			   // Remote, Create, or custom app. exception
			   throw (Exception)e;
			} else
			{
			   throw (Error)e;
			}
      }
    }
    
    public Object findEntity(Method finderMethod, Object[] args, EntityEnterpriseContext ctx)
    throws Exception {
      
       // The store will find the entity and return the primaryKey
       Object id = store.findEntity(finderMethod, args, ctx);
       
       // We return the cache key
        return ((EntityInstanceCache) con.getInstanceCache()).createCacheKey(id);
    }
    
    public Collection findEntities(Method finderMethod, Object[] args, EntityEnterpriseContext ctx)
    throws Exception {

       // The store will find the id and return a collection of PrimaryKeys
       Collection ids = store.findEntities(finderMethod, args, ctx);
       
       // Build a collection of cacheKeys
       ArrayList list = new ArrayList(ids.size());
        Iterator idEnum = ids.iterator();
        while(idEnum.hasNext()) {
         
         // Get a cache key for it
         list.add(((EntityInstanceCache) con.getInstanceCache()).createCacheKey(idEnum.next()));
         }
        
       return list;      
    }
    
    /*
    * activateEntity(EnterpriseContext ctx) 
    *
    * The method calls the target beans for spec compliant callbacks.
    * Since these are pure EJB calls it is not obvious that the store should 
    * expose the interfaces.  In case of jaws however we found that store specific
    * contexts could be set in the activateEntity calls and hence a propagation of 
    * the call made sense.  The persistence store is called for "extension" purposes.
    *
    * @see activateEntity on EntityPersistenceStore.java
    */
    public void activateEntity(EntityEnterpriseContext ctx)
    throws RemoteException {
        
        // Call bean
        try
        {
            ejbActivate.invoke(ctx.getInstance(), new Object[0]);
        } catch (IllegalAccessException e)
        {
        	// Throw this as a bean exception...(?)
        	throw new EJBException(e);
        } catch (InvocationTargetException ite) 
        {
        	Throwable e = ite.getTargetException();
        	if (e instanceof RemoteException)
        	{
        		// Rethrow exception
        		throw (RemoteException)e;
        	} else if (e instanceof EJBException)
        	{
        		// Rethrow exception
        		throw (EJBException)e;
        	} else if (e instanceof RuntimeException)
        	{
        		// Wrap runtime exceptions
        		throw new EJBException((Exception)e);
        	}
        }
        
		store.activateEntity(ctx);
    }
    
    public void loadEntity(EntityEnterpriseContext ctx)
    throws RemoteException {
        
        // Have the store load the fields of the instance
        store.loadEntity(ctx);
        
        try {
            
            // Call ejbLoad on bean instance, wake up!
            ejbLoad.invoke(ctx.getInstance(), new Object[0]);

        } catch (IllegalAccessException e)
        {
        	// Throw this as a bean exception...(?)
        	throw new EJBException(e);
        } catch (InvocationTargetException ite) 
        {
        	Throwable e = ite.getTargetException();
        	if (e instanceof RemoteException)
        	{
        		// Rethrow exception
        		throw (RemoteException)e;
        	} else if (e instanceof EJBException)
        	{
        		// Rethrow exception
        		throw (EJBException)e;
        	} else if (e instanceof RuntimeException)
        	{
        		// Wrap runtime exceptions
        		throw new EJBException((Exception)e);
        	}
        }
    }
    
    public void storeEntity(EntityEnterpriseContext ctx)
    throws RemoteException {
        //      Logger.debug("Store entity");
        try {
            
            // Prepare the instance for storage
            ejbStore.invoke(ctx.getInstance(), new Object[0]);
        } catch (IllegalAccessException e)
        {
        	// Throw this as a bean exception...(?)
        	throw new EJBException(e);
        } catch (InvocationTargetException ite) 
        {
        	Throwable e = ite.getTargetException();
        	if (e instanceof RemoteException)
        	{
        		// Rethrow exception
        		throw (RemoteException)e;
        	} else if (e instanceof EJBException)
        	{
        		// Rethrow exception
        		throw (EJBException)e;
        	} else if (e instanceof RuntimeException)
        	{
        		// Wrap runtime exceptions
        		throw new EJBException((Exception)e);
        	}
        }
            
        // Have the store deal with storing the fields of the instance
        store.storeEntity(ctx);

    }
    
    public void passivateEntity(EntityEnterpriseContext ctx)
    throws RemoteException {
        
        try {
            
            // Prepare the instance for passivation 
            ejbPassivate.invoke(ctx.getInstance(), new Object[0]);
        } catch (IllegalAccessException e)
        {
        	// Throw this as a bean exception...(?)
        	throw new EJBException(e);
        } catch (InvocationTargetException ite) 
        {
        	Throwable e = ite.getTargetException();
        	if (e instanceof RemoteException)
        	{
        		// Rethrow exception
        		throw (RemoteException)e;
        	} else if (e instanceof EJBException)
        	{
        		// Rethrow exception
        		throw (EJBException)e;
        	} else if (e instanceof RuntimeException)
        	{
        		// Wrap runtime exceptions
        		throw new EJBException((Exception)e);
        	}
        }
        
        store.passivateEntity(ctx);
    }
    
    public void removeEntity(EntityEnterpriseContext ctx)
    throws RemoteException, RemoveException {
        
        try {

            // Call ejbRemove
            ejbRemove.invoke(ctx.getInstance(), new Object[0]);
        } catch (IllegalAccessException e)
        {
        	// Throw this as a bean exception...(?)
        	throw new EJBException(e);
        } catch (InvocationTargetException ite) 
        {
        	Throwable e = ite.getTargetException();
        	if (e instanceof RemoteException)
        	{
        		// Rethrow exception
        		throw (RemoteException)e;
        	} else if (e instanceof EJBException)
        	{
        		// Rethrow exception
        		throw (EJBException)e;
        	} else if (e instanceof RuntimeException)
        	{
        		// Wrap runtime exceptions
        		throw new EJBException((Exception)e);
        	}
        }
        
        store.removeEntity(ctx);
    }

    // Z implementation ----------------------------------------------
    
    // Package protected ---------------------------------------------
    
    // Protected -----------------------------------------------------
    
    // Private -------------------------------------------------------
    
    // Inner classes -------------------------------------------------
}

