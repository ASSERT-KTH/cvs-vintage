/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.aspect.proxy;

import java.lang.reflect.Method;

import org.jboss.proxy.compiler.InvocationHandler;
import org.jboss.proxy.compiler.ProxyImplementationFactory;

import org.jboss.aspect.AspectComposition;
import org.jboss.aspect.proxy.*;

/**
 * An aspect object is in reality a Dynamic Proxy which forwards all
 * method calls to a AspectInvocationHandler.
 * 
 * The AspectInvocationHandler stores all the state information associated
 * with an aspect object instance.  When the method call occurs, that 
 * state information is passed down in a AspectInvocation to the first
 * interceptor defined in the aspect definition.
 * 
 * @author <a href="mailto:hchirino@jboss.org">Hiram Chirino</a>
 */
public class AspectInvocationHandler implements InvocationHandler {
	
	AspectComposition composition;
	private Object attachments[];
	Object baseObject;

	public AspectInvocationHandler(AspectComposition composition, Object baseObject) {
		this.composition = composition;
		this.baseObject = baseObject;
	}
	
	/**
	 * @see java.lang.reflect.InvocationHandler#invoke(Object, Method, Object[])
	 */
	public Object invoke(Object target, Method method, Object[] args)
		throws Throwable {
		AspectInvocation i =
			new AspectInvocation(target, method, args, this);
		return i.invokeNext();
	}
	
	Object[] getAttachments() {
		if( attachments==null )
			attachments = new Object[composition.interceptors.length];
		return attachments;
	}
	
}
