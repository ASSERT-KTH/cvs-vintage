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

package org.columba.mail.folder.virtual;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.columba.core.command.WorkerStatusController;
import org.columba.core.config.AdapterNode;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.config.FolderItem;
import org.columba.mail.filter.Filter;
import org.columba.mail.filter.Search;
import org.columba.mail.folder.AbstractLocalSearchEngine;
import org.columba.mail.folder.Folder;
import org.columba.mail.message.AbstractMessage;
import org.columba.mail.message.ColumbaHeader;
import org.columba.mail.message.HeaderInterface;
import org.columba.mail.message.HeaderList;
import org.columba.mail.message.MimePart;
import org.columba.mail.message.MimePartTree;
import org.columba.main.MainInterface;

public class VirtualFolder extends Folder {

	//private MainInterface mainInterface;

	private Search searchFilter;
	protected int nextUid;
	protected HeaderList headerList;

	public VirtualFolder(AdapterNode node, FolderItem item) {
		super(node, item);

		headerList = new HeaderList();
	}

	protected Object generateNextUid() {
		return new Integer(nextUid++);
	}

	public void setNextUid(int next) {
		nextUid = next;
	}

	public boolean exists(Object uid, WorkerStatusController worker) throws Exception {
		return headerList.containsKey(uid);
	}
	
	public HeaderList getHeaderList(WorkerStatusController worker)
		throws Exception {

		headerList.clear();

		applySearch(worker);

		//searchFilter.addSearchToHistory(this);

		return headerList;

	}

	protected void applySearch(WorkerStatusController worker)
		throws Exception {

		int uid = searchFilter.getUid();

		Folder srcFolder = (Folder) MainInterface.treeModel.getFolder(uid);

		boolean result = false;

		applySearch(srcFolder, searchFilter.getFilter(), this, worker);

		VirtualFolder folder =
			(VirtualFolder) MainInterface.treeModel.getFolder(106);

	}

	protected void applySearch(
		Folder parent,
		Filter filter,
		VirtualFolder destFolder,
		WorkerStatusController worker)
		throws Exception {

		Folder folder = parent;

		FolderItem item = null;

		boolean result = false;

		Object[] resultUids = folder.searchMessages(filter, worker);

		for (int i = 0; i < resultUids.length; i++) {
			HeaderInterface header =
				(HeaderInterface) folder.getMessageHeader(
					resultUids[i],
					worker);
			try {
				add((ColumbaHeader) header, folder, resultUids[i]);
			} catch (Exception ex) {
				System.out.println("Search exception: " + ex.getMessage());
				ex.printStackTrace();
			}
		}

		if (searchFilter.isInclude() == true) {
			for (Enumeration e = parent.children(); e.hasMoreElements();) {
				folder = (Folder) e.nextElement();
				if ( folder instanceof VirtualFolder ) continue;
				
				applySearch(folder, filter, this, worker);
			}
		}

	}

	public AbstractLocalSearchEngine getSearchEngine() {
		return null;
	}

	public void setSearchFilter(Search s) {
		searchFilter = s;
	}

	public Search getSearchFilter() {
		return searchFilter;
	}

	public Object getVirtualUid(Folder parent, Object uid) throws Exception {
		for (Enumeration e = headerList.keys(); e.hasMoreElements();) {
			Object virtualUid = e.nextElement();
			VirtualHeader virtualHeader =
				(VirtualHeader) headerList.get(virtualUid);

			Folder srcFolder = virtualHeader.getSrcFolder();
			Object srcUid = virtualHeader.getSrcUid();

			if (srcFolder.equals(parent)) {
				if (srcUid.equals(uid))
					return virtualUid;
			}
		}

		return null;

	}

	public void add(HeaderInterface header, Folder f, Object uid)
		throws Exception {
		Object newUid = generateNextUid();

		//VirtualMessage m = new VirtualMessage(f, uid, index);

		VirtualHeader virtualHeader =
			new VirtualHeader((ColumbaHeader) header, f, uid);
		virtualHeader.set("columba.uid", newUid);
		
		if (header.get("columba.flags.seen").equals(Boolean.FALSE))
			getMessageFolderInfo().incUnseen();
		if (header.get("columba.flags.recent").equals(Boolean.TRUE))
			getMessageFolderInfo().incRecent();
			
		getMessageFolderInfo().incExists();

		headerList.add(virtualHeader, newUid);

	}

	/**
	 * @see org.columba.modules.mail.folder.Folder#expungeFolder(WorkerStatusController)
	 */
	public void expungeFolder(Object[] uids, WorkerStatusController worker) throws Exception {
	}

