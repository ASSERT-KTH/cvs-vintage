/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.aspect.interceptors;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.jboss.aspect.IAspectInterceptor;
import org.jboss.aspect.proxy.AspectInitizationException;
import org.jboss.aspect.proxy.AspectInvocation;

/**
 *
 * This Interceptor associates the AspectInvocation in a ThreadLocal
 * object. The current AspectInvocation can then be retrieved by 
 * any object by calling the <code>getCurrentAspectInvocation()</code>
 * 
 * It accepts the configuration attributes defined in DetypedInterceptor
 * 
 * @see #getCurrentAspectInvocation()
 * 
 * @author <a href="mailto:hchirino@jboss.org">Hiram Chirino</a>
 */
public class AspectInvocationContextInterceptor extends DetypedInterceptor {

	private static ThreadLocal invocationStackTL = new ThreadLocal();

	/**
	 * Returns the current AspectInvocation that is being carried 
	 * invoked.  If not AspectInvocation is currently being invoked,
	 * null is returned.
	 * 
	 */
	public static AspectInvocation getCurrentAspectInvocation() {
		Stack invocationStack = (Stack)invocationStackTL.get();
		if( invocationStack == null ) 
			return null;
		return (AspectInvocation)invocationStack.peek();
	}
	
	/**
	 * @see AspectInterceptor#invoke(AspectInvocation)
	 */
	public Object invoke(AspectInvocation invocation) throws Throwable {
		Stack invocationStack = (Stack)invocationStackTL.get();
		if( invocationStack == null ) {
			invocationStack = new Stack();
			invocationStackTL.set(invocationStack);
		}
		invocationStack.push(invocation);
		try {
			return invocation.invokeNext();
		} finally {
			invocationStack.pop();
		}
	}   
}
