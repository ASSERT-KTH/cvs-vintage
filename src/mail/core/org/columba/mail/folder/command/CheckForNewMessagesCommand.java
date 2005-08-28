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
package org.columba.mail.folder.command;

import java.awt.Toolkit;
import java.io.IOException;

import javax.swing.Action;

import org.columba.api.command.ICommandReference;
import org.columba.api.command.IWorkerStatusController;
import org.columba.core.command.Command;
import org.columba.core.command.CommandCancelledException;
import org.columba.core.command.StatusObservableImpl;
import org.columba.mail.command.IMailFolderCommandReference;
import org.columba.mail.config.ImapItem;
import org.columba.mail.folder.imap.IMAPFolder;
import org.columba.mail.folder.imap.IMAPRootFolder;

/**
 * Check for new messages in IMAPFolder.
 * 
 * 
 * @author fdietz
 */
public class CheckForNewMessagesCommand extends Command {

	IMAPFolder inboxFolder;

	private Action action;

	public CheckForNewMessagesCommand(Action action, ICommandReference reference) {
		super(reference);
		this.action = action;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.columba.api.command.Command#execute(org.columba.api.command.Worker)
	 */
	public void execute(IWorkerStatusController worker) throws Exception {
		// get references
		IMailFolderCommandReference r = (IMailFolderCommandReference) getReference();

		inboxFolder = (IMAPFolder) r.getSourceFolder();
		
		// register for status events
		((StatusObservableImpl) inboxFolder.getObservable()).setWorker(worker);


		// Find old numbers
		int total = inboxFolder.getMessageFolderInfo().getExists();
		int recent = inboxFolder.getMessageFolderInfo().getRecent();
		int unseen = inboxFolder.getMessageFolderInfo().getUnseen();

		// check for new headers
		try {
			inboxFolder.synchronizeHeaderlist();
		} catch (IOException e) {
			worker.cancel();
			throw new CommandCancelledException(e);
		} 

		// Get the new numbers
		int newTotal = inboxFolder.getMessageFolderInfo().getExists();
		int newRecent = inboxFolder.getMessageFolderInfo().getRecent();
		int newUnseen = inboxFolder.getMessageFolderInfo().getUnseen();

		// ALP 04/29/03
		// Call updageGUI() if anything has changed
		if ((newRecent != recent) || (newTotal != total)
				|| (newUnseen != unseen)) {

			// TODO :(tstich) Renable sound
			/*
			if ((newTotal != total) && (item.getBoolean("enable_sound"))) {
				// the number of "recent" messages has changed, so play a sound
				// of told to for new messages on server
				//	re-enable this feature later, make it a general option
				// not a per-account based one
				// -> playing wav-files should be only optional
				// just play a system beep
				// -> this works better for most people
				// -> java doesn't support sound servers like
				// -> alsa or esound anyway
				Toolkit kit = Toolkit.getDefaultToolkit();
				kit.beep(); //system beep

			}*/

			//  END if((newRecent != recent) && (item.getBoolean...
		}

		//  END if (newRecent != recent || newTotal != total ...
	}

	//  END public void execute(Worker worker) throws Exception

	/**
	 * @see org.columba.api.command.Command#updateGUI()
	 */
	public void updateGUI() throws Exception {
		// Reenable the action
		if( action != null) action.setEnabled(true);
	}
}