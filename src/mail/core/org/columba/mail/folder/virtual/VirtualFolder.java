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

package org.columba.mail.folder.virtual;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.JDialog;

import org.columba.core.command.WorkerStatusController;
import org.columba.core.xml.XmlElement;
import org.columba.mail.config.FolderItem;
import org.columba.mail.filter.Filter;
import org.columba.mail.filter.FilterCriteria;
import org.columba.mail.folder.AbstractFolder;
import org.columba.mail.folder.FolderFactory;
import org.columba.mail.folder.HeaderListStorage;
import org.columba.mail.folder.MailboxInterface;
import org.columba.mail.folder.MessageFolder;
import org.columba.mail.folder.headercache.CachedHeaderfields;
import org.columba.mail.folder.imap.IMAPFolder;
import org.columba.mail.folder.search.DefaultSearchEngine;
import org.columba.mail.gui.config.search.SearchFrame;
import org.columba.mail.gui.frame.AbstractMailFrameController;
import org.columba.mail.gui.tree.TreeModel;
import org.columba.mail.message.ColumbaHeader;
import org.columba.mail.message.HeaderList;
import org.columba.ristretto.message.Attributes;
import org.columba.ristretto.message.Flags;
import org.columba.ristretto.message.Header;
import org.columba.ristretto.message.MimePart;
import org.columba.ristretto.message.MimeTree;

/**
 * Virtual folder presenting search results and saving only references to
 * messages of "real" folders.
 * <p>
 * Almost all methods don't do anything here, because the we pass all operatins
 * to the source folders. This happens on the Command and CommandReference
 * abstraction level.
 * <p>
 * 
 * @author fdietz
 *  
 */
public class VirtualFolder extends MessageFolder {
	protected int nextUid;

	protected HeaderList headerList;

	//TODO (@author fdietz): Reduce redundancy in both constructors, eventually create
	// private method that cares for the missing XmlElement.
	public VirtualFolder(FolderItem item, String path) {
		super(item, path);

		headerList = new HeaderList();

		XmlElement filterElement = node.getElement("filter");

		if (filterElement == null) {
			/*
			 * filterElement = new XmlElement("filter");
			 */
			XmlElement filter = new XmlElement("filter");
			filter.addAttribute("description", "new filter");
			filter.addAttribute("enabled", "true");

			XmlElement rules = new XmlElement("rules");
			rules.addAttribute("condition", "matchall");

			XmlElement criteria = new XmlElement("criteria");
			criteria.addAttribute("type", "Subject");
			criteria.addAttribute("headerfield", "Subject");
			criteria.addAttribute("criteria", "contains");
			criteria.addAttribute("pattern", "pattern");
			rules.addElement(criteria);
			filter.addElement(rules);

			Filter f = new Filter(filter);

			getConfiguration().getRoot().addElement(f.getRoot());
		}

		//searchFilter = new Search(this);
	}

	public VirtualFolder(String name, String type, String path) {
		super(name, type, path);

		FolderItem item = getConfiguration();
		item.set("property", "accessrights", "user");
		item.set("property", "subfolder", "true");
		item.set("property", "include_subfolders", "true");
		item.set("property", "source_uid", "101");

		headerList = new HeaderList();

		XmlElement filterElement = node.getElement("filter");

		if (filterElement == null) {
			/*
			 * filterElement = new XmlElement("filter");
			 */
			XmlElement filter = new XmlElement("filter");
			filter.addAttribute("description", "new filter");
			filter.addAttribute("enabled", "true");

			XmlElement rules = new XmlElement("rules");
			rules.addAttribute("condition", "matchall");

			XmlElement criteria = new XmlElement("criteria");
			criteria.addAttribute("type", "Subject");
			criteria.addAttribute("headerfield", "Subject");
			criteria.addAttribute("criteria", "contains");
			criteria.addAttribute("pattern", "pattern");
			rules.addElement(criteria);
			filter.addElement(rules);

			Filter f = new Filter(filter);

			getConfiguration().getRoot().addElement(f.getRoot());
		}
	}

	protected Object generateNextUid() {
		return new Integer(nextUid++);
	}

	public void setNextUid(int next) {
		nextUid = next;
	}

	public JDialog showFilterDialog(AbstractMailFrameController frameController) {
		return new SearchFrame(frameController, this);
	}

	public boolean exists(Object uid) throws Exception {
		return headerList.containsKey(uid);
	}

