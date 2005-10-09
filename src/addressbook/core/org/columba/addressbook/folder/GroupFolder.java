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
import org.columba.addressbook.model.ContactItem;
import org.columba.addressbook.model.ContactItemMap;
import org.columba.addressbook.model.Group;
import org.columba.addressbook.model.IContact;
import org.columba.addressbook.model.IContactItem;
import org.columba.addressbook.model.IContactItemMap;
import org.columba.addressbook.model.IContactModel;
import org.columba.addressbook.model.IGroup;
import org.columba.api.command.IWorkerStatusController;
import org.columba.core.resourceloader.ImageLoader;
import org.columba.core.xml.XmlElement;

/**
 * Group folder storing a list of contact indices.
 * <p>
 * This can be seen as a filtered view of a contact folder. 
 * 
 * @author fdietz
 *  
 */
public class GroupFolder extends AbstractFolder implements IContactStorage, IGroupFolder {

	private IGroup group;

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

	public void createChildren(IWorkerStatusController worker) {
	}

	/**
	 * @see org.columba.addressbook.folder.IContactStorage#add(IContactModel)
	 */
	public Object add(IContactModel contact) throws Exception {
		Object uid = contact.getId();

		group.addMember(uid);

		fireItemAdded(uid);

		return uid;
	}

	/**
	 * @see org.columba.addressbook.folder.IContactStorage#count()
	 */
	public int count() {

		return group.count();
	}

	/**
	 * @see org.columba.addressbook.folder.IContactStorage#exists(java.lang.Object)
	 */
	public boolean exists(Object uid) {

		return group.exists(uid);
	}

	/**
	 * @see org.columba.addressbook.folder.IContactStorage#get(java.lang.Object)
	 */
	public IContactModel get(Object uid) throws Exception {

		AbstractFolder parent = (AbstractFolder) getParent();

		return parent.get(uid);
	}

	/**
	 * @see org.columba.addressbook.folder.IContactStorage#modify(java.lang.Object,
	 *      IContactModel)
	 */
	public void modify(Object uid, IContactModel contact) throws Exception {
		AbstractFolder parent = (AbstractFolder) getParent();

		parent.modify(uid, contact);

		fireItemChanged(uid);

	}

	/**
	 * @see org.columba.addressbook.folder.IContactStorage#remove(java.lang.Object)
	 */
	public void remove(Object uid) throws Exception {
		group.remove(uid);

		fireItemRemoved(uid);
	}

	/**
	 * @see org.columba.addressbook.folder.IContactStorage#getHeaderItemList()
	 */
	public IContactItemMap getContactItemMap() throws Exception {
		AbstractFolder parent = (AbstractFolder) getParent();

		IContactItemMap filter = new ContactItemMap();

		Integer[] members = group.getMembers();
		for (int i = 0; i < members.length; i++) {
			IContactModel c = parent.get(members[i]);
			if (c == null) {
				// contact doesn't exist in parent folder anymore
				// -> remove it

				remove(members[i]);
			} else {
				IContactItem item = new ContactItem(c);
				item.setUid(members[i]);
				
				filter.add(members[i], item);
			}
		}

		return filter;
	}

	/**
	 * @return Returns the group.
	 */
	public IGroup getGroup() {
		return group;
	}

	/**
	 * @see org.columba.addressbook.folder.AddressbookTreeNode#getIcon()
	 */
	public ImageIcon getIcon() {
		return groupImageIcon;
	}
}