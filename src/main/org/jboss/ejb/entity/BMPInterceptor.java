/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.entity;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.HashMap;
import java.util.List;

import javax.ejb.EntityBean;
import javax.ejb.EJBException;
import javax.ejb.EJBLocalHome;

import org.jboss.deployment.DeploymentException;
import org.jboss.ejb.EntityCache;
import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.EJBProxyFactory;
import org.jboss.ejb.LocalProxyFactory;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationResponse;
import org.jboss.invocation.InvocationKey;
import org.jboss.invocation.PayloadKey;
import org.jboss.util.collection.SerializableEnumeration;

/**
 * Persistence manager for BMP entites.  All calls are simply deligated
 * to the entity implementation class.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.3 $
 */
public final class BMPInterceptor extends AbstractEntityTypeInterceptor
{
   private Method isModified;

   public void create() throws Exception
   {
      EntityContainer container = (EntityContainer)getContainer();

      // isModified
      try
      {
         isModified = container.getBeanClass().getMethod(
               "isModified", 
               null);
         if(!isModified.getReturnType().equals(Boolean.TYPE))
         {
            // Must have "boolean" as return type
            isModified = null; 
         }
      }
      catch (NoSuchMethodException ignored) {}
   }

   protected InvocationResponse getValue(Invocation invocation) throws Exception
   {
      throw new UnsupportedOperationException("BMP entities do support " +
            "the getValue opperation");
   }

   protected InvocationResponse setValue(Invocation invocation) throws Exception
   {
      throw new UnsupportedOperationException("BMP entities do support " +
            "the setValue opperation");
   }
 
   protected InvocationResponse createInstance(Invocation invocation) throws Exception
   {
      EntityContainer container = (EntityContainer)getContainer();
      return new InvocationResponse(container.getBeanClass().newInstance());
   }

   protected InvocationResponse query(Invocation invocation) throws Exception
   {
      EntityContainer container = (EntityContainer)getContainer();

      // invoke the finder method
      InvocationResponse response = getNext().invoke(invocation);
      Object finderResult = response.getResponse();
 
      EntityCache cache = (EntityCache)container.getInstanceCache();

      // Single object finder
      Method finderMethod = (Method)
            invocation.getValue(InvocationKey.CALLBACK_METHOD);
      Class returnType = finderMethod.getReturnType();
      if(returnType != Collection.class && 
            returnType != Enumeration.class)
      {
         if(finderResult == null)
         {
            return new InvocationResponse(null);
         }

         // return the EJB[Local]Objects for the cache keys
         if(invocation.getType().isLocal())
         {
            LocalProxyFactory factory = container.getLocalProxyFactory();
            return new InvocationResponse(factory.getEntityEJBLocalObject(finderResult));
         }
         else
         {
            EJBProxyFactory factory = container.getProxyFactory();
            return new InvocationResponse(factory.getEntityEJBObject(finderResult));
         }
      }
   
      // Multi object finder
      if(finderResult == null)
      {
         return new InvocationResponse(Collections.EMPTY_LIST);
      }

      // convert primary keys to cache keys
      List primaryKeys;
      if(finderResult instanceof java.util.Enumeration)
      {
         primaryKeys = Collections.list((Enumeration)finderResult);
      }
      else
      {
         primaryKeys = new ArrayList((Collection)finderResult);
      }

      // Get the EJB[Local]Objects for the cache keys
      Collection results;
      if(invocation.getType().isLocal())
      {
         LocalProxyFactory factory = container.getLocalProxyFactory();
         results = factory.getEntityLocalCollection(primaryKeys);
      }
      else
      {
         EJBProxyFactory factory = container.getProxyFactory();
         results = factory.getEntityCollection(primaryKeys);
      }
         
      // BMP entity finder methods are allowed to return an Enumeration.
      if(returnType == Enumeration.class)
      {
         if(invocation.getType().isLocal())
         {
            return new InvocationResponse(Collections.enumeration(results));
         }
         else
         {
            // This is on a remote interface, so we need a serializable 
            // Enumeration, and Collections.enumeration() is not.
            return new InvocationResponse(new SerializableEnumeration(results));
         }
      }
      
      // return a normal collection
      return new InvocationResponse(results);
   }

   protected InvocationResponse isModified(Invocation invocation) throws Exception 
   {
      if(isModified == null)
      {
         return new InvocationResponse(Boolean.TRUE);
      }
            
      invocation.setValue(
            InvocationKey.CALLBACK_METHOD, 
            isModified,
            PayloadKey.TRANSIENT);
      return getNext().invoke(invocation);
   }
}
