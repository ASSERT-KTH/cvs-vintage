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

import java.io.InputStream;
import java.util.logging.Logger;

import org.columba.core.command.DefaultCommandReference;
import org.columba.core.command.WorkerStatusController;
import org.columba.core.gui.frame.FrameMediator;
import org.columba.mail.command.FolderCommand;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.folder.MessageFolder;

/**
 * @author fdietz
 * 
 * let spamassassin go through all messages: - analyze message - tag as spam/ham
 * in adding two more headerfields
 * 
 * added headerfields are: X-Spam-Level: digit number columba.spam: true/false
 * (create a filter on this headerfield)
 *  
 */
public class AnalyzeMessageCommand extends FolderCommand {

	/** JDK 1.4+ logging framework logger, used for logging. */
	private static final Logger LOG = Logger.getAnonymousLogger();

	MessageFolder srcFolder;

	/**
	 * @param references
	 */
	public AnalyzeMessageCommand(DefaultCommandReference references) {
		super(references);
	}

	/**
	 * @param frame
	 * @param references
	 */
	public AnalyzeMessageCommand(FrameMediator frame,
			DefaultCommandReference references) {
		super(frame, references);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.columba.core.command.Command#execute(org.columba.core.command.Worker)
	 */
	public void execute(WorkerStatusController worker) throws Exception {
		FolderCommandReference r = (FolderCommandReference) getReference();

		srcFolder = (MessageFolder) r.getFolder();

		Object[] uids = r.getUids();
		worker.setDisplayText("Applying analyzer to " + srcFolder.getName()
				+ "...");

		worker.setProgressBarMaximum(uids.length);

		for (int i = 0; i < uids.length; i++) {
			if (worker.cancelled()) {
				return;
			}

			AnalyzeMessageCommand.addHeader(srcFolder, uids[i], worker);
			worker.setProgressBarValue(i);
		}

	}

	public static void addHeader(MessageFolder srcFolder, Object uid,
			WorkerStatusController worker) throws Exception {
		//Header header = srcFolder.getHeaderFields(uid, new String[]
		// {"X-Spam-Level"} );
		InputStream rawMessageSource = srcFolder.getMessageSourceStream(uid);
		IPCHelper ipcHelper = new IPCHelper();

		//String cmd = "spamassassin -L";
		String cmd = ExternalToolsHelper.getSpamc() + " -c";

		String result = null;
		int exitVal = -1;

		try {
			LOG.info("creating process..");

			ipcHelper.executeCommand(cmd);

			LOG.info("sending to stdin..");

			ipcHelper.send(rawMessageSource);

			exitVal = ipcHelper.waitFor();

			LOG.info("exitcode=" + exitVal);

			LOG.info("retrieving output..");
			result = ipcHelper.getOutputString();

			ipcHelper.waitForThreads();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		if (result == null) {
			return;
		}

		//header.set("X-Spam-Level", result);
		if (exitVal == 1) {
			// spam found
			srcFolder.setAttribute(uid, "columba.spam", Boolean.TRUE);
		} else {
			srcFolder.setAttribute(uid, "columba.spam", Boolean.FALSE);
		}

		result = null;
	}
}