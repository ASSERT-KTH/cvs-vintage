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

import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;

import javax.swing.JOptionPane;

import org.columba.core.command.Command;
import org.columba.core.command.CommandCancelledException;
import org.columba.core.command.DefaultCommandReference;
import org.columba.core.command.Worker;
import org.columba.core.logging.ColumbaLogger;
import org.columba.core.main.MainInterface;
import org.columba.core.util.PlaySound;
import org.columba.mail.command.POP3CommandReference;
import org.columba.mail.config.AccountItem;
import org.columba.mail.config.PopItem;
import org.columba.mail.pop3.POP3Server;
import org.columba.mail.util.MailResourceLoader;

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

		command.log(MailResourceLoader.getString(
                                "statusbar",
                                "message",
                                "authenticating"), worker);

		try {
			int totalMessageCount = server.getMessageCount(worker);

			List newUIDList = command.fetchUIDList(totalMessageCount, worker);

			List messageSizeList = command.fetchMessageSizes(worker);

			List newMessagesUIDList = command.synchronize(newUIDList);

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
		} catch (UnknownHostException e) {
			JOptionPane.showMessageDialog(null, e.getLocalizedMessage(), "UnkownHostException", JOptionPane.ERROR_MESSAGE);
		} catch (SocketTimeoutException e) {
			JOptionPane.showMessageDialog(null, e.getLocalizedMessage(), "TimeoutException", JOptionPane.ERROR_MESSAGE);
			server.forceLogout();
		} catch (NoRouteToHostException e ) {
			JOptionPane.showMessageDialog(null, e.getLocalizedMessage(), "NoRouteToHostException", JOptionPane.ERROR_MESSAGE);			
		}
		finally 
		{
			// always enable the menuitem again 
			r[0].getPOP3ServerController().enableActions(true);
		}
	}

	protected void playSound() {
		AccountItem item = server.getAccountItem();
		PopItem popItem = item.getPopItem();
		String file = popItem.get("sound_file");

		if (MainInterface.DEBUG) {
                        ColumbaLogger.log.info("playing sound file=" + file);
                }

		if (file.equalsIgnoreCase("default")) {
			PlaySound.play("newmail.wav");
		} else {
			try {
				PlaySound.play(new URL("file:+"+file));
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
}
