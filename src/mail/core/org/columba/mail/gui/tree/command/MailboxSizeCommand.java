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
package org.columba.mail.gui.tree.command;

import org.columba.api.command.ICommandReference;
import org.columba.api.command.IWorkerStatusController;
import org.columba.core.command.Command;
import org.columba.mail.command.IMailFolderCommandReference;
import org.columba.mail.folder.IMailFolder;
import org.columba.mail.folder.IMailbox;
import org.columba.mail.gui.config.folder.FolderOptionsDialog;
import org.columba.mail.message.ICloseableIterator;
import org.columba.mail.message.IColumbaHeader;
import org.columba.mail.message.IHeaderList;

public class MailboxSizeCommand extends Command {

	private FolderOptionsDialog dialog;

	private int total = 0;

	public MailboxSizeCommand(ICommandReference reference,
			FolderOptionsDialog dialog) {
		super(reference);

		this.dialog = dialog;
	}

	public void execute(IWorkerStatusController worker) throws Exception {

		IMailFolder folder = (IMailFolder) ((IMailFolderCommandReference) getReference())
				.getSourceFolder();

		total = 0;
		
		if (folder instanceof IMailbox) {
			IHeaderList headerList = ((IMailbox) folder).getHeaderList();
			ICloseableIterator it = headerList.headerIterator();
			while (it.hasNext()) {
				IColumbaHeader header = (IColumbaHeader) it.next();
				Integer sizeInt = (Integer) header.getAttributes().get(
						"columba.size");

				if (sizeInt != null) {
					total += sizeInt.intValue();
				}
			}
			it.close();
		}
	}

	/**
	 * @see org.columba.core.command.Command#updateGUI()
	 */
	public void updateGUI() throws Exception {
		dialog.setMailboxSize(total);
	}

}
