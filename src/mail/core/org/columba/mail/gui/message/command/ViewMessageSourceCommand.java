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
package org.columba.mail.gui.message.command;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.columba.core.command.Command;
import org.columba.core.command.ICommandReference;
import org.columba.core.command.StatusObservableImpl;
import org.columba.core.command.Worker;
import org.columba.core.command.WorkerStatusController;
import org.columba.core.gui.frame.FrameMediator;
import org.columba.core.io.ColumbaDesktop;
import org.columba.core.io.TempFileStore;
import org.columba.mail.command.MailFolderCommandReference;
import org.columba.mail.folder.AbstractMessageFolder;
import org.columba.mail.gui.frame.AbstractMailFrameController;

/**
 * @author freddy
 * 
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates. To enable and disable the creation of type
 * comments go to Window>Preferences>Java>Code Generation.
 */
public class ViewMessageSourceCommand extends Command {
	protected File tempFile;

	/**
	 * Constructor for ViewMessageSourceCommand.
	 * 
	 * @param frameMediator
	 * @param references
	 */
	public ViewMessageSourceCommand(FrameMediator frameMediator,
			ICommandReference reference) {
		super(frameMediator, reference);
	}

	/**
	 * @see org.columba.core.command.Command#updateGUI()
	 */
	public void updateGUI() throws Exception {
		ColumbaDesktop.getInstance().open(tempFile);
	}

	/**
	 * @see org.columba.core.command.Command#execute(Worker)
	 */
	public void execute(WorkerStatusController worker) throws Exception {
		AbstractMailFrameController mailFrameController = (AbstractMailFrameController) frameMediator;

		MailFolderCommandReference r = (MailFolderCommandReference) getReference();

		Object[] uids = r.getUids();

		AbstractMessageFolder folder = (AbstractMessageFolder) r
				.getSourceFolder();

		// register for status events
		((StatusObservableImpl) folder.getObservable()).setWorker(worker);

		Object[] destUids = new Object[uids.length];
		Object uid = uids[0];

		InputStream in = null;
		OutputStream out = null;

		try {
			in = new BufferedInputStream(folder.getMessageSourceStream(uid));
			tempFile = TempFileStore.createTempFileWithSuffix("txt");
			out = new BufferedOutputStream(new FileOutputStream(tempFile));

			byte[] buffer = new byte[1024];
			int read;

			while ((read = in.read(buffer, 0, buffer.length)) > 0) {
				out.write(buffer, 0, read);
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException ioe) {
				}
			}

			if (out != null) {
				try {
					out.close();
				} catch (IOException ioe) {
				}
			}
		}
	}
}
