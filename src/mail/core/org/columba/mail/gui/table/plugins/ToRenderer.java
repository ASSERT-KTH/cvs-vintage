// The contents of this file are subject to the Mozilla Public License Version
// 1.1
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
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.mail.gui.table.plugins;

import org.columba.mail.gui.table.model.MessageNode;
import org.columba.mail.message.ColumbaHeader;

import org.columba.ristretto.message.Address;

import java.awt.Component;

import javax.swing.JTable;


public class ToRenderer extends DefaultLabelRenderer {
    public ToRenderer() {
        super();
    }

    public void updateUI() {
        super.updateUI();
    }

    public Component getTableCellRendererComponent(JTable table, Object value,
        boolean isSelected, boolean hasFocus, int row, int column) {
        if (value == null) {
            setText("");

            return this;
        }

        ColumbaHeader header = (ColumbaHeader) ((MessageNode) value).getHeader();

        Object adr = header.get("columba.to");
        if ( adr instanceof Address)
        	setText( ((Address)adr).getShortAddress());
        else
        	setText(adr.toString());
        	
        
        return super.getTableCellRendererComponent(table, value, isSelected,
            hasFocus, row, column);
    }
}
