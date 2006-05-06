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
import java.util.List;
import java.util.Vector;

import org.columba.addressbook.folder.AbstractFolder;
import org.columba.addressbook.folder.IGroupFolder;
import org.columba.addressbook.gui.tree.AddressbookTreeModel;
import org.columba.addressbook.model.ContactModelFactory;
import org.columba.addressbook.model.ContactModelPartial;
import org.columba.addressbook.model.GroupPartial;
import org.columba.addressbook.model.HeaderItemPartial;
import org.columba.addressbook.model.IGroup;
import org.columba.addressbook.model.IGroupPartial;
import org.columba.addressbook.model.IHeaderItemPartial;

public class AddressCollector implements IAddressCollector {

	private Hashtable _adds = new Hashtable();

	private static AddressCollector instance = new AddressCollector();

	private AddressCollector() {
	}

	public static AddressCollector getInstance() {
		return instance;
	}

	/**
	 * Add all contacts and group items to hashmap.
	 * 
	 * @param uid
	 *            selected folder uid
	 * @param includeGroup
	 *            add groups if true. No groups, otherwise.
	 */
	public void addAllContacts(String uid, boolean includeGroup) {
		if (uid == null)
			throw new IllegalArgumentException("uid == null");

		List<IHeaderItemPartial> list = new Vector<IHeaderItemPartial>();

		try {
			AbstractFolder folder = (AbstractFolder) AddressbookTreeModel
					.getInstance().getFolder(uid);
			if (folder == null)
				return;

			list.addAll(folder.getHeaderItemList());

			if (includeGroup) {
				for (int i = 0; i < folder.getChildCount(); i++) {
					IGroupFolder groupFolder = (IGroupFolder) folder
							.getChildAt(i);
					IGroup group = groupFolder.getGroup();

					IGroupPartial groupPartial = ContactModelFactory.createGroupPartial(group, folder);

					list.add(groupPartial);
				}
			}

		} catch (Exception e) {

			e.printStackTrace();
		}

		Iterator it = list.iterator();
		while (it.hasNext()) {
			HeaderItemPartial headerItem = (HeaderItemPartial) it.next();

			if (headerItem.isContact()) {
				// contacts item
				ContactModelPartial item = (ContactModelPartial) headerItem;

				addAddress(item.getName(), item);
				addAddress(item.getLastname(), item);
				addAddress(item.getFirstname(), item);
				addAddress(item.getAddress(), item);
			} else {
				if (includeGroup) {
					// group item
					GroupPartial item = (GroupPartial) headerItem;

					addAddress(item.getName(), item);
				}
			}
		}
	}

	public void addAddress(String add, IHeaderItemPartial item) {
		if (add != null) {
			_adds.put(add, item);
		}
	}

	public Object[] getAddresses() {
		return _adds.keySet().toArray();
	}

	public IHeaderItemPartial getHeaderItem(String add) {
		return (IHeaderItemPartial) _adds.get(add);
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
		// for each JComboBox item
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