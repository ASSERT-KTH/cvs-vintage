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

import java.util.logging.Logger;

import org.columba.core.command.DefaultCommandReference;
import org.columba.core.command.StatusObservableImpl;
import org.columba.core.command.Worker;
import org.columba.core.command.WorkerStatusController;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.folder.MessageFolder;

/**
 * Move selected messages from source to destination folder.
 * <p>
 * A dialog asks the user the destination folder to use.
 * 
 * @author fdietz
 */
public class MoveMessageCommand extends CopyMessageCommand {

	/** JDK 1.4+ logging framework logger, used for logging. */
	private static final Logger LOG = Logger
			.getLogger("org.columba.mail.folder.command");

	/**
	 * Constructor for MoveMessageCommand.
	 * 
	 * @param frameMediator
	 * @param references
	 */
	public MoveMessageCommand(DefaultCommandReference reference) {
		super(reference);
	}

	/**
	 * @see org.columba.core.command.Command#execute(Worker)
	 */
	public void execute(WorkerStatusController worker) throws Exception {

		// get references
		r = (FolderCommandReference) getReference();

		// get source folder
		MessageFolder srcFolder = (MessageFolder) r.getFolder();

		// get destination foldedr
		destFolder = (MessageFolder) r.getDestinationFolder();

		// cancel, if source equals destination folder
		if ( srcFolder.getUid() == destFolder.getUid() ) return;
		
		// calling CopyMessageCommand.execute() here!
		//super.execute(worker);
		doExecute(worker, "move_messages", "err_copy_messages_retry",
				"err_copy_messages_ignore", "err_move_messages_msg",
				"err_move_messages_title", "move_messages_cancelled");

		// get messgae UIDs
		Object[] uids = r.getUids();

		// register for status events
		((StatusObservableImpl) srcFolder.getObservable()).setWorker(worker);

		// setting lastSelection to null
		srcFolder.setLastSelection(null);

		LOG.info("src=" + srcFolder + " dest=" + destFolder);

		// update status message
		worker.setDisplayText("Moving messages to " + destFolder.getName()
				+ "...");
		worker.setProgressBarMaximum(uids.length);

		// mark all messages as expunged
		srcFolder.markMessage(uids, MarkMessageCommand.MARK_AS_EXPUNGED);

		// expunge folder
		srcFolder.expungeFolder();

		// We are done - clear the status message after a delay
		worker.clearDisplayTextWithDelay();

	}
}