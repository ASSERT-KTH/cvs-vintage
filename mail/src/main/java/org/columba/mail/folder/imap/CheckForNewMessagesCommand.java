//The contents of this file are subject to the Mozilla Public License Version 1.1
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
//The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003. 
//
//All Rights Reserved.
package org.columba.mail.folder.imap;

import java.io.IOException;

import javax.swing.Action;

import org.columba.api.command.ICommandReference;
import org.columba.api.command.IWorkerStatusController;
import org.columba.core.command.Command;
import org.columba.core.command.CommandCancelledException;
import org.columba.core.command.StatusObservableImpl;
import org.columba.mail.command.IMailFolderCommandReference;
import org.columba.mail.command.MailFolderCommandReference;
import org.columba.mail.mailchecking.MailCheckingManager;

/**
 * Check for new messages in IMAPFolder.
 * 
 * 
 * @author fdietz
 */
public class CheckForNewMessagesCommand extends Command {

	IMAPFolder imapFolder;

	private Action action;
	private boolean triggerNotification;
	
	
	public CheckForNewMessagesCommand(ICommandReference reference) {
		super(reference);
		triggerNotification = false;

		// get references
		IMailFolderCommandReference r = (IMailFolderCommandReference) getReference();

		imapFolder = (IMAPFolder) r.getSourceFolder();

		imapFolder.setMailboxSyncEnabled(false);
		
	}

	public CheckForNewMessagesCommand(Action action, ICommandReference reference) {
		super(reference);
		this.action = action;
		triggerNotification = true;
		
		// get references
		IMailFolderCommandReference r = (IMailFolderCommandReference) getReference();

		imapFolder = (IMAPFolder) r.getSourceFolder();
		
		imapFolder.setMailboxSyncEnabled(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.columba.api.command.Command#execute(org.columba.api.command.Worker)
	 */
	public void execute(IWorkerStatusController worker) throws Exception {
		
		// register for status events
		((StatusObservableImpl) imapFolder.getObservable()).setWorker(worker);


		// Find old numbers
		int total = imapFolder.getMessageFolderInfo().getExists();

		// check for new headers
		Object[] uids = new Object[0];
		try {
			uids = imapFolder.synchronizeHeaderlist();
		} catch (IOException e) {
			imapFolder.setMailboxSyncEnabled(true);
			worker.cancel();
			throw new CommandCancelledException(e);
		} 

		// Get the new numbers
		int newTotal = imapFolder.getMessageFolderInfo().getExists();
		
		// fire new message event to interested listeners
		if (triggerNotification && (newTotal != total) ) {
			if( ((IMAPRootFolder)imapFolder.getRootFolder()).getAccountItem().getImapItem().getBoolean("enable_sound")) {
				// create reference of newly arrived messages
				IMailFolderCommandReference ref = new MailFolderCommandReference(imapFolder, uids);
				// fire event
				MailCheckingManager.getInstance().fireNewMessageArrived(ref);
				
			}
		}
	}

	/**
	 * @see org.columba.api.command.Command#updateGUI()
	 */
	public void updateGUI() throws Exception {
		// Reenable the action
		if( action != null) action.setEnabled(true);
	}
}