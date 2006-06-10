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

import java.util.Iterator;
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
import org.columba.mail.command.IMailFolderCommandReference;
import org.columba.mail.connector.FacadeUtil;
import org.columba.mail.connector.ServiceConnector;
import org.columba.mail.folder.IMailbox;
import org.columba.ristretto.message.Address;
import org.columba.ristretto.message.Header;
import org.columba.ristretto.parser.ParserException;

/**
 * Add sender of the selected messages to addressbook.
 * <p>
 * A dialog asks the user the destination addressbook.
 * 
 * @author fdietz
 */
public class AddSenderToAddressbookCommand extends Command {
	org.columba.addressbook.folder.IFolder selectedFolder;

	/**
	 * Constructor for AddSenderToAddressbookCommand.
	 * 
	 * @param references
	 */
	public AddSenderToAddressbookCommand(ICommandReference reference) {
		super(reference);
	}

	/**
	 * @see org.columba.api.command.Command#execute(org.columba.api.command.Worker)
	 */
	public void execute(IWorkerStatusController worker) throws Exception {
		// get reference
		IMailFolderCommandReference r = (IMailFolderCommandReference) getReference();

		// get array of message UIDs
		Object[] uids = r.getUids();

		// get source folder
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

		Vector<String> v = new Vector<String>();
		// for each message
		for (int i = 0; i < uids.length; i++) {
			// get header of message
			Header header = folder.getHeaderFields(uids[i],
					new String[] { "From" });

			// get sender
			String sender = (String) header.get("From");

			v.add(sender);
		}

		// add sender to addressbook
		Iterator<String> it = v.listIterator();
		while (it.hasNext()) {
			try {
				Address address = Address.parse(it.next());

				// add contact to addressbook
				IContactItem contactItem = modelFacade.createContactItem();
				FacadeUtil.getInstance().initContactItem(contactItem, address.getDisplayName(), address.getMailAddress());
				contactFacade.addContact(null);
			} catch (ParserException e) {
				e.printStackTrace();
			} catch (StoreException e) {
				e.printStackTrace();
			}
		}
	}

}
