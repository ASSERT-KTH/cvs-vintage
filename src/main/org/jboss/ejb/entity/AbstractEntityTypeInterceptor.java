/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.entity;

import org.jboss.ejb.plugins.AbstractInterceptor;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationResponse;

/**
 * A base interceptor that breaks out the invocation into several methods 
 * based on the type.  All of the methods have a default implementation that
 * simply calls the next interceptor, so only desired methods need to be
 * implemented.  The invoke method, which is not overrideable, will just call
 * the next interceptor if an unknown type or null type is found in the 
 * invocation object.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.3 $
 */
public abstract class AbstractEntityTypeInterceptor extends AbstractInterceptor
{
   public final InvocationResponse invoke(Invocation invocation) throws Exception
   {
      LifeCycleEvent event = LifeCycleEvent.get(invocation);

      if(event == LifeCycleEvent.CREATE_INSTANCE)
      {
         return createInstance(invocation);
      }
      else if(event == LifeCycleEvent.CREATE)
      {
         return createEntity(invocation);
      }
      else if(event == LifeCycleEvent.POST_CREATE)
      {
         return postCreateEntity(invocation);
      }
      else if(event == LifeCycleEvent.REMOVE)
      {
         return removeEntity(invocation);
      }
      else if(event == LifeCycleEvent.QUERY)
      {
         return query(invocation);
      }
      else if(event == LifeCycleEvent.IS_MODIFIED)
      {
         return isModified(invocation);
      }
      else if(event == LifeCycleEvent.LOAD)
      {
         return loadEntity(invocation);
      }
      else if(event == LifeCycleEvent.STORE)
      {
         return storeEntity(invocation);
      }
      else if(event == LifeCycleEvent.ACTIVATE)
      {
         return activateEntity(invocation);
      }
      else if(event == LifeCycleEvent.PASSIVATE)
      {
         return passivateEntity(invocation);
      }
      else
      {
         return invokeOther(invocation);
      }
   }
   
   /**
    * Returns a new instance of the bean class or a subclass of the bean class.
    *
    * @param invocation the invocation information
    * @return the new instance
    * @throws Exception if some problem occures
    */
   protected InvocationResponse createInstance(Invocation invocation) throws Exception
   {
      return getNext().invoke(invocation);
   }

   /**
    * This method is called whenever an entity is to be created.
    *
    * @param invocation the invocation information
    * @return not used and should be null, but could be anything
    * @throws Exception if some problem occures; usually a CreateException
    */
   protected InvocationResponse createEntity(Invocation invocation) throws Exception
   {
      return getNext().invoke(invocation);
   }

   /**
    * This method is called after an entity has been created.
    *
    * @param invocation the invocation information
    * @return not used and should be null, but could be anything
    * @throws Exception if some problem occures; usually a CreateException
    */
   protected InvocationResponse postCreateEntity(Invocation invocation) throws Exception
   {
      return getNext().invoke(invocation);
   }

   /**
    * This method is called when an entity should be removed from the
    * underlying storage.
    *
    * @param invocation the invocation information
    * @return not used and should be null, but could be anything
    * @throws Exception if some problem occures; usually a RemoveException
    */
   protected InvocationResponse removeEntity(Invocation invocation) throws Exception
   {
      return getNext().invoke(invocation);
   }

   /**
    * This method handles general queries.
    *
    * @param invocation the invocation information
    * @return the results of the query
    * @throws Exception if some problem occures; usually a FinderException or
    * an EJBException
    */
   protected InvocationResponse query(Invocation invocation) throws Exception
   {
      return getNext().invoke(invocation);
   }

   /**
    * This method is used to determine if an entity has been modified and 
    * should therefor be stored.
    *
    * @param invocation the invocation information
    * @return true, if the entity has been modified
    * @throws Exception if some problem occures
    */
   protected InvocationResponse isModified(Invocation invocation) throws Exception
   {
      return getNext().invoke(invocation);
   }
 
   /**
    * This method is called whenever an entity should be load from the
    * underlying storage.
    *
    * @param invocation the invocation information
    * @return not used and should be null, but could be anything
    * @throws Exception if some problem occures
    */
   protected InvocationResponse loadEntity(Invocation invocation) throws Exception
   {
      return getNext().invoke(invocation);
   }

 
   /**
    * This method is called whenever an entity should be stored in the
    * underlying storage.
    *
    * @param invocation the invocation information
    * @return not used and should be null, but could be anything
    * @throws Exception if some problem occures
    */
   protected InvocationResponse storeEntity(Invocation invocation) throws Exception
   {
      return getNext().invoke(invocation);
   }

   /**
    * This method is called when an entity is being activated.
    *
    * @param invocation the invocation information
    * @return not used and should be null, but could be anything
    * @throws Exception if some problem occures
    */
   protected InvocationResponse activateEntity(Invocation invocation) throws Exception
   {
      return getNext().invoke(invocation);
   }

   /**
    * This method is called when an entity is being passivated.
    * 
    * @param invocation the invocation information
    * @return not used and should be null, but could be anything
    * @throws Exception if some problem occures
    */
   protected InvocationResponse passivateEntity(Invocation invocation) throws Exception
   {
      return getNext().invoke(invocation);
   }

   /**
    * This method is called when the invocation is not an reconized 
    * LifeCycleEvent.
    * 
    * @param invocation the invocation context
    * @return the results of this invocation
    * @throws Exception if a problem occures during the invocation
    */
   protected InvocationResponse invokeOther(Invocation invocation) throws Exception
   {
      return getNext().invoke(invocation);
   }
}
