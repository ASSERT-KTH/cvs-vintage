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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Logger;

import org.columba.api.plugin.ExtensionHandlerMetadata;
import org.columba.api.plugin.ExtensionMetadata;
import org.columba.api.plugin.IExtensionHandler;
import org.columba.api.plugin.IPluginManager;
import org.columba.api.plugin.PluginHandlerNotFoundException;
import org.columba.api.plugin.PluginMetadata;
import org.columba.core.io.DiskIO;

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

	private static final String FILENAME_EXTENSIONHANDLER_XML = "extensionhandler.xml";

	private Hashtable<String, IExtensionHandler> handlerMap = new Hashtable<String, IExtensionHandler>();

	private Hashtable<String, PluginMetadata> pluginMap = new Hashtable<String, PluginMetadata>();

	private static PluginManager instance = new PluginManager();

	private File[] pluginFolders;

	/**
	 * 
	 */
	private PluginManager() {
		// find all possible plugin directories
		pluginFolders = PluginFinder.searchPlugins();
	}

	/**
	 * @return instance
	 */
	public static IPluginManager getInstance() {
		return instance;
	}

	/**
	 * @see org.columba.api.plugin.IPluginManager#addExtensionHandler(java.lang.String,
	 *      org.columba.api.plugin.IExtensionHandler)
	 */
	public void addExtensionHandler(String id, IExtensionHandler handler) {
		if (id == null)
			throw new IllegalArgumentException("id == null");
		if (handler == null)
			throw new IllegalArgumentException("handler == null");

		LOG.fine("adding extension handler " + id);

		handlerMap.put(id, handler);
	}

	/**
	 * @see org.columba.api.plugin.IPluginManager#getExtensionHandler(java.lang.String)
	 */
	public IExtensionHandler getExtensionHandler(String id)
			throws PluginHandlerNotFoundException {
		if (id == null)
			throw new IllegalArgumentException("id == null");

		if (handlerMap.containsKey(id))
			return (IExtensionHandler) handlerMap.get(id);
		else
			throw new PluginHandlerNotFoundException(id);
	}

	/**
	 * @see org.columba.api.plugin.IPluginManager#addPlugin(java.io.File)
	 */
	public String addPlugin(File xmlFile) {
		Hashtable hashtable = new Hashtable();

		// parse "/plugin.xml" file
		BufferedInputStream buf;
		try {
			buf = new BufferedInputStream(new FileInputStream(xmlFile));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			// cancel
			return null;
		}

		PluginMetadata pluginMetadata = new ExtensionXMLParser().parsePlugin(
				buf, hashtable);
		pluginMetadata.setDirectory(xmlFile.getParentFile());

		String id = pluginMetadata.getId();
		pluginMap.put(id, pluginMetadata);

		// register all extensions
		parseExtensions(hashtable, pluginMetadata, false);

		return id;
	}

	/**
	 * @param hashtable
	 *            hashtable with String id and Vector of
	 *            <code>ExtensionMetadata</code>
	 * @param pluginMetadata
	 */
	private void parseExtensions(Hashtable hashtable,
			PluginMetadata pluginMetadata, boolean internal) {
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
				handler = getExtensionHandler(extensionpointId);
				Enumeration e2 = extensionVector.elements();
				while (e2.hasMoreElements()) {
					ExtensionMetadata extensionMetadata = (ExtensionMetadata) e2
							.nextElement();
					Extension pluginExtension = new Extension(pluginMetadata,
							extensionMetadata);
					pluginExtension.setInternal(internal);

					String extensionId = pluginExtension.getMetadata().getId();
					// if extension wasn't already registered
					if (handler.exists(extensionId) == false)
						handler.addExtension(extensionId, pluginExtension);
				}
			} catch (PluginHandlerNotFoundException e2) {
				LOG.severe("No suitable extension handler with name "
						+ extensionpointId + " found");
			}
		}
	}

	/**
	 * @see org.columba.api.plugin.IPluginManager#initExternalPlugins()
	 */
	public void initExternalPlugins() {

		// if no plugin directory exists -> return
		if (pluginFolders == null || pluginFolders.length == 0) {
			return;
		}

		// try to load all plugins
		for (int i = 0; i < pluginFolders.length; i++) {
			File folder = pluginFolders[i];

			File xmlFile = new File(folder, FILENAME_PLUGIN_XML);

			if (xmlFile == null || !xmlFile.exists()) {
				// skip if it doesn't exist
				continue;
			}

			LOG.fine("registering plugin: " + folder);

			addPlugin(xmlFile);
		}
	}

	/**
	 * @see org.columba.api.plugin.IPluginManager#getPluginConfigFile(java.lang.String)
	 */
	public File getPluginConfigFile(String id) {

		PluginMetadata metadata = (PluginMetadata) pluginMap.get(id);
		File directory = metadata.getDirectory();
		File configFile = new File(directory, FILENAME_CONFIG_XML);

		return configFile;
	}

	/**
	 * @see org.columba.api.plugin.IPluginManager#getPluginMetadata(java.lang.String)
	 */
	public PluginMetadata getPluginMetadata(String id) {
		if (id == null)
			throw new IllegalArgumentException("id == null");

		PluginMetadata metadata = (PluginMetadata) pluginMap.get(id);
		return metadata;
	}

	/**
	 * @see org.columba.api.plugin.IPluginManager#getInfoURL(java.lang.String)
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
	 * @see org.columba.api.plugin.IPluginManager#getPluginMetadataEnumeration()
	 */
	public Enumeration getPluginMetadataEnumeration() {
		return pluginMap.elements();
	}

	/**
	 * @see org.columba.api.plugin.IPluginManager#addPlugin(java.lang.String)
	 */
	public String addPlugin(String resourcePath) {
		if (resourcePath == null)
			throw new IllegalArgumentException("resourcePath == null");

		// retrieve inputstream from resource
		InputStream is;
		try {
			is = DiskIO.getResourceStream(resourcePath);
		} catch (IOException e) {
			e.printStackTrace();
			// cancel
			return null;
		}

		// parse plugin metadata
		Hashtable hashtable = new Hashtable();
		PluginMetadata pluginMetadata = new ExtensionXMLParser().parsePlugin(
				new BufferedInputStream(is), hashtable);

		String id = pluginMetadata.getId();

		// 
		// Note: We intentionally don't remember internal plugins, because
		// we don't want them to appear in the plugin manager currently.
		// 
		// TODO: improve plugin manager dialog to support internal plugins
		// which can't be removed, etc.

		// remember plugin metadata
		// pluginMap.put(id, pluginMetadata);

		// register all extensions
		parseExtensions(hashtable, pluginMetadata, true);

		return id;
	}

	/**
	 * @see org.columba.api.plugin.IPluginManager#initExternalExtensionHandlers()
	 */
	public void initExternalExtensionHandlers() {
		// if no plugin directory exists -> return
		if (pluginFolders == null || pluginFolders.length == 0) {
			return;
		}

		// try to load extensin handlers of all plugins
		for (int i = 0; i < pluginFolders.length; i++) {
			File folder = pluginFolders[i];

			File xmlFile = new File(folder, FILENAME_EXTENSIONHANDLER_XML);

			if (xmlFile == null || !xmlFile.exists()) {
				// skip if it doesn't exist
				continue;
			}

			BufferedInputStream buf;
			try {
				buf = new BufferedInputStream(new FileInputStream(xmlFile));
				LOG.fine("registering extension handler: " + folder);
				addExtensionHandlers(buf);
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
				// cancel
				continue;
			}
		}
	}

	/**
	 * @see org.columba.api.plugin.IPluginManager#addExtensionHandlers(java.io.InputStream)
	 */
	public void addExtensionHandlers(InputStream is) {
		Enumeration<ExtensionHandlerMetadata> e = new ExtensionXMLParser()
				.parseExtensionHandlerList(is);
		while (e.hasMoreElements()) {
			ExtensionHandlerMetadata metadata = e.nextElement();

			IExtensionHandler handler = new ExtensionHandler(metadata.getId(),
					metadata.getParent());

			addExtensionHandler(metadata.getId(), handler);
		}
	}

	/**
	 * @see org.columba.api.plugin.IPluginManager#addExtensionHandlers(java.lang.String)
	 */
	public void addExtensionHandlers(String resourcePath) {
		// retrieve inputstream from resource
		InputStream is;
		try {
			is = DiskIO.getResourceStream(resourcePath);
		} catch (IOException e) {
			e.printStackTrace();
			// cancel
			return;
		}

		addExtensionHandlers(is);
	}
}