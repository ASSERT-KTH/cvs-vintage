package org.jboss.aspect.internal;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Map;

import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.jboss.aspect.AspectInitizationException;
import org.jboss.aspect.spi.AspectDefinitionConstants;
import org.jboss.aspect.spi.AspectInterceptor;
import org.jboss.util.Classes;

/**
 * @author Hiram
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
final public class AspectInterceptorHolder implements AspectDefinitionConstants, Serializable
{
    public final AspectInterceptor interceptor; 

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


    public Class[] getInterfaces() {
        return interceptor.getInterfaces();
    }


    public boolean isIntrestedInMethodCall(Method method)
    {
        return interceptor.isIntrestedInMethodCall(method);
    }

}
