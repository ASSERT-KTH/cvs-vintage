package org.jboss.aspect.interceptors;

import java.lang.reflect.Method;
import java.util.Map;

import org.jboss.aspect.IAspectInterceptor;
import org.jboss.aspect.proxy.AspectInitizationException;
import org.jboss.aspect.proxy.AspectInvocation;

public class DetypedInterceptor implements IAspectInterceptor
{

   public Class[] getInterfaces()
   {
      return NO_INTERFACES;
   }

   public void init(Map properties) throws AspectInitizationException
   {
   }

   public Object invoke(AspectInvocation invocation) throws Throwable
   {
      return invocation.invokeNext();
   }

   public boolean isIntrestedInMethodCall(Method method)
   {
      return true;
   }

}