	public HeaderList getHeaderList() throws Exception {
		headerList.clear();
		getMessageFolderInfo().reset();

		applySearch();

		return headerList;
	}

	public void addSearchToHistory() throws Exception {
		VirtualFolder searchFolder = (VirtualFolder) TreeModel.getInstance()
				.getFolder(106);

		// only create new subfolders if we used the default "Search Folder"
		if (!searchFolder.equals(this)) {
			return;
		}

		// we only want 10 subfolders
		// -> if more children exist remove them
		if (searchFolder.getChildCount() >= 10) {
			MessageFolder child = (MessageFolder) searchFolder.getChildAt(0);
			child.removeFromParent();
		}

		// create new subfolder
		String name = "search result";
		VirtualFolder newFolder = null;

		try {
			newFolder = (VirtualFolder) FolderFactory.getInstance()
					.createChild(searchFolder, name, "VirtualFolder");
		} catch (Exception ex) {
			ex.printStackTrace();

			return;
		}

		// if creation failed
		if (newFolder == null) {
			return;
		}

		// copy all properties to the subfolder
		int uid = getConfiguration().getInteger("property", "source_uid");
		boolean includes = getConfiguration().getBoolean("property",
				"include_subfolders");

		FolderItem newFolderItem = newFolder.getConfiguration();
		newFolderItem.set("property", "source_uid", uid);
		newFolderItem.set("property", "include_subfolders", includes);

		newFolderItem.getElement("filter").removeFromParent();
		newFolderItem.getRoot().addElement(
				(XmlElement) getConfiguration().getElement("filter").clone());

		FilterCriteria newc = new Filter(getConfiguration()
				.getElement("filter")).getFilterRule().get(0);

		/*
		 * FilterCriteria c = new
		 * Filter(getConfiguration().getElement("filter")) .getFilterRule()
		 * .get( 0);
		 * 
		 * FilterCriteria newc = new
		 * Filter(getConfiguration().getElement("filter")) .getFilterRule()
		 * .get( 0); newc.setCriteria(c.getCriteriaString());
		 * newc.setHeaderItem(c.getHeaderItemString());
		 * newc.setPattern(c.getPattern()); newc.setType(c.getType());
		 */

		// lets find a good name for our new vfolder
		StringBuffer buf = new StringBuffer();

		if (newc.getType().equalsIgnoreCase("flags")) {
			System.out.println("flags found");

			buf.append(newc.getType());
			buf.append(" (");
			buf.append(newc.getCriteriaString());
			buf.append(" ");
			buf.append(newc.getPattern());
			buf.append(")");
		} else if (newc.getType().equalsIgnoreCase("custom headerfield")) {
			buf.append(newc.getHeaderItemString());
			buf.append(" (");
			buf.append(newc.getCriteriaString());
			buf.append(" ");
			buf.append(newc.getPattern());
			buf.append(")");
		} else {
			buf.append(newc.getType());
			buf.append(" (");
			buf.append(newc.getCriteriaString());
			buf.append(" ");
			buf.append(newc.getPattern());
			buf.append(")");
		}

		newFolder.setName(buf.toString());

		// update tree-view
		TreeModel.getInstance().nodeStructureChanged(searchFolder);

		// update tree-node (for renaming the new folder)
		TreeModel.getInstance().nodeChanged(newFolder);
	}

	protected void applySearch() throws Exception {
		int uid = getConfiguration().getInteger("property", "source_uid");
		MessageFolder srcFolder = (MessageFolder) TreeModel.getInstance()
				.getFolder(uid);

		XmlElement filter = getConfiguration().getRoot().getElement("filter");

		if (filter == null) {
			filter = new XmlElement("filter");
			filter.addAttribute("description", "new filter");
			filter.addAttribute("enabled", "true");

			XmlElement rules = new XmlElement("rules");
			rules.addAttribute("condition", "match_all");

			XmlElement criteria = new XmlElement("criteria");
			criteria.addAttribute("type", "Subject");
			criteria.addAttribute("headerfield", "Subject");
			criteria.addAttribute("criteria", "contains");
			criteria.addAttribute("pattern", "pattern");
			rules.addElement(criteria);
			filter.addElement(rules);
			getConfiguration().getRoot().addElement(filter);
		}

		Filter f = new Filter(getConfiguration().getRoot().getElement("filter"));

		applySearch(srcFolder, f);

		VirtualFolder folder = (VirtualFolder) TreeModel.getInstance()
				.getFolder(106);
	}

