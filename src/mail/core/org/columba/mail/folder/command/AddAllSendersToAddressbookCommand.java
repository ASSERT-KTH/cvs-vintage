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
package org.columba.mail.folder.command;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import org.columba.addressbook.facade.IContactFacade;
import org.columba.addressbook.facade.IContactItem;
import org.columba.addressbook.facade.IModelFacade;
import org.columba.api.command.ICommandReference;
import org.columba.api.command.IWorkerStatusController;
import org.columba.api.exception.ServiceNotFoundException;
import org.columba.api.exception.StoreException;
import org.columba.core.command.Command;
import org.columba.core.command.StatusObservableImpl;
import org.columba.core.folder.IFolderCommandReference;
import org.columba.mail.connector.FacadeUtil;
import org.columba.mail.connector.ServiceConnector;
import org.columba.mail.folder.IMailbox;
import org.columba.ristretto.message.Address;
import org.columba.ristretto.message.Header;
import org.columba.ristretto.parser.ParserException;

/**
 * Add all senders contained in the selected messages to the addressbook.
 * <p>
 * A dialog asks the user to choose the destination addressbook.
 * 
 * @author fdietz
 */
public class AddAllSendersToAddressbookCommand extends Command {
	org.columba.addressbook.folder.IFolder selectedFolder;

	/**
	 * Constructor for AddAllSendersToAddressbookCommand.
	 * 
	 * @param references
	 */
	public AddAllSendersToAddressbookCommand(ICommandReference reference) {
		super(reference);
	}

	/**
	 * @see org.columba.api.command.Command#execute(org.columba.api.command.Worker)
	 */
	public void execute(IWorkerStatusController worker) throws Exception {
		// get reference
		IFolderCommandReference r = (IFolderCommandReference) getReference();

		// selected messages
		Object[] uids = r.getUids();

		// selected folder
		IMailbox folder = (IMailbox) r.getSourceFolder();

		// register for status events
		((StatusObservableImpl) folder.getObservable()).setWorker(worker);

		IContactFacade contactFacade = null;
		IModelFacade modelFacade = null;
		try {
			contactFacade = ServiceConnector.getContactFacade();
			modelFacade = ServiceConnector.getModelFacade();
		} catch (ServiceNotFoundException e) {
			e.printStackTrace();
			return;
		}

		List<String> addresses = new ArrayList<String>();

		// for every message
		for (int i = 0; i < uids.length; i++) {
			// get header of message
			Header header = folder.getHeaderFields(uids[i], new String[] {
					"From", "To", "Cc", "Bcc" });

			String addrStr = (String) header.get("From");
			addresses.addAll(parseAddrStr(addrStr));

			addrStr = (String) header.get("To");
			addresses.addAll(parseAddrStr(addrStr));

			addrStr = (String) header.get("Cc");
			addresses.addAll(parseAddrStr(addrStr));

			addrStr = (String) header.get("Bcc");
			addresses.addAll(parseAddrStr(addrStr));

		}

		// add sender to addressbook
		Iterator<String> it = addresses.listIterator();
		List<IContactItem> contactItems = new ArrayList<IContactItem>();
		while (it.hasNext()) {
			try {
				String addrStr = it.next();
				if (addrStr == null)
					continue;
				Address address = Address.parse(addrStr);

				// add contact to addressbook
				IContactItem contactItem = modelFacade.createContactItem();
				FacadeUtil.getInstance().initContactItem(contactItem, address.getDisplayName(), address.getMailAddress());
				contactItems.add(contactItem);
			} catch (ParserException e) {
				e.printStackTrace();
			} catch (StoreException e) {
				e.printStackTrace();
			}
		}
		contactFacade.addContacts(contactItems.toArray(new IContactItem[contactItems.size()]));
	}
	
	/**
	 * Parse an address string containing multiple comma-separated mail addresses
	 * @param addrStr The comma-separated address string.
	 * @return List containing individual address strings
	 */
	private List<String> parseAddrStr(String addrStr) {
		List<String> addresses = new ArrayList<String>();
		if (addrStr == null)
			return addresses;
		StringTokenizer st = new StringTokenizer(addrStr, ",");
		while (st.hasMoreTokens()) {
			String addr = st.nextToken();
			addresses.add(addr);
		}
		return addresses;
	}

}