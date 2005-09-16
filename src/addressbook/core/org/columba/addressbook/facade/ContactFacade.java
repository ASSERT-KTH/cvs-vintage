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
import org.columba.addressbook.gui.autocomplete.AddressCollector;
import org.columba.addressbook.gui.autocomplete.IAddressCollector;
import org.columba.addressbook.gui.tree.AddressbookTreeModel;
import org.columba.addressbook.model.Contact;
import org.columba.addressbook.model.IContact;
import org.columba.addressbook.model.VCARD;
import org.columba.core.logging.Logging;
import org.columba.ristretto.message.Address;
import org.columba.ristretto.parser.ParserException;

/**
 * Provides high-level contact management methods.
 * 
 * @author fdietz
 */
public final class ContactFacade implements IContactFacade {
	
	private static final java.util.logging.Logger LOG = 
        java.util.logging.Logger.getLogger("org.columba.addressbook.facade"); //$NON-NLS-1$

	/**
	 * @see org.columba.addressbook.facade.IContactFacade#addContact(int, java.lang.String)
	 */
	public void addContact(int uid, String address) {
		if (address == null) {
			return;
		}

		if (address.length() == 0)
			return;

		Address adr;
		try {
			adr = Address.parse(address);
		} catch (ParserException e1) {
			if (Logging.DEBUG)
				e1.printStackTrace();
			return;
		}

		LOG.info("address:" + address); //$NON-NLS-1$

		AbstractFolder selectedFolder = (AbstractFolder) AddressbookTreeModel
				.getInstance().getFolder(uid);
		try {
			if (selectedFolder.exists(adr.getMailAddress()) == null) {
				IContact card = new Contact();

				String fn = adr.getShortAddress();

				card.set(VCARD.FN, fn);
				card.set(VCARD.DISPLAYNAME, fn);
				card.set(VCARD.EMAIL, VCARD.EMAIL_TYPE_INTERNET, adr
						.getMailAddress());
				card.fillFullName(fn);
				selectedFolder.add(card);

			}
		} catch (Exception e) {

			e.printStackTrace();
		}

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

}