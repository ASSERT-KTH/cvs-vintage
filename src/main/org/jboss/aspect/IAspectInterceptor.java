/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.aspect;

import java.lang.reflect.Method;
import java.util.Map;

import org.jboss.aspect.proxy.AspectInitizationException;
import org.jboss.aspect.proxy.AspectInvocation;

/**
 * Implement the AspectInterceptor interface to add custom
 * behaviors to an aspect.
 * <p>
 * 
 * You must keep in mind all AspectInterceptor implementations must be 
 * thread safe.  A single AspectInterceptor instance will
 * be used to fillter through the method calls made on multiple
 * aspect objects.
 * <p>
 * 
 * AspectInterceptor implementations must be public and have a default 
 * constructor.
 * <p>
 * 
 * @see org.jboss.aspect.proxy.AspectInvocation
 *
 * @author <a href="mailto:hchirino@jboss.org">Hiram Chirino</a>
 */
public interface IAspectInterceptor {

   public static final Class NO_INTERFACES[] = new Class[]{};

	/**
	 * Used to initialize the interceptor.
	 * <p>
	 * 
	 * The properties map will contain all the attribute 
	 * name,value pairs that were configured for the interceptor
	 * in an aspect definition. The name and value objects will
	 * be String types.
	 * 
	 * @throws AspectInitizationException if the configuration 
	 *          object could due to any error.
	 */	
	public void init(Map properties) throws AspectInitizationException;

	/**
	 * Lists the interfaces this interceptor will be adding 
	 * to the aspect for the given interceptor configuration.
	 * <p>
	 * 
	 * The order in which the interfaces are listed matters if methods 
	 * are duplicated by the interfaces.  The first interface listed is 
	 * what will be returned by the <code>method.getDeclaringClass()</code>
	 * passed in the AspectInvocation objects via the <code>invoke(...)</code>
	 * call.
	 * <p>
	 * 
	 * If no interfaces are exposed by the interceptor, return an empty array.
	 * 
	 * @return the list of interfaces that this interceptor will be implementing.
	 */	
	public Class[] getInterfaces();

   /**
    * Used to know if an Interceptor would be doing some
    * processing of a method call.
    * 
    * @return true if the interceptor is interested in the method call.
    */   
   public boolean isIntrestedInMethodCall(Method method);

	/**
	 * Process or filter through a method invocation made on an aspect
	 * object.
	 * <p>
	 * 
	 * If you want delegate the method call to the next interceptor,
	 * you can just do a:
	 * 
	 * <code><pre>
	 *return invocation.invokeNext();
	 * </pre></code.
	 * 
	 * Otherwise you must inspect the AspectInvocation object and provide
	 * the appropriate behavior for the method call.
	 * 
	 * @see AspectInvocation
	 * @return the result of the invocation.
	 * @throws Throwable 
	 */	
	public Object invoke(AspectInvocation invocation) throws Throwable;
	
}
