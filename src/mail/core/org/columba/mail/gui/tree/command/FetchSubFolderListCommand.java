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
package org.columba.mail.gui.tree.command;

import java.util.logging.Logger;

import org.columba.core.command.Command;
import org.columba.core.command.ICommandReference;
import org.columba.core.command.Worker;
import org.columba.core.command.WorkerStatusController;
import org.columba.mail.command.MailFolderCommandReference;
import org.columba.mail.folder.imap.IMAPRootFolder;

/**
 * @author freddy
 */
public class FetchSubFolderListCommand extends Command {

	/** JDK 1.4+ logging framework logger, used for logging. */
	private static final Logger LOG = Logger
			.getLogger("org.columba.mail.gui.tree.command");

	IMAPRootFolder imapRoot;

	/**
	 * Constructor for FetchSubFolderListCommand.
	 * 
	 * @param references
	 */
	public FetchSubFolderListCommand(ICommandReference reference) {
		super(reference);
	}

	/**
	 * @see org.columba.core.command.Command#updateGUI()
	 */
	public void updateGUI() throws Exception {
        // remove all subfolders that are not marked as existonserver
        
        if( imapRoot != null ) {
        	imapRoot.removeNotMarkedSubfolders(imapRoot);
        }

	}

	/**
	 * @see org.columba.core.command.Command#execute(Worker)
	 */
	public void execute(WorkerStatusController worker) throws Exception {
		MailFolderCommandReference r = (MailFolderCommandReference) getReference();

		if (r == null) {
			return;
		}

		if (r.getSourceFolder() instanceof IMAPRootFolder) {
			imapRoot = (IMAPRootFolder) r.getSourceFolder();
			
			imapRoot.syncSubscribedFolders();
			
			// The update of the treemodell follows in the updategui method
		}
	}
}