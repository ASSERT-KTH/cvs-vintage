package org.columba.mail.folder.command;

import org.columba.core.command.DefaultCommandReference;
import org.columba.core.command.Worker;
import org.columba.core.gui.FrameController;
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
public class ExpungeFolderCommand extends FolderCommand {

	protected Folder srcFolder;
	/**
	 * Constructor for ExpungeFolderCommand.
	 * @param frameController
	 * @param references
	 */
	public ExpungeFolderCommand(
		FrameController frameController,
		DefaultCommandReference[] references) {
		super(frameController, references);
	}

	/**
	 * @see org.columba.core.command.Command#updateGUI()
	 */
	public void updateGUI() throws Exception {
		MailFrameController frame = (MailFrameController)frameController;
		
		TableChangedEvent ev = new TableChangedEvent( TableChangedEvent.UPDATE, srcFolder );
		 
		frame.tableController.tableChanged(ev);
		
		MainInterface.treeModel.nodeChanged(srcFolder);
		
	}

	/**
	 * @see org.columba.core.command.Command#execute(Worker)
	 */
	public void execute(Worker worker) throws Exception {
		FolderCommandReference[] r = (FolderCommandReference[]) getReferences();

		srcFolder = (Folder) r[0].getFolder();
		Object[] uids = srcFolder.getUids(worker);

		srcFolder.expungeFolder(uids, worker);
		
		
	}

}
