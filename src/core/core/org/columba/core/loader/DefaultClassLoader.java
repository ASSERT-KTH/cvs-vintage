package org.columba.core.loader;

import java.lang.reflect.Constructor;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class DefaultClassLoader {

	protected static ClassLoader loader = ClassLoader.getSystemClassLoader();

	/**
	 * Constructor for CClassLoader.
	 */
	public DefaultClassLoader() {
		super();
	}

	public Object instanciate(String className) throws Exception {
		Class actClass = loader.loadClass(className);

		return actClass.newInstance();
	}

	public Object instanciate(String className, Object[] args)
		throws Exception {

		Class actClass = loader.loadClass(className);

		Constructor[] constructors = actClass.getConstructors();
		Constructor constructor = constructors[0];

		return constructor.newInstance(args);
	}
}
