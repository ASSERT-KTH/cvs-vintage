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

package org.columba.core.gui.menu;

import org.columba.core.action.AbstractColumbaAction;
import org.columba.core.gui.frame.FrameMediator;
import org.columba.core.main.MainInterface;
import org.columba.core.plugin.PluginHandlerNotFoundException;
import org.columba.core.pluginhandler.MenuPluginHandler;
import org.columba.core.xml.XmlElement;

import java.awt.event.MouseAdapter;

import javax.swing.JMenu;
import javax.swing.JMenuBar;

public class ColumbaMenu extends JMenuBar {
	private MouseAdapter handler;

	String menuRoot;

	private FrameMediator frameController;

	protected MenuBarGenerator menuGenerator;

	public ColumbaMenu(String xmlRoot, FrameMediator frameController) {
		super();

		this.menuRoot = xmlRoot;
		this.frameController = frameController;

		menuGenerator = createMenuBarGeneratorInstance(xmlRoot, frameController);

	}

	public MenuBarGenerator createMenuBarGeneratorInstance(String xmlRoot,
			FrameMediator frameController) {

		menuGenerator = new MenuBarGenerator(frameController, xmlRoot);
		menuGenerator.createMenuBar(this);

		try {
			((MenuPluginHandler) MainInterface.pluginManager
					.getHandler("org.columba.core.menu")).insertPlugins(this);
		} catch (PluginHandlerNotFoundException ex) {
			throw new RuntimeException(ex);
		}

		return menuGenerator;
	}


	public void extendMenuFromFile(FrameMediator mediator, String path) {
		menuGenerator = createMenuBarGeneratorInstance(menuRoot, mediator);

		menuGenerator.extendMenuFromFile(path);
		menuGenerator.createMenuBar(this);
	}

	public void extendMenu(XmlElement menuExtension) {
		menuGenerator.extendMenu(menuExtension);
		menuGenerator.createMenuBar(this);
	}

	public void addMenuEntry(String id, AbstractColumbaAction action) {
		CMenuItem menuItem = new CMenuItem(action);
		menuItem.addMouseListener(handler);

		JMenu menu = getMenu(id);
		menu.add(menuItem);
	}

	public JMenu getMenu(String id) {
		for (int i = 0; i < getMenuCount(); i++) {
			JMenu menu = (JMenu) getComponent(i);

			if (menu.getActionCommand().equalsIgnoreCase(id)) {
				// found the right menu
				return menu;
			}
		}

		return null;
	}

	public void addMenuSeparator(String id) {
		for (int i = 0; i < getMenuCount(); i++) {
			JMenu menu = (JMenu) getComponent(i);

			if (menu.getActionCommand().equalsIgnoreCase(id)) {
				// found the right menu
				menu.addSeparator();
			}
		}
	}

	/**
	 * @return
	 */
	public FrameMediator getFrameController() {
		return frameController;
	}
}