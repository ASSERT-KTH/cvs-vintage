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
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.

package org.columba.mail.spam.command;

import java.io.IOException;
import java.util.ArrayList;

import org.columba.api.command.ICommandReference;
import org.columba.api.command.IWorkerStatusController;
import org.columba.core.command.Command;
import org.columba.core.command.StatusObservableImpl;
import org.columba.core.logging.Logging;
import org.columba.mail.command.IMailFolderCommandReference;
import org.columba.mail.command.MailFolderCommandReference;
import org.columba.mail.config.AccountItem;
import org.columba.mail.filter.plugins.AddressbookFilter;
import org.columba.mail.folder.IMailbox;
import org.columba.mail.folder.command.MarkMessageCommand;
import org.columba.mail.spam.SpamController;

/**
 * Score selected messages as spam, meaning calculate the likelyhood that the
 * message is spam.
 * 
 * @author fdietz
 */
public class ScoreMessageCommand extends Command {

	private Object[] uids;

	private IMailbox srcFolder;

	private MarkMessageCommand markAsSpamCommand;

	private MarkMessageCommand markAsNotSpamCommand;

	/**
	 * @param references
	 */
	public ScoreMessageCommand(ICommandReference reference) {
		super(reference);
	}

	public void updateGUI() throws Exception {
		// update table
		if (markAsSpamCommand != null) {
			markAsSpamCommand.updateGUI();
		}
		if (markAsNotSpamCommand != null) {
			markAsNotSpamCommand.updateGUI();
		}
	}

	/**
	 * @see org.columba.api.command.Command#execute(org.columba.api.command.Worker)
	 */
	public void execute(IWorkerStatusController worker) throws Exception {
		// get source reference
		IMailFolderCommandReference r = (IMailFolderCommandReference) getReference();

		// get array of message UIDs
		uids = r.getUids();

		// get source folder
		srcFolder = (IMailbox) r.getSourceFolder();

		// register for status events
		((StatusObservableImpl) srcFolder.getObservable()).setWorker(worker);

		// update status message
		// TODO (@author fdietz): i18n
		worker.setDisplayText("Scoring messages ...");
		worker.setProgressBarMaximum(uids.length);

		ArrayList spamList = new ArrayList();
		ArrayList nonspamList = new ArrayList();

		for (int j = 0; j < uids.length; j++) {
			if (worker.cancelled()) {
				return;
			}

			try {

				// score message
				boolean result = scoreMessage(j);

				// if message is spam
				if (result) {
					// mark message as spam
					spamList.add(uids[j]);
				} else {
					// mark message as *not* spam
					nonspamList.add(uids[j]);
				}

				// train message as spam or non spam
				trainMessage(j, result);

				worker.setProgressBarValue(j);

				if (worker.cancelled()) {
					break;
				}
			} catch (Exception e) {
				if (Logging.DEBUG) {
					e.printStackTrace();
				}
			}
		}

		// mark spam messages
		if (spamList.size() != 0) {
			MailFolderCommandReference ref = new MailFolderCommandReference(
					srcFolder, spamList.toArray());
			ref.setMarkVariant(MarkMessageCommand.MARK_AS_SPAM);
			markAsSpamCommand = new MarkMessageCommand(ref);
			markAsSpamCommand.execute(worker);
		}

		// mark non spam messages
		if (nonspamList.size() != 0) {
			MailFolderCommandReference ref = new MailFolderCommandReference(
					srcFolder, nonspamList.toArray());
			ref.setMarkVariant(MarkMessageCommand.MARK_AS_NOTSPAM);
			markAsNotSpamCommand = new MarkMessageCommand(ref);
			markAsNotSpamCommand.execute(worker);
		}

	}

	/**
	 * Score message, meaning decide if message is spam or non spam.
	 * 
	 * @param j
	 *            message UID index
	 * @return true, if spam. False, otherwise.
	 * @throws Exception
	 * @throws IOException
	 */
	private boolean scoreMessage(int j) throws Exception, IOException {

		// calculate message score
		boolean result = SpamController.getInstance().scoreMessage(srcFolder,
				uids[j]);

		// message belongs to which account?
		AccountItem item = CommandHelper
				.retrieveAccountItem(srcFolder, uids[j]);

		if (item.getSpamItem().checkAddressbook()) {
			// check if sender is already in addressbook
			boolean isInAddressbook = new AddressbookFilter().process(
					srcFolder, uids[j]);
			result = result && !isInAddressbook;
		}

		return result;
	}

	/**
	 * Train selected message as spam or non spam.
	 * 
	 * @param j
	 *            UID index
	 * @param result
	 *            true, if spam. False, otherwise.
	 * @throws Exception
	 */
	private void trainMessage(int j, boolean result) throws Exception {

		// add this message to frequency database
		if (result) {
			SpamController.getInstance().trainMessageAsSpam(srcFolder, uids[j]);
		} else {
			SpamController.getInstance().trainMessageAsHam(srcFolder, uids[j]);
		}

	}
}