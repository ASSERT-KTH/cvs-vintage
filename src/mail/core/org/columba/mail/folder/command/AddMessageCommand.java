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

import org.columba.core.command.Command;
import org.columba.core.command.DefaultCommandReference;
import org.columba.core.command.StatusObservableImpl;
import org.columba.core.command.Worker;
import org.columba.core.command.WorkerStatusController;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.folder.MessageFolder;
import org.columba.mail.message.ColumbaMessage;
import org.columba.ristretto.io.SourceInputStream;

/**
 * Add message to folder
 * <p>
 * This command isn't used right now, and will most probably be removed in the
 * future.
 *
 * @author fdietz
 */
public class AddMessageCommand extends Command {
    private MessageFolder folder;

    /**
     * Constructor for AddMessageCommand.
     *
     * @param references command arguments.
     */
    public AddMessageCommand(DefaultCommandReference reference) {
        super(reference);
    }

    /**
     * @see org.columba.core.command.Command#execute(Worker)
     */
    public void execute(WorkerStatusController worker)
        throws Exception {
        // get reference
        FolderCommandReference r = (FolderCommandReference) getReference();

        // get source folder
        folder = (MessageFolder) r.getFolder();

        // register for status events
        ((StatusObservableImpl) folder.getObservable()).setWorker(worker);

        // get message from reference
        ColumbaMessage message = (ColumbaMessage) r.getMessage();

        // add message to folder
        SourceInputStream messageStream = new SourceInputStream(message.getSource());
        folder.addMessage(messageStream);
        messageStream.close();
    }
}
