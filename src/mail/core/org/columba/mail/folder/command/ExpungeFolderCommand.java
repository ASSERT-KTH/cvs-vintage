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
import org.columba.core.main.MainInterface;
import org.columba.mail.command.FolderCommand;
import org.columba.mail.command.FolderCommandAdapter;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.folder.Folder;
import org.columba.mail.gui.frame.TableUpdater;
import org.columba.mail.gui.table.model.TableModelChangedEvent;

/**
 * Expunge folder.
 * <p>
 * Delete all messages from this folder, which are marked as expunged.
 * 
 * @author fdietz
 * 
 */
public class ExpungeFolderCommand extends FolderCommand {

	protected FolderCommandAdapter adapter;
	/**
	 * Constructor for ExpungeFolderCommand.
	 * @param frameController
	 * @param references
	 */
	public ExpungeFolderCommand(DefaultCommandReference[] references) {
		super(references);
	}

	/**
	 * @see org.columba.core.command.Command#updateGUI()
	 */
	public void updateGUI() throws Exception {

		FolderCommandReference[] r = adapter.getSourceFolderReferences();

		TableModelChangedEvent ev;
		for (int i = 0; i < r.length; i++) {

			ev = new TableModelChangedEvent( TableModelChangedEvent.UPDATE );
			/*
			ev =
				new TableChangedEvent(
					TableChangedEvent.REMOVE,
					r[i].getFolder(),
					r[i].getUids());
			*/
			
			TableUpdater.tableChanged(ev);

			MainInterface.treeModel.nodeChanged(r[i].getFolder());
		}

		FolderCommandReference u = adapter.getUpdateReferences();
		if (u != null) {
			ev = new TableModelChangedEvent( TableModelChangedEvent.UPDATE );
			/*
			ev =
				new TableChangedEvent(
					TableChangedEvent.REMOVE,
					u.getFolder(),
					u.getUids());
			*/
			TableUpdater.tableChanged(ev);

			MainInterface.treeModel.nodeChanged(u.getFolder());
		}
	}

	/**
	 * @see org.columba.core.command.Command#execute(Worker)
	 */
	public void execute(Worker worker) throws Exception {

		adapter =
			new FolderCommandAdapter(
				(FolderCommandReference[]) getReferences());

		FolderCommandReference[] r = adapter.getSourceFolderReferences();

		for (int i = 0; i < r.length; i++) {

			Folder srcFolder = (Folder) r[i].getFolder();
//			register for status events
		 ((StatusObservableImpl)srcFolder.getObservable()).setWorker(worker);
		 
			worker.setDisplayText("Expunging "+srcFolder.getName()+"..");
			
			srcFolder.expungeFolder();

		}

	}

}
