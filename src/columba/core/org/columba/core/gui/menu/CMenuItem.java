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

package org.columba.core.gui.menu;

import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;

import org.columba.core.action.BasicAction;
import org.columba.core.help.HelpManager;

/**
 * Default MenuItem which automatically sets a JavaHelp topic ID
 * based on the AbstractAction name attribute.
 * <p>
 * This is necessary to provide a complete context-specific help.
 * 
 *
 * @author fdietz
 */
public class CMenuItem extends JMenuItem {

	/**
	 * Creates a menu item with a given action attached.
	 * <br>
	 * If JavaHelp topic ID is defined for the action, help is enabled
	 * for the menu.
	 * <br>
	 * If the name of the action contains &, the next character is used as
	 * mnemonic. If not, the fall-back solution is to use default behaviour,
	 * i.e. the mnemonic defined using setMnemonic on the action.
	 *  
	 * @param action	The action to attach to the menu item
	 */
	public CMenuItem( AbstractAction action )
	{
		super(action);

		BasicAction basicAction = (BasicAction) action;
		
		// Enable JavaHelp support if topic id is defined
		if ( basicAction.getTopicID() != null )
			HelpManager.enableHelpOnButton(this, basicAction.getTopicID());
		
		// Set text, possibly with a mnemonic if defined using &
		setTextInclMnemonic(basicAction.getName());

	}
	
	/**
	 * Creates a menu item with the specified text.
	 * <br>
	 * This does <i>not</i> enable JavaHelp support.
	 * <br>
	 * If the textcontains &, the next character is used as
	 * mnemonic. If not, no mnemonic is set.
	 * 
	 * @param	text	Text of menu item
	 */
	public CMenuItem(String text) {
		super();
		setTextInclMnemonic(text);
	}

	/**
	 * Private helper to set text of menu item incl. taking care of 
	 * setting the rigth mnemonic if specified using an & character in
	 * the menu text
	 * 
	 * @param	text	Text of menu item, possibly incl. & for mnemonic spec.
	 */
	private void setTextInclMnemonic(String text) {

		// search for mnemonic
		int index = text.indexOf("&");
		if ((index != -1) && ((index + 1) < text.length())) {
			// mnemonic found
			// ...and not at the end of the string (which doesn't make sence) 

			char mnemonic = text.charAt(index + 1);

			StringBuffer buf = new StringBuffer();

			// if mnemonic is first character of this string
			if (index == 0)
				buf.append(text.substring(1));
			else {
				buf.append(text.substring(0, index));
				buf.append(text.substring(index + 1));
			}

			// set display text
			this.setText(buf.toString());

			// set mnemonic
			this.setMnemonic(mnemonic);
			this.setDisplayedMnemonicIndex(index);

		} else {
			// no mnemonic found - just set the text on the menu item
			this.setText(text);
		}
		
	}
}
