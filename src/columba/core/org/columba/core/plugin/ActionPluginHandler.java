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

import java.util.HashMap;
import java.util.Map;

import org.columba.core.action.BasicAction;
import org.columba.core.action.IMenu;
import org.columba.core.gui.frame.AbstractFrameController;
import org.columba.core.io.DiskIO;
import org.columba.core.logging.ColumbaLogger;
import org.columba.core.xml.XmlElement;
import org.columba.core.xml.XmlIO;

public class ActionPluginHandler extends AbstractPluginHandler {

	/**
	 * collects all singleton objects 
	 * <p>
	 * this includes:
	 *  - cut
	 *  - copy
	 *  - paste
	 *  - delete
	 *  - select all
	 *  - undo
	 *  - redo
	 * 
	 *  @see isSingleton(String name)
	 * 
	 */
	Map map;

	public ActionPluginHandler() {
		super("org.columba.core.action", "org/columba/core/action/action.xml");

		parentNode = getConfig().getRoot().getElement("actionlist");

		map = new HashMap();

	}

	/**
	 * Returns true, if this is a single instance object which is 
	 * shared among all frames.
	 * 
	 * Note that the most promiment actions overwriting this will be:
	 *  - Cut/Copy/Paste/Delete
	 *  - Select All
	 *  - Undo/Redo
	 * 
	 * Specified by a property called "singletion" which can be of the value
	 * true/false. Found in the action node of the plugin.xml file respective
	 * the action.xml file.
	 * 
	 * @return	true, if action follows the singleton pattern, which
	 *          means its instanciated only once, and reused by every
	 *          MenuItem, Button, etc.
	 * 
	 *          false, is the default (which is the correct value for
	 *          almost all actions)
	 * 
	 */
	public boolean isSingleton(String name) {
		ColumbaLogger.log.debug("isSingleton="+name);

		Boolean b = new Boolean(getAttribute(name, "singleton"));
		boolean isSingleton = false;
		if (b != null)
			isSingleton = b.booleanValue();
		return isSingleton;
	}

	public BasicAction getAction(
		String name,
		AbstractFrameController controller)
		throws Exception {

		if (isSingleton(name)) {
			// their should be only one shared instance
			ColumbaLogger.log.debug("singleton found=" + name);

			if (map.containsKey(name)) {
				// already loaded 
				// -> return existing instance from hashmap
				// -> don't use this temporary instance

				return (BasicAction) map.get(name);
			} else {
				// put first time instance in hashmap
				BasicAction a =
					(BasicAction) getPlugin(name, new Object[] { controller });
				map.put(name, a);

				return a;
			}
		}

		return (BasicAction) getPlugin(name, new Object[] { controller });
	}

	public IMenu getIMenu(String name, AbstractFrameController controller)
		throws Exception {
		return (IMenu) getPlugin(name, new Object[] { controller });
	}

	public void addActionList(String actionXml) {
		XmlIO actionXmlIO = new XmlIO();
		actionXmlIO.setURL(DiskIO.getResourceURL(actionXml));
		actionXmlIO.load();

		XmlElement actionlist = actionXmlIO.getRoot().getElement("actionlist");

		for (int i = 0; i < actionlist.count(); i++) {
			parentNode.addElement(actionlist.getElement(i));
		}
	}

	/* (non-Javadoc)
	 * @see org.columba.core.plugin.AbstractPluginHandler#addPlugin(java.lang.String, java.io.File, org.columba.core.xml.XmlElement)
	 */
	/*
	public void addPlugin(String name, File pluginFolder, XmlElement element) {
		XmlElement extension;
	
		for( int i=0; i<element.count(); i++) {
			extension = element.getElement(i);
			extension.addAttribute("name",name+"$"+extension.getAttribute("name"));
			
			super.addPlugin(extension.getAttribute("name"), pluginFolder, element);
			parentNode.addElement(extension);
		}
	}
	*/

	/*
	public void addExtension(String id, XmlElement extension) {
		ListIterator iterator = extension.getElements().listIterator();
		XmlElement action;
		while( iterator.hasNext() ) {
			action = (XmlElement) iterator.next();
			action.addAttribute("name", id + '$' + action.getAttribute("name"));
			parentNode.addElement(action);
		}
	}
	*/
}
