package org.columba.core.services;

/**
 * A service is described with its service name, which is usually the name of
 * the interface.
 * <p>
 * Keeps a reference to the service instanciation.
 * 
 * @author Frederik Dietz
 */
public class Service {
	private Class serviceInterface;

	private String implementationName;

	private Object serviceInstance;

	public Service(Class serviceInterface, String implementationName) {
		this.serviceInterface = serviceInterface;
		this.implementationName = implementationName;
	}

	public Service(Class serviceInterface, Object serviceInstance) {
		this.serviceInterface = serviceInterface;

		this.serviceInstance = serviceInstance;
	}

	/**
	 * @return Returns the serviceInstance.
	 */
	public Object getServiceInstance() {
		// check if there's already an instanciation available
		// we can reuse here
		if (serviceInstance == null) {
			// load instance of service
			serviceInstance = loadInstance(implementationName);
		}

		return serviceInstance;
	}

	/**
	 * @return Returns the serviceName.
	 */
	public Class getServiceInterface() {
		return serviceInterface;
	}

	/**
	 * Load instance of class.
	 * <p>
	 * TODO: This can be only used for classes in the classpath. It won't work
	 * for services registred by plugins. Should we extend this here?
	 * 
	 * @param className
	 *            class name
	 * @return instance of class
	 */
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
}
