/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.aspect;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.jboss.aspect.proxy.AspectInitizationException;
import org.jboss.aspect.proxy.AspectInvocationHandler;
import org.jboss.aspect.util.XMLConfiguration;
import org.jboss.proxy.compiler.InvocationHandler;
import org.jboss.proxy.compiler.Proxy;

/**
 * The AspectFactory creates new instances of aspects objects.
 * <p>
 * 
 * An aspect is povides you a simple and dynamic way to compose
 * new objects or to dynamicaly alter the behavior of existing 
 * objects.  An aspect works on the principle that the interfaces
 * that an object provides can be extended/modified at runtime by 
 * using a Dynamic Proxy to filter the method calls destined
 * to the object through a list of AspectInterceptor objects.
 * <p>
 * 
 * The AspectInterceptor interface allows you to implement
 * custom aspect behaviors.  The behaviors that a AspectInterceptor
 * can implement are only limited by your imagination.  Keep
 * in mind that this "interceptor" pattern is what implements the 
 * EJB containers in jboss (interceptors provide security, transaction
 * managment, lazy loading, etc.) 
 * <p>
 * 
 * Before you can create an aspect with an AspectFactory,
 * you must configure the AspectFactory.  The simplest way to do 
 * this is shown in the following example:
 * <code><pre>
 *AspectFactory af = new AspectFactory().configure();
 * </pre></code>
 * 
 * This will configure the AspectFactory by searching the CLASSPATH 
 * for the "aspect-config.xml" file.  The format of the aspect-config.xml
 * XML looks like:
 * <code><pre>
 *&lt;!-- Sample aspect-config.xml file --&gt; 
 *&lt;aspects&gt; 
 *	&lt;!-- You can list multiple aspect definitions here --&gt;
 *	&lt;aspect name=&quot;org.foo.Test&quot;&gt;
 *		&lt;!-- An aspect definition can have multiple interceptors --&gt;
 *		&lt;interceptor class=&quot;org.jboss.aspect.interceptors.DelegatingInterceptor&quot;&gt;
 *			&lt;!-- An interceptor may need multiple attributes to configure it --&gt;
 *			&lt;attribute name=&quot;delegate&quot; value=&quot;java.lang.Thread&quot;/&gt;
 *		&lt;/interceptor&gt;
 *	&lt;/aspect&gt;
 *&lt;/aspects&gt; 
 * </pre></code>
 *
 * You can add multiple aspect configurations to the AspectFactory by 
 * using the <code>configure(URL configSource)</code> method.  Example:
 * <code><pre>
 *AspectFactory af = 
 *	new AspectFactory().configure(new URL("file:aspect-set1.xml"))
 *		.configure(new URL("file:aspect-set2.xml"));
 * </pre></code>
 * 
 * After the AspectFactory has been configured, you can create aspects by using
 * one of the <code>createAspect(...)</code> methods.  Example:
 * 
 * <code><pre>s
 *AspectFactory af = new AspectFactory().configure();
 *Runnable aspect = (Runnable)af.createAspect("org.foo.Test");
 *aspect.run();
 * </pre></code>
 * 
 * The previous example looks up the aspected named "org.foo.Test" which is composed 
 * of a single DelegatingInterceptor (based on our sample aspect-config.xml).
 * The DelegatingInterceptor exposes all the Interfaces implemented by the 
 * class specifed via the delegate attribute (in our example, "java.lang.Thread").
 * The DelegatingInterceptor associates a new instance of the delegate with 
 * the aspect object.  It then and diverts all method calls to the delegate if they are 
 * operating on methods implemented by one of the delegate's interfaces. 
 * <p>
 * 
 * In otherwords, the Runnable.run() method will be serviced by a Thread object associated
 * the asspect object.
 * <p>
 * 
 * @see org.jboss.aspect.AspectInterceptor
 *
 * @author <a href="mailto:hchirino@jboss.org">Hiram Chirino</a>
 */
public class AspectFactory {

	private Map aspects = new HashMap();
		
	/**
	 * Searches for the <code>aspect-config.xml</code> file in the classpath
	 * and adds the aspects contained in the file to the AspectFactory configuration.
	 * 
	 * @throws AspectInitizationException if the aspect-config.xml file is not in the 
	 *          classpath or if it is invalid or some aspects could not be initialized.
	 */
	public AspectFactory configure() throws AspectInitizationException {
		URL source = Thread.currentThread().getContextClassLoader().getResource("aspect-config.xml");
		if( source == null )
			throw new AspectInitizationException("aspect-config.xml could not be found in the classpath");
		return configure(source);
	}