	protected void applySearch(MessageFolder parent, Filter filter)
			throws Exception {
		MessageFolder folder = parent;

		Object[] resultUids = folder.searchMessages(filter);
		String[] headerfields = CachedHeaderfields.getCachedHeaderfields();

		if (resultUids != null) {
			for (int i = 0; i < resultUids.length; i++) {
				ColumbaHeader header = null;

				if (folder instanceof VirtualFolder) {
					//	get source folder reference
					VirtualHeader virtualHeader = ((VirtualFolder) folder)
							.getVirtualHeader(resultUids[i]);
					MessageFolder sourceFolder = virtualHeader.getSrcFolder();
					Object sourceUid = virtualHeader.getSrcUid();

					Header h = sourceFolder.getHeaderFields(sourceUid,
							headerfields);
					header = new ColumbaHeader(h);
					header.setAttributes(sourceFolder.getAttributes(sourceUid));
					header.setFlags(sourceFolder.getFlags(sourceUid));
					add((ColumbaHeader) header, sourceFolder, sourceUid);
				} else {
					Header h = folder.getHeaderFields(resultUids[i],
							headerfields);
					header = new ColumbaHeader(h);
					header.setAttributes(folder.getAttributes(resultUids[i]));
					header.setFlags(folder.getFlags(resultUids[i]));
					add((ColumbaHeader) header, folder, resultUids[i]);
				}

			}
		}

		boolean isInclude = Boolean.valueOf(
				getConfiguration().get("property", "include_subfolders"))
				.booleanValue();

		if (isInclude) {
			for (Enumeration e = parent.children(); e.hasMoreElements();) {
				folder = (MessageFolder) e.nextElement();

				if (folder instanceof VirtualFolder) {
					continue;
				}

				applySearch(folder, filter);
			}
		}
	}

	public DefaultSearchEngine getSearchEngine() {
		return null;
	}

	public Filter getFilter() {
		return new Filter(getConfiguration().getRoot().getElement("filter"));
	}

	public void add(ColumbaHeader header, MessageFolder f, Object uid)
			throws Exception {
		Object newUid = generateNextUid();

		//VirtualMessage m = new VirtualMessage(f, uid, index);
		VirtualHeader virtualHeader = new VirtualHeader((ColumbaHeader) header,
				f, uid);
		//virtualHeader.set("columba.uid", newUid);
		virtualHeader.setVirtualUid(newUid);

		if (!header.getFlags().getSeen()) {
			getMessageFolderInfo().incUnseen();
		}

		if (header.getFlags().getRecent()) {
			getMessageFolderInfo().incRecent();
		}

		getMessageFolderInfo().incExists();

		headerList.add(virtualHeader, newUid);
	}

	/**
	 * @see org.columba.modules.mail.folder.Folder#markMessage(Object[], int,
	 *      IMAPFolder)
	 */
	public void markMessage(Object[] uids, int variant) throws Exception {
		for (int i = 0; i < uids.length; i++) {
			// get source folder reference
			VirtualHeader h = (VirtualHeader) headerList.get(uids[i]);
			MessageFolder sourceFolder = h.getSrcFolder();
			Object sourceUid = h.getSrcUid();

			// virtual folder: update mailfolderinfo -> fire treenode change
			updateMailFolderInfo(uids[i], variant);
			// virtual folder: fire message flag changed
			fireMessageFlagChanged(uids[i]);

			// mark message in source folder
			sourceFolder.markMessage(new Object[] { sourceUid }, variant);

		}
	}

	/**
	 * @see org.columba.modules.mail.folder.Folder#removeMessage(Object)
	 */
	public void removeMessage(Object uid) throws Exception {
		//		 notify listeners
		fireMessageRemoved(uid, getFlags(uid));

		// get source folder reference
		VirtualHeader h = (VirtualHeader) headerList.get(uid);
		MessageFolder sourceFolder = h.getSrcFolder();
		Object sourceUid = h.getSrcUid();

		// remove from source folder
		sourceFolder.removeMessage(sourceUid);

		//		 remove from virtual folder
		headerList.remove(uid);
	}

