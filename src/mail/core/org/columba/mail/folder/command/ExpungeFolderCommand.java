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

import org.columba.core.command.DefaultCommandReference;
import org.columba.core.command.StatusObservableImpl;
import org.columba.core.command.Worker;
import org.columba.core.command.WorkerStatusController;
import org.columba.mail.command.FolderCommand;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.folder.MessageFolder;

/**
 * Expunge folder.
 * <p>
 * Delete all messages from this folder, which are marked as expunged.
 * 
 * @author fdietz
 *  
 */
public class ExpungeFolderCommand extends FolderCommand {

	/**
	 * Constructor for ExpungeFolderCommand.
	 * 
	 * @param frameMediator
	 * @param reference
	 */
	public ExpungeFolderCommand(DefaultCommandReference reference) {
		super(reference);
	}

	/**
	 * @see org.columba.core.command.Command#execute(Worker)
	 */
	public void execute(WorkerStatusController worker) throws Exception {

		// get source references
		FolderCommandReference r = (FolderCommandReference) getReference();

		MessageFolder srcFolder = (MessageFolder) r.getFolder();

		// register for status events
		((StatusObservableImpl) srcFolder.getObservable()).setWorker(worker);

		// update status message
		worker.setDisplayText("Expunging " + srcFolder.getName() + "..");

		// expunge folder
		srcFolder.expungeFolder();

	}
}