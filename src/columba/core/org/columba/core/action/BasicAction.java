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
package org.columba.core.action;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

/**
 * @author frd
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class BasicAction extends JAbstractAction {

	boolean showToolbarText = true;

	/**
	 * Method BasicAction.
	 * @param name
	 * @param longDescription
	 * @param tooltip
	 * @param actionCommand
	 * @param small_icon
	 * @param big_icon
	 * @param mnemonic
	 * @param keyStroke
	 * @param showToolbarText
	 */
	public BasicAction(
		String name,
		String longDescription,
		String actionCommand,
		ImageIcon small_icon,
		ImageIcon big_icon,
		int mnemonic,
		KeyStroke keyStroke) {
		this(
			name,
			longDescription,
			null,
			actionCommand,
			small_icon,
			big_icon,
			mnemonic,
			keyStroke);

	}

	public BasicAction(
		String name,
		String longDescription,
		String tooltip,
		String actionCommand,
		ImageIcon small_icon,
		ImageIcon big_icon,
		int mnemonic,
		KeyStroke keyStroke) {
		super();
		putValue(Action.NAME, name);
		putValue(Action.LONG_DESCRIPTION, longDescription);
		putValue(Action.SMALL_ICON, small_icon);
		LARGE_ICON = big_icon;

		putValue(Action.SHORT_DESCRIPTION, longDescription);
		putValue(Action.SHORT_DESCRIPTION, tooltip);
		putValue(Action.ACTION_COMMAND_KEY, actionCommand);
		putValue(Action.ACCELERATOR_KEY, keyStroke);
		putValue(Action.MNEMONIC_KEY, new Integer(mnemonic));

		TOOLBAR_NAME = name;

	}

	public BasicAction(
		String name,
		String longDescription,
		String actionCommand,
		ImageIcon small_icon,
		ImageIcon big_icon,
		int mnemonic,
		KeyStroke keyStroke,
		boolean showToolbarText) {
		this(
			name,
			longDescription,
			actionCommand,
			small_icon,
			big_icon,
			mnemonic,
			keyStroke);
		this.showToolbarText = showToolbarText;
	}

	public BasicAction(
		String name,
		String longDescription,
		String tooltip,
		String actionCommand,
		ImageIcon small_icon,
		ImageIcon big_icon,
		int mnemonic,
		KeyStroke keyStroke,
		boolean showToolbarText) {
		this(
			name,
			longDescription,
			tooltip,
			actionCommand,
			small_icon,
			big_icon,
			mnemonic,
			keyStroke);
		this.showToolbarText = showToolbarText;
	}

	/**
	 * @return boolean
	 */
	public boolean isShowToolbarText() {
		return showToolbarText;
	}

}