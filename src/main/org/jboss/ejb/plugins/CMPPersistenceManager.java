/*
* JBoss, the OpenSource EJB server
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.ejb.plugins;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Field;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.util.Collection;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
import org.jboss.ejb.EntityCache;
import org.jboss.ejb.EntityPersistenceStore;
import org.jboss.metadata.EntityMetaData;

import org.jboss.util.FinderResults;
import org.jboss.util.Sync;

/**
*   The CMP Persistence Manager implements the semantics of the CMP
*  EJB 1.1 call back specification.
*
*  This Manager works with a "EntityPersistenceStore" that takes care of the
*  physical storing of instances (JAWS, JDBC O/R, FILE, Object).
*
*   @see <related>
*   @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
*   @version $Revision: 1.20 $
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

         if (con.getHomeClass() != null)
         {
            Method[] methods = con.getHomeClass().getMethods();
            createMethodCache( methods );
         }
         if (con.getLocalHomeClass() != null)
         {
            Method[] methods = con.getLocalHomeClass().getMethods();
            createMethodCache( methods );
         }     

       store.init();
    }
    
    private void createMethodCache( Method[] methods )
      throws NoSuchMethodException
    {
     // Create cache of create methods
      for (int i = 0; i < methods.length; i++)
      {
                  if (methods[i].getName().equals("create"))
                  {
                          createMethods.put(methods[i], con.getBeanClass().getMethod("ejbCreate", methods[i].getParameterTypes()));
                          postCreateMethods.put(methods[i], con.getBeanClass().getMethod("ejbPostCreate", methods[i].getParameterTypes()));
                  }
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
        store.destroy();
    }

    public void createEntity(Method m, Object[] args, EntityEnterpriseContext ctx)
    throws Exception {
        // Get methods
        Method createMethod = (Method)createMethods.get(m);
    	Method postCreateMethod = (Method)postCreateMethods.get(m);

    	// Reset all attributes to default value
    	// The EJB 1.1 specification is not entirely clear about this,
    	// the EJB 2.0 spec is, see page 169.
    	// Robustness is more important than raw speed for most server
    	// applications, and not resetting atrribute values result in
    	// *very* weird errors (old states re-appear in different instances and the
    	// developer thinks he's on drugs).

    		// first get cmp metadata of this entity
    		Object instance = ctx.getInstance();
    		Class ejbClass = instance.getClass();
    		Field cmpField;
    		Class cmpFieldType;
			for (Iterator i= ((EntityMetaData)ctx.getContainer().getBeanMetaData()).getCMPFields();
				i.hasNext();) {
				try {
					// get the field declaration
                    try{
                        cmpField = ejbClass.getField((String)i.next());
                        cmpFieldType = cmpField.getType();
                        // find the type of the field and reset it
                        // to the default value
                        if (cmpFieldType.equals(boolean.class))  {
                            cmpField.setBoolean(instance,false);
                        } else if (cmpFieldType.equals(byte.class))  {
                            cmpField.setByte(instance,(byte)0);
                        } else if (cmpFieldType.equals(int.class))  {
                            cmpField.setInt(instance,0);
                        } else if (cmpFieldType.equals(long.class))  {
                            cmpField.setLong(instance,0L);
                        } else if (cmpFieldType.equals(short.class))  {
                            cmpField.setShort(instance,(short)0);
                        } else if (cmpFieldType.equals(char.class))  {
                            cmpField.setChar(instance,'\u0000');
                        } else if (cmpFieldType.equals(double.class))  {
                            cmpField.setDouble(instance,0d);
                        } else if (cmpFieldType.equals(float.class))  {
                            cmpField.setFloat(instance,0f);

                        //} else if (... cmr collection in ejb2.0...) {
                        //	cmpField.set(instance,someNewCollection?);

                        } else  {
                            cmpField.set(instance,null);
                        }
                    } catch (NoSuchFieldException e){
                        // will be here with dependant value object's private attributes
                        // should not be a problem
                    }
				} catch (Exception e) {
					throw new EJBException(e);
				}
			}

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
        Object cacheKey = ((EntityCache) con.getInstanceCache()).createCacheKey( id );

        // Give it to the context
        ctx.setCacheKey(cacheKey);

     	// insert instance in cache, it is safe
     	con.getInstanceCache().insert(ctx);

        // Create EJBObject
        if (con.getContainerInvoker() != null)
         ctx.setEJBObject(con.getContainerInvoker().getEntityEJBObject(cacheKey));
        if (con.getLocalHomeClass() != null)
         ctx.setEJBLocalObject(con.getLocalContainerInvoker().getEntityEJBLocalObject(cacheKey));

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
       // For now only optimize fBPK
       if (finderMethod.getName().equals("findByPrimaryKey"))
       {
          Object key = ctx.getCacheKey();
          if (key == null)
          {
             key = ((EntityCache)con.getInstanceCache()).createCacheKey(args[0]);
          }
          if (con.getInstanceCache().isActive(key))
          {
             return key; // Object is active -> it exists -> no need to call finder
          }
       }

       // The store will find the entity and return the primaryKey
       Object id = store.findEntity(finderMethod, args, ctx);

       // We return the cache key
       return ((EntityCache) con.getInstanceCache()).createCacheKey(id);
    }

    public Collection findEntities(Method finderMethod, Object[] args, EntityEnterpriseContext ctx)
        throws Exception 
    {
       // The store will find the id and return a collection of PrimaryKeys
       FinderResults ids = store.findEntities(finderMethod, args, ctx);

       AbstractInstanceCache cache = (AbstractInstanceCache)con.getInstanceCache();
       Map contextMap = new HashMap();
       ArrayList keyList = new ArrayList();
       Iterator idEnum = ids.iterator();
       while(idEnum.hasNext()) {
          Object key = idEnum.next();
          Object cacheKey = ((EntityCache)cache).createCacheKey(key);
          keyList.add(cacheKey);
                   
          Sync mutex = (Sync)cache.getLock(cacheKey);
          try
          {
             mutex.acquire();

             // Get context
             ctx = (EntityEnterpriseContext)cache.get(cacheKey);
             // if ctx has a transaction, we skip it - either it's our Tx
             //    or we plain don't want to block here.
             if (ctx.getTransaction() == null) {
                contextMap.put(key, ctx);
             } 
          } catch (InterruptedException ignored) {
          } finally {
             mutex.release();
          }
       }
       
       ids.setEntityMap(contextMap);
       
       store.loadEntities(ids);

       return keyList;
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

        invokeLoad(ctx);
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
    
    protected void invokeLoad(EntityEnterpriseContext ctx) throws RemoteException {        
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

    // Z implementation ----------------------------------------------

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------
}

