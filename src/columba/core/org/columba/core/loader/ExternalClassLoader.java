//The contents of this file are subject to the Mozilla Public License Version 1.1
//(the "License"); you may not use this file except in compliance with the 
//License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License 
//for the specific language governing rights and
//limitations under the License.
//
//The Original Code is "The Columba Project"
//
//The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003. 
//
//All Rights Reserved.
package org.columba.core.loader;

import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;

import org.columba.core.logging.ColumbaLogger;

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
		
		//ColumbaLogger.log.debug("class="+className);

		Class actClass = findClass(className);

		Constructor[] constructors = actClass.getConstructors();
		Constructor constructor = constructors[0];

		return constructor.newInstance(args);
	}

}
