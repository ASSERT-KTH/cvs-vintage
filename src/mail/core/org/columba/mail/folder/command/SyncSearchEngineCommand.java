/*
 * Created on 12.09.2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.columba.mail.folder.command;

import org.columba.core.command.DefaultCommandReference;
import org.columba.core.command.Worker;
import org.columba.core.main.MainInterface;
import org.columba.mail.command.FolderCommand;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.folder.LocalFolder;
import org.columba.mail.folder.search.AbstractSearchEngine;
import org.columba.mail.folder.search.LuceneSearchEngine;

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
		MainInterface.treeModel.nodeStructureChanged(parentFolder);
	}

	public void execute(Worker worker) throws Exception {
		parentFolder =
			(LocalFolder) ((FolderCommandReference) getReferences()[0])
				.getFolder();
				
		AbstractSearchEngine engine = parentFolder.getSearchEngineInstance();
		if ( engine instanceof LuceneSearchEngine )
			{
				((LuceneSearchEngine) engine).sync();
			}
	}
}
