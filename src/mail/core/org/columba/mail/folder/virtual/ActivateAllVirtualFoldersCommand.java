/*
 * Created on May 3, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.columba.mail.folder.virtual;

import java.util.ArrayList;
import java.util.List;

import org.columba.core.command.Command;
import org.columba.core.command.ICommandReference;
import org.columba.core.command.WorkerStatusController;
import org.columba.core.folder.IFolderCommandReference;
import org.columba.mail.folder.AbstractFolder;
import org.columba.mail.folder.FolderChildrenIterator;
import org.columba.mail.util.MailResourceLoader;

/**
 * @author tstich
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ActivateAllVirtualFoldersCommand extends Command {

	/**
	 * @param reference
	 */
	public ActivateAllVirtualFoldersCommand(ICommandReference reference) {
		super(reference);
	}


	/* (non-Javadoc)
	 * @see org.columba.core.command.Command#execute(org.columba.core.command.WorkerStatusController)
	 */
	public void execute(WorkerStatusController worker) throws Exception {
		FolderChildrenIterator it = new FolderChildrenIterator((AbstractFolder)((IFolderCommandReference)getReference()).getSourceFolder());
		
		worker.setDisplayText(MailResourceLoader.getString(
				"statusbar", "message", "activate_vfolders"));
		
		
		// Put all VirtualFolders in one list
		List vfolderList = new ArrayList();
		
		while(it.hasMoreChildren()) {
			AbstractFolder f = it.nextChild();
			if( f instanceof VirtualFolder ){
				vfolderList.add(f);
			}
		}
		
		
		// Iterate through the list and activate every VFolder
		worker.setProgressBarMaximum(vfolderList.size());
		
		for( int i=0; i<vfolderList.size(); i++) {
			worker.setProgressBarValue(i);
			((VirtualFolder)vfolderList.get(i)).activate();			
		}
		
		worker.setProgressBarMaximum(0);
		worker.setDisplayText("");
	}

}
