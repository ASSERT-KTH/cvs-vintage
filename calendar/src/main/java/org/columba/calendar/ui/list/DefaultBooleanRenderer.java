package org.columba.calendar.ui.list;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellRenderer;

import org.columba.calendar.base.api.ICalendarItem;

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

/**
 * 
 * 
 * @author fdietz
 */

public class DefaultBooleanRenderer extends JCheckBox implements
		TableCellRenderer {

	public DefaultBooleanRenderer() {

		setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));

		setHorizontalAlignment(SwingUtilities.CENTER);
	}

	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {

		ICalendarItem item = (ICalendarItem) value;

		if (isSelected) {
			setForeground(table.getSelectionForeground());
			setBackground(table.getSelectionBackground());
		} else {
			setForeground(table.getForeground());
			setBackground(table.getBackground());

		}

		setBackground(item.getColor());

		setSelected(item.isSelected());

		return this;
	}

}
