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

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.columba.addressbook.folder.AbstractFolder;
import org.columba.addressbook.folder.IContactFolder;
import org.columba.addressbook.folder.IFolder;
import org.columba.addressbook.folder.IGroupFolder;
import org.columba.addressbook.gui.tree.AddressbookTreeModel;
import org.columba.addressbook.gui.tree.util.SelectAddressbookFolderDialog;
import org.columba.addressbook.model.ContactModel;
import org.columba.addressbook.model.EmailModel;
import org.columba.addressbook.model.IContactModel;
import org.columba.addressbook.model.IContactModelPartial;
import org.columba.addressbook.model.IGroup;
import org.columba.addressbook.parser.ParserUtil;
import org.columba.api.exception.StoreException;
import org.columba.core.logging.Logging;
import org.columba.ristretto.message.Address;
import org.columba.ristretto.parser.ParserException;

/**
 * Provides high-level contact management methods.
 * 
 * @author fdietz
 */
public final class ContactFacade implements IContactFacade {

	private static final java.util.logging.Logger LOG = java.util.logging.Logger
			.getLogger("org.columba.addressbook.facade"); //$NON-NLS-1$

	/**
	 * @see org.columba.addressbook.facade.IContactFacade#addContact(int,
	 *      java.lang.String)
	 */
	public void addContact(String uid, String address) throws StoreException {
		if (address == null || address.length() == 0)
			throw new IllegalArgumentException(
					"address == null or empty String");

		if (uid == null)
			throw new IllegalArgumentException("uid == null");

		AbstractFolder selectedFolder = (AbstractFolder) AddressbookTreeModel
				.getInstance().getFolder(uid);

		IContactModel card = createContactModel(address);

		try {
			if (selectedFolder.findByEmailAddress(card.getPreferredEmail()) == null)
				selectedFolder.add(card);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private ContactModel createContactModel(String address) {
		if (address == null || address.length() == 0)
			throw new IllegalArgumentException(
					"address == null or empty String");

		Address adr;
		try {
			adr = Address.parse(address);
		} catch (ParserException e1) {
			if (Logging.DEBUG)
				e1.printStackTrace();
			return null;
		}

		LOG.info("address:" + address); //$NON-NLS-1$

		ContactModel card = new ContactModel();

		String fn = adr.getShortAddress();

		card.setFormattedName(fn);
		// backwards compatibility
		card.setSortString(fn);
		card
				.addEmail(new EmailModel(adr.getMailAddress(),
						EmailModel.TYPE_WORK));

		String[] result = ParserUtil.tryBreakName(fn);
		card.setGivenName(result[0]);
		card.setFamilyName(result[1]);
		card.setAdditionalNames(result[2]);
		return card;
	}

	/**
	 * @see org.columba.addressbook.facade.IContactFacade#addContactToCollectedAddresses(java.lang.String)
	 */
//	public void addContactToCollectedAddresses(String address)
//			throws StoreException {
//		if (address == null || address.length() == 0)
//			throw new IllegalArgumentException(
//					"address == null or empty String");
//
//		addContact("102", address);
//	}

	/**
	 * @see org.columba.addressbook.facade.IContactFacade#addContactToPersonalAddressbook(java.lang.String)
	 */
//	public void addContactToPersonalAddressbook(String address)
//			throws StoreException {
//		if (address == null || address.length() == 0)
//			throw new IllegalArgumentException(
//					"address == null or empty String");
//		addContact("101", address);
//	}

	/**
	 * @see org.columba.addressbook.facade.IContactFacade#addContact(int,
	 *      java.lang.String[])
	 */
	public void addContact(String uid, String[] address) throws StoreException {
		if (uid == null)
			throw new IllegalArgumentException("uid == null");

		if (address == null || address.length == 0)
			throw new IllegalArgumentException(
					"address == null or null entry array");

		AddressbookTreeModel model = AddressbookTreeModel.getInstance();
		IContactFolder folder = (IContactFolder) model.getFolder(uid);

		for (int i = 0; i < address.length; i++) {
			IContactModel card = createContactModel(address[i]);

			try {
				if (folder.findByEmailAddress(card.getPreferredEmail()) == null)
					folder.add(card);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @see org.columba.addressbook.facade.IContactFacade#addContact(java.lang.String[])
	 */
	public void addContact(String[] address) throws StoreException {
		if (address == null || address.length == 0)
			throw new IllegalArgumentException(
					"address == null or null entry array");

		AddressbookTreeModel model = AddressbookTreeModel.getInstance();
		SelectAddressbookFolderDialog dialog = new SelectAddressbookFolderDialog(
				model);
		if (dialog.success()) {
			IFolder folder = dialog.getSelectedFolder();
			String uid = folder.getId();

			addContact(uid, address);
		} else
			return;
	}

	/**
	 * @see org.columba.addressbook.facade.IContactFacade#addContact(java.lang.String)
	 */
	public void addContact(String address) throws StoreException {
		if (address == null || address.length() == 0)
			throw new IllegalArgumentException(
					"address == null or empty String");

		AddressbookTreeModel model = AddressbookTreeModel.getInstance();
		SelectAddressbookFolderDialog dialog = new SelectAddressbookFolderDialog(
				model);
		if (dialog.success()) {
			IFolder folder = dialog.getSelectedFolder();
			String uid = folder.getId();

			addContact(uid, address);
		} else
			return;
	}

	/**
	 * @see org.columba.addressbook.facade.IContactFacade#getAllHeaderItems(java.lang.String)
	 */
	public List<IHeaderItem> getAllHeaderItems(String folderId,
			boolean flattenGroupItems) throws StoreException {
		if (folderId == null)
			throw new IllegalArgumentException("folderId == null");

		Vector<IHeaderItem> v = new Vector<IHeaderItem>();
		AddressbookTreeModel model = AddressbookTreeModel.getInstance();
		IFolder f = model.getFolder(folderId);
		if (f == null)
			return v;

		if (!(f instanceof IContactFolder))
			return v;

		IContactFolder folder = (IContactFolder) f;
		try {
			Iterator<IContactModelPartial> it = folder.getHeaderItemList()
					.iterator();
			while (it.hasNext()) {
				IContactModelPartial itemPartial = it.next();

				IContactItem item = createContactItem(itemPartial);

				v.add(item);
			}

			List<IGroupItem> groupList = getAllGroups(folderId);

			if (flattenGroupItems) {
				// retrieve all contact items and add those to the list only
				Iterator<IGroupItem> it2 = groupList.iterator();
				while (it2.hasNext()) {
					IGroupItem groupItem = it2.next();
					List<IContactItem> l = groupItem.getAllContacts();
					v.addAll(l);
				}
			} else {
				// simply all all group items to the list
				v.addAll(groupList);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return v;
	}

	/**
	 * @see org.columba.addressbook.facade.IContactFacade#getAllContacts(java.lang.String)
	 */
	public List<IContactItem> getAllContacts(String folderId) {
		if (folderId == null)
			throw new IllegalArgumentException("folderId == null");

		Vector<IContactItem> v = new Vector<IContactItem>();
		AddressbookTreeModel model = AddressbookTreeModel.getInstance();
		IFolder f = model.getFolder(folderId);
		if (f == null)
			return v;

		if (!(f instanceof IContactFolder))
			return v;

		IContactFolder folder = (IContactFolder) f;
		try {
			Iterator<IContactModelPartial> it = folder.getHeaderItemList()
					.iterator();
			while (it.hasNext()) {
				IContactModelPartial contactModel = it.next();

				IContactItem item = createContactItem(contactModel);

				v.add(item);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return v;
	}

	/**
	 * @see org.columba.addressbook.facade.IContactFacade#getAllGroups(java.lang.String)
	 */
	public List<IGroupItem> getAllGroups(String folderId) {
		if (folderId == null)
			throw new IllegalArgumentException("folderId = null");

		AddressbookTreeModel model = AddressbookTreeModel.getInstance();
		IFolder f = (IFolder) model.getFolder(folderId);
		if (f == null)
			throw new IllegalArgumentException("contact folder does not exist");

		Vector<IGroupItem> v = new Vector<IGroupItem>();

		if (!(f instanceof IContactFolder))
			return v;

		IContactFolder contactFolder = (IContactFolder) f;

		// add group items
		for (int i = 0; i < f.getChildCount(); i++) {
			IGroupFolder groupFolder = (IGroupFolder) f.getChildAt(i);
			IGroup group = groupFolder.getGroup();

			IGroupItem groupItem = new GroupItem(folderId);
			groupItem.setName(group.getName());
			groupItem.setDescription(group.getDescription());

			String[] members = group.getMembers();
			Map<String, IContactModelPartial> map = contactFolder
					.getContactItemMap(members);
			Iterator<IContactModelPartial> it = map.values().iterator();
			while (it.hasNext()) {
				IContactModelPartial partial = it.next();
				IContactItem item = createContactItem(partial);
				groupItem.addContact(item);
			}
			v.add(groupItem);
		}

		return v;
	}

	/**
	 * @see org.columba.addressbook.facade.IContactFacade#findByEmailAddress(java.lang.String,
	 *      java.lang.String)
	 */
	public String findByEmailAddress(String folderId, String emailAddress)
			throws StoreException {
		if (folderId == null)
			throw new IllegalArgumentException("folderId = null");
		if (emailAddress == null)
			throw new IllegalArgumentException("emailAddress == null");

		AddressbookTreeModel model = AddressbookTreeModel.getInstance();
		IFolder f = (IFolder) model.getFolder(folderId);
		if (f == null)
			throw new IllegalArgumentException("contact folder does not exist");

		if (!(f instanceof IContactFolder))
			return null;

		IContactFolder contactFolder = (IContactFolder) f;
		String id = contactFolder.findByEmailAddress(emailAddress);
		return id;
	}
	
	/**
	 * @see org.columba.addressbook.facade.IContactFacade#findByName(java.lang.String, java.lang.String)
	 */
	public String findByName(String folderId, String name) throws StoreException, IllegalArgumentException {
		if (folderId == null)
			throw new IllegalArgumentException("folderId = null");
		if (name == null)
			throw new IllegalArgumentException("name == null");

		AddressbookTreeModel model = AddressbookTreeModel.getInstance();
		IFolder f = (IFolder) model.getFolder(folderId);
		if (f == null)
			throw new IllegalArgumentException("contact folder does not exist");

		if (!(f instanceof IContactFolder))
			return null;

		IContactFolder contactFolder = (IContactFolder) f;
		String id = contactFolder.findByName(name);
		return id;
	}
	

	private IContactItem createContactItem(IContactModelPartial itemPartial) {
		IContactItem item = new ContactItem(itemPartial.getId(), itemPartial
				.getName(), itemPartial.getFirstname(), itemPartial
				.getLastname(), itemPartial.getAddress());

		return item;
	}

	/**
	 * @see org.columba.addressbook.facade.IContactFacade#getContactItem(java.lang.String, java.lang.String)
	 */
	public IContactItem getContactItem(String folderId, String contactId) throws StoreException, IllegalArgumentException {
		if (folderId == null)
			throw new IllegalArgumentException("folderId = null");
		if (contactId == null)
			throw new IllegalArgumentException("contactId == null");
		AddressbookTreeModel model = AddressbookTreeModel.getInstance();
		IFolder f = (IFolder) model.getFolder(folderId);
		if (f == null)
			throw new IllegalArgumentException("contact folder does not exist");

		if (!(f instanceof IContactFolder))
			return null;

		IContactFolder contactFolder = (IContactFolder) f;
		Map<String,IContactModelPartial> map = contactFolder.getContactItemMap(new String [] {contactId});
		if ( map.isEmpty()) return null;
		
		IContactModelPartial partial = map.get(contactId);
		
		return createContactItem(partial);
	}

	

}