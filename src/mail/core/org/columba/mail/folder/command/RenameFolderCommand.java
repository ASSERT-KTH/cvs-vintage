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
import org.columba.core.command.StatusObservableImpl;
import org.columba.core.command.Worker;
import org.columba.core.main.MainInterface;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.folder.Folder;
import org.columba.mail.folder.FolderTreeNode;

/**
 * Rename selected folder.
 * 
 * @author fdietz
 */
public class RenameFolderCommand extends Command {

	private FolderTreeNode parentFolder;

	/**
	 * Constructor for RenameFolderCommand.
	 * @param frameController
	 * @param references
	 */
	public RenameFolderCommand(DefaultCommandReference[] references) {
		super(references);
	}

	/**
	 * @see org.columba.core.command.Command#updateGUI()
	 */
	public void updateGUI() throws Exception {
		MainInterface.treeModel.nodeStructureChanged(parentFolder);
	}

	/**
	 * @see org.columba.core.command.Command#execute(Worker)
	 */
	public void execute(Worker worker) throws Exception {
		parentFolder =
			((FolderCommandReference) getReferences()[0]).getFolder();


		String name =
			((FolderCommandReference) getReferences()[0]).getFolderName();

		((Folder) parentFolder).renameFolder(name);
	}

}
