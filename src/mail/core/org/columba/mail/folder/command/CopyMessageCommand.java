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
import org.columba.core.logging.ColumbaLogger;
import org.columba.mail.command.FolderCommand;
import org.columba.mail.command.FolderCommandAdapter;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.folder.Folder;
import org.columba.mail.gui.frame.TableUpdater;
import org.columba.mail.gui.table.model.TableModelChangedEvent;
import org.columba.mail.main.MailInterface;

/**
 * Copy a set of messages from a source to a destination
 * folder.
 * <p>
 * A dialog asks the user the destination folder.
 * 
 * @author fdietz
 * 
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

		// notify table of changes
		TableModelChangedEvent ev =
			new TableModelChangedEvent(TableModelChangedEvent.UPDATE, destFolder);

		TableUpdater.tableChanged(ev);

		// notify treemodel
		MailInterface.treeModel.nodeChanged(destFolder);
	}

	/**
	 * Copying messages between folders which are of the same mailbox
	 * format, and exist on the same IMAP server btw. have the same
	 * parent root node.
	 * 
	 * @param srcFolder			source folder
	 * @param destFolder		destination folder
	 * @param uids				arry of message UIDs
	 * @param worker			worker thread
	 * @throws Exception
	 */
	protected void innerCopy(
		Folder srcFolder,
		Folder destFolder,
		Object[] uids,
		Worker worker)
		throws Exception {

		srcFolder.innerCopy(destFolder, uids);

	}

	/**
	 * Simple fall-back copy method, which first copies the message
	 * to the folder, using the raw message source stream.
	 * 
	 * @param srcFolder			source folder
	 * @param destFolder		destination folder
	 * @param uids				array of message UIDs
	 * @param worker			worker thread
	 * @throws Exception
	 */
	protected void defaultCopy(
		Folder srcFolder,
		Folder destFolder,
		Object[] uids,
		Worker worker)
		throws Exception {

		// what does thi method?
        if(destUids == null){
          destUids = new Object[uids.length];
        }
        
        // for each message
		for (int i = 0; i < uids.length; i++) {

			Object uid = uids[i];
			//ColumbaLogger.log.debug("copying UID=" + uid);

			// if message exists in sourcefolder
			if (srcFolder.exists(uid)) {
				// get message source
				String source = srcFolder.getMessageSource(uid);
				// add source to destination folder
                destUids[i] = destFolder.addMessage(source);
			}

			// update progress bar
			worker.setProgressBarValue(i);
		}
		
		// we are done - clear the progress bar
		worker.resetProgressBar();

	}

	/**
	 * @see org.columba.core.command.Command#execute(Worker)
	 */
	public void execute(Worker worker) throws Exception {
		
		// get references
		FolderCommandReference[] references =
			(FolderCommandReference[]) getReferences();
		
		// use wrapper class
		adapter = new FolderCommandAdapter(references);

		// get source references
		FolderCommandReference[] r = adapter.getSourceFolderReferences();
		
		// get destination foldedr
		destFolder = adapter.getDestinationFolder();

		// for each message
		for (int i = 0; i < r.length; i++) {

			Object[] uids = r[i].getUids();

			// get source folder
			Folder srcFolder = (Folder) r[i].getFolder();
			
			// register for status events
			((StatusObservableImpl)srcFolder.getObservable()).setWorker(worker);
				 
			// setting lastSelection for srcFolder to null
			srcFolder.setLastSelection(null);

			ColumbaLogger.log.debug("src=" + srcFolder + " dest=" + destFolder);

			// update status message
			worker.setDisplayText(
				"Copying messages to " + destFolder.getName() + "...");
			
			// initialize progress bar with total number of messages
			worker.setProgressBarMaximum(uids.length);

			// compare source- and dest-folder
			if (srcFolder.getRootFolder().equals(destFolder.getRootFolder())) {
				// source- and dest-folder match
				//  -> use optimized copy operation

				innerCopy(srcFolder, destFolder, uids, worker);
			} else {
				// no match
				//  -> for example: copying from imap-server to local-folder
				defaultCopy(srcFolder, destFolder, uids, worker);
			}
		}
		
		// We are done - clear the status message (with a delay of 500 ms)
		worker.clearDisplayText(500);

	}

}
