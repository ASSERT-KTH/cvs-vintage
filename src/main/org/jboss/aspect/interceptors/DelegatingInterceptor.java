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

import org.jboss.aspect.IAspectInterceptor;
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
 * <li>singleton - if set to "true", then the method calls of multiple
 *                aspect object will be directed to a single instance of
 *                the delegate.  This makes the delegate a singleton. 
 * </ul>
 * 
 * @author <a href="mailto:hchirino@jboss.org">Hiram Chirino</a>
 * 
 */
public class DelegatingInterceptor implements IAspectInterceptor {

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
            
		Object delegate = null;		
		if( c.singeltonObject != null) {
			delegate = c.singeltonObject;
		} else {
			delegate = invocation.getInterceptorAttachment();
			if( delegate == null ) {
				delegate = AspectSupport.createAwareInstance(c.implementingClass, invocation.handler);
				invocation.setInterceptorAttachment(delegate);
			}
		}
		return invocation.method.invoke(delegate, invocation.args);
	
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
			
			String singlton = (String)properties.get("singleton");
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
   
   public boolean isIntrestedInMethodCall(Object configuration, Method method)
   {
      return ((Config)configuration).exposedMethods.contains(method);
   }

}
