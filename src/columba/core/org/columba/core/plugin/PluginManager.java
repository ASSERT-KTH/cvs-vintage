package org.columba.core.plugin;

import java.io.File;
import java.util.Hashtable;

import org.columba.core.logging.ColumbaLogger;
import org.columba.core.xml.XmlElement;
import org.columba.core.xml.XmlIO;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class PluginManager {

	Hashtable hashTable;
	/**
	 * Constructor for PluginManager.
	 */
	public PluginManager() {
		super();

		hashTable = new Hashtable(10);

	}

	public void initPlugins() {
		File[] pluginFolders = PluginFinder.searchPlugins();

		for (int i = 0; i < pluginFolders.length; i++) {
			File folder = pluginFolders[i];
			if (folder.getName().equals("CVS"))
				continue;

			ColumbaLogger.log.info("registering plugin: " + folder);

			File xmlFile = new File(folder, "plugin.xml");
			if (xmlFile == null)
				continue;

			XmlIO config = new XmlIO(xmlFile);
			config.load();

			XmlElement element = config.getRoot().getElement("/plugin");
			String id = element.getAttribute("id");
			String extensionPoint = element.getAttribute("extension_point");
			XmlElement runtime = element.getElement("runtime");
			String type = runtime.getAttribute("type");
			String jar = runtime.getAttribute("jar");

			ColumbaLogger.log.debug("id: " + id);
			ColumbaLogger.log.debug("extension point: " + extensionPoint);
			ColumbaLogger.log.debug("type: " + type);
			ColumbaLogger.log.debug("jar: " + jar);

			if (hashTable.containsKey(extensionPoint)) {
				// we have a plugin-handler for this kind of plugin

				try {

					AbstractPluginHandler handler =
						(AbstractPluginHandler) hashTable.get(extensionPoint);

					File file = null;
					/*
					if (jar != null)
						file = new File(folder, jar);
					else
					*/
					
					file = folder;

					ColumbaLogger.log.info("debug: " + file.toString());

					handler.addPlugin(id, file, element);
				} catch (Exception ex) {
					ColumbaLogger.log.error(ex.getMessage());
				}
			}

		}
	}

	public void registerHandler(AbstractPluginHandler handler) {
		hashTable.put(handler.getId(), handler);

	}

	public AbstractPluginHandler getHandler(String id) {
		if (hashTable.containsKey(id))
			return (AbstractPluginHandler) hashTable.get(id);
		else
			return null;
	}

}
