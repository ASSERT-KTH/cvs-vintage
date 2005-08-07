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

import java.util.Hashtable;
import java.util.Map;

import org.columba.api.exception.ServiceNotFoundException;

/**
 * Service registry and locator. Upper layers can register a service which can
 * be used by others. A service is registered using its full interface name
 * including package and the name of the implementation.
 * <p>
 * <code>ServiceManager</code> uses reflection to instanciate the
 * implementation.
 * 
 * <p>
 * For example: Mail component makes use of the addressbook component
 * 
 * @author fdietz
 */
public class ServiceRegistry {

	private static ServiceRegistry instance;

	private Map map;

	private ServiceRegistry() {
		map = new Hashtable();

	}

	public static ServiceRegistry getInstance() {
		if (instance == null)
			instance = new ServiceRegistry();

		return instance;
	}

	public void register(Class serviceInterface, String serviceImplementation) {
		Service service = new Service(serviceInterface, serviceImplementation);

		map.put(serviceInterface, service);
	}

	public void register(Class serviceInterface, Object serviceInstance) {
		Service service = new Service(serviceInterface, serviceInstance);
		
		map.put(serviceInterface, service);
	}
	
	public Object getService(Class serviceInterface)
			throws ServiceNotFoundException {
		Object o = null;
		Service service = null;

		// check if service is registered
		if (map.containsKey(serviceInterface)) {
			service = (Service) map.get(serviceInterface);

			// retrieve service instance
			if (service != null)
				o = service.getServiceInstance();
		}

		if (o == null)
			throw new ServiceNotFoundException(serviceInterface);

		return o;
	}
}