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

import org.columba.addressbook.model.HeaderItem;

import java.awt.Component;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;


/**
 * 
 *
 * @author fdietz
 */
public class FieldEditor extends DefaultCellEditor {
    /**
 * @param arg0
 */
    public FieldEditor(JComboBox arg0) {
        super(arg0);
    }

    /**
 * @see javax.swing.table.TableCellEditor#getTableCellEditorComponent(javax.swing.JTable, java.lang.Object, boolean, int, int)
 */
    public Component getTableCellEditorComponent(JTable arg0, Object value,
        boolean arg2, int arg3, int arg4) {
        super.getTableCellEditorComponent(arg0, value, arg2, arg3, arg4);

        HeaderItem item = (HeaderItem) value;
        String s = (String) item.getHeader();

        ((JComboBox) editorComponent).setSelectedItem(s);

        return editorComponent;
    }
}
