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
package org.columba.addressbook.gui.autocomplete;


import java.util.List;
import java.util.Vector;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTextField;


/**
 * ComboBox for {@link HeaderItem} objects. Includes an
 * autocomplete feature.
 *
 * @author fdietz
 */
public class BasicAddressAutocompleteComboBox extends JComboBox {
    public BasicAddressAutocompleteComboBox() {
        super();
    }

    public BasicAddressAutocompleteComboBox(ComboBoxModel cm) {
        super(cm);
        addCompleter();
    }

    public BasicAddressAutocompleteComboBox(Object[] items) {
        super(items);
        addCompleter();
    }

    public BasicAddressAutocompleteComboBox(List v) {
        super((Vector) v);
        addCompleter();
    }

    protected void addCompleter() {
        setEditable(true);

        Object[] completions = getAddresses();
        new AddressAutoCompleter(this, completions);
    }

    private Object[] getAddresses() {
        return AddressCollector.getInstance().getAddresses();
    }

    public String getText() {
        return ((JTextField) getEditor().getEditorComponent()).getText();
    }

    public void setText(String text) {
        ((JTextField) getEditor().getEditorComponent()).setText(text);
    }
}
