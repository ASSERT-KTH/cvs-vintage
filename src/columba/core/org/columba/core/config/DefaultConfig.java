// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Library General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

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
