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

import java.util.List;

import org.columba.core.command.Command;
import org.columba.core.command.CommandCancelledException;
import org.columba.core.command.CompoundCommand;
import org.columba.core.command.DefaultCommandReference;
import org.columba.core.command.Worker;
import org.columba.core.command.WorkerStatusController;
import org.columba.core.logging.ColumbaLogger;
import org.columba.core.main.MainInterface;
import org.columba.mail.command.POP3CommandReference;
import org.columba.mail.filter.Filter;
import org.columba.mail.filter.FilterList;
import org.columba.mail.folder.Folder;
import org.columba.mail.gui.frame.TableUpdater;
import org.columba.mail.gui.table.TableChangedEvent;
import org.columba.mail.message.HeaderInterface;
import org.columba.mail.message.Message;
import org.columba.mail.pop3.POP3Server;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class FetchNewMessagesCommand extends Command {

	POP3Server server;
	int totalMessageCount;
	int newMessageCount;

	/**
	 * Constructor for FetchNewMessages.
	 * @param frameController
	 * @param references
	 */
	public FetchNewMessagesCommand(DefaultCommandReference[] references) {
		super(references);

		POP3CommandReference[] r =
			(POP3CommandReference[]) getReferences(FIRST_EXECUTION);

		server = r[0].getServer();
	}

	/**
	 * @see org.columba.core.command.Command#execute(Worker)
	 */
	public void execute(Worker worker) throws Exception {
		POP3CommandReference[] r =
			(POP3CommandReference[]) getReferences(FIRST_EXECUTION);

		server = r[0].getServer();

		log("Authenticating...", worker);

		totalMessageCount = server.getMessageCount(worker);

		try {
			// fetch UID list from server
			List newUIDList = fetchUIDList(totalMessageCount, worker);

			// fetch message size list from server
			List messageSizeList = fetchMessageSizes(worker);

			// synchronize local UID list with server UID list
			List newMessagesUIDList = synchronize(newUIDList);

			// only download new messages
			downloadNewMessages(
				newUIDList,
				messageSizeList,
				newMessagesUIDList,
				worker);

			logout(worker);

		} catch (CommandCancelledException e) {
			server.forceLogout();
		}
		
		r[0].getPOP3ServerController().enableActions(true);

	}

	protected void log(String message, WorkerStatusController worker) {
		worker.setDisplayText(server.getFolderName() + ": " + message);
	}

	public void downloadMessage(
		int index,
		int size,
		Object serverUID,
		Worker worker)
		throws Exception {
		// server message numbers start with 1
		// whereas List numbers start with 0
		//  -> always increase fetch number
		Message message = server.getMessage(index + 1, serverUID, worker);
		if (message == null)
			throw new Exception(
				"Message with UID="
					+ serverUID
					+ " and index="
					+ (index + 1)
					+ " isn't on the server.");

		message.getHeader().set(
			"columba.size",
			new Integer(Math.round(size / 1024)));
		message.getHeader().set("columba.flags.seen", Boolean.FALSE);
		//System.out.println("message:\n" + message.getSource());

		// get inbox-folder from pop3-server preferences
		Folder inboxFolder = server.getFolder();
		Object uid = inboxFolder.addMessage(message, worker);
		Object[] uids = new Object[1];
		uids[0] = uid;

		HeaderInterface[] headerList = new HeaderInterface[1];
		headerList[0] = message.getHeader();
		headerList[0].set("columba.uid", uid);

		// update table-viewer
		TableChangedEvent ev =
			new TableChangedEvent(
				TableChangedEvent.ADD,
				inboxFolder,
				headerList);

		TableUpdater.tableChanged(ev);

		// apply filter on message
		FilterList list = inboxFolder.getFilterList();
		for (int j = 0; j < list.count(); j++) {
			Filter filter = list.get(j);

			Object[] result = inboxFolder.searchMessages(filter, uids, worker);
			if (result.length != 0) {
				CompoundCommand command =
					filter.getCommand(inboxFolder, result);

				MainInterface.processor.addOp(command);
			}

		}
	}

	protected int calculateTotalSize(
		List newUIDList,
		List messageSizeList,
		List newMessagesUIDList) {
		int totalSize = 0;

		for (int i = 0; i < newMessagesUIDList.size(); i++) {
			Object serverUID = newMessagesUIDList.get(i);

			//ColumbaLogger.log.info("fetch message with UID=" + serverUID);

			//int index = ( (Integer) result.get(serverUID) ).intValue();
			int index = newUIDList.indexOf(serverUID);
			//ColumbaLogger.log.info("List index=" + index + " server index=" + (index + 1));

			int size = Integer.parseInt((String) messageSizeList.get(index));
			//size = Math.round(size / 1024);

			totalSize += size;
		}

		return totalSize;
	}

	public void downloadNewMessages(
		List newUIDList,
		List messageSizeList,
		List newMessagesUIDList,
		Worker worker)
		throws Exception {
		if (MainInterface.DEBUG) {
			ColumbaLogger.log.info(
				"need to fetch " + newMessagesUIDList.size() + " messages.");
		}
		int totalSize =
			calculateTotalSize(newUIDList, messageSizeList, newMessagesUIDList);

		worker.setProgressBarMaximum(totalSize);
		worker.setProgressBarValue(0);

		newMessageCount = newMessagesUIDList.size();
		for (int i = 0; i < newMessageCount; i++) {
			// which UID should be downloaded next
			Object serverUID = newMessagesUIDList.get(i);

			if (MainInterface.DEBUG) {
				ColumbaLogger.log.info("fetch message with UID=" + serverUID);
			}

			log(
				"Fetching " + (i + 1) + "/" + newMessageCount + " messages...",
				worker);

			// lookup index of message 
			int index = newUIDList.indexOf(serverUID);
			if (MainInterface.DEBUG)
				ColumbaLogger.log.info(
					"List index=" + index + " server index=" + (index + 1));

			int size = Integer.parseInt((String) messageSizeList.get(index));
			size = Math.round(size / 1024);

			if (server
				.getAccountItem()
				.getPopItem()
				.getBoolean("enable_limit")) {
				// check if message isn't too big to download
				int maxSize =
					server.getAccountItem().getPopItem().getInteger("limit");

				// if message-size is bigger skip download of this message
				if (size > maxSize) {
					if (MainInterface.DEBUG) {
						ColumbaLogger.log.info(
							"skipping download of message, too big");
					}
					continue;
				}
			}

			// now download the message
			downloadMessage(
				index,
				Integer.parseInt((String) messageSizeList.get(index)),
				serverUID,
				worker);

			if (server
				.getAccountItem()
				.getPopItem()
				.getBoolean("leave_messages_on_server")
				== false) {
				// delete message from server

				/*
				// remove UID from server list
				boolean remove = newUIDList.remove(serverUID);
				*/
				
				// server message numbers start with 1
				// whereas List numbers start with 0
				//  -> always increase delete number

				// delete message with <index>==index from server
				server.deleteMessage(index + 1, worker);

				if (MainInterface.DEBUG) {
					ColumbaLogger.log.info(
						"deleted message with index=" + (index + 1));
				}
			}
		}

	}

	public List synchronize(List newUIDList) throws Exception {
		if (MainInterface.DEBUG) {
			ColumbaLogger.log.info(
				"synchronize local UID-list with remote UID-list");
		}
		// synchronize local UID-list with server 		
		List newMessagesUIDList = server.synchronize(newUIDList);

		return newMessagesUIDList;
	}

	public List fetchMessageSizes(WorkerStatusController worker)
		throws Exception {

		log("Fetching message size list...", worker);
		// fetch message-size list 		
		List messageSizeList = server.getMessageSizeList(worker);
		if (MainInterface.DEBUG) {
			ColumbaLogger.log.info(
				"fetched message-size-list capacity=" + messageSizeList.size());
		}
		return messageSizeList;

	}

	public List fetchUIDList(
		int totalMessageCount,
		WorkerStatusController worker)
		throws Exception {
		// fetch UID list 

		log("Fetch UID list...", worker);

		List newUIDList = server.getUIDList(totalMessageCount, worker);
		if (MainInterface.DEBUG) {
			ColumbaLogger.log.info(
				"fetched UID-list capacity=" + newUIDList.size());
		}

		return newUIDList;
	}

	public void logout(WorkerStatusController worker) throws Exception {
		server.logout();

		if (MainInterface.DEBUG) {
			ColumbaLogger.log.info("logout");
		}

		log("Logout...", worker);

		if (newMessageCount == 0)
			log("No new messages on server", worker);
	}
}
