// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Library General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

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

