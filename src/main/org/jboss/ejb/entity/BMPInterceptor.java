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
import java.util.Map;

import org.jboss.ejb.EntityContainer;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationResponse;
import org.jboss.invocation.InvocationKey;
import org.jboss.invocation.PayloadKey;
import org.jboss.metadata.ConfigurationMetaData;

/**
 * Persistence manager for BMP entites.  All calls are simply deligated
 * to the entity implementation class.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.4 $
 */
public final class BMPInterceptor extends AbstractEntityTypeInterceptor
{
   /**
    * The is modified method if the bean has one.
    */
   private Method isModified;

   /**
    * These are the mappings between the home interface find methods and the 
    * bean implementation ejbFind method.
    */
   private final Map finderMapping = new HashMap();

   /**
    * No arg array used when invoking callback methods.
    */
   private final static Object[] noargs = new Object[0];

   public void create() throws Exception
   {
      // isModified
      try
      {
         isModified = getContainer().getBeanClass().getMethod(
               "isModified", 
               null);
         if(!isModified.getReturnType().equals(Boolean.TYPE))
         {
            // Must have "boolean" as return type
            isModified = null; 
         }
      }
      catch (NoSuchMethodException ignored) {}

      try
      {
         setupFinderMapping();
      }
      catch(Exception e)
      {
         // ditch any half built mappings
         finderMapping.clear();
         throw e;
      }

   }

   protected InvocationResponse createInstance(Invocation invocation) throws Exception
   {
      return new InvocationResponse(getContainer().getBeanClass().newInstance());
   }

   protected InvocationResponse query(Invocation invocation) throws Exception
   {
      ConfigurationMetaData configuration = 
            getContainer().getBeanMetaData().getContainerConfiguration();
      if(!configuration.getSyncOnCommitOnly())
      {
         EntityContainer.getEntityInvocationRegistry().synchronizeEntities();
      }   

      Method finderMethod = (Method) finderMapping.get(invocation.getMethod());

      // Set the callback method and arguments
      invocation.setValue(
            InvocationKey.CALLBACK_METHOD, 
            finderMethod,
            PayloadKey.TRANSIENT);
      invocation.setValue(
            InvocationKey.CALLBACK_ARGUMENTS, 
            invocation.getArguments(),
            PayloadKey.TRANSIENT);

      // invoke the finder method
      InvocationResponse response = getNext().invoke(invocation);
      Object finderResult = response.getResponse();
 
      // if we got null return an EMPTY_LIST
      if(finderResult == null)
      {
         return new InvocationResponse(Collections.EMPTY_LIST);
      }

      // Single object finder
      Class returnType = finderMethod.getReturnType();
      if(returnType == Collection.class)
      {
         return new InvocationResponse(finderResult);
      }
      else if(returnType == Enumeration.class)
      {
         return new InvocationResponse(
               Collections.list((Enumeration)finderResult));
      }
      else
      {
         return new InvocationResponse(
               Collections.singletonList(finderResult));
      }
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
      invocation.setValue(
            InvocationKey.CALLBACK_ARGUMENTS, 
            noargs,
            PayloadKey.TRANSIENT);
      return getNext().invoke(invocation);
   }

   private void setupFinderMapping() throws Exception
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

         if(methodName.startsWith("find"))
         {
            finderMapping.put(
                  method,
                  getBeanImplMethod("ejbF" + methodName.substring(1), method));
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
