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

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.jboss.aspect.AspectInterceptor;
import org.jboss.aspect.proxy.AspectInitizationException;
import org.jboss.aspect.proxy.AspectInvocation;
import org.jboss.aspect.util.*;

/**
 * The DelegatingInterceptor allows you delegate method calls to 
 * another class, the delegate, instead of sending method calls 
 * to the base class.
 * 
 * This Interceptor will add all the interfaces of the delegate to
 * the aspect object.
 * 
 * This interceptor uses the following configuration attributes:
 * <ul>
 * <li>delegate - class name of the object that will be used to delegate
 *                method calls to.  This is a required attribute.
 * <li>singlton - if set to "true", then the method calls of multiple
 *                aspect object will be directed to a single instance of
 *                the delegate.  This makes the delegate a singleton. 
 * </ul>
 * 
 * @author <a href="mailto:hchirino@jboss.org">Hiram Chirino</a>
 * 
 */
public class DelegatingInterceptor implements AspectInterceptor {

	private static class Config {
		public Object singeltonObject;
		public Class  []interfaces;
		public Class  implementingClass;
		public Set    exposedMethods;
	}

	/**
	 * @see com.chirino.aspect.AspectInterceptor#invoke(AspectInvocation)
	 */
	public Object invoke(AspectInvocation invocation) throws Throwable {
		Config c = (Config)invocation.getInterceptorConfig();
		Object o = null;
		
		if( c.singeltonObject != null) {
			o = c.singeltonObject;
		} else {
			o = invocation.getInterceptorAttachment();
			if( o == null ) {
				o = c.implementingClass.newInstance();
				invocation.setInterceptorAttachment(o);
			}
		}
		if( c.exposedMethods.contains(invocation.method) ) 
			return invocation.method.invoke(o, invocation.args);
	
		return invokeNext(invocation);
	}
	
	public Object invokeNext(AspectInvocation invocation) throws Throwable {
		return invocation.invokeNext();
	}


	/**
	 * Builds a Config object for the interceptor.
	 * 
	 * @see com.chirino.aspect.AspectInterceptor#translateConfiguration(Element)
	 */
	public Object translateConfiguration(Map properties) throws AspectInitizationException {
		try {
			Config rc= new Config();
			
			String className = (String)properties.get("delegate");
			rc.implementingClass = Thread.currentThread().getContextClassLoader().loadClass(className);
			rc.interfaces = rc.implementingClass.getInterfaces();
			rc.exposedMethods = AspectSupport.getExposedMethods(rc.interfaces);				
			
			String singlton = (String)properties.get("singlton");
			if( "true".equals(singlton) )
				rc.singeltonObject = rc.implementingClass.newInstance();
				
			return rc;
		} catch (Exception e) {
			throw new AspectInitizationException("Aspect Interceptor missconfigured: "+e);
		}
	}
	
	/**
	 * @see AspectInterceptor#getInterfaces(Object)
	 */
	public Class[] getInterfaces(Object configuration) {
		Config c = (Config)configuration;
		return c.interfaces;
	}


}
