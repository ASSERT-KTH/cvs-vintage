/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.entity;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;

import org.jboss.ejb.Container;
import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.plugins.AbstractInterceptor;

import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationResponse;
import org.jboss.invocation.InvocationKey;
import org.jboss.invocation.InvocationType;
import org.jboss.invocation.PayloadKey;

/**
 * This interceptor sets up the invocation in an entity bean context.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.2 $
 */
public final class EntityInterceptor extends AbstractInterceptor
{
   /**
    * These are the mappings between the interface methods and the bean 
    * implementation methods.
    */
   private Map callbackMapping = new HashMap();
   
   /**
    * These are the mappings between the interface methods and the entity
    * invocation type if applicable.
    */
   private Map entityInvocationTypeMapping = new HashMap();

   /**
    * These are the mappings between the home interface create methods and the 
    * bean implementation post create method invocation.
    */
   private Map postCreateMapping = new HashMap();

   public void create() throws Exception
   {
      // get the commit option
      //((EntityContainer)getContainer()).setRootEntityInterceptor(this);

      try
      {
         // Map the bean methods
         setupBeanMapping();

         // Map the home methods
         setupHomeMapping();
      }
      catch(Exception e)
      {
         // ditch the half built mappings
         callbackMapping.clear();
         entityInvocationTypeMapping.clear();
         postCreateMapping.clear();

         throw e;
      }
   }
   
   public void destroy()
   {
      callbackMapping.clear();
      entityInvocationTypeMapping.clear();
      postCreateMapping.clear();
   }

   private void setupHomeMapping() throws Exception
   {
      // create a list of all the methods in the local home and home interfaces
      List methods = new ArrayList();
      if(getContainer().getHomeClass() != null)
      {
         methods.addAll(
               Arrays.asList(getContainer().getHomeClass().getMethods()));
      }
      if(getContainer().getLocalHomeClass() != null)
      {
         methods.addAll(
               Arrays.asList(getContainer().getLocalHomeClass().getMethods()));
      }

      // interator over the list and set up the mappings
      for(Iterator iter = methods.iterator(); iter.hasNext(); ) 
      {
         Method method = (Method) iter.next();
         String methodName = method.getName();

         if(methodName.startsWith("find"))
         {
            entityInvocationTypeMapping.put(
                  method, 
                  EntityInvocationType.QUERY);
            Method finderMethod = getBeanImplMethod(
                  "ejbF" + methodName.substring(1),
                  method,
                  false);
      
            if(finderMethod != null) 
            {
               callbackMapping.put(method, finderMethod);
            }
         }
         else if(methodName.startsWith("create"))
         {
            entityInvocationTypeMapping.put(
                  method, 
                  EntityInvocationType.CREATE);
            callbackMapping.put(
                  method, 
                  getBeanImplMethod(
                        "ejbCreate" + methodName.substring(6),
                        method,
                        true));
            postCreateMapping.put(
                  method, 
                  getBeanImplMethod(
                        "ejbPostCreate" + methodName.substring(6),
                        method,
                        true));
         }
         else if(methodName.equals("remove"))
         {
            entityInvocationTypeMapping.put(
                  method, 
                  EntityInvocationType.REMOVE);
         }
         else if(methodName.equals("getHomeHandle"))
         {
            // Handled by proxy
         }
         else if(methodName.equals("getEJBMetaData"))
         {
            // Handled by proxy
         }
         else
         {
            String ejbHomeMethodName = 
                  "ejbHome" + 
                  methodName.substring(0,1).toUpperCase() + 
                  methodName.substring(1);

            callbackMapping.put(
                  method, 
                  getBeanImplMethod(ejbHomeMethodName, method, true));
         }
      }
   }

   private void setupBeanMapping() throws Exception
   {
      // create a list of all the methods in the local and remote interfaces
      List methods = new ArrayList();
      if(getContainer().getRemoteClass() != null)
      {
         methods.addAll(
               Arrays.asList(getContainer().getRemoteClass().getMethods()));
      }
      if(getContainer().getLocalClass() != null)
      {
         methods.addAll(
               Arrays.asList(getContainer().getLocalClass().getMethods()));
      }

      // interator over the list and set up the mappings
      for(Iterator iter = methods.iterator(); iter.hasNext(); )
      {
         Method method = (Method) iter.next();
         String methodName = method.getName();

         if(method.getDeclaringClass() == EJBObject.class ||
               method.getDeclaringClass() == EJBLocalObject.class)
         {
            if(methodName.equals("remove"))
            {
               entityInvocationTypeMapping.put(
                     method, 
                     EntityInvocationType.REMOVE);
            }
         }
         else
         {
            // Implemented by bean
            callbackMapping.put(
                  method, 
                  getBeanImplMethod( method.getName(), method, true));
         }
      }
   }

   private Method getBeanImplMethod(
         String name, 
         Method method, 
         boolean required) throws NoSuchMethodException
   {
      try
      {
         return getContainer().getBeanClass().getMethod(
               name, 
               method.getParameterTypes());
      }
      catch (NoSuchMethodException e)
      {
         if(required)
         {
            throw new NoSuchMethodException("Method " + method + 
                  " not implemented in bean class " + 
                  getContainer().getBeanClass().getName() + 
                  " looking for method named: " + name);
         }
         return null;
      }
   }

   public InvocationResponse invoke(Invocation invocation) throws Exception
   {
      Method interfaceMethod = invocation.getMethod();

      // set the callback method
      Method callbackMethod = (Method)callbackMapping.get(interfaceMethod);
      if(callbackMethod != null)
      {
         invocation.setValue(
               InvocationKey.CALLBACK_METHOD, 
               callbackMethod,
               PayloadKey.TRANSIENT);

         invocation.setValue(
               InvocationKey.CALLBACK_ARGUMENTS, 
               invocation.getArguments(),
               PayloadKey.TRANSIENT);
      }

      // set the entity invocation type
      EntityInvocationType entityInvocationType = (EntityInvocationType)
            entityInvocationTypeMapping.get(interfaceMethod);
      if(entityInvocationType != null)
      {
         invocation.setValue(
               EntityInvocationKey.TYPE, 
               entityInvocationType,
               PayloadKey.TRANSIENT);
      }

      // invoke the next
      InvocationResponse returnValue = getNext().invoke(invocation);
 
      // if this is a CREATE invocation, we now need to invoke post create
      if(entityInvocationType == EntityInvocationType.CREATE)
      {
         Invocation postCreateInvocation = new Invocation(invocation);
         postCreateInvocation.setValue(
               InvocationKey.CALLBACK_METHOD, 
               postCreateMapping.get(interfaceMethod),
               PayloadKey.TRANSIENT);
         postCreateInvocation.setValue(
               EntityInvocationKey.TYPE, 
               EntityInvocationType.POST_CREATE,
               PayloadKey.TRANSIENT);
         if(invocation.getType().isLocal())
         {
            postCreateInvocation.setType(InvocationType.LOCAL);
         }
         else
         {
            postCreateInvocation.setType(InvocationType.REMOTE);
         }

         getNext().invoke(postCreateInvocation);
      }

      return returnValue;
   }
}
