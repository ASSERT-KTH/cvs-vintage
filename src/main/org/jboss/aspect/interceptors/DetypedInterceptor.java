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
 * DetypedInterceptor can be used as the base class for any detyped 
 * interceptors.  
 * 
 * Interceptors of his type uses the following configuration attributes:
 * <ul>
 * <li>method-filter - String the defines which method calls this detyped
 * interceptor will be used in.  If not set or set to "*", then all
 * method calls will be intercepted.  TODO: implement some regular expression
 * based filtering.  Right now, only methods that begin with the string defined
 * in method-filter will be intercepted.
 * </ul>
 * 
 * @author <a href="mailto:hchirino@jboss.org">Hiram Chirino</a>
 */
abstract public class DetypedInterceptor implements IAspectInterceptor {
   
   public static class Configuration {
      String methodFilter;
   }
   
   /**
    * Since this is a detyped interceptor, this interceptor cannot 
    * expose any interfaces.  We return an empty Class[]
    * 
    * @see AspectInterceptor#getInterfaces(Object)
    */
   public Class[] getInterfaces(Object chainConfiguration) {
      return new Class[]{};
   }   

	/**
    * Subclasses that override this method should also override 
    * isIntrestedInMethodCall.
    * 
	 * @see AspectInterceptor#translateConfiguration(Map)
	 */
	public Object translateConfiguration(Map properties) throws AspectInitizationException  {
      Configuration c = new Configuration();
      c.methodFilter = (String)properties.get("method-filter");
      if( c.methodFilter != null && c.methodFilter.equals("*"))
         c.methodFilter = null;         
		return c;
	}
	   
   /**
    * Uses the method-filter configuration attribute to determine
    * if this interceptor is interested in the method call.
    */
   public boolean isIntrestedInMethodCall(Object configuration, Method method)
   {
      Configuration c = (Configuration)configuration;
      if( c.methodFilter == null )
         return true;         
      return method.getName().startsWith(c.methodFilter);
   }
   
}
