package org.jboss.aspect.interceptors;

import java.lang.reflect.Method;

import org.dom4j.Element;
import org.jboss.aspect.AspectInitizationException;
import org.jboss.aspect.spi.AspectInterceptor;
import org.jboss.aspect.spi.AspectInvocation;

public class DetypedInterceptor implements AspectInterceptor
{

    public Class[] getInterfaces()
    {
        return null;
    }

    public void init(Element properties) throws AspectInitizationException
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
