// The contents of this file are subject to the Mozilla Public License Version
//1.1
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
//Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.mail.folder.command;

import java.util.ArrayList;
import java.util.List;

import org.columba.api.command.ICommand;
import org.columba.api.command.ICommandReference;
import org.columba.api.command.IWorkerStatusController;
import org.columba.core.command.Command;
import org.columba.core.command.CommandProcessor;
import org.columba.core.command.StatusObservableImpl;
import org.columba.core.command.Worker;
import org.columba.mail.command.IMailFolderCommandReference;
import org.columba.mail.command.MailFolderCommandReference;
import org.columba.mail.config.AccountItem;
import org.columba.mail.folder.IMailFolder;
import org.columba.mail.folder.IMailbox;
import org.columba.mail.folder.RootFolder;
import org.columba.mail.gui.tree.FolderTreeModel;
import org.columba.mail.spam.command.CommandHelper;
import org.columba.mail.spam.command.LearnMessageAsHamCommand;
import org.columba.mail.spam.command.LearnMessageAsSpamCommand;
import org.columba.ristretto.message.Flags;

/**
 * Toggle flag.
 * <p>
 * Creates two sets of messages and uses {@link MarkMessageCommand}, which does
 * the flag change.
 * <p>
 * Additionally, if message is marked as spam or non-spam the bayesian filter is
 * trained.
 * 
 * @see MarkMessageCommand
 * @author fdietz
 */
public class ToggleMarkCommand extends Command {
	
	private static final java.util.logging.Logger LOG = 
        java.util.logging.Logger.getLogger("org.columba.mail.folder.command"); //$NON-NLS-1$

	private IWorkerStatusController worker;

	private List<MarkMessageCommand> commandList;

	/**
	 * Constructor for ToggleMarkCommand.
	 * 
	 * @param frameMediator
	 * @param references
	 */
	public ToggleMarkCommand(ICommandReference reference) {
		super(reference);

		commandList = new ArrayList<MarkMessageCommand>();
	}

	/**
	 * @see org.columba.api.command.Command#execute(Worker)
	 */
	public void execute(IWorkerStatusController worker) throws Exception {
		this.worker = worker;

		/*
		 * // use wrapper class for easier handling of references array adapter =
		 * new FolderCommandAdapter( (MailFolderCommandReference[])
		 * getReferences());
		 *  // get array of source references MailFolderCommandReference[] r =
		 * adapter.getSourceFolderReferences();
		 */
		IMailFolderCommandReference r = (IMailFolderCommandReference) getReference();

		// get array of message UIDs
		Object[] uids = r.getUids();

		// get source folder
		IMailbox srcFolder = (IMailbox) r.getSourceFolder();

		// register for status events
		((StatusObservableImpl) srcFolder.getObservable()).setWorker(worker);

		// which kind of mark?
		int markVariant = r.getMarkVariant();

		// create one list containing the marked items, which have to be "unmarked"
		// and another list containing the items remained to be marked
		List<Object> list1 = new ArrayList<Object>();
		List<Object> list2 = new ArrayList<Object>();

		for (int j = 0; j < uids.length; j++) {
			Flags flags = srcFolder.getFlags(uids[j]);

			boolean result = false;
			if (markVariant == MarkMessageCommand.MARK_AS_READ) {
				if (flags.getSeen())
					result = true;
			} else if (markVariant == MarkMessageCommand.MARK_AS_FLAGGED) {
				if (flags.getFlagged())
					result = true;
			} else if (markVariant == MarkMessageCommand.MARK_AS_EXPUNGED) {
				if (flags.getDeleted())
					result = true;
			} else if (markVariant == MarkMessageCommand.MARK_AS_ANSWERED) {
				if (flags.getAnswered())
					result = true;
			} else if (markVariant == MarkMessageCommand.MARK_AS_DRAFT) {
				if (flags.getDraft())
					result = true;
			} else if (markVariant == MarkMessageCommand.MARK_AS_SPAM) {
				boolean spam = ((Boolean) srcFolder.getAttribute(uids[j],
						"columba.spam")).booleanValue();
				if (spam)
					result = true;
			}

			if (result)
				list1.add(uids[j]);
			else
				list2.add(uids[j]);
		}

		MailFolderCommandReference ref = null;

		// un-mark messages
		if (list1.size() > 0) {
			ref = new MailFolderCommandReference(srcFolder, list1.toArray());
			ref.setMarkVariant(-markVariant);
			MarkMessageCommand c = new MarkMessageCommand(ref);
			commandList.add(c);
			c.execute(worker);

			// train bayesian filter
			if ((markVariant == MarkMessageCommand.MARK_AS_SPAM)
					|| (markVariant == MarkMessageCommand.MARK_AS_NOTSPAM)) {
				processSpamFilter(uids, srcFolder, -markVariant);
			}
		}

		// mark messages
		if (list2.size() > 0) {
			ref = new MailFolderCommandReference(srcFolder, list2.toArray());
			ref.setMarkVariant(markVariant);
			MarkMessageCommand c = new MarkMessageCommand(ref);
			commandList.add(c);
			c.execute(worker);

			// train bayesian filter
			if ((markVariant == MarkMessageCommand.MARK_AS_SPAM)
					|| (markVariant == MarkMessageCommand.MARK_AS_NOTSPAM)) {
				processSpamFilter(uids, srcFolder, markVariant);
			}
		}

	}

