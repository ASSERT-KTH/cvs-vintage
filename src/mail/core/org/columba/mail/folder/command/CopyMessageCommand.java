package org.columba.mail.folder.command;

import org.columba.core.command.Command;
import org.columba.core.command.DefaultCommandReference;
import org.columba.core.command.Worker;
import org.columba.core.gui.FrameController;
import org.columba.core.logging.ColumbaLogger;
import org.columba.mail.command.FolderCommand;
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

	protected Folder destFolder;
	
	/**
	 * Constructor for CopyMessageCommand.
	 * @param frameController
	 * @param references
	 */
	public CopyMessageCommand(
		FrameController frameController,
		DefaultCommandReference[] references) {
		super(frameController, references);
		
		commandType = Command.UNDOABLE_OPERATION;
	}

	/**
	 * @see org.columba.core.command.Command#updateGUI()
	 */
	public void updateGUI() throws Exception {
		MailFrameController frame = (MailFrameController)frameController;
		
		TableChangedEvent ev = new TableChangedEvent( TableChangedEvent.UPDATE, destFolder);
		 
		frame.tableController.tableChanged(ev);
	}

	/**
	 * @see org.columba.core.command.Command#execute(Worker)
	 */
	public void execute(Worker worker) throws Exception {
		MailFrameController mailFrameController =
			(MailFrameController) frameController;

		FolderCommandReference[] r = (FolderCommandReference[]) getReferences();

		Object[] uids = r[0].getUids();

		Folder folder = (Folder) r[0].getFolder();
		destFolder = (Folder) r[1].getFolder();

		Object[] destUids = new Object[uids.length];

		ColumbaLogger.log.debug("src=" + folder + " dest=" + destFolder);

		
		worker.setDisplayText("Copying messages to "+destFolder.getName()+"...");
		worker.setProgressBarMaximum(uids.length);
		
		for (int i = 0; i < uids.length; i++) {
			worker.setProgressBarValue(i);
			
			Object uid = uids[i];
			//ColumbaLogger.log.debug("copying UID=" + uid);

			String source = folder.getMessageSource(uid, worker);
			
			
			destUids[i] = destFolder.addMessage(source, worker);
		}

		r[1].setUids(destUids);
	}

	/**
	 * @see org.columba.core.command.Command#undo(Worker)
	 */
	public void undo(Worker worker) throws Exception {
		FolderCommandReference[] r = (FolderCommandReference[]) getReferences();

		Object[] uids = r[1].getUids();

		Folder folder = (Folder) r[1].getFolder();

		for (int i = 0; i < uids.length; i++) {
			Object uid = uids[i];
			ColumbaLogger.log.debug("undo_copying UID=" + uid);

			folder.removeMessage(uid);
		}
	}

	/**
	 * @see org.columba.core.command.Command#redo(Worker)
	 */
	public void redo(Worker worker) throws Exception {
		execute(worker);
	}

}
