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
 * @todo remove this when the metadata is changed to AOP style.
 * My guess is this will become part of the method meta data, but
 * we need to key off of method name, declaring class and arguments
 * so I don't think the current method meta data will cut it for us.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.1 $
 */
public final class EntityMetaDataLoader extends AbstractInterceptor
{
   /**
    * These are the mappings between the interface methods and the bean 
    * implementation methods.
    */
   private Map callbackMapping = new HashMap();
   
   /**
    * These are the mappings between the interface methods and the entity
    * life cycle event if applicable.
    */
   private Map lifeCycleMapping = new HashMap();

   public void create() throws Exception
   {
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
         lifeCycleMapping.clear();

         throw e;
      }
   }
   
   public void destroy()
   {
      callbackMapping.clear();
      lifeCycleMapping.clear();
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
      LifeCycleEvent lifeCycleEvent = (LifeCycleEvent)
            lifeCycleMapping.get(interfaceMethod);
      if(lifeCycleEvent != null)
      {
         LifeCycleEvent.set(invocation, lifeCycleEvent);
      }

      return getNext().invoke(invocation);
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
               lifeCycleMapping.put(
                     method, 
                     LifeCycleEvent.QUERY);
         }
         else if(methodName.startsWith("create"))
         {
               lifeCycleMapping.put(
                     method, 
                     LifeCycleEvent.CREATE);
         }
         else if(methodName.equals("remove"))
         {
               lifeCycleMapping.put(
                     method, 
                     LifeCycleEvent.REMOVE);
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
                  getBeanImplMethod(ejbHomeMethodName, method));
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
               lifeCycleMapping.put(
                     method, 
                     LifeCycleEvent.REMOVE);
            }
         }
         else
         {
            // Implemented by bean
            callbackMapping.put(
                  method, 
                  getBeanImplMethod( method.getName(), method));
         }
      }
   }

   private Method getBeanImplMethod(
         String name, 
         Method method) throws NoSuchMethodException
   {
      try
      {
         return getContainer().getBeanClass().getMethod(
               name, 
               method.getParameterTypes());
      }
      catch (NoSuchMethodException e)
      {
         throw new NoSuchMethodException("Method " + method + 
               " not implemented in bean class " + 
               getContainer().getBeanClass().getName() + 
               " looking for method named: " + name);
      }
   }
}
