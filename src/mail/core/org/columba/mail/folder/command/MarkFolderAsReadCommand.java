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

import java.text.MessageFormat;

import org.columba.core.command.DefaultCommandReference;
import org.columba.core.command.WorkerStatusController;
import org.columba.mail.command.FolderCommand;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.folder.MessageFolder;
import org.columba.mail.util.MailResourceLoader;

/**
 * A command that marks all messages in a folder as read.
 * <p>
 * The command reference should be inserted as these:
 * <ol>
 * <li> A <code>Folder</code> that is going to be marked as read, and the uids to mark as read.
 * </ol>
 * <p>
 * First implementation marks all messages as read, a better approach would
 * be to only mark those who arent read.
 * <p>
 * @author redsolo
 */
public class MarkFolderAsReadCommand extends FolderCommand {

    /** The folder that is supposed to be marked as read. */
    private MessageFolder folderToBeRead;
    
    /**
     * Command doing the actual work.
     */
    private MarkMessageCommand markMessageCommand;

    /**
     * @param references folder references
     */
    public MarkFolderAsReadCommand(DefaultCommandReference[] references) {
        super(references);
    }

    /** {@inheritDoc} */
    public void execute(WorkerStatusController worker) throws Exception {
        // get folder that is going to be moved
        folderToBeRead = (MessageFolder) ((FolderCommandReference) getReferences()[0]).getFolder();

        worker.setDisplayText(MessageFormat.format(
                MailResourceLoader.getString("statusbar", "message",
                    "folder_markasread"), new Object[] {folderToBeRead.getName()}));

        worker.clearDisplayTextWithDelay();

        FolderCommandReference[] markCommandRefs = new FolderCommandReference[1];

        markCommandRefs[0] = new FolderCommandReference(folderToBeRead);
        Object[] uids = folderToBeRead.getUids();
        if ((uids != null) && (uids.length > 0)) {
            markCommandRefs[0].setUids(uids);
            markCommandRefs[0].setMarkVariant(MarkMessageCommand.MARK_AS_READ);

            markMessageCommand = new MarkMessageCommand(markCommandRefs);
            markMessageCommand.execute(worker);
        }
    }

    /** {@inheritDoc} */
    public void updateGUI() throws Exception {
    	// MarkMessageCommand updates everything
    	markMessageCommand.updateGUI();
    }
}
