package org.columba.core.component;

import java.util.Enumeration;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.columba.api.exception.ServiceNotFoundException;
import org.columba.api.plugin.IExtension;
import org.columba.api.plugin.IExtensionHandler;
import org.columba.api.plugin.IExtensionHandlerKeys;
import org.columba.api.plugin.IPluginManager;
import org.columba.api.plugin.PluginException;
import org.columba.api.plugin.PluginHandlerNotFoundException;
import org.columba.core.logging.Logging;
import org.columba.core.services.ServiceRegistry;

public class ComponentManager implements IComponentPlugin {

	private static final Logger LOG = Logger.getLogger("org.columba.core.main"); //$NON-NLS-1$

	private static ComponentManager instance = new ComponentManager();

	private IExtensionHandler extensionHandler;

	private ComponentManager() {
	};

	public static ComponentManager getInstance() {
		return instance;
	}

	private IExtensionHandler getExtensionHandler() {
		if (extensionHandler == null) {
			try {
				// retrieve plugin manager instance
				IPluginManager pm = null;
				try {
					pm = (IPluginManager) ServiceRegistry.getInstance()
							.getService(IPluginManager.class);
				} catch (ServiceNotFoundException e) {
					LOG.severe(e.getMessage());

					if (Logging.DEBUG)
						e.printStackTrace();
				}

				extensionHandler = pm
						.getExtensionHandler(IExtensionHandlerKeys.ORG_COLUMBA_CORE_COMPONENT);
			} catch (PluginHandlerNotFoundException e) {
				LOG.severe(e.getMessage());

				if (Logging.DEBUG)
					e.printStackTrace();
			}
		}
		return extensionHandler;
	}

	public IComponentPlugin getPlugin(String id) {
		IComponentPlugin component = null;

		IExtension extension = getExtensionHandler().getExtension(id);

		try {
			component = (IComponentPlugin) extension.instanciateExtension(null);
		} catch (PluginException e) {
			LOG.severe(e.getMessage());

			if (Logging.DEBUG)
				e.printStackTrace();
		}

		return component;
	}

	/**
	 * @see org.columba.core.component.IComponentPlugin#init()
	 */
	public void init() {
		Enumeration extensionEnumeration = getExtensionHandler()
				.getExtensionEnumeration();

		while (extensionEnumeration.hasMoreElements()) {
			IExtension ext = (IExtension) extensionEnumeration.nextElement();
			IComponentPlugin p;

			try {
				p = (IComponentPlugin) ext.instanciateExtension(null);
				p.init();
			} catch (PluginException e) {
				LOG.severe(e.getMessage());

				if (Logging.DEBUG)
					e.printStackTrace();
				
			}

		}
	}

	/**
	 * @see org.columba.core.component.IComponentPlugin#postStartup()
	 */
	public void postStartup() {
		Enumeration extensionEnumeration = getExtensionHandler()
				.getExtensionEnumeration();

		while (extensionEnumeration.hasMoreElements()) {
			IExtension ext = (IExtension) extensionEnumeration.nextElement();
			IComponentPlugin p;
			try {
				p = (IComponentPlugin) ext.instanciateExtension(null);
				p.postStartup();
			} catch (PluginException e) {
				LOG.severe(e.getMessage());

				if (Logging.DEBUG)
					e.printStackTrace();
			}
		}
	}

	/**
	 * @see org.columba.core.component.IComponentPlugin#registerCommandLineArguments()
	 */
	public void registerCommandLineArguments() {

		Enumeration extensionEnumeration = getExtensionHandler()
				.getExtensionEnumeration();

		while (extensionEnumeration.hasMoreElements()) {
			IExtension ext = (IExtension) extensionEnumeration.nextElement();
			IComponentPlugin p;
			try {
				p = (IComponentPlugin) ext.instanciateExtension(null);
				p.registerCommandLineArguments();
			} catch (PluginException e) {
				LOG.severe(e.getMessage());

				if (Logging.DEBUG)
					e.printStackTrace();
			}

		}
	}

	/**
	 * @see org.columba.core.component.IComponentPlugin#handleCommandLineParameters(org.apache.commons.cli.CommandLine)
	 */
	public void handleCommandLineParameters(CommandLine commandLine) {
		Enumeration extensionEnumeration = getExtensionHandler()
				.getExtensionEnumeration();

		while (extensionEnumeration.hasMoreElements()) {
			IExtension ext = (IExtension) extensionEnumeration.nextElement();
			IComponentPlugin p;
			try {
				p = (IComponentPlugin) ext.instanciateExtension(null);
				p.handleCommandLineParameters(commandLine);
			} catch (PluginException e) {
				LOG.severe(e.getMessage());

				if (Logging.DEBUG)
					e.printStackTrace();
			}

		}
	}

}
