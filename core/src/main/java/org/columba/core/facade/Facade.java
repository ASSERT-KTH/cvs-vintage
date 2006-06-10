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

import java.io.File;

import org.columba.api.backgroundtask.IBackgroundTaskManager;
import org.columba.api.plugin.IPluginManager;
import org.columba.core.backgroundtask.BackgroundTaskManager;
import org.columba.core.plugin.PluginManager;
import org.columba.core.util.TempFileStore;

/**
 * @author fdietz 
 */
public class Facade {

	

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
	 * Returns config.xml file found in the plugin folder.
	 * 
	 * @param pluginId
	 *            id of your plugin
	 * 
	 * @return XmlIO
	 */
	public static File getPluginConfigFile(String pluginId) {
		return PluginManager.getInstance().getPluginConfigFile(pluginId);
	}

	/**
	 * Get background task manager.
	 * 
	 * @return background task manager.
	 */
	public static IBackgroundTaskManager getBackgroundTaskManager() {
		return (IBackgroundTaskManager) BackgroundTaskManager.getInstance();
	}

	/**
	 * Get Plugin Manager;
	 * 
	 * @return plugin manager;
	 */
	public static IPluginManager getPluginManager() {
		return (IPluginManager) PluginManager.getInstance();
	}
}