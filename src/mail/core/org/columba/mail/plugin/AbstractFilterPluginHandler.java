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
package org.columba.mail.plugin;

import java.util.ListIterator;

import org.columba.core.plugin.AbstractPluginHandler;
import org.columba.core.xml.XmlElement;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public abstract class AbstractFilterPluginHandler
	extends AbstractPluginHandler {

	protected XmlElement parentNode;

	public AbstractFilterPluginHandler(
		String name,
		String configFile,
		String parent) {
		super(name, configFile);

		parentNode = getConfig().getRoot().getElement(parent);
	}

	/**
	 * @see org.columba.core.plugin.AbstractPluginHandler#getNames()
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
	 * @see org.columba.core.plugin.AbstractPluginHandler#getPluginClassName(java.lang.String, java.lang.String)
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

	public Object getGuiPlugin(String name, Object[] args) throws Exception {
		String className = getPluginClassName(name, "gui_class");
		return getPlugin(name, className, args);
	}

	public Object getActionPlugin(String name, Object[] args)
		throws Exception {
		String className = getPluginClassName(name, "main_class");

		return getPlugin(name, className, args);
	}

	public String getUserVisibleName(String id) {
		// this is no external plugin
		//  -> just return the name
		if ( id.indexOf('$') == -1 ) return id;
		
		//String pluginId = id.substring(0, id.indexOf('$'));
		
		
		//String name = id.substring(id.indexOf('$'), id.length() - 1);

		int count = parentNode.count();

		for (int i = 0; i < count; i++) {

			XmlElement action = parentNode.getElement(i);
			String s = action.getAttribute("name");
			String s2 = action.getAttribute("uservisiblename");
			
			if ( id.equals(s) )
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
