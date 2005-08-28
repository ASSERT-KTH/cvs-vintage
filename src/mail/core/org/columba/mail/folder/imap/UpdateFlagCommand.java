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

package org.columba.mail.folder.imap;

import org.columba.api.command.ICommandReference;
import org.columba.api.command.IWorkerStatusController;
import org.columba.core.command.Command;
import org.columba.mail.command.IMailFolderCommandReference;
import org.columba.ristretto.imap.IMAPFlags;

public class UpdateFlagCommand extends Command {

	IMAPFlags flag;
	
	/**
	 * Constructs the UpdateFlagCommand.java.
	 * 
	 * @param reference
	 */
	public UpdateFlagCommand(ICommandReference reference, IMAPFlags flag) {
		super(reference);
		this.flag = flag;
	}
	/**
	 * @see org.columba.api.command.Command#execute(org.columba.api.command.IWorkerStatusController)
	 */
	public void execute(IWorkerStatusController worker) throws Exception {
		// get references
		IMailFolderCommandReference r = (IMailFolderCommandReference) getReference();

		// get IMAP rootfolder
		IMAPFolder srcFolder = (IMAPFolder) r.getSourceFolder();
		
		srcFolder.updateFlag(flag);		
	}

}
