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
import org.columba.core.logging.ColumbaLogger;
import org.columba.core.main.MainInterface;
import org.columba.mail.command.FolderCommand;
import org.columba.mail.command.FolderCommandAdapter;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.folder.Folder;
import org.columba.mail.gui.frame.MailFrameController;
import org.columba.mail.gui.table.TableChangedEvent;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class CopyMessageCommand extends FolderCommand {

	protected FolderCommandAdapter adapter;
	protected Folder destFolder;
	protected Object[] destUids;

	/**
	 * Constructor for CopyMessageCommand.
	 * @param frameController
	 * @param references
	 */
	public CopyMessageCommand(DefaultCommandReference[] references) {
		super(references);

		commandType = Command.UNDOABLE_OPERATION;
	}

	public void updateGUI() throws Exception {

		TableChangedEvent ev =
			new TableChangedEvent(TableChangedEvent.UPDATE, destFolder);

		MailFrameController.tableChanged(ev);

		MainInterface.treeModel.nodeChanged(destFolder);
	}

	protected void innerCopy(
		Folder srcFolder,
		Folder destFolder,
		Object[] uids,
		Worker worker)
		throws Exception {

		srcFolder.innerCopy(destFolder, uids, worker);

	}

	protected void defaultCopy(
		Folder srcFolder,
		Folder destFolder,
		Object[] uids,
		Worker worker)
		throws Exception {

        if(destUids == null){
          destUids = new Object[uids.length];
        }
		for (int i = 0; i < uids.length; i++) {

			Object uid = uids[i];
			//ColumbaLogger.log.debug("copying UID=" + uid);

			if (srcFolder.exists(uid, worker)) {
				String source = srcFolder.getMessageSource(uid, worker);
                destUids[i] = destFolder.addMessage(source, worker);
			}

			worker.setProgressBarValue(i);
		}

	}

	/**
	 * @see org.columba.core.command.Command#execute(Worker)
	 */
	public void execute(Worker worker) throws Exception {

		FolderCommandReference[] references =
			(FolderCommandReference[]) getReferences();
		adapter = new FolderCommandAdapter(references);

		FolderCommandReference[] r = adapter.getSourceFolderReferences();
		destFolder = adapter.getDestinationFolder();

		for (int i = 0; i < r.length; i++) {

			Object[] uids = r[i].getUids();

			Folder srcFolder = (Folder) r[i].getFolder();

			ColumbaLogger.log.debug("src=" + srcFolder + " dest=" + destFolder);

			worker.setDisplayText(
				"Copying messages to " + destFolder.getName() + "...");
			worker.setProgressBarMaximum(uids.length);

			// compare source- and dest-folder
			if (srcFolder.getRootFolder().equals(destFolder.getRootFolder())) {
				// source- and dest-folder match
				//  -> user optimized copy operation

				innerCopy(srcFolder, destFolder, uids, worker);
			} else {
				// no match
				//  -> for example: copying from imap-server to local-folder
				defaultCopy(srcFolder, destFolder, uids, worker);
			}
		}
	}

	/**
	 * @see org.columba.core.command.Command#undo(Worker)
	 */
	public void undo(Worker worker) throws Exception {
		/*
		FolderCommandReference[] r = (FolderCommandReference[]) getReferences();

		Object[] uids = r[1].getUids();

		Folder srcFolder = (Folder) r[1].getFolder();

		for (int i = 0; i < uids.length; i++) {
			Object uid = uids[i];
			ColumbaLogger.log.debug("undo_copying UID=" + uid);

			srcFolder.removeMessage(uid, worker);
		}
		*/
	}

	/**
	 * @see org.columba.core.command.Command#redo(Worker)
	 */
	public void redo(Worker worker) throws Exception {
		execute(worker);
	}

}
