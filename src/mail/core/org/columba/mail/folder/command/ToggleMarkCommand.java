// The contents of this file are subject to the Mozilla Public License Version
//1.1
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
//Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.mail.folder.command;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.columba.core.command.DefaultCommandReference;
import org.columba.core.command.StatusObservableImpl;
import org.columba.core.command.Worker;
import org.columba.core.command.WorkerStatusController;
import org.columba.mail.command.FolderCommand;
import org.columba.mail.command.FolderCommandAdapter;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.folder.MessageFolder;
import org.columba.ristretto.message.Flags;

/**
 * Toggle flag.
 * <p>
 * Creates two sets of messages and uses {@link MarkMessageCommand}, which does
 * the flag change.
 * 
 * @see MarkMessageCommand
 * @author fdietz
 */
public class ToggleMarkCommand extends FolderCommand {

    protected FolderCommandAdapter adapter;

    private WorkerStatusController worker;

    private List commandList;

    /**
     * Constructor for ToggleMarkCommand.
     * 
     * @param frameMediator
     * @param references
     */
    public ToggleMarkCommand(DefaultCommandReference[] references) {
        super(references);

        commandList = new ArrayList();
    }

    public void updateGUI() throws Exception {

        Iterator it = commandList.iterator();
        while (it.hasNext()) {
            FolderCommand c = (FolderCommand) it.next();

            c.updateGUI();
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

            List list1 = new ArrayList();
            List list2 = new ArrayList();

            for (int j = 0; j < uids.length; j++) {
                Flags flags = srcFolder.getFlags(uids[j]);

                boolean result = false;
                if (markVariant == MarkMessageCommand.MARK_AS_READ) {
                    if (flags.getSeen()) result = true;
                } else if (markVariant == MarkMessageCommand.MARK_AS_FLAGGED) {
                    if (flags.getFlagged()) result = true;
                } else if (markVariant == MarkMessageCommand.MARK_AS_EXPUNGED) {
                    if (flags.getExpunged()) result = true;
                } else if (markVariant == MarkMessageCommand.MARK_AS_ANSWERED) {
                    if (flags.getAnswered()) result = true;
                } else if (markVariant == MarkMessageCommand.MARK_AS_SPAM) {
                    boolean spam = ((Boolean) srcFolder.getAttribute(uids[j],
                            "columba.spam")).booleanValue();
                    if (spam) result = true;
                }

                if (result)
                    list1.add(uids[j]);
                else
                    list2.add(uids[j]);
            }

            FolderCommandReference[] ref = new FolderCommandReference[1];

            if (list1.size() > 0) {
                ref[0] = new FolderCommandReference(srcFolder, list1.toArray());
                ref[0].setMarkVariant(-markVariant);
                MarkMessageCommand c = new MarkMessageCommand(ref);
                commandList.add(c);
                c.execute(worker);
            }

            if (list2.size() > 0) {
                ref[0] = new FolderCommandReference(srcFolder, list2.toArray());
                ref[0].setMarkVariant(markVariant);
                MarkMessageCommand c = new MarkMessageCommand(ref);
                commandList.add(c);
                c.execute(worker);
            }
        }
    }

}