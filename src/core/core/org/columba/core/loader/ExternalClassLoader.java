package org.columba.core.loader;

import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class ExternalClassLoader extends URLClassLoader {

	/**
	 * Constructor for ExternalClassLoader.
	 * @param urls
	 * @param parent
	 */
	public ExternalClassLoader(URL[] urls, ClassLoader parent) {
		super(urls, parent);
	}

	/**
	 * Constructor for ExternalClassLoader.
	 * @param urls
	 */
	public ExternalClassLoader(URL[] urls) {
		super(urls);
	}

	/**
	 * Constructor for ExternalClassLoader.
	 * @param urls
	 * @param parent
	 * @param factory
	 */
	public ExternalClassLoader(
		URL[] urls,
		ClassLoader parent,
		URLStreamHandlerFactory factory) {
		super(urls, parent, factory);
	}

	public void addURL(URL url) {
		super.addURL(url);
	}

	public Class findClass(String className) throws ClassNotFoundException {
		Class temp = super.findClass(className);
		return temp;
	}

	public Object instanciate(String className) throws Exception {
		Class actClass = findClass(className);

		return actClass.newInstance();
	}

	public Object instanciate(String className, Object[] args)
		throws Exception {

		Class actClass = findClass(className);

		Constructor[] constructors = actClass.getConstructors();
		Constructor constructor = constructors[0];

		return constructor.newInstance(args);
	}

}
