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

/**
 * @author fdietz
 *  
 */
public class ClassLoaderHelper {

	/**
	 * @param args
	 * @param actClass
	 * @return @throws
	 *         NoSuchMethodException
	 */
	public static Constructor findConstructor(Object[] args, Class actClass)
			throws NoSuchMethodException {

		Constructor constructor = null;

		Constructor[] list = actClass.getConstructors();

		for (int i = 0; i < list.length; i++) {
			Constructor c = list[i];

			Class[] parameterTypes = c.getParameterTypes();

			// this constructor has the correct number
			// of arguments
			if (parameterTypes.length == args.length) {
				boolean success = true;

				for (int j = 0; j < parameterTypes.length; j++) {
					Class parameter = parameterTypes[j];

					if (args[j] == null) {
						success = true;
					} else if (!parameter.isAssignableFrom(args[j].getClass())) {
						success = false;
					}
				}

				// ok, we found a matching constructor
				// -> create correct list of arguments
				if (success) {
					constructor = actClass.getConstructor(parameterTypes);
				}
			}
		}
		return constructor;
	}
}