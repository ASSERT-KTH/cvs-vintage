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


import java.util.Vector;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTextField;

import org.columba.mail.util.AddressCollector;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:
 * @author
 * @version 1.0
 */

public class AddressComboBox extends JComboBox
{

    public AddressComboBox() {
	super();
	addCompleter();
    }
    public AddressComboBox( AddressbookTableView table) {
	super();
	addCompleter(table);
    }

    public AddressComboBox(ComboBoxModel cm) {
	super(cm);
	addCompleter();
    }

    public AddressComboBox(Object[] items) {
	super(items);
	addCompleter();
    }

    public AddressComboBox(Vector v) {
	super(v);
	addCompleter();
    }

    private void addCompleter() {
	setEditable(true);
	Object[] completions = getAddresses();
	new AutoCompleter(this, completions);
    }
    
    private void addCompleter(AddressbookTableView table) {
	setEditable(true);
	Object[] completions = getAddresses();
	new AutoCompleter(this, table, completions);
    }

    private Object[] getAddresses() {
	return AddressCollector.getAddresses();
    }

    public String getText() {
	return ((JTextField) getEditor().getEditorComponent()).getText();
    }

    public void setText(String text) {
	((JTextField) getEditor().getEditorComponent()).setText(text);
    }

}

