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
 * Expunge folder.
 * <p>
 * Delete all messages from this folder, which are marked as expunged.
 *
 * @author fdietz
 *
 */
public class ExpungeFolderCommand extends FolderCommand {
    protected FolderCommandAdapter adapter;

    /**
 * Constructor for ExpungeFolderCommand.
 *
 * @param frameMediator
 * @param references
 */
    public ExpungeFolderCommand(DefaultCommandReference[] references) {
        super(references);
    }

    /**
 * @see org.columba.core.command.Command#updateGUI()
 */
    public void updateGUI() throws Exception {
        // get source references
        FolderCommandReference[] r = adapter.getSourceFolderReferences();

        TableModelChangedEvent ev;

        // use source references to update message list and treemodel
        for (int i = 0; i < r.length; i++) {
            // update message list
            ev = new TableModelChangedEvent(TableModelChangedEvent.UPDATE,
                    r[i].getFolder());

            TableUpdater.tableChanged(ev);

            // update tree
            MailInterface.treeModel.nodeChanged(r[i].getFolder());
        }

        // get update references
        // -> only available if virtual folders are involved
        FolderCommandReference u = adapter.getUpdateReferences();

        if (u != null) {
            ev = new TableModelChangedEvent(TableModelChangedEvent.UPDATE,
                    u.getFolder());

            TableUpdater.tableChanged(ev);

            MailInterface.treeModel.nodeChanged(u.getFolder());
        }
    }

    /**
 * @see org.columba.core.command.Command#execute(Worker)
 */
    public void execute(WorkerStatusController worker)
        throws Exception {
        // use wrapper for references
        adapter = new FolderCommandAdapter((FolderCommandReference[]) getReferences());

        // get source references
        FolderCommandReference[] r = adapter.getSourceFolderReferences();

        // for each folder
        for (int i = 0; i < r.length; i++) {
            MessageFolder srcFolder = (MessageFolder) r[i].getFolder();

            // register for status events
            ((StatusObservableImpl) srcFolder.getObservable()).setWorker(worker);

            // update status message
            worker.setDisplayText("Expunging " + srcFolder.getName() + "..");

            // expunge folder
            srcFolder.expungeFolder();
        }
    }
}
