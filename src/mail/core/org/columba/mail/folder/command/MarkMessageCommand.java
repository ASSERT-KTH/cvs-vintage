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

import org.columba.core.command.Command;
import org.columba.core.command.DefaultCommandReference;
import org.columba.core.command.StatusObservableImpl;
import org.columba.core.command.Worker;
import org.columba.core.command.WorkerStatusController;
import org.columba.core.main.MainInterface;
import org.columba.mail.command.FolderCommand;
import org.columba.mail.command.FolderCommandAdapter;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.config.AccountItem;
import org.columba.mail.folder.MessageFolder;
import org.columba.mail.folder.AbstractFolder;
import org.columba.mail.folder.RootFolder;
import org.columba.mail.gui.frame.TableUpdater;
import org.columba.mail.gui.table.model.TableModelChangedEvent;
import org.columba.mail.main.MailInterface;
import org.columba.mail.spam.command.CommandHelper;
import org.columba.mail.spam.command.LearnMessageAsHamCommand;
import org.columba.mail.spam.command.LearnMessageAsSpamCommand;

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

    public final static int MARK_AS_READ = 0;

    public final static int MARK_AS_FLAGGED = 1;

    public final static int MARK_AS_EXPUNGED = 2;

    public final static int MARK_AS_ANSWERED = 3;

    public final static int MARK_AS_SPAM = 4;

    public final static int MARK_AS_UNREAD = -1;

    public final static int MARK_AS_UNFLAGGED = -2;

    public final static int MARK_AS_UNEXPUNGED = -3;

    public final static int MARK_AS_NOTSPAM = -4;

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

            if ((markVariant == MARK_AS_SPAM)
                    || (markVariant == MARK_AS_NOTSPAM)) {
                processSpamFilter(uids, srcFolder, markVariant);
            }

        }
    }

    /**
     * Train spam filter.
     * <p>
     * Move message to specified folder or delete message immediately based on
     * account configuration.
     * 

     * @param uids
     *            message uid
     * @param srcFolder
     *            source folder
     * @param markVariant
     *            mark variant (spam/not spam)
     * @throws Exception
     */
    private void processSpamFilter(
            Object[] uids, MessageFolder srcFolder, int markVariant) throws Exception {
        // mark as/as not spam
        // for each message
        for (int j = 0; j < uids.length; j++) {
            // message belongs to which account?
            AccountItem item = CommandHelper.retrieveAccountItem(srcFolder,
                    uids[j]);
            // skip if account information is not available
            if (item == null) continue;

            // if spam filter is not enabled -> return
            if (item.getSpamItem().isEnabled() == false) continue;

            System.out.println("learning uid=" + uids[j]);

            // create reference
            FolderCommandReference[] ref = new FolderCommandReference[1];
            ref[0] = new FolderCommandReference(srcFolder,
                    new Object[] { uids[j]});

            // create command
            Command c = null;
            if (markVariant == MARK_AS_SPAM)
                c = new LearnMessageAsSpamCommand(ref);
            else
                c = new LearnMessageAsHamCommand(ref);

            // execute command
            c.execute(worker);

            // skip if message is *not* marked as spam
            if (markVariant == MARK_AS_NOTSPAM) continue;
            
            // skip if user didn't enable this option
            if (item.getSpamItem().isMoveMessageWhenMarkingEnabled() == false)
                    continue;

            if (item.getSpamItem().isMoveTrashSelected() == false) {
                // move message to user-configured folder (generally "Junk"
                // folder)
                AbstractFolder destFolder = MailInterface.treeModel.getFolder(item
                        .getSpamItem().getMoveCustomFolder());

                // create reference
                FolderCommandReference[] ref2 = new FolderCommandReference[2];
                ref2[0] = new FolderCommandReference(srcFolder,
                        new Object[] { uids[j]});
                ref2[1] = new FolderCommandReference(destFolder);
                MainInterface.processor.addOp(new MoveMessageCommand(ref2));

            } else {
                // move message to trash
                MessageFolder trash = (MessageFolder) ((RootFolder) srcFolder.getRootFolder())
                        .getTrashFolder();

                // create reference
                FolderCommandReference[] ref2 = new FolderCommandReference[2];
                ref2[0] = new FolderCommandReference(srcFolder,
                        new Object[] { uids[j]});
                ref2[1] = new FolderCommandReference(trash);

                MainInterface.processor.addOp(new MoveMessageCommand(ref2));

            }

        }
    }
}
