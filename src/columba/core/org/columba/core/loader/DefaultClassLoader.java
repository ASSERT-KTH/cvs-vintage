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

/**
 * Classloader responsible for instanciating plugins which are
 * located inside the Columba sources.
 * <p>
 * Note, that this classloader tries to find the correct
 * constructor based on the arguments.
 * 
 * @author fdietz
 */
public class DefaultClassLoader {

	// we can't use SystemClassLoader here, because that doesn't work
	// with java webstart
	// -> instead we use this.getClass().getClassLoader()
	// -> which seems to work perfectly
	/*
	protected static ClassLoader loader = ClassLoader.getSystemClassLoader();
	*/
	
	ClassLoader loader;
	/**
	 * Constructor for CClassLoader.
	 */
	public DefaultClassLoader() {
		super();
		
		loader = this.getClass().getClassLoader();
	}

	public Object instanciate(String className) throws Exception {

		//ColumbaLogger.log.debug("class="+className);

		Class actClass = loader.loadClass(className);

		return actClass.newInstance();
	}

	public Object instanciate(String className, Object[] args)
		throws Exception {

		//ColumbaLogger.log.debug("class="+className);

		Class actClass = loader.loadClass(className);

		Constructor constructor = null;

		// FIXME
		//
		// we can't just load the first constructor 
		//  -> go find the correct constructor based
		//  -> based on the arguments
		//
		//    old solution and wrong:
		//Constructor constructor = actClass.getConstructors()[0];//argClazz);
		//
		
		if ( ( args == null ) || (args.length == 0) ) {
			constructor = actClass.getConstructors()[0];
			
			return constructor.newInstance(args);
		}

		Constructor[] list = actClass.getConstructors();

		Class[] classes = new Class[args.length];

		for (int i = 0; i < list.length; i++) {
			Constructor c = list[i];

			Class[] parameterTypes = c.getParameterTypes();

			// this constructor has the correct number 
			// of arguments
			if (parameterTypes.length == args.length) {
				boolean success = true;
				for (int j = 0; j < parameterTypes.length; j++) {
					Class parameter = parameterTypes[j];

					if (args[j] == null)
						success = true;

					else if (!parameter.isAssignableFrom(args[j].getClass()))
						success = false;
				}

				// ok, we found a matching constructor
				// -> create correct list of arguments
				if (success) {
					constructor = actClass.getConstructor(parameterTypes);
				}

			}
		}

		

		// couldn't find correct constructor
		if (constructor == null)
			return null;

		return constructor.newInstance(args);
	}
}
