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
package org.columba.core.config;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.columba.core.io.DiskIO;

public class DefaultConfig {

	/**
	 * @see java.lang.Object#Object()
	 */
	public DefaultConfig() {

	}

	/**
	 * Method registerPlugin.
	 * @param moduleName
	 * @param id
	 * @param plugin
	 */
	protected static void registerPlugin(
		String moduleName,
		String id,
		DefaultXmlConfig plugin) {
		File file = null;
		if (moduleName.equals("core")) {
			file = ConfigPath.getConfigDirectory();
			copy(moduleName, id, file);
		} else {
			file = new File(ConfigPath.getConfigDirectory(), moduleName);

			//copy("modules/" + moduleName, id, file);
			copy(moduleName, id, file);
		}

		Config.registerPlugin(moduleName, id, plugin);
	}
	
	protected static void registerTemplatePlugin(
			String moduleName,
			String id,
			DefaultXmlConfig plugin) {
		/*
			File file = null;
			
			if (moduleName.equals("core")) {
				file = ConfigPath.getConfigDirectory();
				//copy(moduleName, id, file);
			} else {
				file = new File(ConfigPath.getConfigDirectory(), moduleName);

				//copy("modules/" + moduleName, id, file);
				//copy(moduleName, id, file);
			}
			*/
			
			String	hstr = "org/columba/" + moduleName + "/config/" + id;
			URL url = DiskIO.getResourceURL(hstr);
			plugin.setURL(url);
			Config.registerTemplatePlugin(moduleName, id, plugin);
		}


	/**
	 * Method getPlugin.
	 * @param moduleName
	 * @param id
	 * @return DefaultXmlConfig
	 */
	protected static DefaultXmlConfig getPlugin(String moduleName, String id) {
		return Config.getPlugin(moduleName, id);
	}
	
	protected static DefaultXmlConfig getTemplatePlugin(String moduleName, String id) {
			return Config.getTemplatePlugin(moduleName, id);
		}

	/**
	 * Method createConfigDir.
	 * @param moduleName
	 * @return File
	 */
	protected File createConfigDir(String moduleName) {

		File configDirectory =
			new File(ConfigPath.getConfigDirectory(), moduleName);

		DiskIO.ensureDirectory(configDirectory);

		return configDirectory;
	}

	/**
	 * Method copy.
	 * @param module
	 * @param filename
	 * @param outputDir
	 * @return boolean
	 */
	/** Copies a columba resource file to the directory specified, if and only if
	 *  the destination file <b>does not</b> exist already.
	 */
	public static boolean copy(
		String module,
		String filename,
		File outputDir) {
		File destFile;
		String hstr;

		destFile = new File(outputDir, filename);
		if (destFile.exists())
			return false;

		hstr = "org/columba/" + module + "/config/" + filename;

		try {
			return DiskIO.copyResource(hstr, destFile);
		} catch (IOException e) {
			return false;
		}

	} // copy

}
