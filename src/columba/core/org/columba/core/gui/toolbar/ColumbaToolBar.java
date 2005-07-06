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
package org.columba.core.gui.toolbar;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ListIterator;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import javax.swing.Box;
import javax.swing.JToolBar;

import org.columba.core.action.AbstractColumbaAction;
import org.columba.core.gui.frame.FrameMediator;
import org.columba.core.gui.statusbar.ImageSequenceTimer;
import org.columba.core.plugin.PluginManager;
import org.columba.core.pluginhandler.ActionExtensionHandler;
import org.columba.core.xml.XmlElement;

/**
 * Toolbar which uses xml files to generate itself.
 * <p>
 * TODO: separate code which creates the toolbar from the swing JToolBar.
 * 
 * @author fdietz
 */
public class ColumbaToolBar extends JToolBar {

	private static final Logger LOG = Logger
			.getLogger("org.columba.core.gui.toolbar");

	ResourceBundle toolbarLabels;

	GridBagConstraints gridbagConstraints;

	GridBagLayout gridbagLayout;

	int i;

	XmlElement rootElement;

	FrameMediator frameController;

	public ColumbaToolBar(FrameMediator controller) {
		super();

		this.frameController = controller;

		createButtons();

		setRollover(true);

		setFloatable(false);
	}

	public ColumbaToolBar(XmlElement rootElement, FrameMediator controller) {
		super();
		this.frameController = controller;

		this.rootElement = rootElement;

		createButtons();

		setRollover(true);

		setFloatable(false);
	}

	public void extendToolbar(XmlElement rootElement, FrameMediator mediator) {
		this.frameController = mediator;
		extendToolbar(rootElement);
	}

	public void extendToolbar(XmlElement rootElement) {
		this.rootElement = rootElement;

		removeAll();

		ListIterator iterator = rootElement.getElements().listIterator();
		XmlElement buttonElement = null;

		while (iterator.hasNext()) {
			try {
				buttonElement = (XmlElement) iterator.next();

				if (buttonElement.getName().equals("button")) {
					// skip creation of cancel button
					if (buttonElement.getAttribute("action").equals("Cancel"))
						continue;

					ActionExtensionHandler handler = (ActionExtensionHandler) PluginManager
							.getInstance().getHandler(ActionExtensionHandler.NAME);

					AbstractColumbaAction action = handler.getAction(
							buttonElement.getAttribute("action"),
							frameController);
					
					addButton(action);
				} else if (buttonElement.getName().equals("separator")) {
					addSeparator();
				}
			} catch (Exception e) {
				LOG.info("toolbar-button="
						+ ((String) buttonElement.getAttribute("action")));

				e.printStackTrace();
			}
		}

		createButtons();
	}

	public boolean getVisible() {
		return Boolean.valueOf(rootElement.getAttribute("visible"))
				.booleanValue();
	}

	private void createButtons() {

		// add(Box.createHorizontalGlue());

		try {
			ActionExtensionHandler handler = (ActionExtensionHandler) PluginManager
					.getInstance().getHandler(ActionExtensionHandler.NAME);

			AbstractColumbaAction action = handler.getAction("Cancel",
					frameController);

			addButton(action);
		} catch (Exception e) {

			e.printStackTrace();
		}

		add(Box.createHorizontalGlue());

		ImageSequenceTimer image = frameController.getContainer()
				.getStatusBar().getImageSequenceTimer();
		add(image);
	}

	private void addButton(AbstractColumbaAction action) {
		if (action == null)
			throw new IllegalArgumentException("action == null");
		ToolbarButton button = new ToolbarButton(action);
		button.setRolloverEnabled(true);

		add(button);

	}
}