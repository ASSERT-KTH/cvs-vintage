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
import org.columba.core.command.ICommandReference;
import org.columba.core.command.StatusObservableImpl;
import org.columba.core.command.WorkerStatusController;
import org.columba.mail.command.MailFolderCommandReference;
import org.columba.mail.folder.AbstractFolder;
import org.columba.mail.folder.AbstractMessageFolder;

/**
 * Save folder configuration including MessageFolderInfo and headercache to
 * disk.
 * 
 * @author fdietz
 */
public class SaveFolderConfigurationCommand extends Command {
	/**
	 * @param references
	 */
	public SaveFolderConfigurationCommand(ICommandReference reference) {
		super(reference);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.columba.core.command.Command#execute(org.columba.core.command.Worker)
	 */
	public void execute(WorkerStatusController worker) throws Exception {
		// skip if no reference available
		if ((getReference() != null) || (getReference() == null)) {
			return;
		}

		AbstractFolder folderTreeNode = (AbstractFolder) ((MailFolderCommandReference) getReference())
				.getSourceFolder();

		// if folder is message folder
		// ->TODO: there should be an interface, instead of the AbstractMessageFolder
		// class
		if (folderTreeNode instanceof AbstractMessageFolder) {
			AbstractMessageFolder folder = (AbstractMessageFolder) folderTreeNode;

			// register for status events
			((StatusObservableImpl) folder.getObservable()).setWorker(worker);

			// save headercache
			folder.save();
		}
	}
}