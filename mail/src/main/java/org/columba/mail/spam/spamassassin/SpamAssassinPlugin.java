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
package org.columba.mail.spam.spamassassin;

import java.io.InputStream;
import java.util.logging.Logger;

import org.columba.mail.folder.IMailbox;
import org.columba.mail.spam.ISpamPlugin;

public class SpamAssassinPlugin implements ISpamPlugin {

	/** JDK 1.4+ logging framework logger, used for logging. */
	private static final Logger LOG = Logger
			.getLogger("org.columba.core.gui.htmlviewer");

	public SpamAssassinPlugin() {
		super();

	}

	public boolean scoreMessage(IMailbox mailbox, Object uid) throws Exception {
		InputStream rawMessageSource = mailbox.getMessageSourceStream(uid);
		IPCHelper ipcHelper = new IPCHelper();

		// "-L" use local tests only
		// String cmd = "spamassassin -L";
		// String cmd = "spamc -c -L";
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
			return false;
		}

		if (exitVal == 1) {
			// spam found
			return true;
		} else {
			return false;
		}

	}

	public void trainMessageAsSpam(IMailbox mailbox, Object uid)
			throws Exception {

		InputStream rawMessageSource = mailbox.getMessageSourceStream(uid);

		IPCHelper ipcHelper = new IPCHelper();

		LOG.info("creating process..");
		
		// --no-rebuild option is deprecated in recent SpamAssassin versions
		/*
		ipcHelper.executeCommand(ExternalToolsHelper.getSALearn()
				+ " --no-rebuild --spam --single");
				*/
		
		ipcHelper.executeCommand(ExternalToolsHelper.getSALearn()
				+ " --no-sync --spam --single");
		
		
		LOG.info("sending to stdin..");

		ipcHelper.send(rawMessageSource);

		int exitVal = ipcHelper.waitFor();

		LOG.info("exitcode=" + exitVal);

		LOG.info("retrieving output..");

		String result = ipcHelper.getOutputString();

		LOG.info("output=" + result);

		ipcHelper.waitForThreads();

	}

	public void trainMessageAsHam(IMailbox mailbox, Object uid)
			throws Exception {
		InputStream rawMessageSource = mailbox.getMessageSourceStream(uid);

		IPCHelper ipcHelper = new IPCHelper();

		LOG.info("creating process..");
		// --no-rebuild option is deprecated in recent SpamAssassin versions
		ipcHelper.executeCommand(ExternalToolsHelper.getSALearn()
				+ " --no-sync --ham --single");

		LOG.info("sending to stdin..");

		ipcHelper.send(rawMessageSource);

		int exitVal = ipcHelper.waitFor();

		LOG.info("exitcode=" + exitVal);

		LOG.info("retrieving output..");

		String result = ipcHelper.getOutputString();

		LOG.info("output=" + result);

		ipcHelper.waitForThreads();

	}

	public void save() {
		// don't need this
	}

	public void load() {
		// don't need this
	}

}
