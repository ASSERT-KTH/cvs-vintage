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
package org.columba.addressbook.facade;

import java.util.List;
import java.util.Vector;

/**
 * Convenience class implementation of IGroupItem. This implementation class can be used by clients
 * of the addressbook facade if desired, or can be implemented differently, if necessary.
 */
public class GroupItem extends HeaderItem implements IGroupItem {

	private List<IContactItem> list = new Vector<IContactItem>();

	public GroupItem() {
		super(false);
	}

	/**
	 * @param id
	 */
	public GroupItem(String id) {
		super(id, false);
	}
	
	/**
	 * @param id
	 * @param name
	 * @param description
	 */
	public GroupItem(String id, String name, String description) {
		super(id, name, description, false);
	}	

	/* (non-Javadoc)
	 * @see org.columba.addressbook.facade.IGroupItem#getContacts()
	 */
	public List<IContactItem> getContacts() {
		return list;
	}

	/* (non-Javadoc)
	 * @see org.columba.addressbook.facade.IGroupItem#setContacts(java.util.List)
	 */
	public void setContacts(List<IContactItem> contacts) {
		list = contacts;
	}

	/* (non-Javadoc)
	 * @see org.columba.addressbook.facade.IGroupItem#addContact(org.columba.addressbook.facade.IContactItem)
	 */
	public void addContact(IContactItem item) {
		if (item == null)
			throw new IllegalArgumentException("item == null");
		list.add(item);
	}

	/* (non-Javadoc)
	 * @see org.columba.addressbook.facade.IGroupItem#removeContact(org.columba.addressbook.facade.IContactItem)
	 */
	public void removeContact(IContactItem item) {
		if (item == null)
			throw new IllegalArgumentException("item == null");
		list.remove(item);
	}

	/* (non-Javadoc)
	 * @see org.columba.addressbook.facade.IGroupItem#getContactCount()
	 */
	public int getContactCount() {
		return list.size();
	}
}
