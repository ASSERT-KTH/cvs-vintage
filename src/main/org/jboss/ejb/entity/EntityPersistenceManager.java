/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.entity;

import java.lang.reflect.Method;
import java.util.Collection;
import org.jboss.ejb.Container;
import org.jboss.ejb.ContainerPlugin;
import org.jboss.ejb.Interceptor;
import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationResponse;

/**
 * The EntityPersistenceManager is called by other plugins in the
 * container.  
 *
 * see EntityContainer
 * 
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.1 $
 */
public interface EntityPersistenceManager extends ContainerPlugin
{
   /**
    * Gets the root interceptor of the chain.
    * @return the fist interceptor in the stack
    */
   public Interceptor getInterceptor();

   /**
    * Helper method to easily add a new interceptor to the end of the chain.
    * @param newInterceptor the new interceptor
    */
   public void addInterceptor(Interceptor newInterceptor);
 
   /**
    * Invokes the persistence manager using the information in the invocation
    * object.  This should be the core method in the persistence manager.
    * All other methods should be considered helper methods.
    * @param invocation the context infomation of the invocation
    * @return the results of the invocation
    * @throws Exception if an exceptional problem occures during invocation
    */
   public InvocationResponse invoke(Invocation invocation) throws Exception;
 
   /**
    * Returns a new instance of the entity implementation class or a subclass.
    * <p>
    * NOTE: This should be considered a helper method and should delegate to 
    * the core invoke method. 
    * @return the new instance
    * @throws Exception if an exceptional problem occures during invocation
    */
   public Object createEntityInstance() throws Exception;

   /**
    * This method is called when an entity is to be created (between the 
    * ejbCreate and ejbPostCreate callbacks). The persistence manager doesn't
    * have to do anything but in the simplest cases this is where the enitity 
    * is initially added to the physical store.  This method must be called 
    * before postCreateEntity, or you will get an IllegalStateException.
    * <p>
    * NOTE: This should be considered a helper method and should delegate to 
    * the core invoke method. 
    *
    * @param method the create method in the home interface that was called
    * @param arguments the arguments passed to the create method
    * @param ctx the ctx that is being created
    * @return the primray key of the new entity
    * @throws Exception if an exceptional problem occures during invocation
    */
   public Object createEntity(
         Method method, 
         Object[] arguments, 
         EntityEnterpriseContext ctx) throws Exception;

   /**
    * This method is called after an entity is created (after the ejbCreate 
    * callback). The persistence manager doesn't have to do anything and 
    * normally doesn't.  This method can only be called after createEntity, or 
    * you will get an IllegalStateException from all other methods.
    * <p>
    * NOTE: This should be considered a helper method and should delegate to 
    * the core invoke method. 
    *
    * @param method the create method in the home interface that was called
    * @param arguments the arguments passed to the create method
    * @param ctx the ctx that is being created
    * @throws Exception if an exceptional problem occures during invocation
    */
   public void postCreateEntity(
         Method method, 
         Object[] arguments, 
         EntityEnterpriseContext ctx) throws Exception;
   
   /**
    * This method is called when an entity is to be created (after the 
    * ejbRemove callbacks). The persistence manager doesn't have to do 
    * anything but in the simplest cases this is where the enitity is 
    * finally remove from the physical store.
    * <p>
    * NOTE: This should be considered a helper method and should delegate to 
    * the core invoke method. 
    *
    * @param ctx the ctx that is being removed
    * @throws Exception if an exceptional problem occures during invocation
    */
   public void removeEntity(EntityEnterpriseContext ctx) throws Exception;

   /**
    * Executes a query. 
    * <p>
    * NOTE: This should be considered a helper method and should delegate to 
    * the core invoke method. 
    *
    * @param method the query method to invoke
    * @param arguments any quert parameters
    * @param ctx the ctx to use for the query
    * @return the results of the query 
    * @throws Exception if an exceptional problem occures during invocation
    */
   public Collection query(
         Method method, 
         Object[] arguments, 
         EntityEnterpriseContext ctx) throws Exception;

   /**
    * This method is used to determine if an entity has been modified within
    * the invocation context, which can be the current transaction or a single
    * invocation when there is no transaction
    * <p>
    * NOTE: This should be considered a helper method and should delegate to 
    * the core invoke method. 
    *
    * @param ctx the ctx to check
    * @return true if the entity has been modified; false otherwise
    * @throws Exception if an exceptional problem occures during invocation
    */
   public boolean isEntityModified(EntityEnterpriseContext ctx) throws Exception;

   /**
    * This method is called whenever an entity shall be load from the
    * underlying storage. The persistence manager must load the state from
    * the underlying storage and then call ejbLoad on the supplied instance.
    * <p>
    * NOTE: This should be considered a helper method and should delegate to 
    * the core invoke method. 
    *
    * @param ctx the ctx to synchronize
    * @throws Exception if an exceptional problem occures during invocation
    */
   public void loadEntity(EntityEnterpriseContext ctx) throws Exception;

   /**
    * This method is called whenever an entity shall be stored to the
    * underlying storage. The persistence manager must call ejbStore on the
    * supplied instance and then store the state to the underlying storage.
    * <p>
    * NOTE: This should be considered a helper method and should delegate to 
    * the core invoke method. 
    *
    * @param ctx the ctx to synchronize
    * @throws Exception if an exceptional problem occures during invocation
    */
   public void storeEntity(EntityEnterpriseContext ctx) throws Exception;

   /**
    * This method is called when an entity shall be activated. The persistence
    * manager must call the ejbActivate method on the instance.
    * <p>
    * NOTE: This should be considered a helper method and should delegate to 
    * the core invoke method. 
    *
    * @param ctx the ctx to use for the activation
    * @throws Exception if an exceptional problem occures during invocation
    */
   public void activateEntity(EntityEnterpriseContext ctx) throws Exception;

   /**
    * This method is called when an entity shall be passivate. The persistence
    * manager must call the ejbPassivate method on the ctx.
    * <p>
    * NOTE: This should be considered a helper method and should delegate to 
    * the core invoke method. 
    *
    * @param ctx the ctx to passivate
    * @throws Exception if an exceptional problem occures during invocation
    */
   public void passivateEntity(EntityEnterpriseContext ctx) throws Exception;
}

