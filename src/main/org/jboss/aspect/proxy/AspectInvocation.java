/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.aspect.proxy;

import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;

/**
 * A method call performed on an aspect will get encapsulated
 * into an AspectInvocation by the AspectInvocationHandler and
 * then passed down the AspectInterceptor list.
 * 
 * This object can be used by the Interceptors to get state data
 * about the aspect object and aspect/interceptor configuration.
 * 
 * @author <a href="mailto:hchirino@jboss.org">Hiram Chirino</a>
 */
final public class AspectInvocation {
	
	private AspectInvocationHandler handler;
	private int currentInterceptor=-1;
	
	/** the aspect object that the invocation was performed on */
	public Object aspectObject;
	/** the method that was call on the aspect object */
	public Method method;
	/** the arguments that were passed in the method call */
	public Object[] args;

	/** 
	 * Constructor used by the AspectInvocationHandler
	 * to create a AspectInvocation.
	 */
	public AspectInvocation(Object aspectObject, Method method, Object[] args, AspectInvocationHandler handler) {
		this.aspectObject=aspectObject;
		this.method=method;
		this.args=args;
		this.handler=handler;
	}

	/**
	 * Passes the method invocation to the next Interceptor
	 * in the interceptor list.
	 */
	public Object invokeNext() throws Throwable {
		currentInterceptor++;
		try {
         // have we reached the aspectObject object??
			if(currentInterceptor==handler.composition.interceptors.length) {
            // Do we need to lazy load the aspectObject object.
            if( handler.targetObject == null ) {
               // test again in a synchronized block because we only want to lazyload once
               // even if called concurrently.
               synchronized( handler ) {
                  if( handler.targetObject == null ) {
                     Class clazz = handler.targetClass;
                     if( clazz == null )
                        clazz = handler.composition.targetClass;
                     if( clazz == null )
                        throw new RuntimeException("Cannot lazyload aspect aspectObject object (aspectObject class not set)");
      
                     // create an instance of the class               
                     handler.targetObject = clazz.newInstance();
                  }
               }
            }
				return method.invoke(handler.targetObject, args);
         }
			return handler.composition.interceptors[currentInterceptor].invoke(this);
		} finally {
			currentInterceptor--;
		}
	}
	
	/**
	 * Returns the configuration Object that the current 
	 * interceptor created for the aspect configuration.
	 * 
	 * This configuration object will be shared by multiple
	 * aspect object created from the same aspect defintion.
	 */
	public Object getInterceptorConfig() {
		return handler.composition.interceptorConfigs[currentInterceptor];
	}

	/**
	 * Returns the attachment object that that was previously
	 * attached by the current interceptor.  The attachement 
	 * is made on against aspect object.
	 */
	public Object getInterceptorAttachment() {
		return handler.getAttachments()[currentInterceptor];
	}
	
	/**
	 * Attaches a object that to the aspect object.  This object 
	 * can be retrieve later by a <code>getInterceptorAttachment()</code>
	 * method call.
	 */	
	public void setInterceptorAttachment(Object attachment) {
		handler.getAttachments()[currentInterceptor] = attachment;
	}
	
}
