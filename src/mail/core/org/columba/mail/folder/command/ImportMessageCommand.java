/*
 * Created on 24.03.2003
 *
 * To change this generated comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.folder.command;

import org.columba.core.command.DefaultCommandReference;
import org.columba.core.command.Worker;
import org.columba.core.command.WorkerStatusController;
import org.columba.core.gui.frame.FrameMediator;

import org.columba.mail.command.FolderCommand;
import org.columba.mail.command.ImportFolderCommandReference;
import org.columba.mail.folder.mailboximport.DefaultMailboxImporter;


/**
 * Import messages to folder.
 * <p>
 * This command is used by the mail import wizard to import messages.
 * All the interesting work happens in {@link DefaultMailboxImporter}.
 * <p>
 * Note, that the import wizard needs a command to make sure that the folder
 * is locked.
 *
 * @author fdietz
 */
public class ImportMessageCommand extends FolderCommand {
    /**
     * @param references
     */
    public ImportMessageCommand(DefaultCommandReference[] references) {
        super(references);
    }

    /**
     * @param frame
     * @param references
     */
    public ImportMessageCommand(FrameMediator frame,
        DefaultCommandReference[] references) {
        super(frame, references);
    }

    /* (non-Javadoc)
     * @see org.columba.core.command.Command#execute(org.columba.core.command.Worker)
     */
    public void execute(WorkerStatusController worker) throws Exception {
        ImportFolderCommandReference[] r = (ImportFolderCommandReference[]) getReferences();

        DefaultMailboxImporter importer = r[0].getImporter();

        importer.run(worker);
    }
}