	/**
	 * @see org.columba.modules.mail.folder.Folder#getMimePart(Object,
	 *      Integer[], WorkerStatusController)
	 * @TODO dont use deprecated method
	 */
	public MimePart getMimePart(Object uid, Integer[] address) throws Exception {

		VirtualHeader h = (VirtualHeader) headerList.get(uid);
		MessageFolder sourceFolder = h.getSrcFolder();
		Object sourceUid = h.getSrcUid();

		return sourceFolder.getMimePart(sourceUid, address);
	}

	/**
	 * @see org.columba.modules.mail.folder.Folder#getMimeTree(Object,
	 *      IMAPFolder)
	 */
	public MimeTree getMimePartTree(Object uid) throws Exception {

		VirtualHeader h = (VirtualHeader) headerList.get(uid);
		MessageFolder sourceFolder = h.getSrcFolder();
		Object sourceUid = h.getSrcUid();

		return sourceFolder.getMimePartTree(sourceUid);
	}

	/**
	 * @see org.columba.modules.mail.folder.Folder#getMessageHeader(Object,
	 *      WorkerStatusController)
	 * @TODO dont use deprecated method
	 */
	public ColumbaHeader getMessageHeader(Object uid) throws Exception {
		VirtualHeader h = (VirtualHeader) headerList.get(uid);
		MessageFolder sourceFolder = h.getSrcFolder();
		Object sourceUid = h.getSrcUid();

		return (ColumbaHeader) sourceFolder.getMessageHeader(sourceUid);
	}

	/**
	 * Get virtual header.
	 * 
	 * @param virtualUid
	 *            virtual uid
	 * @return virtual header
	 */
	public VirtualHeader getVirtualHeader(Object virtualUid) {
		return (VirtualHeader) headerList.get(virtualUid);
	}

	/**
	 * @see org.columba.modules.mail.folder.Folder#searchMessages(Filter,
	 *      Object[], WorkerStatusController)
	 */
	public Object[] searchMessages(Filter filter, Object[] uids)
			throws Exception {

		List list = new ArrayList();

		for (int i = 0; i < uids.length; i++) {
			// get source folder reference
			VirtualHeader h = (VirtualHeader) headerList.get(uids[i]);
			MessageFolder sourceFolder = h.getSrcFolder();
			Object sourceUid = h.getSrcUid();

			Object[] result = sourceFolder.searchMessages(filter,
					new Object[] { sourceUid });

			if ((result != null) && (result.length > 0)) {
				list.add(h.getVirtualUid());
			}
		}

		return list.toArray();
	}

	public Object[] searchMessages(Filter filter) throws Exception {
		Object[] uids = getUids();

		return searchMessages(filter, uids);
	}

	/**
	 * @see org.columba.modules.mail.folder.FolderTreeNode#instanceNewChildNode(AdapterNode,
	 *      FolderItem)
	 */
	public String getDefaultChild() {
		return null;
	}

	public static XmlElement getDefaultProperties() {
		XmlElement props = new XmlElement("property");
		props.addAttribute("accessrights", "user");
		props.addAttribute("subfolder", "true");
		props.addAttribute("include_subfolders", "true");
		props.addAttribute("source_uid", "101");

		return props;
	}

	/*
	 * public FolderCommandReference getCommandReference( FolderCommandReference
	 * r) { FolderCommandReference[] newReference = null;
	 * 
	 * Object[] uids = r[0].getUids(); // if we didn't pass uid array here, use
	 * all message in this virtual // folder try { if (uids == null) { uids =
	 * getUids(); } } catch (Exception e1) { e1.printStackTrace(); }
	 * 
	 * if (uids == null) { return r; }
	 * 
	 * Hashtable list = new Hashtable();
	 * 
	 * for (int i = 0; i < uids.length; i++) { VirtualHeader virtualHeader =
	 * (VirtualHeader) headerList .get(uids[i]); MessageFolder srcFolder =
	 * virtualHeader.getSrcFolder(); Object srcUid = virtualHeader.getSrcUid();
	 * 
	 * if (list.containsKey(srcFolder)) { // bucket for this folder exists
	 * already } else { // create new bucket for this folder list.put(srcFolder,
	 * new Vector()); }
	 * 
	 * List v = (Vector) list.get(srcFolder); v.add(srcUid); }
	 * 
	 * newReference = new FolderCommandReference[list.size() + 2];
	 * 
	 * int i = 0;
	 * 
	 * for (Enumeration e = list.keys(); e.hasMoreElements();) { MessageFolder
	 * srcFolder = (MessageFolder) e.nextElement(); List v = (Vector)
	 * list.get(srcFolder);
	 * 
	 * newReference[i] = new FolderCommandReference(srcFolder);
	 * 
	 * Object[] uidArray = new Object[v.size()]; ((Vector)
	 * v).copyInto(uidArray); newReference[i].setUids(uidArray);
	 * newReference[i].setMarkVariant(r[0].getMarkVariant());
	 * newReference[i].setMessage(r[0].getMessage());
	 * newReference[i].setDestFile(r[0].getDestFile());
	 * 
	 * i++; }
	 * 
	 * if (r.length > 1) { newReference[i] = new
	 * FolderCommandReference((MessageFolder) r[1] .getFolder()); } else {
	 * newReference[i] = null; }
	 * 
	 * newReference[i + 1] = r[0];
	 * 
	 * return newReference; }
	 */

