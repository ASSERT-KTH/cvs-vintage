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
package org.columba.mail.gui.composer.util;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.columba.addressbook.facade.IContactItem;
import org.columba.addressbook.facade.IGroupItem;
import org.columba.addressbook.facade.IHeaderItem;

public class AddressCollector {

	private	Hashtable _adds = new Hashtable();
	private static AddressCollector instance = new AddressCollector();

	private AddressCollector() {
	}
	
	public static AddressCollector getInstance() {
		return instance;
	}

	/**
	 * Add all contacts and group items to hashmap.
	 * 
	 * @param uid			selected folder uid
	 * @param includeGroup	add groups if true. No groups, otherwise.
	 */
	public void addAllContacts(List<IHeaderItem> list, boolean includeGroup) {
		if ( list == null ) throw new IllegalArgumentException("list == null");
		
		Iterator<IHeaderItem> it = list.iterator();
		while (it.hasNext()) {
			IHeaderItem headerItem = it.next();

			if (headerItem.isContact()) {
				// contacts item
				IContactItem item = (IContactItem) headerItem;

				addAddress(item.getName(), item);
				addAddress(item.getFirstName(), item);
				addAddress(item.getLastName(), item);
				addAddress(item.getEmailAddress(), item);
			} else {
				if (includeGroup) {
					// group item
					IGroupItem item = (IGroupItem) headerItem;

					addAddress(item.getName(), item);
				}
			}
		}
	}

	public void addAddress(String add, IHeaderItem item) {
		if (add != null) {
			_adds.put(add, item);
		}
	}

	public Object[] getAddresses() {
		return _adds.keySet().toArray();
	}

	public IHeaderItem getHeaderItem(String add) {
		return (IHeaderItem) _adds.get(add);
	}

	public void clear() {
		_adds.clear();
	}

	/**
	 * @see org.frappucino.addresscombobox.ItemProvider#getMatchingItems(java.lang.String)
	 */
	public Object[] getMatchingItems(String s) {
		Object[] items = getAddresses();

		Vector v = new Vector();
		//		 for each JComboBox item
		for (int k = 0; k < items.length; k++) {
			// to lower case
			String item = items[k].toString().toLowerCase();
			// compare if item starts with str
			if (item.startsWith(s.toLowerCase())) {
				v.add(item);
			}
		}
		return v.toArray();
	}
}