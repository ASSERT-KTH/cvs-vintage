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
package org.columba.core.services;

import java.util.HashMap;
import java.util.Map;

/**
 * @author fdietz
 *  
 */
public class ServiceManager {

	private static ServiceManager instance;

	private Map map;

	private ServiceManager() {

		map = new HashMap();
	}

	public static ServiceManager getInstance() {
		if (instance == null)
			instance = new ServiceManager();

		return instance;
	}

	private Object loadInstance(String className) {
		Object object = null;

		try {
			Class clazz = this.getClass().getClassLoader().loadClass(className);

			object = clazz.newInstance();

		} catch (ClassNotFoundException e) {

			e.printStackTrace();
		} catch (InstantiationException e) {

			e.printStackTrace();
		} catch (IllegalAccessException e) {

			e.printStackTrace();
		}

		return object;
	}

	public void register(String serviceName, String className) {
		map.put(serviceName, className);
	}

	public Object createService(String serviceName)
			throws ServiceNotFoundException {
		Object o = null;
		String className = null;
		if (map.containsKey(serviceName)) {
			className = (String) map.get(serviceName);

			if (className != null)
				o = loadInstance(className);
		}

		if (o == null)
			throw new ServiceNotFoundException(serviceName, className);

		return o;
	}
}