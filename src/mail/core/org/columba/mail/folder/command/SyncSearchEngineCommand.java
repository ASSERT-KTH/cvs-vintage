/*
 * Created on 12.09.2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.columba.mail.folder.command;

import org.columba.core.command.DefaultCommandReference;
import org.columba.core.command.Worker;
import org.columba.core.command.WorkerStatusController;

import org.columba.mail.command.FolderCommand;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.folder.LocalFolder;
import org.columba.mail.folder.search.DefaultSearchEngine;
import org.columba.mail.folder.search.LuceneQueryEngine;
import org.columba.mail.main.MailInterface;


/**
 *
 * Sync search engine.
 *
 *
 * @author fdietz
 */
public class SyncSearchEngineCommand extends FolderCommand {
    private LocalFolder parentFolder;

    public SyncSearchEngineCommand(DefaultCommandReference[] references) {
        super(references);
    }

    public void updateGUI() throws Exception {
        // update treemodel
        MailInterface.treeModel.nodeStructureChanged(parentFolder);
    }

    public void execute(WorkerStatusController worker) throws Exception {
        // get source folder
        parentFolder = (LocalFolder) ((FolderCommandReference) getReferences()[0]).getFolder();

        // resync search engine
        // -> this is only needed for Lucene right now
        DefaultSearchEngine engine = parentFolder.getSearchEngineInstance();
        engine.sync();
    }
}
