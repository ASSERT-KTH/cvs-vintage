package org.jboss.aspect.interceptors;

import java.util.Map;

import org.jboss.aspect.IAspectInterceptor;
import org.jboss.aspect.proxy.AspectInitizationException;
import org.jboss.aspect.proxy.AspectInvocationHandler;
import org.jboss.aspect.util.IAspectEditor;
import org.jboss.aspect.util.IAspectInvocationHandlerAware;

/**
 * Exposes a IAspectEditor via IAdaptor
 * 
 */
public class AspectEditorInterceptor extends AdaptorInterceptor
{
   static class AspectEditor implements IAspectEditor, IAspectInvocationHandlerAware
   {
      AspectInvocationHandler aih;
      public int getInterceptorListSize()
      {
         return aih.getInterceptorListSize();
      }

      public void insertInterceptor(int position, IAspectInterceptor interceptor, Object config, Object attachment)
         throws AspectInitizationException
      {
         aih.insertInterceptor(position, interceptor, config, attachment);
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

   public Object translateConfiguration(Map properties) throws AspectInitizationException
   {
      properties.put("adaptor", IAspectEditor.class.getName());
      properties.put("implementation", AspectEditor.class.getName());
      properties.put("singlton", "false");
      return super.translateConfiguration(properties);
   }

}
