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

import org.columba.addressbook.folder.HeaderItem;
import org.columba.addressbook.gui.table.util.HeaderColumnInterface;

import java.awt.Component;

import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;


/**
 * @author frd
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class ComboBoxHeaderColumn extends JComboBox
    implements HeaderColumnInterface, TableCellRenderer {
    protected Border unselectedBorder = null;
    protected Border selectedBorder = null;
    protected boolean isBordered = true;
    protected String value;
    protected String name;

    public ComboBoxHeaderColumn(String name) {
        this.value = null;
        this.name = name;
        setOpaque(true);

        isBordered = true;
    }

    public ComboBoxHeaderColumn(String name, String value) {
        this.value = value;
        this.name = name;
        setOpaque(true);

        isBordered = true;
    }

    public Component getTableCellRendererComponent(JTable table, Object object,
        boolean isSelected, boolean hasFocus, int row, int column) {
        setSelectedItem((String) object);

        return this;
    }

    public Object getValue(HeaderItem item) {
        if (item == null) {
            return "";
        }

        if (name == null) {
            return "";
        }

        Object o = item.get((String) name);

        if (o == null) {
            return "";
        }

        return o;
    }

    public String getName() {
        return name;
    }

    public String getValueString() {
        return value;
    }

    public int getColumnSize() {
        return -1;
    }
}
