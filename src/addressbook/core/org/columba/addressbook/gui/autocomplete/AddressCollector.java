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
package org.columba.addressbook.gui.autocomplete;

import java.util.Hashtable;
import java.util.Iterator;

import org.columba.addressbook.folder.AbstractFolder;
import org.columba.addressbook.main.AddressbookInterface;
import org.columba.addressbook.model.ContactItem;
import org.columba.addressbook.model.GroupItem;
import org.columba.addressbook.model.HeaderItem;
import org.columba.addressbook.model.HeaderItemList;

public class AddressCollector {
	private Hashtable _adds;

	private static AddressCollector instance;

	public AddressCollector() {
		_adds = new Hashtable();

	}

	public void addAllContacts(int uid) {
		HeaderItemList list = null;

		try {
			AbstractFolder folder = (AbstractFolder) AddressbookInterface.addressbookTreeModel
					.getFolder(uid);
			list = folder.getHeaderItemList();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (list == null)
			return;

		Iterator it = list.iterator();
		while (it.hasNext()) {
			HeaderItem headerItem = (HeaderItem) it.next();

			if (headerItem.isContact()) {
				// contacts item
				ContactItem item = (ContactItem) headerItem;

				addAddress(item.getDisplayName(), item);

				addAddress(item.getAddress(), item);
			} else {
				// group item
				GroupItem item = (GroupItem) headerItem;
				
				addAddress(item.getDisplayName(), item);
			}
		}
	}

	public static AddressCollector getInstance() {
		if (instance == null)
			instance = new AddressCollector();

		return instance;
	}

	public void addAddress(String add, HeaderItem item) {
		if (add != null) {
			_adds.put(add, item);
		}
	}

	public Object[] getAddresses() {
		return _adds.keySet().toArray();
	}

	public HeaderItem getHeaderItem(String add) {
		return (HeaderItem) _adds.get(add);
	}

	public void clear() {
		_adds.clear();
	}
}