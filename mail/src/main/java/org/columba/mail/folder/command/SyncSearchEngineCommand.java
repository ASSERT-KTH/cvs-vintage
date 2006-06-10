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

import org.columba.api.command.ICommandReference;
import org.columba.api.command.IWorkerStatusController;
import org.columba.core.command.Command;
import org.columba.mail.command.IMailFolderCommandReference;
import org.columba.mail.folder.AbstractLocalFolder;
import org.columba.mail.folder.search.DefaultSearchEngine;

/**
 * Sync search engine.
 *
 *
 * @author fdietz
 */
public class SyncSearchEngineCommand extends Command {
    private AbstractLocalFolder parentFolder;

    public SyncSearchEngineCommand(ICommandReference reference) {
        super(reference);
    }

    public void execute(IWorkerStatusController worker)
        throws Exception {
        // get source folder
        parentFolder = (AbstractLocalFolder) ((IMailFolderCommandReference) getReference()).getSourceFolder();

        // resync search engine
        // -> this is only needed for Lucene right now
        DefaultSearchEngine engine = parentFolder.getSearchEngine();
        engine.sync();
    }
}
