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
package org.columba.core.gui.util;

import javax.swing.JButton;

/**
 * Adds additionally a mnemonic to JButton.
 * <p>
 * Mnemonic should be specified in the button text 
 * by using the "&" character.
 *
 * @author fdietz
 */
public class ButtonWithMnemonic extends JButton {

	/**
	 * default constructor
	 *
	 */
	public ButtonWithMnemonic()
	{
		super();
	}
	
	/**
	 * @param str	text including a mnemonic
	 */
	public ButtonWithMnemonic(String textWithMnemonic) {
		super();
		
		int index = textWithMnemonic.indexOf("&");
		if (index != -1) {
			// mnemonic found

			StringBuffer buf = new StringBuffer();

			// if mnemonic is first character of this string
			if (index == 0)
				buf.append(textWithMnemonic.substring(1));
			else {
				buf.append(textWithMnemonic.substring(0, index));
				buf.append(textWithMnemonic.substring(index + 1));
			}

			setText(buf.toString());

			setDisplayedMnemonicIndex(index);

		} else {
			// no mnemonic found 
			// -> just display the label
			setText(textWithMnemonic);
		}
	}
}
