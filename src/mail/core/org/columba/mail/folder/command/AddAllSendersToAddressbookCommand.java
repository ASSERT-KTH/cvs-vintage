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

import org.columba.addressbook.facade.IContactFacade;
import org.columba.addressbook.facade.IDialogFacade;
import org.columba.addressbook.gui.tree.util.ISelectFolderDialog;
import org.columba.api.command.ICommandReference;
import org.columba.api.command.IWorkerStatusController;
import org.columba.api.exception.ServiceNotFoundException;
import org.columba.core.command.Command;
import org.columba.core.command.StatusObservableImpl;
import org.columba.core.folder.IFolderCommandReference;
import org.columba.mail.connector.ServiceConnector;
import org.columba.mail.folder.AbstractMessageFolder;
import org.columba.mail.folder.IMailbox;
import org.columba.ristretto.message.Header;

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

		IDialogFacade dialogFacade = null;
		try {
			dialogFacade = ServiceConnector.getDialogFacade();
		} catch (ServiceNotFoundException e) {
			e.printStackTrace();
			return;
		}
		// ask the user which addressbook he wants to save this address to
		ISelectFolderDialog dialog = dialogFacade.getSelectFolderDialog();

		selectedFolder = dialog.getSelectedFolder();

		if (selectedFolder == null) {
			return;
		}

		IContactFacade contactFacade = null;
		try {
			contactFacade = ServiceConnector.getContactFacade();
		} catch (ServiceNotFoundException e) {
			e.printStackTrace();
			return;
		}

		// for every message
		for (int i = 0; i < uids.length; i++) {
			// get header of message
			Header header = folder.getHeaderFields(uids[i], new String[] {
					"From", "Cc", "Bcc" });

			String sender = (String) header.get("From");

			// add sender to addressbook
			contactFacade.addContact(selectedFolder.getUid(), sender);

			sender = (String) header.get("Cc");

			contactFacade.addContact(selectedFolder.getUid(), sender);

			sender = (String) header.get("Bcc");

			contactFacade.addContact(selectedFolder.getUid(), sender);
		}
	}

}