	/**
	 * @see org.columba.mail.folder.FolderTreeNode#tryToGetLock(java.lang.Object)
	 */
	public boolean tryToGetLock(Object locker) {

		//return super.tryToGetLock(locker);
		Object[] uids = null;
		try {
			uids = getUids();
		} catch (Exception e) {

			e.printStackTrace();
			return false;
		}

		boolean success = true;
		// try to get locks of all folders
		for (int i = 0; i < uids.length; i++) {
			VirtualHeader h = (VirtualHeader) headerList.get(uids[i]);

			MessageFolder sourceFolder = h.getSrcFolder();
			Object sourceUid = h.getSrcUid();
			success &= sourceFolder.tryToGetLock(locker);

		}

		// if this failed
		if (!success) {
			// release all folder locks
			for (int i = 0; i < uids.length; i++) {
				VirtualHeader h = (VirtualHeader) headerList.get(uids[i]);
				MessageFolder sourceFolder = h.getSrcFolder();
				sourceFolder.releaseLock(locker);

			}
		}

		return success;
	}

	/**
	 * @see org.columba.mail.folder.AbstractFolder#releaseLock(java.lang.Object)
	 */
	public void releaseLock(Object locker) {

		//super.releaseLock(locker);
		Object[] uids = null;
		try {
			uids = getUids();
		} catch (Exception e) {

			e.printStackTrace();
			return;
		}

		//		 release all folder locks
		for (int i = 0; i < uids.length; i++) {
			VirtualHeader h = (VirtualHeader) headerList.get(uids[i]);
			MessageFolder sourceFolder = h.getSrcFolder();
			sourceFolder.releaseLock(locker);

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.columba.mail.folder.Folder#getUids(org.columba.core.command.WorkerStatusController)
	 */
	public Object[] getUids() throws Exception {
		int count = headerList.count();
		Object[] uids = new Object[count];
		int i = 0;

		for (Enumeration e = headerList.keys(); e.hasMoreElements();) {
			uids[i++] = e.nextElement();
		}

		return uids;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.columba.mail.folder.MailboxInterface#addMessage(java.io.InputStream)
	 */
	public Object addMessage(InputStream in) throws Exception {
		// not supported
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.columba.mail.folder.MailboxInterface#getAttribute(java.lang.Object,
	 *      java.lang.String)
	 */
	public Object getAttribute(Object uid, String key) throws Exception {
		VirtualHeader h = (VirtualHeader) headerList.get(uid);
		MessageFolder sourceFolder = h.getSrcFolder();
		Object sourceUid = h.getSrcUid();

		return sourceFolder.getAttribute(sourceUid, key);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.columba.mail.folder.MailboxInterface#getFlags(java.lang.Object)
	 */
	public Flags getFlags(Object uid) throws Exception {

		VirtualHeader h = (VirtualHeader) headerList.get(uid);
		MessageFolder sourceFolder = h.getSrcFolder();
		Object sourceUid = h.getSrcUid();

		return sourceFolder.getFlags(sourceUid);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.columba.mail.folder.MailboxInterface#getHeaderFields(java.lang.Object,
	 *      java.lang.String[])
	 */
	public Header getHeaderFields(Object uid, String[] keys) throws Exception {

		VirtualHeader h = (VirtualHeader) headerList.get(uid);
		MessageFolder sourceFolder = h.getSrcFolder();
		Object sourceUid = h.getSrcUid();

		return sourceFolder.getHeaderFields(sourceUid, keys);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.columba.mail.folder.MailboxInterface#getMessageSourceStream(java.lang.Object)
	 */
	public InputStream getMessageSourceStream(Object uid) throws Exception {

		VirtualHeader h = (VirtualHeader) headerList.get(uid);
		MessageFolder sourceFolder = h.getSrcFolder();
		Object sourceUid = h.getSrcUid();

		return sourceFolder.getMessageSourceStream(sourceUid);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.columba.mail.folder.MailboxInterface#getMimePartBodyStream(java.lang.Object,
	 *      java.lang.Integer[])
	 */
	public InputStream getMimePartBodyStream(Object uid, Integer[] address)
			throws Exception {

		VirtualHeader h = (VirtualHeader) headerList.get(uid);
		MessageFolder sourceFolder = h.getSrcFolder();
		Object sourceUid = h.getSrcUid();

		return sourceFolder.getMimePartBodyStream(sourceUid, address);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.columba.mail.folder.MailboxInterface#getMimePartSourceStream(java.lang.Object,
	 *      java.lang.Integer[])
	 */
	public InputStream getMimePartSourceStream(Object uid, Integer[] address)
			throws Exception {

		VirtualHeader h = (VirtualHeader) headerList.get(uid);
		MessageFolder sourceFolder = h.getSrcFolder();
		Object sourceUid = h.getSrcUid();

		return sourceFolder.getMimePartSourceStream(sourceUid, address);
	}

	/**
	 * 
	 * VirtualFolder doesn't allow adding messages, in comparison to other
	 * regular mailbox folders.
	 * 
	 * @see org.columba.mail.folder.FolderTreeNode#supportsAddMessage()
	 */
	public boolean supportsAddMessage() {
		return false;
	}

	/**
	 * Virtual folders can only accept other Virtual folders as childs.
	 * 
	 * @param newFolder
	 *            a folder to check if it is a Virtual folder.
	 * @return true if the folder is a VirtualFolder; false otherwise.
	 */
	public boolean supportsAddFolder(AbstractFolder newFolder) {
		return (newFolder instanceof VirtualFolder);
	}

	/**
	 * Not implemented.
	 */
	public void innerCopy(MailboxInterface destFolder, Object[] uids) {
	}

	public void setAttribute(Object uid, String key, Object value)
			throws Exception {
		// get header with UID
		/*
		ColumbaHeader header = (ColumbaHeader) headerList.get(uid);
		header.getAttributes().put(key, value);
		*/
		
		VirtualHeader h = (VirtualHeader) headerList.get(uid);
		h.getAttributes().put(key, value);
		MessageFolder sourceFolder = h.getSrcFolder();
		Object sourceUid = h.getSrcUid();

		sourceFolder.setAttribute(sourceUid, key, value);
	}

	public Attributes getAttributes(Object uid) throws Exception {
		VirtualHeader h = (VirtualHeader) headerList.get(uid);
		MessageFolder sourceFolder = h.getSrcFolder();
		Object sourceUid = h.getSrcUid();

		return sourceFolder.getAttributes(sourceUid);

		/*
		 * if (getHeaderList().containsKey(uid)) { return
		 * getHeaderList().get(uid).getAttributes(); } else { return null; }
		 */
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.columba.mail.folder.MailboxInterface#addMessage(java.io.InputStream,
	 *      org.columba.ristretto.message.Attributes)
	 */
	public Object addMessage(InputStream in, Attributes attributes, Flags flags)
			throws Exception {

		// not supported

		return null;
	}

	/**
	 * @see org.columba.mail.folder.Folder#getHeaderListStorage()
	 */
	public HeaderListStorage getHeaderListStorage() {

		// not necessary

		return null;
	}

	/**
	 * @see org.columba.mail.folder.AbstractFolder#supportsMove()
	 */
	public boolean supportsMove() {
		return true;
	}

	/**
	 * @see org.columba.mail.folder.MailboxInterface#getAllHeaderFields(java.lang.Object)
	 */
	public Header getAllHeaderFields(Object uid) throws Exception {
		VirtualHeader h = (VirtualHeader) headerList.get(uid);
		MessageFolder sourceFolder = h.getSrcFolder();
		Object sourceUid = h.getSrcUid();

		return sourceFolder.getAllHeaderFields(sourceUid);
	}

}