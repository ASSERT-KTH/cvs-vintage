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

import javax.swing.AbstractButton;
import javax.swing.JLabel;


/**
 * This class contains utility methods to set text on buttons, checkboxes,
 * menus, menuitems and labels with mnemonics. The mnemonics is to be
 * specified using the & character in the display text just before the
 * mnemonic character.
 * <br>
 * The first & character in the display text is used to define the
 * mnemonic. Please be aware of this when trying to set display texts
 * containing a & character.
 *
 * @author Karl Peder Olesen (karlpeder)
 */
public class MnemonicSetter {
    /**
 * Sets the text of a menu, menuitem, button or checkbox. If
 * a & character is found, it is used to define the mnemonic.
 * Else the text is set just as if the setText method of the component
 * was called.
 *
 * @param        component        Menu, menuitem, button or checkbox to handle
 * @param        text                Displaytext, possibly including & for mnemonic
 *                                                 specification
 */
    public static void setTextWithMnemonic(AbstractButton component, String text) {
        // search for mnemonic
        int index = text.indexOf("&");

        if ((index != -1) && ((index + 1) < text.length())) {
            // mnemonic found
            // ...and not at the end of the string (which doesn't make sence) 
            char mnemonic = text.charAt(index + 1);

            StringBuffer buf = new StringBuffer();

            // if mnemonic is first character of this string
            if (index == 0) {
                buf.append(text.substring(1));
            } else {
                buf.append(text.substring(0, index));
                buf.append(text.substring(index + 1));
            }

            // set display text
            component.setText(buf.toString());

            // set mnemonic
            component.setMnemonic(mnemonic);
            component.setDisplayedMnemonicIndex(index);
        } else {
            // no mnemonic found - just set the text on the menu item
            component.setText(text);
        }
    }

    /**
 * Sets the text of a label including mnemonic.
 * <br>
 * Same functionality as @see setTextWithMnemonic
 *
 * @param        label                Label to handle
 * @param        text                Displaytext, possibly including & for mnemonic
 *                                                 specification
 */
    public static void setTextWithMnemonicOnLabel(JLabel label, String text) {
        // search for mnemonic
        int index = text.indexOf("&");

        if ((index != -1) && ((index + 1) < text.length())) {
            // mnemonic found
            // ...and not at the end of the string (which doesn't make sence) 
            char mnemonic = text.charAt(index + 1);

            StringBuffer buf = new StringBuffer();

            // if mnemonic is first character of this string
            if (index == 0) {
                buf.append(text.substring(1));
            } else {
                buf.append(text.substring(0, index));
                buf.append(text.substring(index + 1));
            }

            // set display text
            label.setText(buf.toString());

            // set mnemonic
            label.setDisplayedMnemonic(mnemonic);
            label.setDisplayedMnemonicIndex(index);
        } else {
            // no mnemonic found - just set the text on the menu item
            label.setText(text);
        }
    }
}
