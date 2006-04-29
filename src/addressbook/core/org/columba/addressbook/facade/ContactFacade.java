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

import org.columba.addressbook.folder.AbstractFolder;
import org.columba.addressbook.folder.IContactFolder;
import org.columba.addressbook.folder.IFolder;
import org.columba.addressbook.gui.autocomplete.AddressCollector;
import org.columba.addressbook.gui.autocomplete.IAddressCollector;
import org.columba.addressbook.gui.tree.AddressbookTreeModel;
import org.columba.addressbook.gui.tree.util.SelectAddressbookFolderDialog;
import org.columba.addressbook.model.ContactModel;
import org.columba.addressbook.model.EmailModel;
import org.columba.addressbook.model.IContactModel;
import org.columba.addressbook.parser.ParserUtil;
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
	public void addContact(int uid, String address) {
		if (address == null) {
			return;
		}

		if (address.length() == 0)
			return;

		AbstractFolder selectedFolder = (AbstractFolder) AddressbookTreeModel
				.getInstance().getFolder(uid);

		IContactModel card = createContactModel(address);

		try {
			if (selectedFolder.exists(card.getPreferredEmail()) == null)
				selectedFolder.add(card);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private ContactModel createContactModel(String address) {
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
	public void addContactToCollectedAddresses(String address) {
		addContact(102, address);
	}

	/**
	 * @see org.columba.addressbook.facade.IContactFacade#getAddressCollector()
	 */
	public IAddressCollector getAddressCollector() {
		return AddressCollector.getInstance();
	}

	/**
	 * @see org.columba.addressbook.facade.IContactFacade#addContactToPersonalAddressbook(java.lang.String)
	 */
	public void addContactToPersonalAddressbook(String address) {
		addContact(101, address);
	}

	/**
	 * @see org.columba.addressbook.facade.IContactFacade#addContact(int,
	 *      java.lang.String[])
	 */
	public void addContact(int uid, String[] address) {
		AddressbookTreeModel model = AddressbookTreeModel.getInstance();
		IContactFolder folder = (IContactFolder) model.getFolder(uid);

		for (int i = 0; i < address.length; i++) {
			IContactModel card = createContactModel(address[i]);

			try {
				if (folder.exists(card.getPreferredEmail()) == null)
					folder.add(card);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @see org.columba.addressbook.facade.IContactFacade#addContact(java.lang.String[])
	 */
	public void addContact(String[] address) {
		AddressbookTreeModel model = AddressbookTreeModel.getInstance();
		SelectAddressbookFolderDialog dialog = new SelectAddressbookFolderDialog(
				model);
		if (dialog.success()) {
			IFolder folder = dialog.getSelectedFolder();
			int uid = folder.getUid();

			addContact(uid, address);
		} else
			return;
	}

	/**
	 * @see org.columba.addressbook.facade.IContactFacade#addContact(java.lang.String)
	 */
	public void addContact(String address) {
		AddressbookTreeModel model = AddressbookTreeModel.getInstance();
		SelectAddressbookFolderDialog dialog = new SelectAddressbookFolderDialog(
				model);
		if (dialog.success()) {
			IFolder folder = dialog.getSelectedFolder();
			int uid = folder.getUid();

			addContact(uid, address);
		} else
			return;
	}

}