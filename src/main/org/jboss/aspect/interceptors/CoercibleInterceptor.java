/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/
package org.jboss.aspect.interceptors;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Map;

import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.jboss.aspect.AspectInitizationException;
import org.jboss.aspect.spi.AspectInterceptor;
import org.jboss.aspect.spi.AspectInvocation;
import org.jboss.util.Classes;
import org.jboss.util.Coercible;

/**
 * The CoercibleInterceptor allows you proivde add an Coercible
 * via the Coercible interface.  
 * 
 * The problem with the Delegating interceptor is that as you add more
 * interfaces to an object you run a higher chance of having method name 
 * clashes.
 * 
 * 
 * This interceptor uses the following configuration attributes:
 * <ul>
 * <li>Coercible  - The interface that the implementation object is exposing 
 *                via the Adaptable interface.  This is a required attribute. 
 * <li>implementation  - class name of the object that will be used to delegate
 *                method calls to.  This is a required attribute.
 * <li>singleton - if set to "true", then the method calls of multiple
 *                aspect object will be directed to a single instance of
 *                the delegate.  This makes the Coercible a singleton. 
 * </ul>
 * 
 * @author <a href="mailto:hchirino@jboss.org">Hiram Chirino</a>
 * 
 */
public class CoercibleInterceptor implements AspectInterceptor, Serializable
{

    public static final Namespace NAMESPACE = Namespace.get(CoercibleInterceptor.class.getName());
    public static final QName ATTR_CLASS = new QName("class", NAMESPACE);
    public static final QName ATTR_IMPLEMENTATION = new QName("implementation", NAMESPACE);
    public static final QName ATTR_SIGLETON = new QName("singleton", NAMESPACE);

    public Object singeltonObject;
    public Class implementingClass;
    public Class CoercibleClass;

    static final Method GET_ADAPTER_METHOD;
    static {
        Method m = null;
        try
        {
            m = Coercible.class.getMethod("coerce", new Class[] { Class.class });
        }
        catch (NoSuchMethodException e)
        {
        }
        GET_ADAPTER_METHOD = m;
    }

    /**
     * @see org.jboss.aspect.spi.AspectInterceptor#invoke(AspectInvocation)
     */
    public Object invoke(AspectInvocation invocation) throws Throwable
    {

        if (!CoercibleClass.equals(invocation.args[0]))
        {
            if (invocation.isNextIntrestedInMethodCall())
                return invocation.invokeNext();
            return null;
        }

        Object o = null;
        if (singeltonObject != null)
        {
            o = singeltonObject;
        }
        else
        {
            Map attachments = invocation.aspectAttachments;
            o = attachments.get(this);
            if (o == null)
            {
                o = implementingClass.newInstance();
                attachments.put(this, o);
            }
        }
        return o;
    }

    /**
     * Builds a Config object for the interceptor.
     * 
     * @see org.jboss.aspect.spi.AspectInterceptor#init(Element)
     */
    public void init(Element xml) throws AspectInitizationException
    {
        try
        {
            String CoercibleName = xml.attribute(ATTR_CLASS).getValue();
            String className = xml.attribute(ATTR_IMPLEMENTATION).getValue();

            CoercibleClass = Classes.loadClass(CoercibleName);
            implementingClass = Classes.loadClass(className);

            String singlton = xml.attribute(ATTR_SIGLETON) == null ? null : xml.attribute(ATTR_SIGLETON).getValue();
            if ("true".equals(singlton))
                singeltonObject = implementingClass.newInstance();

        }
        catch (Exception e)
        {
            throw new AspectInitizationException("Aspect Interceptor missconfigured: ", e);
        }
    }

    /**
     * @see org.jboss.aspect.spi.AspectInterceptor#getInterfaces()
     */
    public Class[] getInterfaces()
    {
        return new Class[] { Coercible.class };
    }

    public boolean isIntrestedInMethodCall(Method method)
    {
        return GET_ADAPTER_METHOD.equals(method);
    }
}
