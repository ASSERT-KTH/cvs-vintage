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
package org.columba.core.facade;

import org.columba.core.io.TempFileStore;
import org.columba.core.main.MainInterface;
import org.columba.core.plugin.PluginResourceLoader;
import org.columba.core.xml.XmlElement;
import org.columba.core.xml.XmlIO;

import java.io.File;

/**
 * @author fdietz
 * 
 *  
 */
public class Facade {
	/**
	 * @param configName
	 *            id of config-file example: options
	 * 
	 * 
	 * 
	 * @return XmlElement represents an xml-treenode
	 */
	public static XmlElement getConfigElement(String configName) {
		XmlElement root = MainInterface.config.get(configName);

		return root;
	}

	/**
	 * 
	 * create temporary File which exists also when Columba is not running.
	 * 
	 * This is useful when opening attachments with your web-browser. When you
	 * close Columba and use java's internal temp-file stuff, closing Columba
	 * would also close the web-browser.
	 * 
	 * @return File
	 */
	public static File createTempFile() {
		return TempFileStore.createTempFile();
	}

	/**
	 * 
	 * Returns the top xml node of config.xml found in the plugin folder.
	 * 
	 * @param pluginId
	 *            id of your plugin
	 * 
	 * @return XmlIO
	 */
	public static XmlIO getPluginConfiguration(String pluginId) {
		return MainInterface.pluginManager.getConfiguration(pluginId);
	}

	public static PluginResourceLoader createPluginResourceLoader(
			String pluginId) {
		return new PluginResourceLoader(pluginId);
	}
}