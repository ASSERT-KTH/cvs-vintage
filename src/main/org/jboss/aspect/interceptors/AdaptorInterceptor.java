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
 * The AdaptorInterceptor allows you proivde add an adaptor
 * via the IAdaptor interface.
 * <p>
 * Sample:
 * <code>
 * ...
 * <aspect name="MyAspect">
 *   <interceptor class="AdaptorInterceptor">
 *      <attribute name="adaptor" value="java.lang.Runnable"/>
 *      <attribute name="implementation" value="Bar"/>
 *   </interceptor>
 *   <interceptor class="AdaptorInterceptor">
 *      <attribute name="adaptor" value="org.foo.Killable"/>
 *      <attribute name="implementation" value="Fiz"/>
 *   </interceptor>
 * </aspect>
 * ....
 * 
 * class Bar implements Runnable {
 *    ...
 * }
 * </code>
 * 
 * later you can do:
 * 
 * <code>
 * 
 * AspectFactory af = new AspectFactory().configure();
 * IAdaptor adaptor = (IAdaptor)af.createAspect("MyAspect", someObject);
 * 
 * Runnable r = (Runnable)adaptor.getAdapter(Runnable.class); 
 * r.run();
 * 
 * org.foo.Killable k = (org.foo.Killable)adaptor.getAdapter(org.foo.Killable.class); 
 * if( k != null )
 *    // do something with k
 * 
 * </code>
 * 
 * This interceptor uses the following configuration attributes:
 * <ul>
 * <li>adaptor  - The interface that the implementation object is exposing 
 *                via the Adaptable interface.  This is a required attribute. 
 * <li>implementation  - class name of the object that will be used to delegate
 *                method calls to.  This is a required attribute.
 * <li>singleton - if set to "true", then the method calls of multiple
 *                aspect object will be directed to a single instance of
 *                the delegate.  This makes the adaptor a singleton. 
 * </ul>
 * 
 * @author <a href="mailto:hchirino@jboss.org">Hiram Chirino</a>
 * 
 */
public class AdaptorInterceptor implements IAspectInterceptor {

	private static class Config {
		public Object singeltonObject;
		public Class implementingClass;
      public Class adaptorClass;
	}

   static final Method GET_ADAPTER_METHOD;
   static {
      Method m=null;
      try  {
         m = IAdaptor.class.getMethod("getAdapter", new Class[] { Class.class });
      } catch (NoSuchMethodException e) {
      }
      GET_ADAPTER_METHOD = m;
   }
   
	/**
	 * @see com.chirino.aspect.AspectInterceptor#invoke(AspectInvocation)
	 */
	public Object invoke(AspectInvocation invocation) throws Throwable {
      
		Config c = (Config)invocation.getInterceptorConfig();
      
      if( !c.adaptorClass.equals(invocation.args[0]) ) {
         if( invocation.isNextIntrestedInMethodCall() )
            return invokeNext(invocation);
         return null;
      }
      
      
		Object o = null;		
		if( c.singeltonObject != null) {
			o = c.singeltonObject;
		} else {
			o = invocation.getInterceptorAttachment();
			if( o == null ) {
				o = AspectSupport.createAwareInstance( c.implementingClass, invocation.handler );
				invocation.setInterceptorAttachment(o);
			}
		}
		return o;
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
			
			String adaptorName = (String)properties.get("adator");
         String className = (String)properties.get("implementation");
         
         rc.adaptorClass = Thread.currentThread().getContextClassLoader().loadClass(adaptorName);
			rc.implementingClass = Thread.currentThread().getContextClassLoader().loadClass(className);
			
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
		return new Class[] {IAdaptor.class};
	}
   
   public boolean isIntrestedInMethodCall(Object configuration, Method method)
   {
      return GET_ADAPTER_METHOD.equals(method);
   }
}
