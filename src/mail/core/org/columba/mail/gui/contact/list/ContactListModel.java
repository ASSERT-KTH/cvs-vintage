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
package org.columba.mail.gui.contact.list;

import java.util.List;
import java.util.Vector;

import javax.swing.AbstractListModel;

import org.columba.addressbook.facade.IHeaderItem;

/**
 * @version 1.0
 * @author
 */

public class ContactListModel extends AbstractListModel {
	private List<IHeaderItem> list;

	private String patternString = "";

	public ContactListModel() {
		super();

		list = new Vector<IHeaderItem>();

	}

	public Object getElementAt(int index) {
		return (IHeaderItem) list.get(index);
	}

	public int getSize() {
		return list.size();
	}

	public String getPatternString() {
		return patternString;
	}

	public void setPatternString(String s) throws Exception {
		patternString = s;

		// manipulateModel(TableModelPlugin.STRUCTURE_CHANGE);
	}

	public void clear() {
		list.clear();
	}

	public void addElement(IHeaderItem item) {
		list.add(item);

		int index = list.indexOf(item);

		fireIntervalAdded(this, index, index);
	}

	public void setHeaderItemList(List<IHeaderItem> l) {

		this.list = l;

		fireContentsChanged(this, 0, list.size() - 1);
	}

	public IHeaderItem get(int i) {
		return (IHeaderItem) list.get(i);
	}

	public boolean addItem(IHeaderItem header) {
		boolean result1 = false;

		String o = header.getName();

		if (o != null) {

			String item = (String) o;

			// System.out.println("add item?:"+item);
			item = item.toLowerCase();

			String pattern = getPatternString().toLowerCase();

			if (item.indexOf(pattern) != -1) {
				result1 = true;
			} else {
				result1 = false;
			}

		} 

		return result1;
	}

	public Object[] toArray() {
		return list.toArray();
	}

	public void remove(int index) {
		list.remove(index);
		fireIntervalRemoved(this, index, index);
	}

	public void removeElement(IHeaderItem item) {
		int index = list.indexOf(item);

		remove(index);
	}
}
