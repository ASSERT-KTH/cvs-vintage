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

import javax.swing.table.AbstractTableModel;

/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class AbstractTreeTableModel extends AbstractTableModel {

	protected Tree tree;

	protected String[] columns;

	/**
	 * 
	 */
	public AbstractTreeTableModel(String[] columns) {
		this.columns = columns;

	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount() {
		return tree.getRowCount();
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount() {
		return columns.length;
	}

	/**
	 * overwrite this method...
	 */
	public Object getValueAt(int row, int col) {
		if (col == 0)
			return tree;

		return null;
	}

	public String getColumnName(int col) {
		return columns[col];
	}

	public Class getColumnClass(int c) {
		if (c == 0)
			return tree.getClass();

		return getValueAt(0, c).getClass();
	}

	public boolean isCellEditable(int row, int col) {
		if (col == 0)
			return true;

		return false;
	}

	/**
	 * @param tree
	 */
	public void setTree(Tree tree) {
		this.tree = tree;
		
		
	}

	/**
	 * @return
	 */
	public Tree getTree() {
		return tree;
	}

}