	/**
	 * @see org.columba.modules.mail.folder.Folder#addMessage(AbstractMessage, WorkerStatusController)
	 */
	public Object addMessage(
		AbstractMessage message,
		WorkerStatusController worker)
		throws Exception {
		return null;
	}

	/**
	 * @see org.columba.modules.mail.folder.Folder#addMessage(String, WorkerStatusController)
	 */

	public Object addMessage(String source, WorkerStatusController worker)
		throws Exception {
		return null;
	}

	/**
	 * @see org.columba.modules.mail.folder.Folder#exists(Object)
	 */
	public boolean exists(Object uid) throws Exception {
		return false;
	}

	/**
	 * @see org.columba.modules.mail.folder.Folder#markMessage(Object[], int, WorkerStatusController)
	 */
	public void markMessage(
		Object[] uids,
		int variant,
		WorkerStatusController worker)
		throws Exception {
	}

	/**
	 * @see org.columba.modules.mail.folder.Folder#removeMessage(Object)
	 */
	public void removeMessage(Object uid, WorkerStatusController worker) throws Exception {
	}

	/**
	 * @see org.columba.modules.mail.folder.Folder#getMimePart(Object, Integer[], WorkerStatusController)
	 */
	public MimePart getMimePart(
		Object uid,
		Integer[] address,
		WorkerStatusController worker)
		throws Exception {

		return null;

	}

	/**
	 * @see org.columba.modules.mail.folder.Folder#getMessageSource(Object, WorkerStatusController)
	 */
	public String getMessageSource(Object uid, WorkerStatusController worker)
		throws Exception {

		return null;
	}

	/**
	 * @see org.columba.modules.mail.folder.Folder#getMimePartTree(Object, WorkerStatusController)
	 */
	public MimePartTree getMimePartTree(
		Object uid,
		WorkerStatusController worker)
		throws Exception {

		return null;
	}

	/**
	 * @see org.columba.modules.mail.folder.Folder#getMessageHeader(Object, WorkerStatusController)
	 */
	public ColumbaHeader getMessageHeader(
		Object uid,
		WorkerStatusController worker)
		throws Exception {

		return null;
	}

	/**
	 * @see org.columba.modules.mail.folder.Folder#getMessage(Object, WorkerStatusController)
	 */
	public AbstractMessage getMessage(
		Object uid,
		WorkerStatusController worker)
		throws Exception {

		return null;

	}

	/**
	 * @see org.columba.modules.mail.folder.Folder#searchMessages(Filter, Object[], WorkerStatusController)
	 */
	public Object[] searchMessages(
		Filter filter,
		Object[] uids,
		WorkerStatusController worker)
		throws Exception {
		return null;
	}

	public Object[] searchMessages(
		Filter filter,
		WorkerStatusController worker)
		throws Exception {
		return null;
	}

	/**
	 * @see org.columba.modules.mail.folder.FolderTreeNode#instanceNewChildNode(AdapterNode, FolderItem)
	 */
	public Folder instanceNewChildNode(AdapterNode node, FolderItem item) {
		return null;
	}

	/**
	 * @see org.columba.modules.mail.folder.FolderTreeNode#getAttributes()
	 */
	public Hashtable getAttributes() {
		return null;
	}

	public FolderCommandReference[] getCommandReference(FolderCommandReference[] r) {

		FolderCommandReference[] newReference = null;

		Object[] uids = r[0].getUids();

		Hashtable list = new Hashtable();

		for (int i = 0; i < uids.length; i++) {
			VirtualHeader virtualHeader =
				(VirtualHeader) headerList.get(uids[i]);
			Folder srcFolder = virtualHeader.getSrcFolder();
			Object srcUid = virtualHeader.getSrcUid();
			//list.put(srcUid, srcFolder);
			if (list.containsKey(srcFolder)) {
				// bucket for this folder exists already
			} else {
				// create new bucket for this folder
				list.put(srcFolder, new Vector());
			}

			Vector v = (Vector) list.get(srcFolder);
			v.add(srcUid);
		}

		for (Enumeration e = list.keys(); e.hasMoreElements();) {
			Folder srcFolder = (Folder) e.nextElement();
			Vector v = (Vector) list.get(srcFolder);

			int size = 1;

			// check if we need a destination folder 
			if (r.length > 1)
				newReference = new FolderCommandReference[2];
			else
				newReference = new FolderCommandReference[1];

			newReference[0] = new FolderCommandReference(srcFolder);
			Object[] uidArray = new Object[v.size()];
			v.copyInto(uidArray);
			newReference[0].setUids(uidArray);
			newReference[0].setMarkVariant(r[0].getMarkVariant());
			newReference[0].setMessage(r[0].getMessage());

			if (r.length > 1)
				newReference[1] =
					new FolderCommandReference((Folder) r[1].getFolder());

		}

		return newReference;

	}
}
