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

import java.util.Map;

import javax.swing.event.TableModelEvent;
import javax.swing.table.TableModel;

import org.columba.addressbook.model.IContactModelPartial;

/**
 * Decorates TableModel which additional sorting functionality.
 * <p>
 * Using bubble-sort. Note, that we use an index array, which maps to the
 * real-model decorated by this class. So, we don't change the order of the real
 * model data.
 * 
 * @author fdietz
 */
public class SortDecorator extends TableModelDecorator {
	/** ****************** sorting algorithm ******************* */
	private int[] indexes;

	private int selectedColumn;

	private boolean sortOrder;

	public SortDecorator(ContactItemTableModel model) {
		super(model);

		selectedColumn = 0;
		sortOrder = false;

		allocate();
	}

	/**
	 * @see org.columba.addressbook.gui.table.model.ContactItemTableModel#setHeaderItemList(org.columba.addressbook.folder.HeaderItemList)
	 */
	public void setContactItemMap(Map<String,IContactModelPartial> list) {

		super.setContactItemMap(list);

		sort(selectedColumn);

	}

	/**
	 * @see org.columba.addressbook.gui.table.model.ContactItemTableModel#getHeaderItem(int)
	 */
	public IContactModelPartial getContactItem(int index) {
		return getRealModel().getContactItem(indexes[index]);
	}

	public void tableChanged(TableModelEvent e) {
		allocate();
	}

	public Object getValueAt(int row, int column) {
		return getRealModel().getValueAt(indexes[row], column);
	}

	public void setValueAt(Object aValue, int row, int column) {
		getRealModel().setValueAt(aValue, indexes[row], column);
	}

	public void sort(int column) {

		selectedColumn = column;

		int rowCount = getRowCount();

		for (int i = 0; i < rowCount; i++) {
			for (int j = i + 1; j < rowCount; j++) {
				int c = compare(indexes[i], indexes[j], column);
				if (!sortOrder) {
					if (c < 0)
						swap(i, j);
				} else {
					if (c > 0)
						swap(i, j);
				}
			}
		}
	}

	private void swap(int i, int j) {
		int tmp = indexes[i];
		indexes[i] = indexes[j];
		indexes[j] = tmp;
	}

	private int compare(int i, int j, int column) {
		TableModel realModel = getRealModel();
		Object io = realModel.getValueAt(i, column);
		Object jo = realModel.getValueAt(j, column);

		if ((io == null) || (jo == null)) {
			return 0;
		}

		int c = jo.toString().compareTo(io.toString());

		return (c < 0) ? (-1) : ((c > 0) ? 1 : 0);
	}

	private void allocate() {
		indexes = new int[getRowCount()];

		for (int i = 0; i < indexes.length; ++i) {
			indexes[i] = i;
		}
	}

	/**
	 * @return Returns the sortOrder.
	 */
	public boolean isSortOrder() {
		return sortOrder;
	}

	/**
	 * @param sortOrder
	 *            The sortOrder to set.
	 */
	public void setSortOrder(boolean sortOrder) {
		this.sortOrder = sortOrder;
	}

	/**
	 * @return Returns the selectedColumn.
	 */
	public int getSelectedColumn() {
		return selectedColumn;
	}

	/**
	 * @param selectedColumn
	 *            The selectedColumn to set.
	 */
	public void setSelectedColumn(int selectedColumn) {
		this.selectedColumn = selectedColumn;
	}
}