	/**
	 * Train spam filter.
	 * <p>
	 * Move message to specified folder or delete message immediately based on
	 * account configuration.
	 * 
	 * @param uids
	 *            message uid
	 * @param srcFolder
	 *            source folder
	 * @param markVariant
	 *            mark variant (spam/not spam)
	 * @throws Exception
	 */
	private void processSpamFilter(Object[] uids, IMailbox srcFolder,
			int markVariant) throws Exception {

		// update status message
		worker.setDisplayText("Training messages...");
		worker.setProgressBarMaximum(uids.length);

		// mark as spam /as not spam
		// for each message
		for (int j = 0; j < uids.length; j++) {

			worker.setDisplayText("Training messages...");
			worker.setProgressBarMaximum(uids.length);
			// increase progressbar value
			worker.setProgressBarValue(j);

			// cancel here if user requests
			if (worker.cancelled()) {
				break;
			}

			// message belongs to which account?
			AccountItem item = CommandHelper.retrieveAccountItem(srcFolder,
					uids[j]);
			// skip if account information is not available
			if (item == null)
				continue;

			// if spam filter is not enabled -> return
			if (item.getSpamItem().isEnabled() == false)
				continue;

			LOG.info("learning uid=" + uids[j]); //$NON-NLS-1$

			// create reference
			IMailFolderCommandReference ref = new MailFolderCommandReference(srcFolder,
					new Object[] { uids[j] });

			// create command
			ICommand c = null;
			if (markVariant == MarkMessageCommand.MARK_AS_SPAM)
				c = new LearnMessageAsSpamCommand(ref);
			else
				c = new LearnMessageAsHamCommand(ref);

			// execute command
			c.execute(worker);

			// skip if message is *not* marked as spam
			if (markVariant == MarkMessageCommand.MARK_AS_NOTSPAM)
				continue;

			// skip if user didn't enable this option
			if (item.getSpamItem().isMoveMessageWhenMarkingEnabled() == false)
				continue;

			if (item.getSpamItem().isMoveTrashSelected() == false) {
				// move message to user-configured folder (generally "Junk"
				// folder)
				IMailFolder destFolder = FolderTreeModel.getInstance()
						.getFolder(item.getSpamItem().getMoveCustomFolder());

				// create reference
				MailFolderCommandReference ref2 = new MailFolderCommandReference(
						srcFolder, destFolder, new Object[] { uids[j] });
				CommandProcessor.getInstance().addOp(new MoveMessageCommand(ref2));

			} else {
				// move message to trash
				IMailbox trash = (IMailbox) ((RootFolder) srcFolder
						.getRootFolder()).getTrashFolder();

				// create reference
				MailFolderCommandReference ref2 = new MailFolderCommandReference(
						srcFolder, trash, new Object[] { uids[j] });

				CommandProcessor.getInstance().addOp(new MoveMessageCommand(ref2));

			}

		}
	}
}