	/**
	 * Adds the the set of aspect definitions contained in the source URL
	 * to the AspectFactory configuration.
	 * <p>
	 * If the source contains a definition for a previously configured aspect,
	 * a AspectInitizationException will be thrown.
	 * 
	 * @AspectInitizationException if the source file is invalid or some aspects could not be initialized.
	 */
	public AspectFactory configure(URL source) throws AspectInitizationException {
		if( source == null )
			throw new NullPointerException("configuration source was null.");
			
		Map map = XMLConfiguration.loadAspects(source);
    	Iterator i = map.keySet().iterator();
    	while( i.hasNext() ) {
    		Object aspectName = i.next();
    		Object composition = map.get(aspectName);
			Object previous = aspects.put(aspectName, composition);
			if( previous!=null ) 
				throw new AspectInitizationException("Invalid Aspect configuration file: "+source); 
    	}		
		return this;
	}	

	/**
	 * Creates an aspect object from a given aspect name.
	 * 
	 * @throws AspectNotFoundException if the aspectName was not configured with the AspectFactory
	 */
	public Object createAspect(String aspectName) throws AspectNotFoundException {
		AspectComposition composition = getComposition(aspectName);
		if( composition == null )
			throw new AspectNotFoundException(aspectName);
		return createAspect(composition);
	}

	/**
	 * Creates an aspect object from a given aspect name.
	 * 
	 * This form of createAspect forces the baseObject of the aspect to 
	 * be of type baseClass.  The baseObeject of the aspect will be a lazy 
	 * loaded instance of baseClass.  Therefore, the baseClass must have
	 * a public default constructor.
	 * <p>
	 * All interfaces of the baseClass will also be exposed by the aspect 
	 * object.
	 * 
	 * @throws AspectNotFoundException if the aspectName was not configured with the AspectFactory
	 */
	public Object createAspect(String aspectName, Class baseClass) throws AspectNotFoundException {
		AspectComposition composition = getComposition(aspectName);
		if( composition == null )
			throw new AspectNotFoundException(aspectName);
		return createAspect(composition,baseClass);
	}
		
	/**
	 * Creates an aspect object from a given aspect name.
	 * 
	 * This form of createAspect sets the baseObject of the aspect to 
	 * be the provided object.
	 * <p>
	 * All interfaces of the baseClass will also be exposed by the aspect 
	 * object.
	 * 
	 * @throws AspectNotFoundException if the aspectName was not configured with the AspectFactory
	 */
	public Object createAspect(String aspectName, Object baseObject) throws AspectNotFoundException {
		AspectComposition composition = getComposition(aspectName);
		if( composition == null )
			throw new AspectNotFoundException(aspectName);
		return createAspect(composition,baseObject);
	}
	
	/**
	 * Creates an aspect object from a given AspectComposition.
	 * 
	 * Instead of using XML files to define the aspects, you can 
	 * create new AspectComposition objects to create new types
	 * of aspects.
	 * 
	 */
	static public Object createAspect(AspectComposition composition) {
		InvocationHandler h = new AspectInvocationHandler(composition, null);
		Class interfaces[] = composition.interfaces;
		return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), interfaces, h);
	}

	/**
	 * Creates an aspect object from a given AspectComposition.
	 * 
	 * Instead of using XML files to define the aspects, you can 
	 * create new AspectComposition objects to create new types
	 * of aspects.
	 * 
	 */
	static public Object createAspect(AspectComposition composition, Object baseObject) {
		InvocationHandler h = new AspectInvocationHandler(composition, baseObject);
		Class interfaces[] = composition.interfaces;
		interfaces = appendInterfaces(interfaces, baseObject.getClass());
		return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), interfaces, h);
	}

	/**
	 * Creates an aspect object from a given AspectComposition.
	 * 
	 * Instead of using XML files to define the aspects, you can 
	 * create new AspectComposition objects to create new types
	 * of aspects.
	 * 
	 */
	static public Object createAspect(AspectComposition composition, Class baseClass) {
		if (baseClass==null) 
			baseClass = composition.baseClass;
									
		InvocationHandler h = new AspectInvocationHandler(composition, null);
		Class interfaces[] = composition.interfaces;
		
		if (baseClass!=null) 
			interfaces = appendInterfaces(interfaces, baseClass.getClass());
			
		return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), interfaces, h);
	}
	
	static private Class[] appendInterfaces( Class interfaces[], Class baseClass ) {
			
		ArrayList interfaceList = new ArrayList();

		for( int i=0; i < interfaces.length; i++ ) 
			interfaceList.add( interfaces[i] );
		
		interfaces = baseClass.getInterfaces();
		for( int i=0; i < interfaces.length; i++ ) {
			if( interfaceList.contains(interfaces[i]) )
				continue;
			interfaceList.add( interfaces[i] );
		}
	
		interfaces = new Class[interfaceList.size()];
		interfaces = (Class[])interfaceList.toArray(interfaces);
		
		return interfaces;
	}
		
	private AspectComposition getComposition(String aspect) {
		return (AspectComposition)aspects.get(aspect);
	}	
}
