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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;

import javax.ejb.EntityBean;
import javax.ejb.CreateException;
import javax.ejb.FinderException;

import org.jboss.util.FastKey;
import org.jboss.ejb.Container;
import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.EntityInstanceCache;
import org.jboss.ejb.EntityPersistenceManager;
import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.logging.Logger;


/**
*	<description> 
*      
*	@see <related>
*	@author Rickard �berg (rickard.oberg@telkel.com)
*  @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
*	@version $Revision: 1.7 $
*/
public class BMPPersistenceManager
implements EntityPersistenceManager
{
	// Constants -----------------------------------------------------
	
	// Attributes ----------------------------------------------------
	EntityContainer con;
	
	Method ejbLoad;
	Method ejbStore;
	Method ejbActivate;
	Method ejbPassivate;
	Method ejbRemove;
	
	// Static --------------------------------------------------------
	
	// Constructors --------------------------------------------------
	
	// Public --------------------------------------------------------
	public void setContainer(Container c)
	{
		con = (EntityContainer)c;
	}
	
	public void init()
	throws Exception
	{
		ejbLoad = EntityBean.class.getMethod("ejbLoad", new Class[0]);
		ejbStore = EntityBean.class.getMethod("ejbStore", new Class[0]);
		ejbActivate = EntityBean.class.getMethod("ejbActivate", new Class[0]);
		ejbPassivate = EntityBean.class.getMethod("ejbPassivate", new Class[0]);
		ejbRemove = EntityBean.class.getMethod("ejbRemove", new Class[0]);
	}
	
	public void start()
	{
	}
	
	public void stop()
	{
	}
	
	public void destroy()
	{
	}
	
	public void createEntity(Method m, Object[] args, EntityEnterpriseContext ctx)
	throws RemoteException, CreateException
	{
		// Get methods
		try
		{
			Method createMethod = null;
			Method postCreateMethod = null;
			
			// try to get the create method
			try {
				createMethod = con.getBeanClass().getMethod("ejbCreate", m.getParameterTypes());
			} catch (NoSuchMethodException nsme) {
				throw new CreateException("corresponding ejbCreate not found " + parametersToString(m.getParameterTypes()) + nsme);
			}
			
			// try to get the post create method
			try {
				postCreateMethod = con.getBeanClass().getMethod("ejbPostCreate", m.getParameterTypes());
			} catch (NoSuchMethodException nsme) {
				throw new CreateException("corresponding ejbPostCreate not found " + parametersToString(m.getParameterTypes()) + nsme);
			}
			
			Object id = null;
			try {
				// Call ejbCreate
				id = createMethod.invoke(ctx.getInstance(), args);
			} catch (InvocationTargetException ite) {
				throw new CreateException("Create failed(could not call ejbCreate):"+ite);
			}
			
			// set the id
			ctx.setId(id);
			
			// Create a new CacheKey
			Object cacheKey = ((EntityInstanceCache) con.getInstanceCache()).createCacheKey( id );
			
			// Give it to the context
			ctx.setCacheKey(cacheKey);
			
			// Lock instance in cache
			((EntityInstanceCache) con.getInstanceCache()).insert(ctx);
			
			// Create EJBObject
			ctx.setEJBObject(con.getContainerInvoker().getEntityEJBObject(cacheKey));
			
			try {
				postCreateMethod.invoke(ctx.getInstance(), args);
			} catch (InvocationTargetException ite) {
				throw new CreateException("Create failed(could not call ejbPostCreate):" + ite);
			}
			
			//      } catch (InvocationTargetException e)
			//      {
			//         throw new CreateException("Create failed:"+e);
			//      } catch (NoSuchMethodException e)
			//      {
			//         throw new CreateException("Create methods not found:"+e);
		} catch (IllegalAccessException e)
		{
			throw new CreateException("Could not create entity:"+e);
		}
	}
	
	public Object findEntity(Method finderMethod, Object[] args, EntityEnterpriseContext ctx)
	   throws RemoteException, FinderException
	{
		// call the finder method
		Object objectId = callFinderMethod(finderMethod, args, ctx);
		
		// get the cache, create a new key and return this new key
		return ((EntityInstanceCache)con.getInstanceCache()).createCacheKey( objectId );
	}
	
	public Collection findEntities(Method finderMethod, Object[] args, EntityEnterpriseContext ctx)
   	throws RemoteException, FinderException
	{
		// call the finder method
		Object result = callFinderMethod(finderMethod, args, ctx);
		
		if (result == null) {
			// for EJB 1.0 compliance
			// if the bean couldn't find any matching entities
			// it returns null, so we return an empty collection
			return new ArrayList();
		}
		
		if (result instanceof java.util.Enumeration) {
			// to preserve 1.0 spec compatiblity
			ArrayList array = new ArrayList();
			Enumeration enum = (Enumeration) result;
			while (enum.hasMoreElements() == true) {
				// Wrap a cache key around the given object id/primary key
				array.add(((EntityInstanceCache) con.getInstanceCache()).createCacheKey(enum.nextElement()));
			}
			return array;
		} 
		else if (result instanceof java.util.Collection) {
			
			ArrayList array = new ArrayList(((Collection) result).size());
			Iterator enum =  ((Collection) result).iterator();
			while (enum.hasNext()) {
				// Wrap a cache key around the given object id/primary key
				array.add(((EntityInstanceCache) con.getInstanceCache()).createCacheKey(enum.next()));
			}
			return array;
		}
		else {
			// so we received something that's not valid
			// throw an exception reporting it
			throw new RemoteException("result of finder method is not a valid return type: " + result.getClass());
		}
	}
	
	public void activateEntity(EntityEnterpriseContext ctx)
	throws RemoteException
	{
		try
		{
			ejbActivate.invoke(ctx.getInstance(), new Object[0]);
		} catch (Exception e)
		{
			throw new ServerException("Activate failed", e);
		}
	}
	
	public void loadEntity(EntityEnterpriseContext ctx)
	throws RemoteException
	{
		try
		{
			ejbLoad.invoke(ctx.getInstance(), new Object[0]);
		} catch (Exception e)
		{
			throw new ServerException("Load failed", e);
		}
	}
	
	public void storeEntity(EntityEnterpriseContext ctx)
	throws RemoteException
	{
		//      Logger.log("Store entity");
		try
		{
			ejbStore.invoke(ctx.getInstance(), new Object[0]);
		} catch (Exception e)
		{
			throw new ServerException("Store failed", e);
		}
	}
	
	public void passivateEntity(EntityEnterpriseContext ctx)
	throws RemoteException
	{
		try
		{
			ejbPassivate.invoke(ctx.getInstance(), new Object[0]);
		} catch (Exception e)
		{
			throw new ServerException("Passivate failed", e);
		}
	}
	
	public void removeEntity(EntityEnterpriseContext ctx)
	throws RemoteException
	{
		try
		{
			ejbRemove.invoke(ctx.getInstance(), new Object[0]);
		} catch (Exception e)
		{
			throw new ServerException("Remove failed", e);
		}
	}
	// Z implementation ----------------------------------------------
	
	// Package protected ---------------------------------------------
	
	// Protected -----------------------------------------------------
	
	// Private -------------------------------------------------------
	private Object callFinderMethod(Method finderMethod, Object[] args, EntityEnterpriseContext ctx) 
      throws RemoteException, FinderException
   {
		// get the finder method
		Method callMethod = null;
		try {
			callMethod = getFinderMethod(con.getBeanClass(), finderMethod, args);
		} catch (NoSuchMethodException me) {
			// debug
			//Logger.exception(me);
			throw new RemoteException("couldn't find finder method in bean class. " + me.toString());
		}
		
		// invoke the finder method
		Object result = null;
		try {
			result = callMethod.invoke(ctx.getInstance(), args);
		} catch (InvocationTargetException e) {
        Throwable targetException = e.getTargetException();
        if (targetException instanceof FinderException) {
          throw (FinderException)targetException;
        }
        else {
          throw new ServerException("exception occured while invoking finder method", (Exception)targetException);
        }
      } catch (Exception e) {
			// debug
			// DEBUG Logger.exception(e);
			throw new ServerException("exception occured while invoking finder method",e);
		}
		
		return result;
	}
	
	private Method getFinderMethod(Class beanClass, Method finderMethod, Object[] args) throws NoSuchMethodException {
		String methodName = "ejbF" + finderMethod.getName().substring(1);
		return beanClass.getMethod(methodName, finderMethod.getParameterTypes());
	}
	
	private String parametersToString(Object []a) {
		String r = new String();
		for(int i=0;i<a.length;i++) r = r + ", " + a[i];
			return r;
	}
	
	// Inner classes -------------------------------------------------
}

