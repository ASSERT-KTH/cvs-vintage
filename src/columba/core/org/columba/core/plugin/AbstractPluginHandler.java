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
import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;
import java.util.ListIterator;

import org.columba.core.loader.DefaultClassLoader;
import org.columba.core.logging.ColumbaLogger;
import org.columba.core.xml.XmlElement;

/*
 * 
 * @author fdietz
 *
 * Every entrypoint is represented by this abstract handler class
 * 
 * 
 */
public abstract class AbstractPluginHandler {
	protected String id;

	protected XmlElement parentNode;

	protected PluginListConfig pluginListConfig;
	protected PluginManager pluginManager;

	//	translate plugin-id to user-visible name
	//  example: org.columba.example.HelloWorld$HelloPlugin -> HelloWorld
	protected Hashtable transformationTable;

	/**
	 * @param id
	 * @param config
	 */
	public AbstractPluginHandler(String id, String config) {
		super();
		this.id = id;
		transformationTable = new Hashtable();
		if (config != null)
			pluginListConfig = new PluginListConfig(config);
		ColumbaLogger.log.debug("initialising plugin-handler: " + id);
	}

	/**
	 * @return
	 */
	protected PluginListConfig getConfig() {
		return pluginListConfig;
	}

	/**
	 * @return
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param name
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public Object getPlugin(String name, Object[] args) throws Exception {
		ColumbaLogger.log.debug("name="+name);
		
		String className = getPluginClassName(name, "class");
		return getPlugin(name, className, args);
	}

	/**
	 * @param name
	 * @param className
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public Object getPlugin(String name, String className, Object[] args)
		throws Exception {

		/*
		ColumbaLogger.log.debug("name="+name);
		ColumbaLogger.log.debug("classname="+className);
		ColumbaLogger.log.debug("args="+args);
		*/

		try {
			return loadPlugin(className, args);
		} catch (ClassNotFoundException ex) {
			int dollarLoc = name.indexOf('$');
			String pluginId =
				(dollarLoc > 0 ? name.substring(0, dollarLoc) : name);

			String type = pluginManager.getPluginType(pluginId);
			File pluginDir = pluginManager.getPluginDir(pluginId);

			return PluginLoader.loadExternalPlugin(
				className,
				type,
				pluginDir,
				args);
		} catch (InvocationTargetException ex) {
			ex.getTargetException().printStackTrace();
			throw ex;
		}
	}

	/**
	 * @param name
	 * @return
	 */
	public Class getPluginClass(String name) {
		String className = getPluginClassName(name, "class");

		try {

			Class clazz = Class.forName(className);
			return clazz;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	/**
	 * @param name
	 * @param id
	 * @return
	 */
	protected String getPluginClassName(String name, String id) {

		int count = parentNode.count();

		for (int i = 0; i < count; i++) {

			XmlElement action = parentNode.getElement(i);
			String s = action.getAttribute("name");

			if (name.equals(s))
				return action.getAttribute(id);

		}

		return null;
	}

	/**
	 * @return
	 */
	public String[] getPluginIdList() {
		int count = parentNode.count();

		String[] list = new String[count];

		for (int i = 0; i < count; i++) {
			XmlElement action = parentNode.getElement(i);
			String s = action.getAttribute("name");

			list[i] = s;

		}

		return list;
	}

	/**
	 * @return
	 */
	public PluginManager getPluginManager() {
		return pluginManager;
	}

	/**
	 * @return
	 */
	public Hashtable getTransformationTable() {
		return transformationTable;
	}

	/**
	 * @param className
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public Object loadPlugin(String className, Object[] args)
		throws Exception {

		return new DefaultClassLoader().instanciate(className, args);
	}

	/**
	 * @param pluginManager
	 */
	public void setPluginManager(PluginManager pluginManager) {
		this.pluginManager = pluginManager;
	}

	public String getUserVisibleName(String id) {
		// this is no external plugin
		//  -> just return the name
		if (id.indexOf('$') == -1)
			return id;

		//String pluginId = id.substring(0, id.indexOf('$'));

		//String name = id.substring(id.indexOf('$'), id.length() - 1);

		int count = parentNode.count();

		for (int i = 0; i < count; i++) {

			XmlElement action = parentNode.getElement(i);
			String s = action.getAttribute("name");
			String s2 = action.getAttribute("uservisiblename");

			if (id.equals(s))
				return s2;
		}

		return null;
	}

	public void addExtension(String id, XmlElement extension) {
		ListIterator iterator = extension.getElements().listIterator();
		XmlElement action;
		while (iterator.hasNext()) {
			action = (XmlElement) iterator.next();
			String newName = id + '$' + action.getAttribute("name");
			String userVisibleName = action.getAttribute("name");

			// associate id with newName for later reference
			//transformationTable.put(id, newName);

			action.addAttribute("name", newName);
			action.addAttribute("uservisiblename", userVisibleName);

			parentNode.addElement(action);
		}
	}

}
