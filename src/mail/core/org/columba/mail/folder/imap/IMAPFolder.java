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

package org.columba.mail.folder.imap;

import java.util.Enumeration;
import java.util.Vector;

import org.columba.core.command.WorkerStatusController;
import org.columba.core.logging.ColumbaLogger;
import org.columba.core.xml.XmlElement;
import org.columba.mail.config.FolderItem;
import org.columba.mail.config.ImapItem;
import org.columba.mail.filter.Filter;
import org.columba.mail.folder.Folder;
import org.columba.mail.folder.FolderTreeNode;
import org.columba.mail.folder.RemoteFolder;
import org.columba.mail.folder.RemoteHeaderCache;
import org.columba.mail.folder.RemoteSearchEngine;
import org.columba.mail.folder.SearchEngineInterface;
import org.columba.mail.folder.command.MarkMessageCommand;
import org.columba.mail.imap.IMAPStore;
import org.columba.mail.imap.parser.IMAPFlags;
import org.columba.mail.message.AbstractMessage;
import org.columba.mail.message.ColumbaHeader;
import org.columba.mail.message.HeaderList;
import org.columba.mail.message.Message;
import org.columba.mail.message.MimePart;
import org.columba.mail.message.MimePartTree;

public class IMAPFolder extends RemoteFolder {

	private ImapItem item;
	//private boolean select=false;
	//private boolean fetch=false;

	//private StringBuffer cache;

	private Object aktMessageUid;
	private Message aktMessage;

	private boolean mailcheck = false;

	private IMAPStore store;

	protected HeaderList headerList;
	//Vector uids;

	protected RemoteHeaderCache cache;

	protected RemoteSearchEngine searchEngine;

	public IMAPFolder(FolderItem folderItem) {
		super(folderItem);

		cache = new RemoteHeaderCache(this);

		
		//setChanged(true);
	}

