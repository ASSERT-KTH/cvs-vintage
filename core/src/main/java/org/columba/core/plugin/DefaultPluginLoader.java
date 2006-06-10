// The contents of this file are subject to the Mozilla Public License Version
// 1.1
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
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.core.plugin;

import java.lang.reflect.Constructor;
import java.util.logging.Logger;

import org.columba.api.plugin.IExtensionInterface;

/**
 * Use default classloader to instanciate a class using a constructor
 * with matching arguments.
 * <p>
 * Currently, this is not used.
 * 
 * @author fdietz
 */
public class DefaultPluginLoader  {

	private static final Logger LOG = Logger
			.getLogger("org.columba.core.loader");

	// we can't use SystemClassLoader here, because that doesn't work
	// with java webstart
	// -> instead we use this.getClass().getClassLoader()
	// -> which seems to work perfectly

	/*
	 * protected static ClassLoader loader = ClassLoader.getSystemClassLoader();
	 */
	ClassLoader loader;

	public DefaultPluginLoader() {
		super();

		loader = this.getClass().getClassLoader();

	}

	public IExtensionInterface loadPlugin(String id, String className,
			Object[] arguments) throws Exception {

		if (className == null)
			throw new IllegalArgumentException("className == null");

		IExtensionInterface plugin = null;

		Class actClass;

		actClass = loader.loadClass(className);

		//
		// we can't just load the first constructor
		// -> go find the correct constructor based
		// -> based on the arguments
		//
		if ((arguments == null) || (arguments.length == 0)) {

			plugin = (IExtensionInterface) actClass.newInstance();

		} else {
			Constructor constructor;

			constructor = ClassLoaderHelper
					.findConstructor(arguments, actClass);

			// couldn't find correct constructor
			if (constructor == null) {
				LOG.severe("Couldn't find constructor for " + className
						+ " with matching argument-list: ");
				for (int i = 0; i < arguments.length; i++) {
					LOG.severe("argument[" + i + "]=" + arguments[i]);
				}

				return null;
			} else {

				plugin = (IExtensionInterface) constructor
						.newInstance(arguments);

			}

		}

		return plugin;
	}

}
