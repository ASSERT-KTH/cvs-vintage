/*
 * Created on 24.03.2003
 *
 * To change this generated comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.folder.command;

import org.columba.core.command.DefaultCommandReference;
import org.columba.core.command.WorkerStatusController;
import org.columba.core.gui.frame.FrameMediator;
import org.columba.mail.command.FolderCommand;
import org.columba.mail.command.ImportFolderCommandReference;
import org.columba.mail.folder.mailboximport.AbstractMailboxImporter;

/**
 * Import messages to folder.
 * <p>
 * This command is used by the mail import wizard to import messages. All the
 * interesting work happens in {@link AbstractMailboxImporter}.
 * <p>
 * Note, that the import wizard needs a command to make sure that the folder is
 * locked.
 * 
 * @author fdietz
 */
public class ImportMessageCommand extends FolderCommand {
	/**
	 * @param references
	 */
	public ImportMessageCommand(DefaultCommandReference reference) {
		super(reference);
	}

	/**
	 * @param frame
	 * @param references
	 */
	public ImportMessageCommand(FrameMediator frame,
			DefaultCommandReference reference) {
		super(frame, reference);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.columba.core.command.Command#execute(org.columba.core.command.Worker)
	 */
	public void execute(WorkerStatusController worker) throws Exception {
		ImportFolderCommandReference r = (ImportFolderCommandReference) getReference();

		AbstractMailboxImporter importer = r.getImporter();

		importer.run(worker);
	}
}