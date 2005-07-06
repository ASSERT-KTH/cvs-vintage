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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Logger;

import org.columba.core.io.DiskIO;
import org.columba.core.main.Main;
import org.columba.core.plugin.exception.PluginHandlerNotFoundException;
import org.columba.core.plugin.util.PluginFinder;
import org.columba.core.xml.XmlElement;
import org.columba.core.xml.XmlIO;

/**
 * Plugin manager is a singleton registry for all plugins and all
 * extension handlers.
 * 
 * @author fdietz
 *
 */
public class PluginManager implements IPluginManager {

	private static final Logger LOG = Logger
			.getLogger("org.columba.core.plugin");

	private static final String XML_ELEMENT_EXTENSION = "extension";

	private static final String XML_ELEMENT_EXTENSIONLIST = "extensionlist";

	private static final String XML_ATTRIBUTE_TYPE = "type";

	private static final String XML_ELEMENT_JAR = "jar";

	private static final String XML_ELEMENT_RUNTIME = "runtime";

	private static final String XML_ATTRIBUTE_DESCRIPTION = "description";

	private static final String XML_ATTRIBUTE_CATEGORY = "category";

	private static final String XML_ATTRIBUTE_VERSION = "version";

	private static final String XML_ATTRIBUTE_NAME = "name";

	private static final String FILENAME_PLUGIN_XML = "plugin.xml";

	private static final String XML_ELEMENT_HANDLERLIST = "handlerlist";

	private static final String FILENAME_CONFIG_XML = "config.xml";

	private static final String XML_ATTRIBUTE_SINGLETON = "singleton";

	private static final String XML_ATTRIBUTE_ENABLED = "enabled";

	private static final String XML_ATTRIBUTE_CLASS = "class";

	private static final String XML_ATTRIBUTE_ID = "id";

	private static final String XML_ELEMENT_PROPERTIES = "properties";

	private Hashtable handlerMap = new Hashtable();

	private Hashtable pluginMap = new Hashtable();

	private static PluginManager instance = new PluginManager();

	/**
	 * 
	 */
	private PluginManager() {
		// load core plugin handlers
		addHandlers("org/columba/core/plugin/pluginhandler.xml");
	}

	/**
	 * @return
	 */
	public static IPluginManager getInstance() {
		return instance;
	}

	/**
	 * @see org.columba.core.plugin.IPluginManager#addHandler(java.lang.String, org.columba.core.plugin.IExtensionHandler)
	 */
	public void addHandler(String id, IExtensionHandler handler) {
		if (id == null)
			throw new IllegalArgumentException("id == null");
		if (handler == null)
			throw new IllegalArgumentException("handler == null");

		LOG.fine("adding extension handler " + id);

		handlerMap.put(id, handler);

	}

	/**
	 * @see org.columba.core.plugin.IPluginManager#getHandler(java.lang.String)
	 */
	public IExtensionHandler getHandler(String id)
			throws PluginHandlerNotFoundException {
		if (id == null)
			throw new IllegalArgumentException("id == null");

		if (handlerMap.containsKey(id))
			return (IExtensionHandler) handlerMap.get(id);
		else
			throw new PluginHandlerNotFoundException(id);

	}

	/**
	 * Add a list of handlers specified in path to the plugin manager.
	 * 
	 * @param path
	 *            xml-file validating against pluginhandler.dtd
	 */
	/**
	 * @see org.columba.core.plugin.IPluginManager#addHandlers(java.lang.String)
	 */
	public void addHandlers(String xmlResource) {
		XmlIO xmlFile = new XmlIO(DiskIO.getResourceURL(xmlResource));
		xmlFile.load();

		XmlElement list = xmlFile.getRoot().getElement(
				PluginManager.XML_ELEMENT_HANDLERLIST);
		if (list == null) {
			LOG.severe("element <handlerlist> expected.");
			return;
		}

		Iterator it = list.getElements().iterator();
		while (it.hasNext()) {
			XmlElement child = (XmlElement) it.next();
			// skip non-matching elements
			if (child.getName().equals("handler") == false)
				continue;
			String id = child.getAttribute(PluginManager.XML_ATTRIBUTE_ID);
			String clazz = child
					.getAttribute(PluginManager.XML_ATTRIBUTE_CLASS);

			IExtensionHandler handler = null;
			try {
				Class c = Class.forName(clazz);
				handler = (IExtensionHandler) c.newInstance();
				addHandler(handler.getId(), handler);
			} catch (ClassNotFoundException e) {
				LOG.severe("Error while adding handler from " + xmlResource
						+ ": " + e.getMessage());
				if (Main.DEBUG)
					e.printStackTrace();
			} catch (InstantiationException e) {
				LOG.severe("Error while adding handler from " + xmlResource
						+ ": " + e.getMessage());
				if (Main.DEBUG)
					e.printStackTrace();
			} catch (IllegalAccessException e) {
				LOG.severe("Error while adding handler from " + xmlResource
						+ ": " + e.getMessage());

				if (Main.DEBUG)
					e.printStackTrace();
			}
		}
	}

