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
package org.columba.addressbook.gui.table.model;

import javax.swing.event.TableModelListener;

import org.columba.addressbook.folder.HeaderItem;
import org.columba.addressbook.folder.HeaderItemList;

/**
 * Decorator for TableModel.
 * 
 * @author fdietz
 */
public abstract class TableModelDecorator
		implements
			HeaderListTableModel,
			TableModelListener {
	// the model which is decorated
	private HeaderListTableModel realModel;

	public TableModelDecorator(HeaderListTableModel model) {
		this.realModel = model;
		realModel.addTableModelListener(this);
	}

	/** *********************** TableModel implementation ******************* */
	public void addTableModelListener(TableModelListener l) {
		realModel.addTableModelListener(l);
	}

	public Class getColumnClass(int columnIndex) {
		return realModel.getColumnClass(columnIndex);
	}

	public int getColumnCount() {
		return realModel.getColumnCount();
	}

	public String getColumnName(int columnIndex) {
		return realModel.getColumnName(columnIndex);
	}

	public int getRowCount() {
		return realModel.getRowCount();
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		return realModel.getValueAt(rowIndex, columnIndex);
	}

	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return realModel.isCellEditable(rowIndex, columnIndex);
	}

	public void removeTableModelListener(TableModelListener l) {
		realModel.removeTableModelListener(l);
	}

	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		realModel.setValueAt(aValue, rowIndex, columnIndex);
	}

	/**
	 * Subclasses should use this method to access the underlying "real model"
	 * 
	 * @return
	 */
	protected HeaderListTableModel getRealModel() {
		return realModel;
	}

	/**
	 * @see org.columba.addressbook.gui.table.model.HeaderListTableModel#getHeaderList()
	 */
	public HeaderItemList getHeaderList() {
		return realModel.getHeaderList();
	}

	/**
	 * @see org.columba.addressbook.gui.table.model.HeaderListTableModel#setHeaderList(org.columba.addressbook.folder.HeaderItemList)
	 */
	public void setHeaderList(HeaderItemList list) {
		realModel.setHeaderList(list);
	}

	/**
	 * @see org.columba.addressbook.gui.table.model.HeaderListTableModel#getHeaderItem(int)
	 */
	public HeaderItem getHeaderItem(int index) {
		return realModel.getHeaderItem(index);
	}
}