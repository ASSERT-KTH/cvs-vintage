/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.aspect;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.Hashtable;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.jboss.aspect.proxy.AspectInitizationException;
import org.jboss.aspect.proxy.AspectInvocationHandler;
import org.jboss.aspect.proxy.AspectProxyImplementationFactory;
import org.jboss.aspect.util.AspectSupport;
import org.jboss.aspect.util.XMLConfiguration;
import org.jboss.proxy.compiler.Proxy;
import org.jboss.proxy.compiler.ProxyCompiler;

/**
 * The AspectClassLoader allows you to load aspect objects like normal
 * Java objects.
 * <p>
 * 
 * If you ask the AspectClassLoader to load a class which cannot be 
 * loaded by the parent classloader, it will attempt to load the
 * corresponding class ".aspect" file.  So, if you tried to load
 * the "org.foo.Test" class, it would try to load the "org/foo/Test.aspect"
 * file.  If the aspect file is found, you will be handed back a 
 * Class object that will instaniate new instances of the aspect
 * defined in the file.
 * <p>
 * 
 * The format of the aspect file should look like:
 * <code><pre>&lt;!-- Define the aspect composition here --&gt;
 *&lt;aspect name=&quot;org.foo.Test&quot;&gt;
 *	&lt;!-- An aspect definition can have multiple interceptors --&gt;
 *	&lt;interceptor class=&quot;org.jboss.aspect.interceptors.DelegatingInterceptor&quot;&gt;
 *		&lt;!-- An interceptor may need multiple attributes to configure it --&gt;
 *		&lt;attribute name=&quot;delegate&quot; value=&quot;java.lang.Thread&quot;/&gt;
 *	&lt;/interceptor&gt;
 *&lt;/aspect&gt;
 * </pre></code>
 * 
 * 
 * This AspectClassLoader work just like any other class loader, you can
 * use it directly as the following example shows:
 * 
 * <code><pre>
 *ClassLoader loader = new AspectClassLoader();
 *Object o = loader.loadClass("org.foo.Test").newInstance();
 *Runnable aspect = (Runnable)o;
 *aspect.run();
 * </pre></code>
 * 
 * But a better idea is for you application classes to have been loaded
 * by a ClassLoader higharchy which allready contained an AspectClassLoader.
 * <p>
 * 
 * @see org.jboss.aspect.AspectFactory
 *
 * @author <a href="mailto:hchirino@jboss.org">Hiram Chirino</a>
 */
public class AspectClassLoader extends java.lang.ClassLoader {

	Hashtable classes = new Hashtable();
	Hashtable aspectDefinitions = new Hashtable();

	public AspectClassLoader() {
		super(ClassLoader.getSystemClassLoader());
	}

	public AspectClassLoader(ClassLoader parent) {
		super(parent);
	}

	protected Class loadClass(String class_name, boolean resolve)
		throws ClassNotFoundException {
		Class cl = null;

		// First try: lookup hash table.
		cl = (Class) classes.get(class_name);
		if (cl != null)
			return cl;

		// 2nd try: Does my parent have the class?
		try {
			return getParent().loadClass(class_name);
		} catch (ClassNotFoundException e) {
		}

		// 3rd try: is it an Aspect??
		cl = loadAspect(class_name);
		classes.put(class_name, cl);

		return cl;
	}

	protected Class loadAspect(String class_name)
		throws ClassNotFoundException {

		// Find the aspect definition for the aspect class.
		String aspectDefPath = class_name.replace('.', '/') + ".aspect";
		URL aspectURL = this.getResource(aspectDefPath);
		if (aspectURL == null)
			throw new AspectNotFoundException(class_name);

		// Parse the aspect file.
		AspectDefinition aspectDefinition;
		try {
			SAXReader xmlReader = new SAXReader();
			Document doc = xmlReader.read(aspectURL);
			aspectDefinition =
				XMLConfiguration.loadAspectObjectDefinition(
					doc.getRootElement());
			if (!class_name.equals(aspectDefinition.name))
				throw new ClassFormatError(
					aspectDefPath
						+ " invalid: aspect name did not match class name");

		} catch (AspectInitizationException e) {
			throw new ClassFormatError(aspectDefPath + " invalid: " + e);
		} catch (DocumentException e) {
			throw new ClassFormatError(aspectDefPath + " invalid: " + e);
		}

		try {
         
         Class interfaces[] = aspectDefinition.interfaces;
         if( aspectDefinition.targetClass != null )
            interfaces = AspectSupport.appendInterfaces(interfaces, aspectDefinition.targetClass);

			Set mset = AspectSupport.getExposedMethods(interfaces);
			Method m[] = new Method[mset.size()];
			mset.toArray(m);

			// Build the class byte codes.
			ProxyCompiler pc =
				new ProxyCompiler(
					this,
					Object.class,
					interfaces,
					m,
					new AspectProxyImplementationFactory());

			Class proxyClass = pc.getProxyType();
			aspectDefinitions.put(proxyClass, aspectDefinition);
			return proxyClass;

		} catch (Exception e) {
			throw new ClassFormatError(
				class_name + ": Could not generate dynamic proxy: " + e);
		}
		/*		
				AspectInvocationHandler h = new AspectInvocationHandler(aspectDefinition, null);
		*/
	}

	public AspectDefinition getAspectDefinition(Class clazz) {
		return (AspectDefinition) aspectDefinitions.get(clazz);
	}
}