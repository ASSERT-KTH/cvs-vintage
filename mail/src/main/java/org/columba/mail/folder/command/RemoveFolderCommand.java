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

import org.columba.api.command.ICommandReference;
import org.columba.api.command.IWorkerStatusController;
import org.columba.core.command.Command;
import org.columba.core.command.Worker;
import org.columba.mail.command.IMailFolderCommandReference;
import org.columba.mail.folder.IMailbox;

/**
 * Delete this folder.
 * 
 * @author fdietz
 */
public class RemoveFolderCommand extends Command {

	/**
	 * Constructor for RemoveFolder.
	 * 
	 * @param references
	 *            command arguments.
	 */
	public RemoveFolderCommand(ICommandReference reference) {
		super(reference);

	}

	/**
	 * @see org.columba.api.command.Command#execute(Worker)
	 */
	public void execute(IWorkerStatusController worker) throws Exception {
		// get source folder
		IMailbox childFolder = (IMailbox) ((IMailFolderCommandReference) getReference())
				.getSourceFolder();

		// remove source folder
		childFolder.removeFolder();
	}
}