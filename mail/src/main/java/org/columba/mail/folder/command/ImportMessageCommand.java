/*
 * Created on 24.03.2003
 *
 * To change this generated comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.folder.command;

import org.columba.api.command.ICommandReference;
import org.columba.api.command.IWorkerStatusController;
import org.columba.core.command.Command;
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
public class ImportMessageCommand extends Command {
	/**
	 * @param references
	 */
	public ImportMessageCommand(ICommandReference reference) {
		super(reference);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.columba.api.command.Command#execute(org.columba.api.command.Worker)
	 */
	public void execute(IWorkerStatusController worker) throws Exception {
		ImportFolderCommandReference r = (ImportFolderCommandReference) getReference();

		AbstractMailboxImporter importer = r.getImporter();

		importer.run(worker);
	}
}