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

import org.columba.core.command.DefaultCommandReference;
import org.columba.core.command.Worker;
import org.columba.core.gui.frame.AbstractFrameController;
import org.columba.core.logging.ColumbaLogger;
import org.columba.mail.command.FolderCommand;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.folder.Folder;
import org.columba.mail.message.HeaderInterface;

/**
 * @author fdietz
 *

 */
public class AddAddressToBlackListCommand extends FolderCommand {

	/**
	 * 
	 * @param references
	 */
	public AddAddressToBlackListCommand(DefaultCommandReference[] references) {
		super(references);
	}

	/**
	 * 
	 * @param frame
	 * @param references
	 */
	public AddAddressToBlackListCommand(
		AbstractFrameController frame,
		DefaultCommandReference[] references) {
		super(frame, references);
	}

	/**
	 * @see org.columba.core.command.Command#execute(org.columba.core.command.Worker)
	 */
	public void execute(Worker worker) throws Exception {
		FolderCommandReference[] r = (FolderCommandReference[]) getReferences();

		Object[] uids = r[0].getUids();
		Folder folder = (Folder) r[0].getFolder();

		for (int i = 0; i < uids.length; i++) {

			HeaderInterface header = folder.getMessageHeader(uids[i], worker);
			String sender = (String) header.get("From");

			addSender(sender);

		}

	}

	public void addSender(String sender) {
		if (sender == null)
			return;

		if (sender.length() > 0) {

			IPCHelper ipcHelper = new IPCHelper();

			if (sender.length() > 0) {
				int exitVal = -1;
				try {
					ColumbaLogger.log.debug("creating process..");

					String cmd =
						"spamassassin -a --add-addr-to-blacklist=\""
							+ sender
							+ "\"";
					ipcHelper.executeCommand(cmd);

					exitVal = ipcHelper.waitFor();

					ColumbaLogger.log.debug("exitcode=" + exitVal);

					ipcHelper.waitForThreads();

				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}

}
