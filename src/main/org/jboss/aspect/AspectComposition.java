/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.aspect;

import java.util.ArrayList;
import java.util.Map;

import org.jboss.aspect.proxy.AspectInitizationException;

/**
 * The AspectComposition holds the aspect definition for a single aspect.
 * <p>
 * 
 * Multiple instances of the same aspect will share the AspectComposition
 * configuration object.  This class is immutable and, therefore, threadsafe.
 * <p>
 * 
 * AspectComposition objects can be dynamicaly created at runtime and passed
 * to the AspectFactory to create dynamicaly generated aspects.
 * <p>
 * 
 * @see org.jboss.aspect.AspectFactory#createAspect(AspectComposition)
 *
 * @author <a href="mailto:hchirino@jboss.org">Hiram Chirino</a>
 */
final public class AspectComposition {
	
	/**
	 * Constructor.
	 * 
	 * @param name The name of the aspect.
	 * @param interceptorNames The list of interceptors 
	 *         that will be used to compose the aspect.
	 * @param config The configurations that will be used to 
	 *         initialize the interceptors
	 * @param baseClassName The name of the optinal base class that
	 *         this aspect will operate on, can be null.
	 */
	public AspectComposition(String name, String[] interceptorNames, Map[] config, String baseClassName) throws AspectInitizationException {
		this.name = name;
		ClassLoader cl = Thread.currentThread().getContextClassLoader();		
		
		try {
			this.baseClass = baseClassName==null ? null : cl.loadClass(baseClassName);
		} catch ( Exception e ) {
			throw new AspectInitizationException("Invalid Aspect base class : "+baseClassName+": "+e);
		}
		
		this.interceptors = new AspectInterceptor[interceptorNames.length];
		this.interceptorConfigs = new Object[interceptorNames.length];
		ArrayList interfaceList = new ArrayList();
		for( int i=0; i < interceptorNames.length; i++ ) {
			try {
				this.interceptors[i] = (AspectInterceptor)cl.loadClass(interceptorNames[i]).newInstance();
			} catch ( Exception e ) {
				throw new AspectInitizationException("Invalid Aspect Interceptor: "+interceptorNames[i]+": "+e);
			}
			this.interceptorConfigs[i] = this.interceptors[i].translateConfiguration(config[i]);
			Class t[] = this.interceptors[i].getInterfaces(interceptorConfigs[i]);
			for( int j=0; j < t.length; j++ ) {
				if( interfaceList.contains(t[j]) )
					continue;
				interfaceList.add( t[j] );
			}
		}
			
		this.interfaces = new Class[interfaceList.size()];
		interfaceList.toArray(this.interfaces);
	}

	final public String                     name;
	final public AspectInterceptor 		 interceptors[];
	final public Object                     interceptorConfigs[];
	final public Class                      interfaces[];
	final public Class                      baseClass;
}
