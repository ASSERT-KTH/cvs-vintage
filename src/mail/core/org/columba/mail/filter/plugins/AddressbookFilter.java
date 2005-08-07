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
package org.columba.mail.filter.plugins;

import org.columba.addressbook.facade.IFolderFacade;
import org.columba.api.exception.ServiceNotFoundException;
import org.columba.core.filter.AbstractFilter;
import org.columba.core.filter.FilterCriteria;
import org.columba.core.folder.IFolder;
import org.columba.mail.connector.ServiceConnector;
import org.columba.mail.folder.IMailbox;
import org.columba.ristretto.message.Address;
import org.columba.ristretto.message.Header;
import org.columba.ristretto.parser.AddressParser;

/**
 * Check if sender is in all available addressbooks.
 * 
 * @author fdietz
 *  
 */
public class AddressbookFilter extends AbstractFilter {

	public AddressbookFilter() {

	}

	/**
	 * @see org.columba.core.filter.AbstractFilter#process(IFolder,
	 *      java.lang.Object)
	 */
	public boolean process(IFolder folder, Object uid)
			throws Exception {
		Header header = ((IMailbox)folder).getHeaderFields(uid, new String[] { "From" });
		String from = header.get("From");

		Address address = null;
		try {
			address = AddressParser.parseAddress(from);
		} catch (Exception ex) {
			return false;
		}

		IFolderFacade folderFacade = null;
		try {
			folderFacade = ServiceConnector.getFolderFacade();
		} catch (ServiceNotFoundException e) {

			e.printStackTrace();
			return false;
		}

		org.columba.addressbook.folder.IContactFolder addressbook = folderFacade
				.getCollectedAddresses();

		Object contactUid = addressbook.exists(address.getMailAddress());
		if (contactUid != null)
			return true;

		addressbook = folderFacade.getLocalAddressbook();

		contactUid = addressbook.exists(address.getMailAddress());

		if (contactUid != null)
			return true;

		return false;
	}

	/**
	 * @see org.columba.core.filter.AbstractFilter#setUp(org.columba.mail.filter.FilterCriteria)
	 */
	public void setUp(FilterCriteria f) {

	}
}