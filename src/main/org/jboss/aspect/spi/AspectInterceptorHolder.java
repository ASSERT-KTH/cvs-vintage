/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/
package org.jboss.aspect.spi;

import gnu.regexp.RE;
import gnu.regexp.REException;
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
    public final RE methodFilter;

    public AspectInterceptorHolder(AspectInterceptor interceptor)
    {
        this(interceptor, null);
    }
    public AspectInterceptorHolder(AspectInterceptor interceptor, RE methodFilter)
    {
        this.interceptor = interceptor;
        this.methodFilter = methodFilter;
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

        if (xml.attribute(ATTR_FILTER) != null)
        {
            String s = xml.attribute(ATTR_FILTER).getValue();
            try
            {
                methodFilter = new RE(s);
            }
            catch (REException e)
            {
                throw new AspectInitizationException("Invalid regular expression for method filer: " + s, e);
            }
        }
        else
        {
            methodFilter = null;
        }

        interceptor.init(xml);
    }

    public Class[] getInterfaces()
    {
        return interceptor.getInterfaces();
    }

    public boolean isIntrestedInMethodCall(Method method)
    {
    	if( methodFilter != null) 
    		if( !methodFilter.isMatch(method.getName()) )
    			return false;
    	
        return interceptor.isIntrestedInMethodCall(method);
    }

}
