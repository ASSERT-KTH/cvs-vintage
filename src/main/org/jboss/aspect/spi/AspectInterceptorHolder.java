/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/
package org.jboss.aspect.spi;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.dom4j.Element;
import org.jboss.aspect.AspectInitizationException;
import org.jboss.util.Classes;

/**
 * This class will in the future provide things like method call filtering so that
 * Interceptors writers do not have to implement it themselfs.
 * 
 * The AspectDefinition actually maintains a list of AspectInterceptorHolder objects.
 * 
 * @author <a href="mailto:hchirino@jboss.org">Hiram Chirino</a>
 */
final public class AspectInterceptorHolder implements AspectDefinitionConstants, Serializable
{
    public final AspectInterceptor interceptor;
    
    
    public AspectInterceptorHolder(AspectInterceptor interceptor) {
    	this.interceptor = interceptor;
    }

    public AspectInterceptorHolder(Element xml) throws AspectInitizationException
    {
        String className = xml.attribute(ATTR_CLASS).getValue();
        try
        {
            ClassLoader cl = Classes.getContextClassLoader();
            interceptor = (AspectInterceptor) cl.loadClass(className).newInstance();
        }
        catch (Exception e)
        {
            throw new AspectInitizationException("Invlaid interceptor class: " + className + ": " + e);
        }

        interceptor.init(xml);
    }

    public Class[] getInterfaces()
    {
        return interceptor.getInterfaces();
    }

    public boolean isIntrestedInMethodCall(Method method)
    {
        return interceptor.isIntrestedInMethodCall(method);
    }

}
