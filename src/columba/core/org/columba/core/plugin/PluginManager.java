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
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.columba.core.gui.util.NotifyDialog;
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

	//File[] pluginFolders;
	//String[] ids;
	//XmlElement[] elements;
	Hashtable plugins;
	Map folders;
	Map elements;
	Map jarFiles;

	List ids;

	/**
	 * Constructor for PluginManager.
	 */
	public PluginManager() {
		super();
		plugins = new Hashtable(10);

	}

	public void addPlugin(File folder) {
		if ((!folder.isDirectory()) || (folder.getName().equals("CVS"))) {
			return;
		}

		ColumbaLogger.log.info("registering plugin: " + folder);

		File xmlFile = new File(folder, "plugin.xml");
		if (xmlFile == null)
			return;
		if (xmlFile.exists() == false)
			return;

		XmlIO config = new XmlIO();
		try {
			config.setURL(xmlFile.toURL());
		} catch (MalformedURLException mue) {
		}
		config.load();

		XmlElement element = config.getRoot().getElement("/plugin");
		String id = element.getAttribute("id");
		ids.add(id);
		elements.put(id, element);
		folders.put(id, folder);
		/*
		elements[i] = element;
		
		ids[i] = id;
		*/

		// save plugin folder

		//String extensionPoint = element.getAttribute("extension_point");

		XmlElement runtime = element.getElement("runtime");
		String type = runtime.getAttribute("type");
		String jar = runtime.getAttribute("jar");

		if (jar != null)
			//pluginFolders[i] = new File(pluginFolders[i], jar);
			jarFiles.put(id, new File(folder, jar));

		ColumbaLogger.log.debug("id: " + id);
		//ColumbaLogger.log.debug("extension point: " + extensionPoint);
		ColumbaLogger.log.debug("type: " + type);
		ColumbaLogger.log.debug("jar: " + jar);

		XmlElement extension;
		String extensionPoint;

		for (int j = 0; j < element.count(); j++) {
			extension = element.getElement(j);
			if (extension.getName().equals("extension")) {
				extensionPoint = extension.getAttribute("name");

				if (plugins.containsKey(extensionPoint)) {
					// we have a plugin-handler for this kind of plugin

					try {
						AbstractPluginHandler handler =
							(AbstractPluginHandler) plugins.get(extensionPoint);

						File file = null;
						/*
						if (jar != null)
							file = new File(folder, jar);
						else
						*/

						file = folder;

						ColumbaLogger.log.info("debug: " + file.toString());

						handler.addExtension(id, extension);
					} catch (Exception ex) {
						ColumbaLogger.log.error(ex.getMessage());
					}
				}
			}
		}

	}

	public void initPlugins() {
		File[] pluginFolders = PluginFinder.searchPlugins();
		if (pluginFolders == null)
			return;
		//ids = new String[pluginFolders.length];
		//elements = new XmlElement[pluginFolders.length];
		folders = new HashMap();
		elements = new HashMap();
		jarFiles = new HashMap();
		ids = new Vector();

		for (int i = 0; i < pluginFolders.length; i++) {
			File folder = pluginFolders[i];
			addPlugin(folder);

		}
	}

	public void registerHandler(AbstractPluginHandler handler) {
		plugins.put(handler.getId(), handler);
		handler.setPluginManager(this);
	}

	public AbstractPluginHandler getHandler(String id)
		throws PluginHandlerNotFoundException {
		if (plugins.containsKey(id)) {
			return (AbstractPluginHandler) plugins.get(id);
		} else {
			ColumbaLogger.log.error("PluginHandler not found: " + id);

			throw new PluginHandlerNotFoundException(id);
		}
	}

	public Enumeration getHandlers() {
		return plugins.elements();
	}

	/*
	public XmlElement getPlugin( String id ) {
		return (XmlElement) plugins.get(id);
	}
	*/

	/*
	protected int getIndex(String id) {
		for (int i = 0; i < ids.length; i++) {
			if (ids[i] == null)
				continue;
	
			if (ids[i].equals(id))
				return i;
		}
		return -1;
	}
	*/
	/**
	 * @param id
	 * 
	 * @return 
	 */
	public File getJarFile(String id) {
		/*
		if (getIndex(id) >= 0 && getIndex(id) < pluginFolders.length) {
			return pluginFolders[getIndex(id)];
		} else {
			return null;
		}*/
		return (File) jarFiles.get(id);
	}

	/**
		 * @param id
		 * 
		 * @return directory of this plugin
		 */
	public File getFolder(String id) {
		return (File) folders.get(id);
	}

	/**
	 * @param id
	 * 
	 * @return	parent xml treenode of "plugin.xml"
	 */
	public XmlElement getPluginElement(String id) {
		/*
		if (getIndex(id) >= 0 && getIndex(id) < elements.length) {
			return elements[getIndex(id)];
		} else {
			return null;
		}*/

		return (XmlElement) elements.get(id);
	}

	public String getPluginType(String id) {
		/*
		if (getIndex(id) >= 0 && getIndex(id) < elements.length) {
			XmlElement runtime = elements[getIndex(id)].getElement("runtime");
			return runtime.getAttribute("type");
		} else {
			ColumbaLogger.log.error("runtime attribute not found");
			return null;
		}
		*/
		XmlElement e = getPluginElement(id);
		XmlElement runtime = e.getElement("runtime");
		return runtime.getAttribute("type");
	}

	public List getIds() {
		return ids;
	}

	/**
	 * @return	URL of Readme.html, readme.txt, etc.
	 */
	public URL getInfoURL(String id) {
		File pluginDirectory = getFolder(id);
		if (pluginDirectory == null)
			return null;

		try {
			// try all possible version of readme files...
			File infoFile = new File(pluginDirectory, "readme.html");
			if (infoFile.exists() == false)
				infoFile = new File(pluginDirectory, "readme.txt");
			if (infoFile.exists() == false)
				infoFile = new File(pluginDirectory, "Readme.html");
			if (infoFile.exists() == false)
				infoFile = new File(pluginDirectory, "Readme.txt");

			ColumbaLogger.log.debug("infofile-URL=" + infoFile.toURL());
			return infoFile.toURL();
		} catch (MalformedURLException ex) {
			NotifyDialog d = new NotifyDialog();
			d.showDialog(ex);
		}

		return null;
	}

	/**
	 * enable/disable plugin
	 * -> save changes in plugin.xml
	 * 
	 * @param b
	 */
	public void setEnabled(String id, boolean b) {
		
		//	get directory of plugin
		File folder = getFolder(id);

		// get plugin.xml of plugin
		File configFile = new File(folder, "plugin.xml");

		try {
			XmlIO io = new XmlIO(configFile.toURL());
			io.load();

			//	get xml tree node
			XmlElement e = io.getRoot().getElement("/plugin");
			if (e == null)
				return;

			// update XmlElement reference in HashMap cache
			elements.put(id, e);
			
			// set enabled attribute
			e.addAttribute("enabled", new Boolean(b).toString());

			io.save();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

}
