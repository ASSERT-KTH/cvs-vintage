package org.columba.mail.folder.command;

import org.columba.core.command.DefaultCommandReference;
import org.columba.core.command.Worker;
import org.columba.core.gui.FrameController;
import org.columba.core.logging.ColumbaLogger;
import org.columba.mail.command.FolderCommand;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.folder.Folder;
import org.columba.mail.gui.frame.MailFrameController;
import org.columba.mail.gui.table.TableChangedEvent;
import org.columba.main.MainInterface;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class MoveMessageCommand extends FolderCommand {

	protected Folder destFolder;
	protected Folder srcFolder;
	protected Object[] uids;
	
	
	/**
	 * Constructor for MoveMessageCommand.
	 * @param frameController
	 * @param references
	 */
	public MoveMessageCommand(
		FrameController frameController,
		DefaultCommandReference[] references) {
		super(frameController, references);
	}
	
	public void updateSelectedGUI() throws Exception {
		MailFrameController frame = (MailFrameController) frameController;
		
		TableChangedEvent ev = new TableChangedEvent( TableChangedEvent.UPDATE, destFolder);
		 
		frame.tableController.tableChanged(ev);
		
		TableChangedEvent ev2 = new TableChangedEvent( TableChangedEvent.UPDATE, srcFolder);
		 
		frame.tableController.tableChanged(ev2);
	}

	/**
	 * @see org.columba.core.command.Command#updateGUI()
	 */
	public void updateGUI() throws Exception {
		MailFrameController frame = (MailFrameController)frameController;
		
		
		
		MainInterface.treeModel.nodeChanged(destFolder);
		MainInterface.treeModel.nodeChanged(srcFolder);
	}

	/**
	 * @see org.columba.core.command.Command#execute(Worker)
	 */
	public void execute(Worker worker) throws Exception {
		MailFrameController mailFrameController =
			(MailFrameController) frameController;

		FolderCommandReference[] r = (FolderCommandReference[]) getReferences();

		uids = r[0].getUids();

		srcFolder = (Folder) r[0].getFolder();
		destFolder = (Folder) r[1].getFolder();

		ColumbaLogger.log.debug("src=" + srcFolder + " dest=" + destFolder);

		worker.setDisplayText("Moving messages to "+destFolder.getName()+"...");
		worker.setProgressBarMaximum(uids.length);
		
		for (int i = 0; i < uids.length; i++) {
			worker.setProgressBarValue(i);
			Object uid = uids[i];

			ColumbaLogger.log.debug("copying UID=" + uid);

			String source = srcFolder.getMessageSource(uid, worker);

			destFolder.addMessage(source, worker);

		}

		srcFolder.markMessage(uids, MarkMessageCommand.MARK_AS_EXPUNGED, worker);
		
		srcFolder.expungeFolder(worker);

	}

}
