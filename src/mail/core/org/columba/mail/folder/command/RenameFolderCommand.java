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

import org.columba.core.command.Command;
import org.columba.core.command.DefaultCommandReference;
import org.columba.core.command.Worker;
import org.columba.core.command.WorkerStatusController;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.folder.AbstractFolder;
import org.columba.mail.folder.MessageFolder;

/**
 * Rename selected folder.
 * 
 * @author fdietz
 */
public class RenameFolderCommand extends Command {
	private AbstractFolder selectedFolder;

	/**
	 * Constructor for RenameFolderCommand.
	 * 
	 * @param references
	 *            command arguments.
	 */
	public RenameFolderCommand(DefaultCommandReference reference) {
		super(reference);
	}

	/**
	 * @see org.columba.core.command.Command#updateGUI()
	 */
	/*
	 * public void updateGUI() throws Exception { // update treemodel
	 * MailInterface.treeModel.nodeChanged(selectedFolder); }
	 */

	/**
	 * @see org.columba.core.command.Command#execute(Worker)
	 */
	public void execute(WorkerStatusController worker) throws Exception {
		// get source folder
		selectedFolder = ((FolderCommandReference) getReference()).getFolder();

		// get name of folder
		String name = ((FolderCommandReference) getReference()).getFolderName();

		// rename folder
		((MessageFolder) selectedFolder).setName(name);
	}
}