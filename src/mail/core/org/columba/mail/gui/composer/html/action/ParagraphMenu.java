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
import org.columba.mail.util.MailResourceLoader;

/**
 * Submenu for formatting text. 
 * <p>
 * Possible values are:
 *  - normal 
 *  - preformatted
 *  - heading 1
 *  - heading 2
 *  - heading 3
 *  - address
 * 
 * Note: This is the place to add further formats like lists, etc.
 *
 * TODO: add actionPerformed-method
 * 
 * @author fdietz
 */
public class ParagraphMenu extends IMenu {

	ButtonGroup group;

	public final static String[] STYLES =
		{
			"Normal",
			"Preformatted",
			"Heading 1",
			"Heading 2",
			"Heading 3",
			"Address" };
			

	/**
	 * @param controller
	 * @param caption
	 */
	public ParagraphMenu(AbstractFrameController controller) {
		super(
			controller,
			MailResourceLoader.getString(
				"menu",
				"composer",
				"menu_format_paragraph"));

		initMenu();
	}

	protected void initMenu() {
		group = new ButtonGroup();

		for (int i = 0; i < STYLES.length; i++) {
			JRadioButtonMenuItem m = new JRadioButtonMenuItem(STYLES[i]);
			add(m);

			group.add(m);
		}
	}

}
