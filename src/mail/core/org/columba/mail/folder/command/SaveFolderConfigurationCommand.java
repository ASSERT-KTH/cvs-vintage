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

import org.columba.core.command.DefaultCommandReference;
import org.columba.core.command.StatusObservableImpl;
import org.columba.core.command.Worker;
import org.columba.mail.command.FolderCommand;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.folder.Folder;
import org.columba.mail.folder.FolderTreeNode;

/**
 * Save folder configuration including MessageFolderInfo and
 * headercache to disk.
 * 
 * @author fdietz
 */
public class SaveFolderConfigurationCommand extends FolderCommand {

	/**
	 * @param references
	 */
	public SaveFolderConfigurationCommand(DefaultCommandReference[] references) {
		super(references);

	}

	/* (non-Javadoc)
	 * @see org.columba.core.command.Command#execute(org.columba.core.command.Worker)
	 */
	public void execute(Worker worker) throws Exception {
		// skip if no reference available
		if ( getReferences().length == 0 ) return;
		
		if ( getReferences()[0] == null ) return;
		
		FolderTreeNode folderTreeNode =
			(FolderTreeNode) ((FolderCommandReference) getReferences()[0])
				.getFolder();

		// if folder is message folder
		// ->TODO: there should be an interface, instead of the Folder class
		if (folderTreeNode instanceof Folder) {
			Folder folder = (Folder) folderTreeNode;

			// register for status events
			 ((StatusObservableImpl) folder.getObservable()).setWorker(worker);

			// save headercache
			folder.save();
		}	

	}


}
