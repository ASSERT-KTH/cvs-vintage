/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/
package org.jboss.aspect.interceptors;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.jboss.aspect.AspectInitizationException;
import org.jboss.aspect.internal.AspectSupport;
import org.jboss.aspect.spi.AspectInterceptor;
import org.jboss.aspect.spi.AspectInvocation;
import org.jboss.util.Classes;

/**
 * The GetSetInterceptor implements the getter and setter methods
 * for all get* and set* method calls so that the interface behaves
 * like a POJO.
 * 
 * The bean attribute values are store in a HashMap associcated with
 * the aspect object.
 * 
 * @author <a href="mailto:hchirino@jboss.org">Hiram Chirino</a>
 */
public class GetSetInterceptor implements AspectInterceptor, Serializable
{

    /**
     * @see org.jboss.aspect.spi.AspectInterceptor#invoke(AspectInvocation)
     */
    public Object invoke(AspectInvocation invocation) throws Throwable
    {
    	String methodName = invocation.method.getName();
    	String attribute = methodName.substring(3);
    	Map attributeMap = (Map)invocation.aspectAttachments.get(this);
    	if( attributeMap == null ) {
    		attributeMap = new HashMap();
    		invocation.aspectAttachments.put(this, attributeMap);
    	}

		// Is this a setter method    	
    	if( methodName.startsWith("set") ) {
    		attributeMap.put(attribute, invocation.args[0]);
    		return null;
    		
    	// No? then this must be a getter method.
    	} else {
    		return attributeMap.get(attribute);
    	}
    }

    /**
     * @see org.jboss.aspect.spi.AspectInterceptor#init(Element)
     */
    public void init(Element xml) throws AspectInitizationException
    {
    }

    /**
     * @see AspectInterceptor#getInterfaces()
     */
    public Class[] getInterfaces()
    {
        return null;
    }

	/**
	 * We are only interested in getters and setters.
	 */
    public boolean isIntrestedInMethodCall(Method method)
    {
    	String s = method.getName();
    	// TODO: Improve the check:  validate arg count and return types.
    	return s.startsWith("get") || s.startsWith("set");
    }

}
