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
import java.util.Vector;
import java.util.logging.Logger;

import org.columba.core.main.Main;
import org.columba.core.plugin.exception.PluginHandlerNotFoundException;
import org.columba.core.plugin.util.ExtensionXMLParser;
import org.columba.core.plugin.util.PluginFinder;
import org.columba.core.xml.XmlIO;

/**
 * Plugin manager is a singleton registry for all plugins and all extension
 * handlers.
 * 
 * @author fdietz
 * 
 */
public class PluginManager implements IPluginManager {

	private static final Logger LOG = Logger
			.getLogger("org.columba.core.plugin");

	private static final String FILENAME_PLUGIN_XML = "plugin.xml";

	private static final String FILENAME_CONFIG_XML = "config.xml";

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
	 * @see org.columba.core.plugin.IPluginManager#addHandler(java.lang.String,
	 *      org.columba.core.plugin.IExtensionHandler)
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
		Enumeration e = new ExtensionXMLParser()
				.parseExtensionHandlerlist(xmlResource);

		while (e.hasMoreElements()) {
			ExtensionHandlerMetadata metadata = (ExtensionHandlerMetadata) e
					.nextElement();

			IExtensionHandler handler = null;
			try {
				Class c = Class.forName(metadata.getClassName());
				handler = (IExtensionHandler) c.newInstance();
				addHandler(metadata.getId(), handler);
			} catch (ClassNotFoundException ex) {
				LOG.severe("Error while adding handler from " + xmlResource
						+ ": " + ex.getMessage());
				if (Main.DEBUG)
					ex.printStackTrace();
			} catch (InstantiationException ex) {
				LOG.severe("Error while adding handler from " + xmlResource
						+ ": " + ex.getMessage());
				if (Main.DEBUG)
					ex.printStackTrace();
			} catch (IllegalAccessException ex) {
				LOG.severe("Error while adding handler from " + xmlResource
						+ ": " + ex.getMessage());
				if (Main.DEBUG)
					ex.printStackTrace();
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

		Hashtable hashtable = new Hashtable();

		// parse "/plugin.xml" file
		PluginMetadata pluginMetadata = new ExtensionXMLParser().parsePlugin(xmlFile,
				hashtable);
		pluginMetadata.setDirectory(folder);

		String id = pluginMetadata.getId();
		pluginMap.put(id, pluginMetadata);

		// loop through all extensions this plugin uses
		// -> search the corresponding extension handler
		// -> register the extension at the extension handler
		Enumeration e = hashtable.keys();
		while (e.hasMoreElements()) {
			String extensionpointId = (String) e.nextElement();
			Vector extensionVector = (Vector) hashtable.get(extensionpointId);

			IExtensionHandler handler = null;

			// we have a plugin-handler for this kind of extension
			try {
				handler = getHandler(extensionpointId);
				Enumeration e2 = extensionVector.elements();
				while (e2.hasMoreElements()) {
					ExtensionMetadata extensionMetadata = (ExtensionMetadata) e2
							.nextElement();
					Extension pluginExtension = new Extension(pluginMetadata, extensionMetadata);
					
					handler.addExtension(pluginExtension.getMetadata().getId(),
							pluginExtension);
				}
			} catch (PluginHandlerNotFoundException e2) {
				LOG.severe("No suitable extension handler with name "
						+ extensionpointId + " found");
			}

		}

		return id;
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
	 * @see org.columba.core.plugin.IPluginManager#getPluginConfigFile(java.lang.String)
	 */
	public File getPluginConfigFile(String id) {

		PluginMetadata metadata = (PluginMetadata) pluginMap.get(id);
		File directory = metadata.getDirectory();
		File configFile = new File(directory, FILENAME_CONFIG_XML);

		return configFile;
	}

	/**
	 * @see org.columba.core.plugin.IPluginManager#getPluginMetadata(java.lang.String)
	 */
	public PluginMetadata getPluginMetadata(String id) {
		if (id == null)
			throw new IllegalArgumentException("id == null");

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
		Enumeration e = pluginMap.elements();
		while (e.hasMoreElements()) {
			PluginMetadata metadata = (PluginMetadata) e.nextElement();

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