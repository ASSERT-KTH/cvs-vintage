// The contents of this file are subject to the Mozilla Public License Version
// 1.1
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
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.core.gui.globalactions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JMenuItem;

import org.columba.api.gui.frame.IFrameMediator;
import org.columba.api.plugin.IExtension;
import org.columba.api.plugin.IExtensionHandler;
import org.columba.api.plugin.IExtensionHandlerKeys;
import org.columba.api.plugin.PluginException;
import org.columba.api.plugin.PluginHandlerNotFoundException;
import org.columba.api.plugin.PluginLoadingFailedException;
import org.columba.core.gui.action.AbstractColumbaAction;
import org.columba.core.gui.base.CMenuItem;
import org.columba.core.gui.frame.FrameManager;
import org.columba.core.gui.menu.IMenu;
import org.columba.core.plugin.PluginManager;

public class NewSubmenu extends IMenu implements ActionListener {

	public NewSubmenu(IFrameMediator controller) {
		super(controller, controller.getString("menu", "mainframe",
				"menu_file_new"), "NewSubMenu");

		IExtensionHandler frameHandler = null;
		try {
			frameHandler = PluginManager.getInstance().getExtensionHandler(
					IExtensionHandlerKeys.ORG_COLUMBA_CORE_FRAME);
		} catch (PluginHandlerNotFoundException e) {
			e.printStackTrace();
			return;
		}

		IExtensionHandler newItemHandler = null;
		try {
			newItemHandler = PluginManager.getInstance().getExtensionHandler(
					IExtensionHandlerKeys.ORG_COLUMBA_CORE_NEWITEM);
		} catch (PluginHandlerNotFoundException e) {
			e.printStackTrace();
			return;
		}

		// create menuitems for all registered frame extensions
		String[] managedFrames = getManagedFrames(frameHandler);
		for (int i = 0; i < managedFrames.length; i++) {
			JMenuItem menu = createMenu(managedFrames[i], managedFrames[i]);
			add(menu);
		}

		addSeparator();

		// create menuitems for all registered new items
		String[] ids = getAllItems(newItemHandler);
		for ( int i=0; i<ids.length; i++) {
			JMenuItem item = createNewItemMenuItem(newItemHandler, ids[i]);
			if ( item == null ) continue;
			add(item);
		}

	}

	private JMenuItem createNewItemMenuItem(IExtensionHandler newItemHandler, String id) {
		AbstractColumbaAction action = null;
		IExtension extension = newItemHandler.getExtension(id);
		if ( extension == null ) return null;
		
		try {
			action = (AbstractColumbaAction) extension.instanciateExtension(new Object[]{getFrameMediator()});
			return new CMenuItem(action);
		} catch (PluginException e) {
			e.printStackTrace();
			return null;
		}
	}

	private JMenuItem createMenu(String name, String actionCommand) {
		JMenuItem menu = new CMenuItem(name);
		menu.setActionCommand(actionCommand);
		menu.addActionListener(this);
		return menu;
	}

	public String[] getManagedFrames(IExtensionHandler handler) {
		Vector<String> result = new Vector<String>();
		Enumeration _enum = handler.getExtensionEnumeration();
		while (_enum.hasMoreElements()) {
			IExtension extension = (IExtension) _enum.nextElement();
			String managed = extension.getMetadata().getAttribute("managed");
			if (managed == null)
				managed = "false";

			if (managed.equals("true"))
				result.add(extension.getMetadata().getId());
		}
		return (String[]) result.toArray(new String[0]);
	}

	public String[] getAllItems(IExtensionHandler handler) {
		Vector<String> result = new Vector<String>();
		Enumeration _enum = handler.getExtensionEnumeration();
		while (_enum.hasMoreElements()) {
			IExtension extension = (IExtension) _enum.nextElement();
			result.add(extension.getMetadata().getId());
		}
		return (String[]) result.toArray(new String[0]);
	}

	public void actionPerformed(ActionEvent event) {
		final String action = event.getActionCommand();

		try {
			FrameManager.getInstance().openView(action);
		} catch (PluginLoadingFailedException e) {
			e.printStackTrace();
		}

	}

}
