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

package org.columba.mail.spam.command;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.columba.core.command.DefaultCommandReference;
import org.columba.core.command.StatusObservableImpl;
import org.columba.core.command.WorkerStatusController;
import org.columba.core.main.MainInterface;
import org.columba.mail.command.FolderCommand;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.folder.MessageFolder;
import org.columba.mail.spam.SpamController;
import org.columba.ristretto.message.Header;
import org.macchiato.Message;

/**
 * Learn selected messages as spam.
 * 
 * @author fdietz
 */
public class LearnMessageAsSpamCommand extends FolderCommand {

	/**
	 * @param references
	 */
	public LearnMessageAsSpamCommand(DefaultCommandReference reference) {
		super(reference);
	}

	/**
	 * @see org.columba.core.command.Command#execute(org.columba.core.command.Worker)
	 */
	public void execute(WorkerStatusController worker) throws Exception {

		// get array of source references
		FolderCommandReference r = (FolderCommandReference) getReference();

		// get array of message UIDs
		Object[] uids = r.getUids();

		// get source folder
		MessageFolder srcFolder = (MessageFolder) r.getFolder();

		//	update status message
		if (uids.length > 1) {
			//TODO: i18n
			worker.setDisplayText("Training messages...");
			worker.setProgressBarMaximum(uids.length);
		}

		InputStream istream = null;
		for (int j = 0; j < uids.length; j++) {
			if (worker.cancelled()) {
				break;
			}

			try {
				// register for status events
				((StatusObservableImpl) srcFolder.getObservable())
						.setWorker(worker);

				// get inputstream of message body
				istream = CommandHelper.getBodyPart(srcFolder, uids[j]);

				// get headers
				Header h = srcFolder.getHeaderFields(uids[j],
						Message.HEADERFIELDS);

				// put headers in list
				Enumeration e = h.getKeys();
				List list = new ArrayList();

				while (e.hasMoreElements()) {
					String key = (String) e.nextElement();
					list.add(h.get(key));
				}

				//train message as spam
				SpamController.getInstance().trainMessageAsSpam(istream, list);

				if (uids.length > 1) {
					worker.setProgressBarValue(j);
				}
			} catch (Exception e) {
				if (MainInterface.DEBUG) {
					e.printStackTrace();
				}
			} finally {
				try {
					istream.close();
				} catch (IOException ioe) {
				}
			}
		}

	}
}