	/**
	 * @see org.columba.core.plugin.IPluginManager#addPlugin(java.io.File)
	 */
	public String addPlugin(File folder) {
		LOG.fine("registering plugin: " + folder);

		// load plugin.xml file
		// skip if it doesn't exist
		File xmlFile = new File(folder, FILENAME_PLUGIN_XML);

		if (xmlFile == null || !xmlFile.exists()) {
			return null;
		}

		XmlIO config = new XmlIO();

		try {
			config.setURL(xmlFile.toURL());
		} catch (MalformedURLException mue) {
		}

		config.load();

		// determine plugin ID
		XmlElement element = config.getRoot().getElement("/plugin");
		String id = element.getAttribute(PluginManager.XML_ATTRIBUTE_ID);
		String name = element.getAttribute(PluginManager.XML_ATTRIBUTE_NAME);
		String version = element
				.getAttribute(PluginManager.XML_ATTRIBUTE_VERSION);
		String enabled = element
				.getAttribute(PluginManager.XML_ATTRIBUTE_ENABLED);
		String category = element
				.getAttribute(PluginManager.XML_ATTRIBUTE_CATEGORY);
		String description = element
				.getAttribute(PluginManager.XML_ATTRIBUTE_DESCRIPTION);

		XmlElement runtime = element
				.getElement(PluginManager.XML_ELEMENT_RUNTIME);
		String jar = runtime.getAttribute(PluginManager.XML_ELEMENT_JAR);
		String type = runtime.getAttribute(PluginManager.XML_ATTRIBUTE_TYPE);

		PluginMetadata pluginMetadata = new PluginMetadata(id, name,
				description, version, category, new Boolean(enabled)
						.booleanValue(), folder, type);
		pluginMetadata.setRuntimeJar(jar);
		
		pluginMap.put(id, pluginMetadata);

		// loop through all extensions this plugin uses
		// -> search the corresponding plugin handler
		// -> register the plugin at the plugin handler
		for (int j = 0; j < element.count(); j++) {
			XmlElement extensionListXmlElement = element.getElement(j);

			// skip if no <extensionlist> element found
			if (extensionListXmlElement.getName().equals(
					PluginManager.XML_ELEMENT_EXTENSIONLIST) == false)
				continue;

			String extensionId = extensionListXmlElement
					.getAttribute(PluginManager.XML_ATTRIBUTE_ID);
			IExtensionHandler handler = null;
			if (handlerMap.containsKey(extensionId)) {
				// we have a plugin-handler for this kind of extension
				try {
					handler = getHandler(extensionId);
				} catch (PluginHandlerNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				LOG.severe("No suitable extension handler with name "
						+ extensionId + " found");
				continue;
			}

			for (int k = 0; k < extensionListXmlElement.count(); k++) {
				XmlElement extensionXmlElement = extensionListXmlElement
						.getElement(k);

				// skip if no <extension> element found
				if (extensionXmlElement.getName().equals(
						PluginManager.XML_ELEMENT_EXTENSION) == false)
					continue;

				Extension extension = parseExtension(pluginMetadata,
						extensionXmlElement);
				if (extension == null)
					continue;

				handler
						.addExtension(extension.getMetadata().getId(),
								extension);
			}
		}

		return id;
	}

	/**
	 * @param pluginMetadata
	 * @param extensionXmlElement
	 */
	/**
	 * @param pluginMetadata
	 * @param extensionXmlElement
	 * @return
	 */
	private Extension parseExtension(PluginMetadata pluginMetadata,
			XmlElement extensionXmlElement) {
		String extensionId = extensionXmlElement
				.getAttribute(PluginManager.XML_ATTRIBUTE_ID);
		String extensionClazz = extensionXmlElement
				.getAttribute(PluginManager.XML_ATTRIBUTE_CLASS);
		String extensionEnabled = extensionXmlElement
				.getAttribute(PluginManager.XML_ATTRIBUTE_ENABLED);
		String extensionSingleton = extensionXmlElement
				.getAttribute(PluginManager.XML_ATTRIBUTE_SINGLETON);
		XmlElement attributesElement = extensionXmlElement
				.getElement(XML_ELEMENT_PROPERTIES);
		Hashtable attributes = null;
		if (attributesElement != null)
			attributes = attributesElement.getAttributes();

		if (extensionId == null) {
			LOG.severe("wrong extension point syntax specified in plugin.xml");
			if (Main.DEBUG)
				XmlElement.printNode(extensionXmlElement, " ");
			return null;
		}

		ExtensionMetadata extMetadata = null;
		if (attributes != null)
			extMetadata = new ExtensionMetadata(extensionId, extensionClazz,
					attributes);
		else
			extMetadata = new ExtensionMetadata(extensionId, extensionClazz);

		if (extensionEnabled != null)
			extMetadata
					.setEnabled(new Boolean(extensionEnabled).booleanValue());
		if (extensionSingleton != null)
			extMetadata.setSingleton(new Boolean(extensionSingleton)
					.booleanValue());

		Extension pluginExtension = new Extension(pluginMetadata, extMetadata);

		return pluginExtension;
	}

	/**
	 * @see org.columba.core.plugin.IPluginManager#initPlugins()
	 */
	public void initPlugins() {
		// find all possible plugin directories
		File[] pluginFolders = PluginFinder.searchPlugins();

		// if no plugin directory exists -> return
		if (pluginFolders == null) {
			return;
		}

		// try to load all plugins
		for (int i = 0; i < pluginFolders.length; i++) {
			File folder = pluginFolders[i];
			addPlugin(folder);
		}
	}

	/**
	 * Gets top level tree xml node of config.xml
	 * <p>
	 * This can be used in conjunction with {@link AbstractConfigPlugin}as an
	 * easy way to configure plugins.
	 * 
	 * @param id
	 *            id of plugin
	 * @return top leve xml treenode
	 */
	/**
	 * @see org.columba.core.plugin.IPluginManager#getConfiguration(java.lang.String)
	 */
	public XmlIO getConfiguration(String id) {
		try {
			PluginMetadata metadata = (PluginMetadata) pluginMap.get(id);
			File directory = metadata.getDirectory();
			File configFile = new File(directory, FILENAME_CONFIG_XML);
			XmlIO io = new XmlIO(configFile.toURL());

			return io;
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return null;
	}

	/**
	 * @see org.columba.core.plugin.IPluginManager#getPluginMetadata(java.lang.String)
	 */
	public PluginMetadata getPluginMetadata(String id) {
		if ( id == null) throw new IllegalArgumentException("id == null");
		
		PluginMetadata metadata = (PluginMetadata) pluginMap.get(id);
		return metadata;
	}

	/**
	 * @see org.columba.core.plugin.IPluginManager#getInfoURL(java.lang.String)
	 */
	public URL getInfoURL(String id) {
		PluginMetadata metadata = (PluginMetadata) pluginMap.get(id);
		File pluginDirectory = metadata.getDirectory();

		if (pluginDirectory == null) {
			return null;
		}

		try {
			// try all possible version of readme files...
			File infoFile = new File(pluginDirectory, "readme.html");

			if (!infoFile.exists()) {
				infoFile = new File(pluginDirectory, "readme.txt");
			}

			if (!infoFile.exists()) {
				infoFile = new File(pluginDirectory, "Readme.html");
			}

			if (!infoFile.exists()) {
				infoFile = new File(pluginDirectory, "Readme.txt");
			}

			if (infoFile.exists()) {
				LOG.fine("infofile-URL=" + infoFile.toURL());

				return infoFile.toURL();
			}
		} catch (MalformedURLException ex) {
		} // does not occur

		return null;
	}

	/**
	 * @see org.columba.core.plugin.IPluginManager#getPluginIds()
	 */
	public String[] getPluginIds() {
		Vector result = new Vector();
		Enumeration enum = pluginMap.elements();
		while (enum.hasMoreElements()) {
			PluginMetadata metadata = (PluginMetadata) enum.nextElement();

			String id = metadata.getId();

			result.add(id);
		}

		return (String[]) result.toArray(new String[0]);
	}

	/**
	 * @see org.columba.core.plugin.IPluginManager#getPluginMetadataEnumeration()
	 */
	public Enumeration getPluginMetadataEnumeration() {
		return pluginMap.elements();
	}

}