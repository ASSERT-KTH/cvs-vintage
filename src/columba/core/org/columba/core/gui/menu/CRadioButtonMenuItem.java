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

import javax.swing.Action;
import javax.swing.JRadioButtonMenuItem;

import org.columba.core.action.AbstractColumbaAction;
import org.columba.core.gui.util.MnemonicSetter;


/**
 * Quick fix to fix the Mnemonic correctly.
 * @author redsolo
 */
public class CRadioButtonMenuItem extends JRadioButtonMenuItem {

    /**
     * default constructor
     */
    public CRadioButtonMenuItem() {
        super();
    }

    /**
     * @param name the name of the radio button.
     */
    public CRadioButtonMenuItem(String name) {
        super();
        // Set text, possibly with a mnemonic if defined using &
        MnemonicSetter.setTextWithMnemonic(this, name);
    }

    /**
     * Creates a checkbox menu item with a given action attached.
     * <br>
     * If the name of the action contains &, the next character is used as
     * mnemonic. If not, the fall-back solution is to use default behaviour,
     * i.e. the mnemonic defined using setMnemonic on the action.
     *
     * @param action        The action to attach to the menu item
     */
    public CRadioButtonMenuItem(AbstractColumbaAction action) {
        super(action);

        // Set text, possibly with a mnemonic if defined using &
        MnemonicSetter.setTextWithMnemonic(this,
            (String) action.getValue(Action.NAME));
        }
}
