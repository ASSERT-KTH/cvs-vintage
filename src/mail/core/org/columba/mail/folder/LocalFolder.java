// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Library General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

package org.columba.mail.folder;

import org.columba.core.command.WorkerStatusController;
import org.columba.core.config.AdapterNode;
import org.columba.core.io.DiskIO;
import org.columba.core.logging.ColumbaLogger;
import org.columba.mail.config.FolderItem;
import org.columba.mail.filter.Filter;
import org.columba.mail.message.AbstractMessage;
import org.columba.mail.message.ColumbaHeader;
import org.columba.mail.message.HeaderInterface;
import org.columba.mail.message.HeaderList;
import org.columba.mail.message.MimePart;
import org.columba.mail.message.MimePartTree;
import org.columba.mail.parser.Rfc822Parser;

public abstract class LocalFolder extends Folder {

	protected int nextUid;
	protected AbstractMessage aktMessage;
	protected DataStorageInterface dataStorage;
	protected LocalSearchEngine searchEngine;

	public LocalFolder(AdapterNode node, FolderItem item) {
		super(node, item);

		nextUid = 0;
	} // constructor

	// use this constructor only with tempfolders
	public LocalFolder(String name) {
		super(name);

		nextUid = 0;
	} // constructor

	public void removeFolder() throws Exception {
		boolean b = DiskIO.deleteDirectory(directoryFile);

		if (b == true)
			super.removeFolder();
	}

	protected Object generateNextUid() {
		return new Integer(nextUid++);
	}

	public void setNextUid(int next) {
		nextUid = next;
	}

	/********************* datastorage methods ***************************/

	public abstract DataStorageInterface getDataStorageInstance();

	public boolean exists(Object uid, WorkerStatusController worker)
		throws Exception {
		return getDataStorageInstance().exists(uid);
	}

	public Object addMessage(
		AbstractMessage message,
		WorkerStatusController worker)
		throws Exception {

		getHeaderList(worker);

		Object newUid = generateNextUid();

		ColumbaLogger.log.debug("new UID=" + newUid);

		String source = message.getSource();

		getDataStorageInstance().saveMessage(source, newUid);

		getMessageFolderInfo().incExists();

		return newUid;
	}

	public Object addMessage(String source, WorkerStatusController worker)
		throws Exception {
		Object newUid = generateNextUid();

		ColumbaLogger.log.debug("new UID=" + newUid);

		getDataStorageInstance().saveMessage(source, newUid);

		getMessageFolderInfo().incExists();

		return newUid;
	}

	public void removeMessage(Object uid, WorkerStatusController worker)
		throws Exception {
		getDataStorageInstance().removeMessage(uid);

		getMessageFolderInfo().decExists();
	}

	protected AbstractMessage getMessage(
		Object uid,
		WorkerStatusController worker)
		throws Exception {
		if (aktMessage != null) {
			if (aktMessage.getUID().equals(uid)) {
				// this message is already cached
				//ColumbaLogger.log.info("using already cached message..");

				return aktMessage;
			}
		}

		String source = getMessageSource(uid, worker);
		//ColumbaHeader h = getMessageHeader(uid, worker);

		AbstractMessage message =
			new Rfc822Parser().parse(source, true, null , 0);
		message.setUID(uid);
		message.setSource(source);
		//message.setHeader(h);

		aktMessage = message;

		return message;
	}

	public MimePart getMimePart(
		Object uid,
		Integer[] address,
		WorkerStatusController worker)
		throws Exception {
		//System.out.println("localfolder->getmimepart");

		AbstractMessage message = getMessage(uid, worker);

		MimePart mimePart = message.getMimePartTree().getFromAddress(address);

		return mimePart;
	}

	public ColumbaHeader getMessageHeader(
		Object uid,
		WorkerStatusController worker)
		throws Exception {
		AbstractMessage message = getMessage(uid, worker);
		ColumbaHeader header = (ColumbaHeader) message.getHeader();

		return header;
	}

	public String getMessageSource(Object uid, WorkerStatusController worker)
		throws Exception {
		String source = getDataStorageInstance().loadMessage(uid);

		return source;
	}

	public MimePartTree getMimePartTree(
		Object uid,
		WorkerStatusController worker)
		throws Exception {

		AbstractMessage message = getMessage(uid, worker);

		MimePartTree mptree = message.getMimePartTree();

		return mptree;
	}

	public HeaderList getHeaderList(WorkerStatusController worker)
		throws Exception {
		return getDataStorageInstance().recreateHeaderList(worker);
	}

	/********************** searching/filtering ***********************/

	public SearchEngineInterface getSearchEngineInstance() {
		if (searchEngine == null)
			searchEngine = new LocalSearchEngine(this);

		return searchEngine;
	}

	public Object[] searchMessages(
		Filter filter,
		Object[] uids,
		WorkerStatusController worker)
		throws Exception {
		return getSearchEngineInstance().searchMessages(filter, uids, worker);
	}

	public Object[] searchMessages(
		Filter filter,
		WorkerStatusController worker)
		throws Exception {
		return getSearchEngineInstance().searchMessages(filter, worker);
	}

}