	public SearchEngineInterface getSearchEngineInstance() {
		if (searchEngine == null)
			searchEngine = new RemoteSearchEngine(this);

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

	public Class getDefaultChild() {
		return IMAPFolder.class;
	}

	public void addFolder(String name) throws Exception {

		String path = getImapPath() + getStore().getDelimiter() + name;

		boolean result = getStore().createFolder(path);

		super.addFolder(name);
	}

	public void removeFolder() throws Exception {

		String path = getImapPath();

		boolean result = getStore().deleteFolder(path);
		if (result == false)
			return;

		super.removeFolder();

	}

	public boolean renameFolder(String name) throws Exception {
		String oldPath = getImapPath();
		ColumbaLogger.log.debug("old path=" + oldPath);

		String newPath = null;
		if (getParent() instanceof IMAPFolder)
			newPath = ((IMAPFolder) getParent()).getImapPath();

		newPath += getStore().getDelimiter() + name;
		ColumbaLogger.log.debug("new path=" + newPath);

		boolean result = getStore().renameFolder(oldPath, newPath);
		if (result == false)
			return false;

		return super.renameFolder(name);

	}

	public FolderTreeNode getRootFolder() {
		FolderTreeNode folderTreeNode = (FolderTreeNode) getParent();
		while (folderTreeNode != null) {
			// System.out.println("name: "+ folder.getName() );
			if (folderTreeNode instanceof IMAPRootFolder) {

				return (IMAPRootFolder) folderTreeNode;
			}

			folderTreeNode = (FolderTreeNode) folderTreeNode.getParent();

		}

		return null;
	}

	public IMAPStore getStore() {
		if (store == null)
			store = ((IMAPRootFolder) getRootFolder()).getStore();

		return store;
	}

	public HeaderList getHeaderList(WorkerStatusController worker)
		throws Exception {

		headerList = cache.getHeaderList(worker);

		worker.setDisplayText("Fetching UID list...");

		Vector newList = getStore().fetchUIDList(worker, getImapPath());

		if (newList == null)
			return new HeaderList();

		Vector result = synchronize(headerList, newList);

		worker.setDisplayText("Fetching FLAGS list...");

		IMAPFlags[] flags = getStore().fetchFlagsList(worker, getImapPath());

		worker.setDisplayText("Fetching header list ");

		// if available -> fetch new headers
		if (result.size() > 0) {
			getStore().fetchHeaderList(
				headerList,
				result,
				worker,
				getImapPath());
		}

		updateFlags(flags);

		return headerList;
	}

	protected void updateFlags(IMAPFlags[] flagsList) {
		for (int i = 0; i < flagsList.length; i++) {
			IMAPFlags flags = (IMAPFlags) flagsList[i];
			ColumbaHeader header =
				(ColumbaHeader) headerList.get(flags.getUid());
			if (header == null)
				continue;

			if (flags.isSeen())
				header.set("columba.flags.seen", Boolean.TRUE);
			else
				getMessageFolderInfo().incUnseen();

			if (flags.isAnswered())
				header.set("columba.flags.answered", Boolean.TRUE);
			if (flags.isDeleted())
				header.set("columba.flags.expunged", Boolean.TRUE);
			if (flags.isFlagged())
				header.set("columba.flags.flagged", Boolean.TRUE);
			if (flags.isRecent()) {
				header.set("columba.flags.recent", Boolean.TRUE);
				getMessageFolderInfo().incRecent();
			}

			getMessageFolderInfo().incExists();
		}
	}

	public void save() {
		try {
			cache.save(null);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public Vector synchronize(HeaderList headerList, Vector newList)
		throws Exception {
		Vector result = new Vector();

		// delete old headers

		for (Enumeration e = headerList.keys(); e.hasMoreElements();) {
			String str = (String) e.nextElement();

			if (existsRemotely(str, 0, newList)) {
				// mail exists on server
				//  -> keep it
			} else {
				// mail doesn't exist on server
				//  -> remove it from local cache
				headerList.remove(str);
			}
		}

		for (int i = 0; i < newList.size(); i++) {
			String str = (String) newList.get(i);

			if (existsLocally(str, headerList) == false) {
				// new message on server
				result.add(str);
			}
		}

		return result;
	}

	protected boolean existsRemotely(
		String uid,
		int startIndex,
		Vector uidList)
		throws Exception {
		for (int i = startIndex; i < uidList.size(); i++) {
			String serverUID = (String) uidList.get(i);

			//System.out.println("server message uid: "+ serverUID );
			if (uid.equals(serverUID)) {
				//System.out.println("local uid exists remotely");
				return true;
			}
		}

		return false;
	}

	protected boolean existsLocally(String uid, HeaderList list)
		throws Exception {

		for (Enumeration e = headerList.keys(); e.hasMoreElements();) {
			String localUID = (String) e.nextElement();

			//System.out.println("server message uid: "+ serverUID );
			if (uid.equals(localUID)) {
				//System.out.println("local uid exists remotely");
				return true;
			}
		}

		return false;
	}

	public MimePartTree getMimePartTree(
		Object uid,
		WorkerStatusController worker)
		throws Exception {

		//System.out.println("------------------->IMAPFolder->getMimePartTree");

		return getStore().getMimePartTree(uid, worker, getImapPath());
	}

	public MimePart getMimePart(
		Object uid,
		Integer[] address,
		WorkerStatusController worker)
		throws Exception {

		//System.out.println("------------------->IMAPFolder->getMimePart");

		return getStore().getMimePart(uid, address, worker, getImapPath());
	}

	public void innerCopy(
		Folder destFolder,
		Object[] uids,
		WorkerStatusController worker)
		throws Exception {

		getStore().copy(
			((IMAPFolder) destFolder).getImapPath(),
			uids,
			worker,
			getImapPath());

	}

	public Object addMessage(
		AbstractMessage message,
		WorkerStatusController worker)
		throws Exception {
		return null;
	}

	public Object addMessage(String source, WorkerStatusController worker)
		throws Exception {

		getStore().append(getImapPath(), source, worker);

		return null;
	}

	public boolean exists(Object uid, WorkerStatusController worker)
		throws Exception {
		return cache.getHeaderList(worker).containsKey(uid);
	}

	protected void markMessage(
		Object uid,
		int variant,
		WorkerStatusController worker)
		throws Exception {
		ColumbaHeader h = (ColumbaHeader) cache.getHeaderList(worker).get(uid);

		switch (variant) {
			case MarkMessageCommand.MARK_AS_READ :
				{
					if (h.get("columba.flags.recent").equals(Boolean.TRUE))
						getMessageFolderInfo().decRecent();
					if (h.get("columba.flags.seen").equals(Boolean.FALSE))
						getMessageFolderInfo().decUnseen();

					h.set("columba.flags.seen", Boolean.TRUE);
					h.set("columba.flags.recent", Boolean.FALSE);
					break;
				}
			case MarkMessageCommand.MARK_AS_FLAGGED :
				{
					h.set("columba.flags.flagged", Boolean.TRUE);
					break;
				}
			case MarkMessageCommand.MARK_AS_EXPUNGED :
				{
					h.set("columba.flags.expunged", Boolean.TRUE);
					break;
				}
			case MarkMessageCommand.MARK_AS_ANSWERED :
				{
					h.set("columba.flags.answered", Boolean.TRUE);
					break;
				}
		}
	}

	public void markMessage(
		Object[] uids,
		int variant,
		WorkerStatusController worker)
		throws Exception {

		getStore().markMessage(uids, variant, worker, getImapPath());

		for (int i = 0; i < uids.length; i++) {
			markMessage(uids[i], variant, worker);
		}

	}

	public void removeMessage(Object uid, WorkerStatusController worker)
		throws Exception {
	}

	public Object[] getUids(WorkerStatusController worker) throws Exception {
		Object[] uids = new Object[headerList.size()];
		int i = 0;
		for (Enumeration e = headerList.keys(); e.hasMoreElements();) {
			Object uid = e.nextElement();
			uids[i++] = uid;
		}

		return uids;
	}

	public void expungeFolder(Object[] uids, WorkerStatusController worker)
		throws Exception {

		boolean result = getStore().expunge(worker, getImapPath());
		if (result == false)
			return;

		Object[] uids2 = getUids(worker);

		for (int i = 0; i < uids.length; i++) {
			Object uid = uids2[i];

			ColumbaHeader h = (ColumbaHeader) headerList.getHeader(uid);

			Boolean expunged = (Boolean) h.get("columba.flags.expunged");

			ColumbaLogger.log.debug("expunged=" + expunged);

			if (expunged.equals(Boolean.TRUE)) {
				// move message to trash

				ColumbaLogger.log.debug(
					"moving message with UID " + uid + " to trash");

				// remove message
				headerList.remove(uid);

			}
		}
	}

	public String getMessageSource(Object uid, WorkerStatusController worker)
		throws Exception {
		return getStore().getMessageSource(uid, worker, getImapPath());
	}

	public ColumbaHeader getMessageHeader(
		Object uid,
		WorkerStatusController worker)
		throws Exception {
		if (headerList == null)
			getHeaderList(worker);
		return (ColumbaHeader) headerList.getHeader((String) uid);
	}

	public AbstractMessage getMessage(
		Object uid,
		WorkerStatusController worker)
		throws Exception {
		return new Message((ColumbaHeader) headerList.getHeader((String) uid));
	}

	public String getImapPath() {
		StringBuffer path = new StringBuffer();
		path.append(getName());
		FolderTreeNode child = this;

		while (true) {
			child = (FolderTreeNode) child.getParent();
			if (child instanceof IMAPRootFolder)
				break;

			String n = ((IMAPFolder) child).getName();

			path.insert(0, n + getStore().getDelimiter());

		}

		return path.toString();
	}

	public void createChildren(WorkerStatusController worker) {

		/*
		try {
			ListInfo[] listInfo = store.lsub("\"\"", "\""+getImapPath()+getStore().getDelimiter()+"*\"");
		
			for (int i = 0; i < listInfo.length; i++) {
				ListInfo info = listInfo[i];
				String name = info.getName();
				System.out.println("creating folder=" + name);
		
				boolean hasChildren = info.hasChildren();
		
				Hashtable attributes = getAttributes();
				attributes.put("name", listInfo[i].getSource());
				addSubFolder(attributes);
		
				
			}
		} catch (Exception ex) {
		}
		*/
	}

	/**
	 * @see org.columba.mail.folder.FolderTreeNode#getDefaultProperties()
	 */
	public static XmlElement getDefaultProperties() {
		XmlElement props = new XmlElement("property");
		props.addAttribute("accessrights", "user");
		props.addAttribute("subfolder", "true");

		return props;
	}

	/**
	 * @see org.columba.mail.folder.FolderTreeNode#tryToGetLock(java.lang.Object)
	 */
	public boolean tryToGetLock(Object locker) {
		// IMAP Folders have no own lock ,but share the lock from the Root
		// to ensure that only one operation can be processed simultanous		

		return getRootFolder().tryToGetLock(locker);
	}

	/**
	 * @see org.columba.mail.folder.FolderTreeNode#releaseLock()
	 */
	public void releaseLock() {
		getRootFolder().releaseLock();
	}

}