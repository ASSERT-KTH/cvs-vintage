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
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.columba.core.io.DiskIO;
import org.columba.core.xml.XmlElement;

public class Config {

	public static File inboxDirectory;
	public static File sentDirectory;
	public static File headerDirectory;
	public static File pop3Directory;
	public static String userDir;

	private static OptionsXmlConfig optionsConfig;

	public static File loggerPropertyFile;

	private static Hashtable pluginList;
	private static Hashtable templatePluginList;

	private static File optionsFile;

	/**
	 * @see java.lang.Object#Object()
	 */
	public Config() {

		pluginList = new Hashtable();
		templatePluginList = new Hashtable();

		optionsFile = new File(ConfigPath.getConfigDirectory(), "options.xml");

		DefaultConfig.registerPlugin(
			"core",
			optionsFile.getName(),
			new OptionsXmlConfig(optionsFile));

		/*
		File file = new File(ConfigPath.getConfigDirectory(), "mail");
		
		
		if (file.exists() == false) {
			// convert to new config-schema
			
			Python.runResource(
				"org/columba/mail/config/convert.py",
				ConfigPath.getConfigDirectory().getPath());
			
		
		}
		*/

	}

	/**
	 * Method init.
	 */
	public static void init() {

		File configDirectory = ConfigPath.getConfigDirectory();

		load();

		pop3Directory = new File(configDirectory, "mail/pop3server");
		if (!pop3Directory.exists()) {
			pop3Directory.mkdir();
		}

	}

	/**
	 * Method registerPlugin.
	 * @param moduleName
	 * @param id
	 * @param configPlugin
	 */
	public static void registerPlugin(
		String moduleName,
		String id,
		DefaultXmlConfig configPlugin) {

		if (!pluginList.containsKey(moduleName)) {
			Hashtable table = new Hashtable();
			pluginList.put(moduleName, table);
		}

		addPlugin(moduleName, id, configPlugin);
	}

	public static void registerTemplatePlugin(
		String moduleName,
		String id,
		DefaultXmlConfig configPlugin) {

		if (!templatePluginList.containsKey(moduleName)) {
			Hashtable table = new Hashtable();
			templatePluginList.put(moduleName, table);
		}

		addTemplatePlugin(moduleName, id, configPlugin);
	}

	/**
	 * Method getPlugin.
	 * @param moduleName
	 * @param id
	 * @return DefaultXmlConfig
	 */
	public static DefaultXmlConfig getPlugin(String moduleName, String id) {

		if (pluginList.containsKey(moduleName)) {
			Hashtable table = (Hashtable) pluginList.get(moduleName);

			if (table.containsKey(id)) {
				DefaultXmlConfig plugin = (DefaultXmlConfig) table.get(id);
				return plugin;
			}
		}

		return null;
	}

	public static DefaultXmlConfig getTemplatePlugin(
		String moduleName,
		String id) {

		if (templatePluginList.containsKey(moduleName)) {
			Hashtable table = (Hashtable) templatePluginList.get(moduleName);

			if (table.containsKey(id)) {
				DefaultXmlConfig plugin = (DefaultXmlConfig) table.get(id);
				return plugin;
			}
		}

		return null;
	}

	/**
	 * Method addPlugin.
	 * @param moduleName
	 * @param id
	 * @param configPlugin
	 */
	public static void addPlugin(
		String moduleName,
		String id,
		DefaultXmlConfig configPlugin) {
		Hashtable table = (Hashtable) pluginList.get(moduleName);

		if (table != null) {
			table.put(id, configPlugin);
		}
	}

	public static void addTemplatePlugin(
		String moduleName,
		String id,
		DefaultXmlConfig configPlugin) {
		Hashtable table = (Hashtable) templatePluginList.get(moduleName);

		if (table != null) {
			table.put(id, configPlugin);
		}
	}

	/**
	 * Method getPluginList.
	 * @return Vector
	 */
	public static Vector getPluginList() {
		Vector v = new Vector();

		for (Enumeration keys = pluginList.keys(); keys.hasMoreElements();) {
			String key = (String) keys.nextElement();
			Hashtable table = (Hashtable) pluginList.get(key);

			if (table != null) {
				for (Enumeration keys2 = table.keys();
					keys2.hasMoreElements();
					) {
					String key2 = (String) keys2.nextElement();
					DefaultXmlConfig plugin =
						(DefaultXmlConfig) table.get(key2);

					v.add(plugin);
				}
			}

		}

		return v;
	}

	public static Vector getTemplatePluginList() {
		Vector v = new Vector();

		for (Enumeration keys = templatePluginList.keys();
			keys.hasMoreElements();
			) {
			String key = (String) keys.nextElement();
			Hashtable table = (Hashtable) templatePluginList.get(key);

			if (table != null) {
				for (Enumeration keys2 = table.keys();
					keys2.hasMoreElements();
					) {
					String key2 = (String) keys2.nextElement();
					DefaultXmlConfig plugin =
						(DefaultXmlConfig) table.get(key2);

					v.add(plugin);
				}
			}

		}

		return v;
	}

	/**
	 * Method save.
	 */
	public static void save() throws Exception {

		Vector v = getPluginList();
		for (int i = 0; i < v.size(); i++) {
			DefaultXmlConfig plugin = (DefaultXmlConfig) v.get(i);
			if (plugin == null)
				continue;

			plugin.save();

		}

	}

	/**
	 * Method load.
	 */
	public static void load() {

		Vector v = getPluginList();
		for (int i = 0; i < v.size(); i++) {
			DefaultXmlConfig plugin = (DefaultXmlConfig) v.get(i);
			if (plugin == null)
				continue;

			plugin.load();

		}

		Vector v2 = getTemplatePluginList();
		for (int i = 0; i < v2.size(); i++) {
			DefaultXmlConfig plugin = (DefaultXmlConfig) v2.get(i);
			if (plugin == null)
				continue;

			plugin.load();

		}

	}

	public static XmlElement get(String name) {
		DefaultXmlConfig xml = DefaultConfig.getPlugin("core", name + ".xml");
		return xml.getRoot();
	}
	/**
	 * Method getOptionsConfig.
	 * @return OptionsXmlConfig
	 */
	public static OptionsXmlConfig getOptionsConfig() {

		return (OptionsXmlConfig) DefaultConfig.getPlugin(
			"core",
			optionsFile.getName());
	}

	/**
	 * Method getLoggingPropertyFile.
	 * @return File
	 */
	public static File getLoggingPropertyFile() {
		File configDirectory = ConfigPath.getConfigDirectory();

		loggerPropertyFile = new File(configDirectory, "log4j.properties");

		File loggingDirectory = new File(configDirectory, "log");
		DiskIO.ensureDirectory(loggingDirectory);

		if (loggerPropertyFile.exists() == false) {
			String str = LogProperty.createPropertyString(loggingDirectory);

			//DefaultConfig.copy("core", "log4j.properties", configDirectory);
			loggerPropertyFile = new File(configDirectory, "log4j.properties");

			try {
				DiskIO.saveStringInFile(loggerPropertyFile, str);
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		return loggerPropertyFile;
	}

}
