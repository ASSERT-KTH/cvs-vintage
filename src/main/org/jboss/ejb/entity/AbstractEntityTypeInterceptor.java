/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.entity;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.HashMap;
import java.util.List;

import javax.ejb.EntityBean;
import javax.ejb.CreateException;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;
import javax.ejb.EJBException;
import javax.ejb.EJBObject;

import org.jboss.ejb.Container;
import org.jboss.ejb.EntityCache;
import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.EJBProxyFactory;
import org.jboss.ejb.LocalProxyFactory;

import org.jboss.ejb.plugins.AbstractInterceptor;

import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationResponse;

import org.jboss.metadata.ConfigurationMetaData;

/**
 * A base interceptor that breaks out the invocation into several methods 
 * based on the type.  All of the methods have a default implementation that
 * simply calls the next interceptor, so only desired methods need to be
 * implemented.  The invoke method, which is not overrideable, will just call
 * the next interceptor if an unknown type or null type is found in the 
 * invocation object.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.2 $
 */
public abstract class AbstractEntityTypeInterceptor extends AbstractInterceptor
{
   public final InvocationResponse invoke(Invocation invocation) throws Exception
   {
      EntityInvocationType type = (EntityInvocationType)
            invocation.getValue(EntityInvocationKey.TYPE);

      if(type == EntityInvocationType.GET_VALUE)
      {
         return getValue(invocation);
      }
      else if(type == EntityInvocationType.SET_VALUE)
      {
         return setValue(invocation);
      }
      else if(type == EntityInvocationType.CREATE_INSTANCE)
      {
         return createInstance(invocation);
      }
      else if(type == EntityInvocationType.CREATE)
      {
         return createEntity(invocation);
      }
      else if(type == EntityInvocationType.POST_CREATE)
      {
         return postCreateEntity(invocation);
      }
      else if(type == EntityInvocationType.REMOVE)
      {
         return removeEntity(invocation);
      }
      else if(type == EntityInvocationType.QUERY)
      {
         return query(invocation);
      }
      else if(type == EntityInvocationType.IS_MODIFIED)
      {
         return isModified(invocation);
      }
      else if(type == EntityInvocationType.LOAD)
      {
         return loadEntity(invocation);
      }
      else if(type == EntityInvocationType.STORE)
      {
         return storeEntity(invocation);
      }
      else if(type == EntityInvocationType.ACTIVATE)
      {
         return activateEntity(invocation);
      }
      else if(type == EntityInvocationType.PASSIVATE)
      {
         return passivateEntity(invocation);
      }
      else
      {
         return invokeOther(invocation);
      }
   }
   
   /**
    * Gets the value of a field.
    *
    * @param invocation the invocation information
    * @return the field value
    * @throws Exception if some problem occures
    */
   protected InvocationResponse getValue(Invocation invocation) throws Exception
   {
      return getNext().invoke(invocation);
   }

   /**
    * Sets the value of a field.
    *
    * @param invocation the invocation information
    * @return not used and should be null, but could be anything
    * @throws Exception if some problem occures
    */
   protected InvocationResponse setValue(Invocation invocation) throws Exception
   {
      return getNext().invoke(invocation);
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
    * EntityInvocationType.
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
