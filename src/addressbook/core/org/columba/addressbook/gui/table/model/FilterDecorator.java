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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.event.TableModelEvent;

import org.columba.addressbook.model.ContactItem;
import org.columba.addressbook.model.ContactItemMap;

/**
 * @author fdietz
 *  
 */
public class FilterDecorator extends TableModelDecorator {

	private List list;

	private String pattern = "";

	/**
	 * @param model
	 */
	public FilterDecorator(ContactItemTableModel model) {
		super(model);

		list = new ArrayList();

	}

	public void tableChanged(TableModelEvent e) {
		//allocate();
	}

	private void allocate() {
		list = new ArrayList();
	}

	/**
	 * @see org.columba.addressbook.gui.table.model.ContactItemTableModel#setContactItemMap(org.columba.addressbook.model.ContactItemMap)
	 */
	public void setContactItemMap(ContactItemMap list) {
		super.setContactItemMap(list);

		filter();
	}

	protected void filter() {
		this.list = new ArrayList();

		pattern = pattern.toLowerCase();

		Iterator it = getRealModel().getContactItemMap().iterator();
		int i = 0;
		while (it.hasNext()) {
			ContactItem item = (ContactItem) it.next();
			String name = item.getDisplayName();
			String adr = item.getAddress();

			name = name.toLowerCase();
			adr = adr.toLowerCase();

			if ((name.indexOf(pattern) != -1) || (adr.indexOf(pattern) != -1)) {

				this.list.add(new Integer(i));

			}
			i++;
		}
	}

	/**
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount() {
		return list.size();
	}

	/**
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	public Object getValueAt(int rowIndex, int columnIndex) {
		return getRealModel().getValueAt(
				((Integer) list.get(rowIndex)).intValue(), columnIndex);
	}

	/**
	 * @see javax.swing.table.TableModel#setValueAt(java.lang.Object, int, int)
	 */
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		getRealModel().setValueAt(aValue,
				((Integer) list.get(rowIndex)).intValue(), columnIndex);
	}

	/**
	 * @return Returns the pattern.
	 */
	public String getPattern() {
		return pattern;
	}

	/**
	 * @param pattern
	 *            The pattern to set.
	 */
	public void setPattern(String pattern) {
		this.pattern = pattern;

		filter();
	}

	/**
	 * @see org.columba.addressbook.gui.table.model.ContactItemTableModel#getContactItem(int)
	 */
	public ContactItem getContactItem(int index) {
		return getRealModel().getContactItem(
				((Integer) list.get(index)).intValue());
	}
}