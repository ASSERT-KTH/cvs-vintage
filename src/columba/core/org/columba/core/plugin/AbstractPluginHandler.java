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

import org.columba.core.loader.DefaultClassLoader;
import org.columba.core.xml.XmlElement;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public abstract class AbstractPluginHandler {

	protected PluginListConfig pluginListConfig;
	protected String id;
	protected PluginManager pluginManager;


	/**
	 * Constructor for PluginHandler.
	 */
	public AbstractPluginHandler(String id, String config) {
		super();

		this.id = id;

		if( config != null )
		pluginListConfig = new PluginListConfig(config);


	}

	protected PluginListConfig getConfig() {
		return pluginListConfig;
	}

	public String getId() {
		return id;
	}

	public abstract String[] getDefaultNames();

	public Object loadPlugin(String className, Object[] args)
		throws Exception {

		return new DefaultClassLoader().instanciate(className, args);

	}



	public Object getPlugin(String name, String className, Object[] args)
		throws Exception {

		try {
			return loadPlugin(className, args);
		} catch (ClassNotFoundException ex) {
			String pluginId = name.substring(0,name.indexOf('$'));
			//XmlElement parent = (XmlElement) MainInterface.pluginManager.getPluginElement(pluginId);
			//XmlElement child = parent.getElement("runtime");
			
			String type = pluginManager.getPluginType(pluginId);
			/*
			String jar = child.getAttribute("jar");
			//String importPackages = child.getAttribute("import");
			
			File file = (File) pluginFolders.get(name);
			if ( jar!=null ) file = new File(file, jar);
			*/
			File pluginDir = pluginManager.getPluginDir(pluginId);
			
			return PluginLoader.loadExternalPlugin(className, type, pluginDir, args);
		}

	}

	/*
	public void addPlugin(String id, File pluginFolder, XmlElement element) {
		externalPlugins.put(id, element);
		pluginFolders.put(id, pluginFolder);
	}
	*/
	
	public abstract void addExtension(String id, XmlElement extension);

	/**
	 * @return PluginManager
	 */
	public PluginManager getPluginManager() {
		return pluginManager;
	}

	/**
	 * Sets the pluginManager.
	 * @param pluginManager The pluginManager to set
	 */
	public void setPluginManager(PluginManager pluginManager) {
		this.pluginManager = pluginManager;
	}

}
