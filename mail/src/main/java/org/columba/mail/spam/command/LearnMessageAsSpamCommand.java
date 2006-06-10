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

package org.columba.mail.spam.command;

import org.columba.api.command.ICommandReference;
import org.columba.api.command.IWorkerStatusController;
import org.columba.core.command.Command;
import org.columba.core.command.StatusObservableImpl;
import org.columba.core.logging.Logging;
import org.columba.mail.command.IMailFolderCommandReference;
import org.columba.mail.folder.IMailbox;
import org.columba.mail.spam.SpamController;

/**
 * Learn selected messages as spam.
 * 
 * @author fdietz
 */
public class LearnMessageAsSpamCommand extends Command {

	/**
	 * @param references
	 */
	public LearnMessageAsSpamCommand(ICommandReference reference) {
		super(reference);
	}

	/**
	 * @see org.columba.api.command.Command#execute(org.columba.api.command.Worker)
	 */
	public void execute(IWorkerStatusController worker) throws Exception {

		// get array of source references
		IMailFolderCommandReference r = (IMailFolderCommandReference) getReference();

		// get array of message UIDs
		Object[] uids = r.getUids();

		// get source folder
		IMailbox srcFolder = (IMailbox) r.getSourceFolder();

		// register for status events
		((StatusObservableImpl) srcFolder.getObservable()).setWorker(worker);

		// update status message
		if (uids.length > 1) {
			// TODO (@author fdietz): i18n
			worker.setDisplayText("Training messages...");
			worker.setProgressBarMaximum(uids.length);
		}

		for (int j = 0; j < uids.length; j++) {
			if (worker.cancelled()) {
				break;
			}

			try {

				// train message as spam
				SpamController.getInstance().trainMessageAsSpam(srcFolder,
						uids[j]);

				if (uids.length > 1) {
					worker.setProgressBarValue(j);
				}
			} catch (Exception e) {
				if (Logging.DEBUG) {
					e.printStackTrace();
				}
			}
		}

	}
}