/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.aspect.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.jboss.aspect.AspectInterceptor;
import org.jboss.aspect.proxy.AspectInvocation;

/**
 *
 * Holds functions that were usefull during the implemenation of
 * the Aspect related classes.
 * 
 * @author <a href="mailto:hchirino@jboss.org">Hiram Chirino</a>
 */
final public class AspectSupport {

	/**
	 * Returns a Set containing all the methods that were defined
	 * the a list of interfaces.
	 */
	static public Set getExposedMethods(Class[] interfaces) {
		
		Set set = new HashSet();
		for( int i=0; i < interfaces.length; i ++ ) {
			Method t[] = interfaces[i].getMethods();
			for( int j=0; j < t.length; j++ ) {
				Method method = null;
				
				// If it is a Object method use that method instead.
				try {
					method = Object.class.getMethod(t[j].getName(), t[j].getParameterTypes());
				} catch ( NoSuchMethodException e ) {
				}
				
				if( method == null )
					method = t[j];
					
				set.add(method);
			}
		}
		return set;
	}

}
