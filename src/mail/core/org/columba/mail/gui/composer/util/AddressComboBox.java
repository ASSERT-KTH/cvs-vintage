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
package org.columba.mail.gui.composer.util;

import org.columba.addressbook.gui.autocomplete.AddressCollector;
import org.columba.mail.gui.composer.HeaderView;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JComboBox;
import javax.swing.JTextField;


/**
 * Combox for entering email address. This widget is aware of 
 * it being an underlying component of JTable.
 * 
 * @author fdietz
 */
public class AddressComboBox extends JComboBox implements KeyListener {
    private HeaderView table;

    public AddressComboBox(HeaderView table) {
        super();

        this.table = table;

        setEditable(true);

        Object[] completions = AddressCollector.getInstance().getAddresses();

        new AutoCompleter(this, table, completions);

        setRenderer(new AddressComboBoxRenderer());

        //getTextField().addKeyListener(this);
    }

    private JTextField getTextField() {
        return ((JTextField) getEditor().getEditorComponent());
    }

    /******************* Key Listener **************************/
    public void keyTyped(KeyEvent e) {
        System.out.println("typed..");

        char ch = e.getKeyChar();

        if (ch == KeyEvent.VK_BACK_SPACE) {
            int length = getTextField().getText().length();

            if (length == 0) {
                table.removeEditingRow();

                int row = this.table.getSelectedRow();

                if (table.editCellAt(row, 1)) {
                    table.focusToTextField();
                }
            }
        }
    }

    public void keyPressed(KeyEvent e) {
        System.out.println("pressed..");
    }

    public void keyReleased(KeyEvent e) {
        System.out.println("released..");
    }
}
