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
package org.columba.mail.pop3;

import java.io.File;
import java.util.Enumeration;
import java.util.Vector;

import org.columba.core.command.WorkerStatusController;
import org.columba.core.config.Config;
import org.columba.core.logging.ColumbaLogger;
import org.columba.mail.config.AccountItem;
import org.columba.mail.config.PopItem;
import org.columba.mail.config.SpecialFoldersItem;
import org.columba.mail.folder.Folder;
import org.columba.mail.message.ColumbaHeader;
import org.columba.mail.message.HeaderList;
import org.columba.mail.message.Message;
import org.columba.mail.pop3.protocol.POP3Protocol;
import org.columba.core.main.MainInterface;

public class POP3Server {

	private AccountItem accountItem;

	private File file;

	public POP3Protocol pop3Connection;

	private boolean alreadyLoaded;

	private POP3Store store;

	protected POP3HeaderCache headerCache;

	public POP3Server(AccountItem accountItem) {
		this.accountItem = accountItem;

		int uid = accountItem.getUid();

		file =
			new File(
				Config.pop3Directory,
				(new Integer(uid)).toString());

		PopItem item = accountItem.getPopItem();

		store = new POP3Store(item);

		headerCache = new POP3HeaderCache(this);

	}

	public void save() throws Exception {
		headerCache.save();
	}

	public File getConfigFile() {
		return file;
	}

	public AccountItem getAccountItem() {
		return accountItem;
	}

	public Folder getFolder() {
		SpecialFoldersItem foldersItem = accountItem.getSpecialFoldersItem();
		String inboxStr = foldersItem.get("inbox");
		System.out.println("inbox-string=" + inboxStr);

		int inboxInt = Integer.parseInt(inboxStr);

		Folder f = (Folder) MainInterface.treeModel.getFolder(inboxInt);
		System.out.println("f=" + f);

		return f;
	}

	public void logout() throws Exception {
		getStore().logout();
	}

	public void forceLogout() throws Exception {
		getStore().close();
	}

	public Vector getUIDList(int totalMessageCount, WorkerStatusController worker) throws Exception {
		return getStore().fetchUIDList(totalMessageCount, worker);
	}

	public Vector getMessageSizeList(WorkerStatusController worker) throws Exception {
		return getStore().fetchMessageSizeList(worker);
	}

	protected boolean existsLocally(Object uid, HeaderList list)
		throws Exception {

		for (Enumeration e = headerCache.getHeaderList().keys();
			e.hasMoreElements();
			) {
			Object localUID = e.nextElement();

			//System.out.println("local message uid: " + localUID);
			if (uid.equals(localUID)) {
				//System.out.println("remote uid exists locally");
				return true;
			}
		}

		return false;
	}

	protected boolean existsRemotely(Object uid, Vector uidList)
		throws Exception {
		for (int i = 0; i < uidList.size(); i++) {
			Object serverUID = uidList.get(i);

			//System.out.println("server message uid: " + serverUID);
			if (uid.equals(serverUID)) {
				//System.out.println("local uid exists remotely");
				return true;
			}
		}

		return false;
	}

	public Vector synchronize(Vector newList) throws Exception {
		Vector result = new Vector();
		HeaderList headerList = headerCache.getHeaderList();

		for (Enumeration e = headerList.keys(); e.hasMoreElements();) {
			String str = (String) e.nextElement();

			if (existsRemotely(str, newList) == true) {
				// mail exists on server
				//  -> keep it
			} else {
				// mail doesn't exist on server
				//  -> remove it from local cache
				ColumbaLogger.log.debug("remove uid=" + str);
				headerList.remove(str);
			}
		}

		for (int i = 0; i < newList.size(); i++) {
			Object str = newList.get(i);

			if (existsLocally(str, headerList) == false) {
				// new message on server

				result.add(str);
				ColumbaLogger.log.debug("adding uid=" + str);
				//System.out.println("download:" + str);

			}
		}

		return result;
	}

	public void deleteMessages(int[] indexes, WorkerStatusController worker) throws Exception {
		for (int i = 0; i < indexes.length; i++) {
			store.deleteMessage(indexes[i], worker);
		}
	}
	
	public int getMessageCount(WorkerStatusController worker) throws Exception
	{
		return getStore().fetchMessageCount(worker);
	}

	public Message getMessage(int index, Object uid, WorkerStatusController worker) throws Exception {
		Message message = getStore().fetchMessage(index, worker);

		ColumbaHeader header = (ColumbaHeader) message.getHeader();
		header.set("columba.pop3uid", uid);
		header.set("columba.flags.recent", Boolean.TRUE);

		headerCache.getHeaderList().add(header, uid);

		return message;
	}

	public String getFolderName() {
		return accountItem.getName();
	}

	/**
	 * Returns the store.
	 * @return POP3Store
	 */
	public POP3Store getStore() {
		return store;
	}

}
