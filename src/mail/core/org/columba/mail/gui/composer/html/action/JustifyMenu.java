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
package org.columba.mail.gui.composer.html.action;

import javax.swing.ButtonGroup;
import javax.swing.JRadioButtonMenuItem;

import org.columba.core.action.IMenu;
import org.columba.core.gui.frame.AbstractFrameController;
import org.columba.core.main.MainInterface;
import org.columba.core.plugin.ActionPluginHandler;
import org.columba.mail.util.MailResourceLoader;

/**
 * Submenu lets you choose the paragraphs justification style. Which
 * can be a value of left, center or right.
 * 
 * TODO: add actionPerformed-method
 * 
 * @author fdietz
 */
public class JustifyMenu extends IMenu {

	ButtonGroup buttonGroup;

	/**
	 * @param controller
	 * @param caption
	 */
	public JustifyMenu(AbstractFrameController controller) {
		super(
			controller,
			MailResourceLoader.getString(
				"menu",
				"composer",
				"menu_format_justification"));

		try {
			initMenu();
		} catch (Exception e) {

			e.printStackTrace();
		}
	}

	protected void initMenu() throws Exception {
		buttonGroup = new ButtonGroup();

		ActionPluginHandler handler =
			(ActionPluginHandler) MainInterface.pluginManager.getHandler(
				"org.columba.core.action");

		JRadioButtonMenuItem m =
			new JRadioButtonMenuItem(
				handler.getAction("LeftJustifyAction", controller));

		add(m);
		buttonGroup.add(m);

		m =
			new JRadioButtonMenuItem(
				handler.getAction("CenterJustifyAction", controller));

		add(m);
		buttonGroup.add(m);

		m =
			new JRadioButtonMenuItem(
				handler.getAction("RightJustifyAction", controller));

		add(m);
		buttonGroup.add(m);
	}
}
