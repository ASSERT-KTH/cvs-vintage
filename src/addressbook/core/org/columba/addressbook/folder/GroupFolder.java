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
package org.columba.addressbook.folder;

import javax.swing.ImageIcon;

import org.columba.addressbook.config.FolderItem;
import org.columba.addressbook.model.Contact;
import org.columba.addressbook.model.ContactItem;
import org.columba.addressbook.model.ContactItemMap;
import org.columba.addressbook.model.Group;
import org.columba.core.command.WorkerStatusController;
import org.columba.core.gui.util.ImageLoader;
import org.columba.core.xml.XmlElement;

/**
 * Group folder storing a list of contact indices.
 * <p>
 * This can be seen as a filtered view of a contact folder. 
 * 
 * @author fdietz
 *  
 */
public class GroupFolder extends AbstractFolder implements ContactStorage {

	private Group group;

	private ImageIcon groupImageIcon = ImageLoader
			.getSmallImageIcon("group_small.png");

	/**
	 * @param name
	 */
	public GroupFolder(String name, String dir) {
		super(name, dir);

		group = new Group();
	}

	/**
	 * @param item
	 */
	public GroupFolder(FolderItem item) {
		super(item);

		XmlElement property = item.getElement("property");
		XmlElement e = property.getElement("group");
		if (e == null) {
			e = new XmlElement("group");
			property.addElement(e);
		}

		group = new Group(e, getUid());
	}

	public void createChildren(WorkerStatusController worker) {
	}

	/**
	 * @see org.columba.addressbook.folder.ContactStorage#add(org.columba.addressbook.model.Contact)
	 */
	public Object add(Contact contact) throws Exception {
		Object uid = contact.getUid();

		group.addMember(uid);

		fireItemAdded(uid);

		return uid;
	}

	/**
	 * @see org.columba.addressbook.folder.ContactStorage#count()
	 */
	public int count() {

		return group.count();
	}

	/**
	 * @see org.columba.addressbook.folder.ContactStorage#exists(java.lang.Object)
	 */
	public boolean exists(Object uid) {

		return group.exists(uid);
	}

	/**
	 * @see org.columba.addressbook.folder.ContactStorage#get(java.lang.Object)
	 */
	public Contact get(Object uid) throws Exception {

		AbstractFolder parent = (AbstractFolder) getParent();

		return parent.get(uid);
	}

	/**
	 * @see org.columba.addressbook.folder.ContactStorage#modify(java.lang.Object,
	 *      org.columba.addressbook.model.Contact)
	 */
	public void modify(Object uid, Contact contact) throws Exception {
		AbstractFolder parent = (AbstractFolder) getParent();

		parent.modify(uid, contact);

		fireItemChanged(uid);

	}

	/**
	 * @see org.columba.addressbook.folder.ContactStorage#remove(java.lang.Object)
	 */
	public void remove(Object uid) throws Exception {
		group.remove(uid);

		fireItemRemoved(uid);
	}

	/**
	 * @see org.columba.addressbook.folder.ContactStorage#getHeaderItemList()
	 */
	public ContactItemMap getContactItemMap() throws Exception {
		AbstractFolder parent = (AbstractFolder) getParent();

		ContactItemMap filter = new ContactItemMap();

		Integer[] members = group.getMembers();
		for (int i = 0; i < members.length; i++) {
			Contact c = parent.get(members[i]);
			if (c == null) {
				// contact doesn't exist in parent folder anymore
				// -> remove it

				remove(members[i]);
			} else {
				ContactItem item = new ContactItem(c);
				item.setUid(members[i]);
				
				filter.add(members[i], item);
			}
		}

		return filter;
	}

	/**
	 * @return Returns the group.
	 */
	public Group getGroup() {
		return group;
	}

	/**
	 * @see org.columba.addressbook.gui.tree.AddressbookTreeNode#getIcon()
	 */
	public ImageIcon getIcon() {
		return groupImageIcon;
	}
}