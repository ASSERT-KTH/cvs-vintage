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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ejb.EntityBean;

import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationResponse;
import org.jboss.invocation.InvocationKey;
import org.jboss.invocation.PayloadKey;

/**
 * Sets the callback method and arguments for a life cycle event.
 *
 * @todo remove this when the metadata is changed to AOP style 
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.1 $
 */
public final class LifeCycleCallbackLoader
extends AbstractEntityTypeInterceptor
{
   private Method ejbLoad;
   private Method ejbStore;
   private Method ejbActivate;
   private Method ejbPassivate;
   private Method ejbRemove;
   private final Object[] noargs = new Object[0];

   /**
    * These are the mappings between the home interface create methods and the 
    * bean implementation ejbCreate method.
    */
   private final Map createMapping = new HashMap();

   /**
    * These are the mappings between the home interface create methods and the 
    * bean implementation ejbPostCreate method.
    */
   private final Map postCreateMapping = new HashMap();

   public void create() throws Exception
   {
      // get reference to the ejb call back methods
      ejbLoad = EntityBean.class.getMethod("ejbLoad", null);
      ejbStore = EntityBean.class.getMethod("ejbStore", null);
      ejbActivate = EntityBean.class.getMethod("ejbActivate", null);
      ejbPassivate = EntityBean.class.getMethod("ejbPassivate", null);
      ejbRemove = EntityBean.class.getMethod("ejbRemove", null);

      try
      {
         setupCreateMapping();
      }
      catch(Exception e)
      {
         // ditch any half built mappings
         createMapping.clear();
         postCreateMapping.clear();
         throw e;
      }
   }

   protected InvocationResponse createEntity(Invocation invocation) throws Exception
   {
      invocation.setValue(
            InvocationKey.CALLBACK_METHOD, 
            createMapping.get(invocation.getMethod()),
            PayloadKey.TRANSIENT);
      invocation.setValue(
            InvocationKey.CALLBACK_ARGUMENTS, 
            invocation.getArguments(),
            PayloadKey.TRANSIENT);
      return getNext().invoke(invocation);
   }

   protected InvocationResponse postCreateEntity(Invocation invocation) throws Exception
   {
      invocation.setValue(
            InvocationKey.CALLBACK_METHOD, 
            postCreateMapping.get(invocation.getMethod()),
            PayloadKey.TRANSIENT);
      invocation.setValue(
            InvocationKey.CALLBACK_ARGUMENTS, 
            invocation.getArguments(),
            PayloadKey.TRANSIENT);
      return getNext().invoke(invocation);
   }

   protected InvocationResponse removeEntity(Invocation invocation) throws Exception
   {
      invocation.setValue(
            InvocationKey.CALLBACK_METHOD, 
            ejbRemove,
            PayloadKey.TRANSIENT);
      invocation.setValue(
            InvocationKey.CALLBACK_ARGUMENTS, 
            noargs,
            PayloadKey.TRANSIENT);
      return getNext().invoke(invocation);
   }

   protected InvocationResponse query(Invocation invocation) throws Exception
   {
      // if there is a callback for the query it is handled by another interceptor
      return getNext().invoke(invocation);
   }

   protected InvocationResponse isModified(Invocation invocation) throws Exception
   {
      // if there is a callback for isModified it is handled by another interceptor
      return getNext().invoke(invocation);
   }

   protected InvocationResponse loadEntity(Invocation invocation) throws Exception
   {
      invocation.setValue(
            InvocationKey.CALLBACK_METHOD, 
            ejbLoad,
            PayloadKey.TRANSIENT);
      invocation.setValue(
            InvocationKey.CALLBACK_ARGUMENTS, 
            noargs,
            PayloadKey.TRANSIENT);
      return getNext().invoke(invocation);
   }

   protected InvocationResponse storeEntity(Invocation invocation) throws Exception
   {
      invocation.setValue(
            InvocationKey.CALLBACK_METHOD, 
            ejbStore,
            PayloadKey.TRANSIENT);
      invocation.setValue(
            InvocationKey.CALLBACK_ARGUMENTS, 
            noargs,
            PayloadKey.TRANSIENT);
      return getNext().invoke(invocation);
   }

   protected InvocationResponse activateEntity(Invocation invocation) throws Exception
   {
      invocation.setValue(
            InvocationKey.CALLBACK_METHOD, 
            ejbActivate,
            PayloadKey.TRANSIENT);
      invocation.setValue(
            InvocationKey.CALLBACK_ARGUMENTS, 
            noargs,
            PayloadKey.TRANSIENT);
      return getNext().invoke(invocation);
   }

   protected InvocationResponse passivateEntity(Invocation invocation) throws Exception
   {
      invocation.setValue(
            InvocationKey.CALLBACK_METHOD, 
            ejbPassivate,
            PayloadKey.TRANSIENT);
      invocation.setValue(
            InvocationKey.CALLBACK_ARGUMENTS, 
            noargs,
            PayloadKey.TRANSIENT);
      return getNext().invoke(invocation);
   }

   private void setupCreateMapping() throws Exception
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
      for(Iterator iterator = methods.iterator(); iterator.hasNext(); ) 
      {
         Method method = (Method) iterator.next();
         String methodName = method.getName();

         if(methodName.startsWith("create"))
         {
            createMapping.put(
                  method, 
                  getBeanImplMethod(
                        "ejbCreate" + methodName.substring(6),
                        method));
            postCreateMapping.put(
                  method, 
                  getBeanImplMethod(
                        "ejbPostCreate" + methodName.substring(6),
                        method));
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
