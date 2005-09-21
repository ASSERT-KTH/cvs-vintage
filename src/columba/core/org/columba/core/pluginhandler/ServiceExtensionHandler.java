package org.columba.core.pluginhandler;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.columba.api.exception.PluginException;
import org.columba.api.exception.PluginLoadingFailedException;
import org.columba.api.plugin.IExtensionInterface;
import org.columba.core.logging.Logging;
import org.columba.core.plugin.Extension;
import org.columba.core.plugin.ExtensionHandler;
import org.columba.core.scripting.service.IColumbaService;

public class ServiceExtensionHandler extends ExtensionHandler {

	private static final Logger LOG = Logger
			.getLogger("org.columba.core.ServicePluginHandler");

	public static final String XML_RESOURCE = "/org/columba/core/plugin/services.xml";

	public static final String NAME = "org.columba.core.service";

	private final Map serviceList;

	public ServiceExtensionHandler() {

		/* only initialize this core service */
		super(NAME);

		InputStream is = this.getClass().getResourceAsStream(XML_RESOURCE);
		loadExtensionsFromStream(is);

		serviceList = new HashMap();

	}
	
	/**
	 * Retrieve service instance. <code>ExtensionHandler</code> automatically
	 * handles singleton extensions. We don't need to cache instances.
	 * 
	 * @param extension		extension metadata
	 * @return				instance of extension interface
	 */
	private IColumbaService getServiceInstance(Extension extension) {
		
		IExtensionInterface service = null;
		try {
			service = (IExtensionInterface) extension
					.instanciateExtension(new Object[] {});
		} catch (PluginException e1) {
			LOG.severe("Failed to load service: " + e1.getMessage());

			if (Logging.DEBUG)
				e1.printStackTrace();
			
			return null;
		}

		if (!(service instanceof IColumbaService)) {
			LOG.log(Level.WARNING,
			"Service plugin doesn't explicitly declare an "
				+ "IColumbaService interface. Service ignored...");
			return null;
		}
		
		return (IColumbaService) service;
		
	}

	/**
	 * Instanciate all services.
	 *
	 */
	public void initServices() {
		Enumeration e = getExtensionEnumeration();
		while (e.hasMoreElements()) {
			Extension extension = (Extension) e.nextElement();

			// retrieving the instance for the first time
			// creates an instance in ExtensionHandler subclass
			// 
			// instance reference is kept in hashmap automatically
			IColumbaService service = getServiceInstance(extension);
		}

	}

	public void disposeServices() {
		Enumeration e = getExtensionEnumeration();
		while (e.hasMoreElements()) {
			Extension extension = (Extension) e.nextElement();
			IColumbaService service = getServiceInstance(extension);
			service.disposeService();
		}
	}

	public void startServices() {
		Enumeration e = getExtensionEnumeration();
		while (e.hasMoreElements()) {
			Extension extension = (Extension) e.nextElement();
			IColumbaService service = getServiceInstance(extension);
			service.startService();
		}

	}

	public void stopServices() {
		Enumeration e = getExtensionEnumeration();
		while (e.hasMoreElements()) {
			Extension extension = (Extension) e.nextElement();
			IColumbaService service = getServiceInstance(extension);
			service.stopService();
		}
	}

}
