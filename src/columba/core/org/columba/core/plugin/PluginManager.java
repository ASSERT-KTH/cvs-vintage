//The contents of this file are subject to the Mozilla Public License Version 1.1
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
//The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003. 
//
//All Rights Reserved.
package org.columba.core.plugin;

import java.io.File;
import java.net.MalformedURLException;
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
		if ( pluginFolders == null ) return;
		
		for (int i = 0; i < pluginFolders.length; i++) {
			File folder = pluginFolders[i];
			if (folder.getName().equals("CVS"))
				continue;

			ColumbaLogger.log.info("registering plugin: " + folder);

			File xmlFile = new File(folder, "plugin.xml");
			if (xmlFile == null)
				continue;

			XmlIO config = new XmlIO();
			try{
				config.setURL(xmlFile.toURL());
			}catch(MalformedURLException mue){}
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
