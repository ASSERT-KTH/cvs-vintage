package org.jboss.aspect.interceptors;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

import org.jboss.aspect.IAspectInterceptor;
import org.jboss.aspect.proxy.AspectInitizationException;
import org.jboss.aspect.proxy.AspectInvocation;
import org.jboss.aspect.proxy.AspectInvocationHandler;
import org.jboss.aspect.util.IAspectEditor;
import org.jboss.aspect.util.IAspectInvocationHandlerAware;

/**
 * Exposes a IAspectEditor via IAdaptor
 */
public class AspectEditorInterceptor implements IAspectInterceptor
{
   AdaptorInterceptor ai = new AdaptorInterceptor();
   
   public static class AspectEditor implements IAspectEditor, IAspectInvocationHandlerAware
   {
      AspectInvocationHandler aih;
      public int getInterceptorListSize()
      {
         return aih.getInterceptorListSize();
      }

      public void insertInterceptor(int position, IAspectInterceptor interceptor, Set filteredMethods, Object attachment)
         throws AspectInitizationException
      {
         aih.insertInterceptor(position, interceptor, filteredMethods, attachment);
      }

      public void removeInterceptor(int position)
      {
         aih.removeInterceptor(position);
      }

      public void setTargetObject(Object targetObject)
      {
         aih.setTargetObject(targetObject);
      }

      public void setAspectInvocationHandler(AspectInvocationHandler aspectInvocationHandler)
      {
         aih = aspectInvocationHandler;
      }
   }

   public void init(Map properties) throws AspectInitizationException
   {
      properties.put("adaptor", IAspectEditor.class.getName());
      properties.put("implementation", AspectEditor.class.getName());
      properties.put("singlton", "false");
      ai.init(properties);
   }

   public Class[] getInterfaces()
   {
      return ai.getInterfaces();
   }

   public Object invoke(AspectInvocation invocation) throws Throwable
   {
      return ai.invoke(invocation);
   }

   public boolean isIntrestedInMethodCall(Method method)
   {
      return ai.isIntrestedInMethodCall(method);
   }

}
