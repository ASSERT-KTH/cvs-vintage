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
package org.columba.mail.gui.tree.command;

import java.util.Hashtable;

import org.columba.core.command.Command;
import org.columba.core.command.DefaultCommandReference;
import org.columba.core.command.Worker;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.folder.FolderTreeNode;
import org.columba.core.main.MainInterface;

/**
 * @author Timo Stich (tstich@users.sourceforge.net)
 * 
 */
public class CreateSubFolderCommand extends Command {

	private FolderTreeNode parentFolder;
	private boolean success;
	private Hashtable attributes;

	/**
	 * Constructor for CreateSubFolderCommand.
	 * @param references
	 */
	public CreateSubFolderCommand(DefaultCommandReference[] references) {
		super(references);

		priority = Command.REALTIME_PRIORITY;
		commandType = Command.UNDOABLE_OPERATION;

		success = true;
	}

	/**
	 * @see org.columba.core.command.Command#updateGUI()
	 */
	public void updateGUI() throws Exception {
		// if creating of folder failed -> exit
		if (success == false)
			return;

		// else update TreeModel
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

		/*
		attributes = parentFolder.getAttributes();
		attributes.put("name", name);
		*/
		try {
			parentFolder.addFolder(name);
		} catch (Exception ex) {
			success = false;
			throw ex;
		}
	}

	/**
	 * @see org.columba.core.command.Command#undo(Worker)
	 */
	public void undo(Worker worker) throws Exception {
	}

	/**
	 * @see org.columba.core.command.Command#undo(Worker)
	 */
	public void redo(Worker worker) throws Exception {
	}

}
