///The contents of this file are subject to the Mozilla Public License Version 1.1
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
package org.columba.mail.gui.config.pop3preprocessor;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

import org.columba.core.xml.XmlElement;


class FilterListTable extends JTable {
    public FilterListTable(XmlElement filterList, ConfigFrame frame) {
        super(new FilterListTableModel(filterList));
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setShowGrid(false);
        setIntercellSpacing(new java.awt.Dimension(0, 0));

        TableColumn tc = getColumnModel().getColumn(1);
        tc.setMaxWidth(80);
        tc.setMinWidth(80);

        DefaultTableCellRenderer renderer = (DefaultTableCellRenderer) tableHeader.getDefaultRenderer();
        renderer.setHorizontalAlignment(DefaultTableCellRenderer.LEFT);
    }

    public void update() {
        ((FilterListTableModel) getModel()).fireTableDataChanged();
    }
}
