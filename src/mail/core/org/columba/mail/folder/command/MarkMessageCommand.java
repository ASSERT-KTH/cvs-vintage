// The contents of this file are subject to the Mozilla Public License Version
// 1.1
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
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.mail.folder.command;

import org.columba.core.command.DefaultCommandReference;
import org.columba.core.command.StatusObservableImpl;
import org.columba.core.command.Worker;
import org.columba.core.command.WorkerStatusController;
import org.columba.mail.command.FolderCommand;
import org.columba.mail.command.FolderCommandAdapter;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.folder.MessageFolder;
import org.columba.mail.gui.frame.TableUpdater;
import org.columba.mail.gui.table.model.TableModelChangedEvent;
import org.columba.mail.main.MailInterface;

/**
 * Mark selected messages with specific variant.
 * <p>
 * 
 * Variant can be: - read/unread - flagged/unflagged - expunged/unexpunged -
 * answered
 * 
 * @author fdietz
 */
public class MarkMessageCommand extends FolderCommand {

    public final static int MARK_AS_READ = 1;

    public final static int MARK_AS_UNREAD = -1;

    public final static int MARK_AS_FLAGGED = 2;

    public final static int MARK_AS_UNFLAGGED = -2;

    public final static int MARK_AS_EXPUNGED = 3;

    public final static int MARK_AS_UNEXPUNGED = -3;

    public final static int MARK_AS_ANSWERED = 4;

    public final static int MARK_AS_UNANSWERED = -4;

    public final static int MARK_AS_SPAM = 5;

    public final static int MARK_AS_NOTSPAM = -5;

    protected FolderCommandAdapter adapter;

    private WorkerStatusController worker;

    /**
     * Constructor for MarkMessageCommand.
     * 
     * @param frameMediator
     * @param references
     */
    public MarkMessageCommand(DefaultCommandReference[] references) {
        super(references);
    }

    public void updateGUI() throws Exception {
        // get source references
        FolderCommandReference[] r = adapter.getSourceFolderReferences();

        // for every source references
        TableModelChangedEvent ev;

        for (int i = 0; i < r.length; i++) {
            // update table
            ev = new TableModelChangedEvent(TableModelChangedEvent.MARK, r[i]
                    .getFolder(), r[i].getUids(), r[i].getMarkVariant());

            TableUpdater.tableChanged(ev);

            // update treemodel
            MailInterface.treeModel.nodeChanged(r[i].getFolder());
        }

        // get update reference
        // -> only available if VirtualFolder is involved in operation
        FolderCommandReference u = adapter.getUpdateReferences();

        if (u != null) {
            ev = new TableModelChangedEvent(TableModelChangedEvent.MARK, u
                    .getFolder(), u.getUids(), u.getMarkVariant());

            TableUpdater.tableChanged(ev);
            MailInterface.treeModel.nodeChanged(u.getFolder());
        }
    }

    /**
     * @see org.columba.core.command.Command#execute(Worker)
     */
    public void execute(WorkerStatusController worker) throws Exception {
        this.worker = worker;

        // use wrapper class for easier handling of references array
        adapter = new FolderCommandAdapter(
                (FolderCommandReference[]) getReferences());

        // get array of source references
        FolderCommandReference[] r = adapter.getSourceFolderReferences();

        // for every folder
        for (int i = 0; i < r.length; i++) {
            // get array of message UIDs
            Object[] uids = r[i].getUids();

            // get source folder
            MessageFolder srcFolder = (MessageFolder) r[i].getFolder();

            // register for status events
            ((StatusObservableImpl) srcFolder.getObservable())
                    .setWorker(worker);

            // which kind of mark?
            int markVariant = r[i].getMarkVariant();

            // saving last selected message to the folder
            srcFolder.setLastSelection(uids[0]);

            // mark message
            srcFolder.markMessage(uids, markVariant);

           

        }
    }

    
}