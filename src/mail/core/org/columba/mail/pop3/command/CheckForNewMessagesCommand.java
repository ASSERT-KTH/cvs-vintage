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
package org.columba.mail.pop3.command;

import java.net.URL;
import java.util.Vector;

import org.columba.core.command.Command;
import org.columba.core.command.CommandCancelledException;
import org.columba.core.command.DefaultCommandReference;
import org.columba.core.command.Worker;
import org.columba.core.logging.ColumbaLogger;
import org.columba.core.util.PlaySound;
import org.columba.mail.command.POP3CommandReference;
import org.columba.mail.config.AccountItem;
import org.columba.mail.config.PopItem;
import org.columba.mail.pop3.POP3Server;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class CheckForNewMessagesCommand extends Command {

	POP3Server server;

	public CheckForNewMessagesCommand(DefaultCommandReference[] references) {
		super(references);
	}

	/**
	 * @see org.columba.core.command.Command#execute(Worker)
	 */
	public void execute(Worker worker) throws Exception {

		FetchNewMessagesCommand command =
			new FetchNewMessagesCommand(getReferences());

		POP3CommandReference[] r =
			(POP3CommandReference[]) getReferences(FIRST_EXECUTION);

		server = r[0].getServer();

		command.log("Authenticating...", worker);

		int totalMessageCount = server.getMessageCount(worker);

		try {
			Vector newUIDList = command.fetchUIDList(totalMessageCount, worker);

			Vector messageSizeList = command.fetchMessageSizes(worker);

			Vector newMessagesUIDList = command.synchronize(newUIDList);

			int newMessagesCount = newMessagesUIDList.size();
			if ((newMessagesCount > 0)
				&& (server
					.getAccountItem()
					.getPopItem()
					.getBoolean("enable_sound")))
				playSound();

			if (server
				.getAccountItem()
				.getPopItem()
				.getBoolean("automatically_download_new_messages"))
				command.downloadNewMessages(
					newUIDList,
					messageSizeList,
					newMessagesUIDList,
					worker);

			command.logout(worker);

		} catch (CommandCancelledException e) {
			server.forceLogout();
		}
	}

	protected void playSound() {

		AccountItem item = server.getAccountItem();
		PopItem popItem = item.getPopItem();
		String file = popItem.get("sound_file");

		ColumbaLogger.log.info("playing sound file=" + file);

		if (file.equalsIgnoreCase("default")) {
			PlaySound.play("newmail.wav");
		} else {
			try {
				PlaySound.play(new URL(file));
			} catch (Exception ex) {
				ex.printStackTrace();
			}

		}
	}

}
