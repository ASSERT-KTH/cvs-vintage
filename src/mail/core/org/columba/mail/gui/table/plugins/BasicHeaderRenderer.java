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
package org.columba.mail.gui.table.plugins;

import org.columba.core.gui.util.AscendingIcon;
import org.columba.core.gui.util.DescendingIcon;

import org.columba.mail.gui.table.model.TableModelSorter;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;


public class BasicHeaderRenderer extends DefaultTableCellRenderer {
    private TableModelSorter sorter;
    private ImageIcon ascending = new AscendingIcon();
    private ImageIcon descending = new DescendingIcon();
    private String name;

    public BasicHeaderRenderer(String name, TableModelSorter sorter) {
        super();

        this.name = name;
        this.sorter = sorter;

        setHorizontalAlignment(SwingConstants.LEFT);
        setHorizontalTextPosition(SwingConstants.LEFT);
    }

    public Component getTableCellRendererComponent(JTable table, Object value,
        boolean isSelected, boolean hasFocus, int row, int column) {
        if (table != null) {
            JTableHeader header = table.getTableHeader();

            if (header != null) {
                setForeground(header.getForeground());
                setBackground(header.getBackground());
                setFont(header.getFont());
            }
        }

        setText((value == null) ? "" : value.toString());
        setBorder(UIManager.getBorder("TableHeader.cellBorder"));

        if (sorter.getSortingColumn().equals(name)) {
            if (sorter.getSortingOrder()) {
                setIcon(descending);
            } else {
                setIcon(ascending);
            }
        } else {
            setIcon(null);
        }

        return this;
    }
}
