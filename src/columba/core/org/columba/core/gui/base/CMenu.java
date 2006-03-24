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
package org.columba.core.gui.base;

import javax.swing.JMenu;


/**
 * @author frd
 * 
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates. To enable and disable the creation of type
 * comments go to Window>Preferences>Java>Code Generation.
 */

public class CMenu extends JMenu {
	private String menuId;

	/**
	 * Creates a menu item with the specified text. <br>
	 * If the textcontains &, the next character is used as mnemonic. If not, no
	 * mnemonic is set.
	 * 
	 * @param str
	 *            Menu text
	 */
	public CMenu(String str, String id) {
		super(str);

		menuId = id;
		// super(str);
		// set menu text incl. mnemonic if specified
		MnemonicSetter.setTextWithMnemonic(this, str);
	}

	public String getMenuId() {
		return menuId;
	}
}
