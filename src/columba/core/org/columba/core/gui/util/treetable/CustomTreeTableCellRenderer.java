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
package org.columba.core.gui.util.treetable;

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class CustomTreeTableCellRenderer extends Tree implements TableCellRenderer {

	protected int rowToPaint;
	protected TreeTable table;
	public CustomTreeTableCellRenderer(TreeTable table) {
		super();
		this.table = table;
	}

	// Move and resize the tree to the table position
	public void setBounds(int x, int y, int w, int h) {
		super.setBounds(x, 0, w, table.getHeight());
	}

	// start painting at the rowToPaint
	public void paint(Graphics g) {
		g.translate(0, -rowToPaint * getRowHeight());
		super.paint(g);
	}

	public Component getTableCellRendererComponent(
		JTable table,
		Object value,
		boolean isSelected,
		boolean hasFocus,
		int row,
		int column) {

		if (isSelected)
			setBackground(table.getSelectionBackground());
		else
			setBackground(table.getBackground());

		rowToPaint = row;
		return this;
	}
}