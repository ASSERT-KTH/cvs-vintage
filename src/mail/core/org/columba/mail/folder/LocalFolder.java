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
import org.columba.core.io.DiskIO;
import org.columba.core.logging.ColumbaLogger;
import org.columba.core.xml.XmlElement;
import org.columba.mail.config.FolderItem;
import org.columba.mail.filter.Filter;
import org.columba.mail.filter.FilterList;
import org.columba.mail.message.AbstractMessage;
import org.columba.mail.message.ColumbaHeader;
import org.columba.mail.message.HeaderList;
import org.columba.mail.message.Message;
import org.columba.mail.message.MimePart;
import org.columba.mail.message.MimePartTree;
import org.columba.mail.parser.Rfc822Parser;

public abstract class LocalFolder extends Folder {

	protected int nextMessageUid;
	protected AbstractMessage aktMessage;
	protected DataStorageInterface dataStorage;
	protected AbstractSearchEngine searchEngine;

	public LocalFolder(FolderItem item) {
		super(item);

		XmlElement filterListElement = node.getElement("filterlist");
		if (filterListElement == null) {
			filterListElement = new XmlElement("filterlist");
			getFolderItem().getRoot().addElement(filterListElement);
		}

		filterList = new FilterList(filterListElement);

	} // constructor

	// use this constructor only with tempfolders
	public LocalFolder(String name) {
		super(name);
	} // constructor

	public void removeFolder() throws Exception {
		boolean b = DiskIO.deleteDirectory(directoryFile);

		if (b == true)
			super.removeFolder();
	}

	protected Object generateNextMessageUid() {
		return new Integer(nextMessageUid++);
	}

	public void setNextMessageUid(int next) {
		nextMessageUid = next;
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

		Object newUid = generateNextMessageUid();
		message.setUID(newUid);

		ColumbaLogger.log.debug("new UID=" + newUid);

		String source = message.getSource();

		getDataStorageInstance().saveMessage(source, newUid);

		getMessageFolderInfo().incExists();
		
		getSearchEngineInstance().messageAdded(message);

		return newUid;
	}	

	public Object addMessage(String source, WorkerStatusController worker)
		throws Exception {
		Rfc822Parser parser = new Rfc822Parser();

		ColumbaHeader header = parser.parseHeader(source);
		int size = Math.round(source.length() / 1024);
		header.set("columba.size", new Integer(size));

		AbstractMessage m = new Message(header);
		m.setSource(source);

		return addMessage(m, worker);
	}

	public void removeMessage(Object uid, WorkerStatusController worker)
		throws Exception {
		getDataStorageInstance().removeMessage(uid);
		getSearchEngineInstance().messageRemoved(uid);

		getMessageFolderInfo().decExists();
	}

	public AbstractMessage getMessage(
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
			new Rfc822Parser().parse(source, true, null, 0);
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

	public AbstractSearchEngine getSearchEngineInstance() {
		if (searchEngine == null){
			searchEngine = new LuceneSearchEngine(this);
			//searchEngine = new LocalSearchEngine(this);
		}

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

	/**
	 * @see org.columba.mail.folder.Folder#expungeFolder(java.lang.Object, org.columba.core.command.WorkerStatusController)
	 */
	public void expungeFolder(Object[] uids, WorkerStatusController worker)
		throws Exception {
	}

	/**
	 * @see org.columba.mail.folder.Folder#markMessage(java.lang.Object, int, org.columba.core.command.WorkerStatusController)
	 */
	public void markMessage(
		Object[] uids,
		int variant,
		WorkerStatusController worker)
		throws Exception {
	}

	/**
	 * @see org.columba.mail.folder.Folder#size()
	 */
	public int size() {		
		return getDataStorageInstance().getMessageCount();
	}

	/**
	 * @see org.columba.mail.folder.FolderTreeNode#getDefaultChild()
	 */
	public String getDefaultChild() {
		return null;
	}

}
