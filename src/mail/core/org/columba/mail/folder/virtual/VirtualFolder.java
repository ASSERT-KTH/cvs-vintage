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
import org.columba.core.filter.Filter;
import org.columba.core.filter.FilterCriteria;
import org.columba.core.filter.FilterRule;
import org.columba.core.xml.XmlElement;
import org.columba.mail.config.FolderItem;
import org.columba.mail.config.IFolderItem;
import org.columba.mail.filter.MailFilterCriteria;
import org.columba.mail.folder.AbstractFolder;
import org.columba.mail.folder.AbstractMessageFolder;
import org.columba.mail.folder.FolderChildrenIterator;
import org.columba.mail.folder.FolderFactory;
import org.columba.mail.folder.IHeaderListStorage;
import org.columba.mail.folder.IMailFolder;
import org.columba.mail.folder.IMailbox;
import org.columba.mail.folder.event.FolderListener;
import org.columba.mail.folder.event.IFolderEvent;
import org.columba.mail.folder.headercache.CachedHeaderfields;
import org.columba.mail.folder.imap.IMAPFolder;
import org.columba.mail.folder.search.DefaultSearchEngine;
import org.columba.mail.gui.config.search.SearchFrame;
import org.columba.mail.gui.frame.AbstractMailFrameController;
import org.columba.mail.gui.tree.FolderTreeModel;
import org.columba.mail.message.ColumbaHeader;
import org.columba.mail.message.HeaderList;
import org.columba.mail.message.IHeaderList;
import org.columba.ristretto.message.Attributes;
import org.columba.ristretto.message.Flags;
import org.columba.ristretto.message.Header;
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
public class VirtualFolder extends AbstractMessageFolder implements FolderListener {
	protected int nextUid;

	protected HeaderList headerList;

	private boolean active;
	
	public VirtualFolder(FolderItem item, String path) {
		super(item, path);

		headerList = new HeaderList();

		ensureValidFilterElement();
	}

	public VirtualFolder(String name, String type, String path) {
		super(name, type, path);

		IFolderItem item = getConfiguration();
		item.setString("property", "accessrights", "user");
		item.setString("property", "subfolder", "true");
		item.setString("property", "include_subfolders", "true");
		item.setString("property", "source_uid", "101");

		headerList = new HeaderList();

		ensureValidFilterElement();
	}

	private void registerWithSource() {
		AbstractFolder folder = getSourceFolder();
		
		folder.addFolderListener(this);
		
		if( isRecursive() ) {
			FolderChildrenIterator it = new FolderChildrenIterator(folder);
		
			while( it.hasMoreChildren() ) {
				IMailFolder next = it.nextChild();
				
				if( !(next instanceof VirtualFolder) ) {
					next.addFolderListener(this);
				}
			}
		}
	}
	
	private void unregisterWithSource() {
		AbstractFolder folder = getSourceFolder();
		
		folder.removeFolderListener(this);
		
		if( isRecursive() ) {
			FolderChildrenIterator it = new FolderChildrenIterator(folder);
		
			while( it.hasMoreChildren() ) {
				IMailFolder next = it.nextChild();
				
				if( !(next instanceof VirtualFolder) ) {
					next.removeFolderListener(this);
				}
			}
		}
	}

	/**
	 * Ensures that there is at least one valid filter entry in the
	 * VFolder.
	 */
	private void ensureValidFilterElement() {		
		XmlElement filter = getConfiguration().getRoot().getElement("filter");
		
		if (filter == null) {
			filter = new XmlElement("filter");
			filter.addAttribute("description", "new filter");
			filter.addAttribute("enabled", "true");
			getConfiguration().getRoot().addElement(filter);
		} 
		
		if( filter.count() == 0 ) {
			XmlElement rules = new XmlElement("rules");
			rules.addAttribute("condition", "matchall");

			XmlElement criteria = new XmlElement("criteria");
			criteria.addAttribute("type", "Subject");
			criteria.addAttribute("headerfield", "Subject");
			criteria.addAttribute("criteria", "contains");
			criteria.addAttribute("pattern", "pattern");
			rules.addElement(criteria);
			filter.addElement(rules);
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

	public IHeaderList getHeaderList() throws Exception {
		if( !active ) {
			activate();
		} else {
			revalidateSearch();
		}
		
		return headerList;
	}

	/**
	 * 
	 */
	private void revalidateSearch() {
		VirtualHeader h;
		
		// Analyze the Filter
		Filter filter = (Filter) getFilter().clone();
		FilterRule rule = filter.getFilterRule();
		for( int i=0;i <rule.count(); i++) {
			FilterCriteria c = rule.get(i);
			if( ! c.getTypeString().equalsIgnoreCase("flags")) {
				rule.remove(i);
				i--;
			}
		}
		
		
		// If no flags filter the seach is still valid
		if( rule.count() == 0) {
			return;
		}
		
		// redo the seach for the flags criteria		
		Enumeration uids = headerList.keys();
		while( uids.hasMoreElements()) {
			h = (VirtualHeader) headerList.get(uids.nextElement());
			
			try {
				if( h.getSrcFolder().searchMessages(filter, new Object[] {h.getSrcUid()}).length == 0) {
					headerList.remove(h.getVirtualUid());

					// notify listeners
					fireMessageRemoved(h.getVirtualUid(), null);
					
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void addSearchToHistory() throws Exception {
		VirtualFolder searchFolder = (VirtualFolder) FolderTreeModel
				.getInstance().getFolder(106);

		// only create new subfolders if we used the default "Search Folder"
		if (!searchFolder.equals(this)) {
			return;
		}

		// (tstich) reduced to 3 because all need to be
		// search when activated on startup
		// we only want 3 subfolders
		// -> if more children exist remove them
		if (searchFolder.getChildCount() >= 3) {
			AbstractMessageFolder child = (AbstractMessageFolder) searchFolder
					.getChildAt(0);
			child.removeFolder();
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

		IFolderItem newFolderItem = newFolder.getConfiguration();
		newFolderItem.setInteger("property", "source_uid", uid);
		newFolderItem.setBoolean("property", "include_subfolders", includes);

		newFolderItem.getElement("filter").removeFromParent();
		newFolderItem.getRoot().addElement(
				(XmlElement) getConfiguration().getElement("filter").clone());

		MailFilterCriteria newc = new MailFilterCriteria(new Filter(
				getConfiguration().getElement("filter")).getFilterRule().get(0));

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

		if (newc.getTypeString().equalsIgnoreCase("flags")) {
			System.out.println("flags found");

			buf.append(newc.getTypeString());
			buf.append(" (");
			buf.append(newc.getCriteriaString());
			buf.append(" ");
			buf.append(newc.getPatternString());
			buf.append(")");
		} else if (newc.getTypeString().equalsIgnoreCase("custom headerfield")) {
			buf.append(newc.getHeaderfieldString());
			buf.append(" (");
			buf.append(newc.getCriteriaString());
			buf.append(" ");
			buf.append(newc.getPatternString());
			buf.append(")");
		} else {
			buf.append(newc.getTypeString());
			buf.append(" (");
			buf.append(newc.getCriteriaString());
			buf.append(" ");
			buf.append(newc.getPatternString());
			buf.append(")");
		}

		newFolder.setName(buf.toString());

		// update tree-view
		FolderTreeModel.getInstance().nodeStructureChanged(searchFolder);

		// update tree-node (for renaming the new folder)
		FolderTreeModel.getInstance().nodeChanged(newFolder);
	}

	protected void applySearch() throws Exception {
		AbstractMessageFolder srcFolder = getSourceFolder();

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
	}

	/**
	 * @return
	 */
	private AbstractMessageFolder getSourceFolder() {
		int uid = getConfiguration().getInteger("property", "source_uid");
		AbstractMessageFolder srcFolder = (AbstractMessageFolder) FolderTreeModel
				.getInstance().getFolder(uid);
		return srcFolder;
	}

	protected void applySearch(AbstractMessageFolder parent, Filter filter)
			throws Exception {
		AbstractMessageFolder folder = parent;

		Object[] resultUids = folder.searchMessages(filter);
		String[] headerfields = CachedHeaderfields.getDefaultHeaderfields();

		
		if (resultUids != null) {
			for (int i = 0; i < resultUids.length; i++) {
				ColumbaHeader header = null;

				if (folder instanceof VirtualFolder) {
					//	get source folder reference
					VirtualHeader virtualHeader = ((VirtualFolder) folder)
							.getVirtualHeader(resultUids[i]);
					AbstractMessageFolder sourceFolder = virtualHeader
							.getSrcFolder();
					Object sourceUid = virtualHeader.getSrcUid();

					Header h = sourceFolder.getHeaderFields(sourceUid,
							headerfields);
					header = new ColumbaHeader(h);
					header.setAttributes(sourceFolder.getAttributes(sourceUid));
					header.setFlags(sourceFolder.getFlags(sourceUid));
					fireMessageAdded(add((ColumbaHeader) header, sourceFolder, sourceUid));
				} else {
					Header h = folder.getHeaderFields(resultUids[i],
							headerfields);
					header = new ColumbaHeader(h);
					header.setAttributes(folder.getAttributes(resultUids[i]));
					header.setFlags(folder.getFlags(resultUids[i]));
					fireMessageAdded(add(header, folder, resultUids[i]));

				}

			}
		}
		
		if (isRecursive()) {
			for (Enumeration e = parent.children(); e.hasMoreElements();) {
				folder = (AbstractMessageFolder) e.nextElement();

				if (folder instanceof VirtualFolder) {
					continue;
				}

				applySearch(folder, filter);
			}
		}
	}
	
	private boolean isRecursive() {
		return Boolean.valueOf(
				getConfiguration().getString("property", "include_subfolders"))
				.booleanValue();
	}

	public DefaultSearchEngine getSearchEngine() {
		return null;
	}

	public Filter getFilter() {
		return new Filter(getConfiguration().getRoot().getElement("filter"));
	}

	public Object add(ColumbaHeader header, AbstractMessageFolder f, Object uid)
			throws Exception {
		Object newUid = generateNextUid();

		//VirtualMessage m = new VirtualMessage(f, uid, index);
		VirtualHeader virtualHeader = new VirtualHeader((ColumbaHeader) header,
				f, uid);
		//virtualHeader.set("columba.uid", newUid);
		virtualHeader.setVirtualUid(newUid);

		headerList.add(virtualHeader, newUid);
		
		return newUid;
	}

	/**
	 * @see org.columba.modules.mail.folder.Folder#markMessage(Object[], int,
	 *      IMAPFolder)
	 */
	public void markMessage(Object[] uids, int variant) throws Exception {
		for (int i = 0; i < uids.length; i++) {
			// get source folder reference
			VirtualHeader h = (VirtualHeader) headerList.get(uids[i]);
			if( h == null ) continue;
			
			AbstractMessageFolder sourceFolder = h.getSrcFolder();
			Object sourceUid = h.getSrcUid();

			/*
			// virtual folder: update mailfolderinfo -> fire treenode change
			updateMailFolderInfo(getFlags(uids[i]), variant);
			// virtual folder: fire message flag changed
			fireMessageFlagChanged(uids[i], variant);*/

			// mark message in source folder
			sourceFolder.markMessage(new Object[] { sourceUid }, variant);

		}
	}

	/**
	 * @see org.columba.modules.mail.folder.Folder#removeMessage(Object)
	 */
	public void removeMessage(Object uid) throws Exception {
		// notify listeners
		fireMessageRemoved(uid, getFlags(uid));

		// get source folder reference
		VirtualHeader h = (VirtualHeader) headerList.get(uid);
		AbstractMessageFolder sourceFolder = h.getSrcFolder();
		Object sourceUid = h.getSrcUid();

		// remove from source folder
		sourceFolder.removeMessage(sourceUid);

		// remove from virtual folder
		headerList.remove(uid);
	}

	/**
	 * @see org.columba.modules.mail.folder.Folder#getMimeTree(Object,
	 *      IMAPFolder)
	 */
	public MimeTree getMimePartTree(Object uid) throws Exception {
		if( !exists(uid)) return null;
		
		VirtualHeader h = (VirtualHeader) headerList.get(uid);
		AbstractMessageFolder sourceFolder = h.getSrcFolder();
		Object sourceUid = h.getSrcUid();

		return sourceFolder.getMimePartTree(sourceUid);
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
			AbstractMessageFolder sourceFolder = h.getSrcFolder();
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
	 * public MailFolderCommandReference getCommandReference(
	 * MailFolderCommandReference r) { MailFolderCommandReference[] newReference =
	 * null;
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
	 * (VirtualHeader) headerList .get(uids[i]); AbstractMessageFolder srcFolder =
	 * virtualHeader.getSrcFolder(); Object srcUid = virtualHeader.getSrcUid();
	 * 
	 * if (list.containsKey(srcFolder)) { // bucket for this folder exists
	 * already } else { // create new bucket for this folder list.put(srcFolder,
	 * new Vector()); }
	 * 
	 * List v = (Vector) list.get(srcFolder); v.add(srcUid); }
	 * 
	 * newReference = new MailFolderCommandReference[list.size() + 2];
	 * 
	 * int i = 0;
	 * 
	 * for (Enumeration e = list.keys(); e.hasMoreElements();) {
	 * AbstractMessageFolder srcFolder = (AbstractMessageFolder)
	 * e.nextElement(); List v = (Vector) list.get(srcFolder);
	 * 
	 * newReference[i] = new MailFolderCommandReference(srcFolder);
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
	 * MailFolderCommandReference((AbstractMessageFolder) r[1] .getFolder()); }
	 * else { newReference[i] = null; }
	 * 
	 * newReference[i + 1] = r[0];
	 * 
	 * return newReference; }
	 */

	/**
	 * @see org.columba.mail.folder.FolderTreeNode#tryToGetLock(java.lang.Object)
	 */
	public boolean tryToGetLock(Object locker) {
		// We need to get the locks of all folders		
		AbstractFolder folder = getSourceFolder();
		
		boolean success = true;
		success &= folder.tryToGetLock(locker);
		
		if( success && isRecursive() ) {
			FolderChildrenIterator it = new FolderChildrenIterator(folder);

			while( success && it.hasMoreChildren() ) {
				IMailFolder next = it.nextChild();
				
				if( !(next instanceof VirtualFolder) ) {
					success &= next.tryToGetLock(locker);
				}				
			}
		}
		
		if( ! success ) {
			releaseLock(locker);
		}
		
		return success;
	}

	/**
	 * @see org.columba.mail.folder.AbstractFolder#releaseLock(java.lang.Object)
	 */
	public void releaseLock(Object locker) {
		AbstractFolder folder = getSourceFolder();
		
		folder.releaseLock(locker);
		
		if( isRecursive() ) {
			FolderChildrenIterator it = new FolderChildrenIterator(folder);
		
			while( it.hasMoreChildren() ) {
				IMailFolder next = it.nextChild();
				
				if( !(next instanceof VirtualFolder) ) {
					next.releaseLock(locker);
				}
			}
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

	protected Object srcUidToVirtualUid(IMailFolder srcFolder, Object uid) {
		Enumeration uids = headerList.keys();
		while( uids.hasMoreElements() ) {
			VirtualHeader h = (VirtualHeader) headerList.get(uids.nextElement());
			if( h.getSrcUid().equals(uid) && h.getSrcFolder().equals(srcFolder) ) {
				return h.getVirtualUid();
			}
			
		}
		
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.columba.mail.folder.IMailbox#addMessage(java.io.InputStream)
	 */
	public Object addMessage(InputStream in) throws Exception {
		// not supported
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.columba.mail.folder.IMailbox#getAttribute(java.lang.Object,
	 *      java.lang.String)
	 */
	public Object getAttribute(Object uid, String key) throws Exception {
		VirtualHeader h = (VirtualHeader) headerList.get(uid);
		AbstractMessageFolder sourceFolder = h.getSrcFolder();
		Object sourceUid = h.getSrcUid();

		return sourceFolder.getAttribute(sourceUid, key);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.columba.mail.folder.IMailbox#getFlags(java.lang.Object)
	 */
	public Flags getFlags(Object uid) throws Exception {

		VirtualHeader h = (VirtualHeader) headerList.get(uid);
		AbstractMessageFolder sourceFolder = h.getSrcFolder();
		Object sourceUid = h.getSrcUid();

		return sourceFolder.getFlags(sourceUid);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.columba.mail.folder.IMailbox#getHeaderFields(java.lang.Object,
	 *      java.lang.String[])
	 */
	public Header getHeaderFields(Object uid, String[] keys) throws Exception {

		VirtualHeader h = (VirtualHeader) headerList.get(uid);
		AbstractMessageFolder sourceFolder = h.getSrcFolder();
		Object sourceUid = h.getSrcUid();

		return sourceFolder.getHeaderFields(sourceUid, keys);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.columba.mail.folder.IMailbox#getMessageSourceStream(java.lang.Object)
	 */
	public InputStream getMessageSourceStream(Object uid) throws Exception {

		VirtualHeader h = (VirtualHeader) headerList.get(uid);
		AbstractMessageFolder sourceFolder = h.getSrcFolder();
		Object sourceUid = h.getSrcUid();

		return sourceFolder.getMessageSourceStream(sourceUid);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.columba.mail.folder.IMailbox#getMimePartBodyStream(java.lang.Object,
	 *      java.lang.Integer[])
	 */
	public InputStream getMimePartBodyStream(Object uid, Integer[] address)
			throws Exception {

		VirtualHeader h = (VirtualHeader) headerList.get(uid);
		AbstractMessageFolder sourceFolder = h.getSrcFolder();
		Object sourceUid = h.getSrcUid();

		return sourceFolder.getMimePartBodyStream(sourceUid, address);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.columba.mail.folder.IMailbox#getMimePartSourceStream(java.lang.Object,
	 *      java.lang.Integer[])
	 */
	public InputStream getMimePartSourceStream(Object uid, Integer[] address)
			throws Exception {

		VirtualHeader h = (VirtualHeader) headerList.get(uid);
		AbstractMessageFolder sourceFolder = h.getSrcFolder();
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
	 * @param newFolderType
	 *            a folder to check if it is a Virtual folder.
	 * @return true if the folder is a VirtualFolder; false otherwise.
	 */
	public boolean supportsAddFolder(String newFolderType) {
		return (newFolderType.equals(getType()));
	}

	public void innerCopy(IMailbox destFolder, Object[] uids) throws Exception {
		for( int i=0; i<uids.length; i ++) {
			VirtualHeader h = (VirtualHeader) headerList.get(uids[i]);
			AbstractMessageFolder sourceFolder = h.getSrcFolder();
			Object sourceUid = h.getSrcUid();
			
			sourceFolder.innerCopy(destFolder, new Object[] {sourceUid});
		}
		
	}

	public void setAttribute(Object uid, String key, Object value)
			throws Exception {
		// get header with UID
		/*
		 * ColumbaHeader header = (ColumbaHeader) headerList.get(uid);
		 * header.getAttributes().put(key, value);
		 */

		VirtualHeader h = (VirtualHeader) headerList.get(uid);
		h.getAttributes().put(key, value);
		AbstractMessageFolder sourceFolder = h.getSrcFolder();
		Object sourceUid = h.getSrcUid();

		sourceFolder.setAttribute(sourceUid, key, value);
	}

	public Attributes getAttributes(Object uid) throws Exception {
		VirtualHeader h = (VirtualHeader) headerList.get(uid);
		AbstractMessageFolder sourceFolder = h.getSrcFolder();
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
	 * @see org.columba.mail.folder.IMailbox#addMessage(java.io.InputStream,
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
	public IHeaderListStorage getHeaderListStorage() {

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
	 * @see org.columba.mail.folder.IMailbox#getAllHeaderFields(java.lang.Object)
	 */
	public Header getAllHeaderFields(Object uid) throws Exception {
		VirtualHeader h = (VirtualHeader) headerList.get(uid);
		AbstractMessageFolder sourceFolder = h.getSrcFolder();
		Object sourceUid = h.getSrcUid();

		return sourceFolder.getAllHeaderFields(sourceUid);
	}

	/**
	 * @see org.columba.mail.folder.IMailbox#expungeFolder()
	 */
	public void expungeFolder() throws Exception {
		AbstractMessageFolder srcFolder = getSourceFolder();
		
		boolean isInclude = Boolean.valueOf(
				getConfiguration().getString("property", "include_subfolders"))
				.booleanValue();

		
		if( isInclude ) {
			recursiveExpunge(srcFolder);			
		} else {
			srcFolder.expungeFolder();
		}
	}
	
	private void recursiveExpunge(AbstractMessageFolder srcFolder) throws Exception {
		AbstractMessageFolder folder;

		srcFolder.expungeFolder();
		
		for (Enumeration e = srcFolder.children(); e.hasMoreElements();) {
			folder = (AbstractMessageFolder) e.nextElement();

			if (folder instanceof VirtualFolder) {
				continue;
			}
			
			recursiveExpunge(folder);
		}
		
	}
	/* (non-Javadoc)
	 * @see org.columba.mail.folder.IMailFolder#getRootFolder()
	 */
	public IMailFolder getRootFolder() {
		return getSourceFolder().getRootFolder();		
	}

	/* (non-Javadoc)
	 * @see org.columba.mail.folder.event.IFolderListener#messageAdded(org.columba.mail.folder.event.IFolderEvent)
	 */
	public void messageAdded(IFolderEvent e) {
		AbstractMessageFolder folder = (AbstractMessageFolder)e.getSource();
		
		try {
			Object[] resultUids = folder.searchMessages(getFilter(), new Object[] {e.getChanges()});
			
			if( resultUids.length > 0 ) {
				Header h = folder.getHeaderFields(resultUids[0],
						CachedHeaderfields.getDefaultHeaderfields());
				ColumbaHeader header = new ColumbaHeader(h);
				header.setAttributes(folder.getAttributes(resultUids[0]));
				header.setFlags(folder.getFlags(resultUids[0]));
				
				fireMessageAdded(add(header, folder, resultUids[0]));
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
	}

	/* (non-Javadoc)
	 * @see org.columba.mail.folder.event.IFolderListener#messageRemoved(org.columba.mail.folder.event.IFolderEvent)
	 */
	public void messageRemoved(IFolderEvent e) {
		Object srcUid = e.getChanges();
		
		Object vUid = srcUidToVirtualUid((IMailFolder) e.getSource(), srcUid);
		if( vUid != null ) {
			headerList.remove(vUid);

			// notify listeners
			fireMessageRemoved(vUid, null);
		}
	}

	/* (non-Javadoc)
	 * @see org.columba.mail.folder.event.IFolderListener#messageFlagChanged(org.columba.mail.folder.event.IFolderEvent)
	 */
	public void messageFlagChanged(IFolderEvent e) {
		Object virtualUid = srcUidToVirtualUid((IMailFolder)e.getSource(), e.getChanges());

		if( virtualUid == null ) {
			// Maybe add the message			
			AbstractMessageFolder folder = (AbstractMessageFolder)e.getSource();
			try {
				Object[] resultUids = folder.searchMessages(getFilter(), new Object[] {e.getChanges()});
				
				if( resultUids.length > 0 ) {
					Header h = folder.getHeaderFields(resultUids[0],
							CachedHeaderfields.getDefaultHeaderfields());
					ColumbaHeader header = new ColumbaHeader(h);
					header.setAttributes(folder.getAttributes(resultUids[0]));
					header.setFlags(folder.getFlags(resultUids[0]));
					
					fireMessageAdded(add(header, folder, resultUids[0]));
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			
			return;
		}
		
		try {
			//Shall we do another search and maybe remove the message?
			updateMailFolderInfo(e.getOldFlags(), e.getParameter());
		} catch (Exception e1) {
		}
		
		//fire updates
		fireMessageFlagChanged(virtualUid, e.getOldFlags(), e.getParameter());
	}

	/* (non-Javadoc)
	 * @see org.columba.mail.folder.event.IFolderListener#folderPropertyChanged(org.columba.mail.folder.event.IFolderEvent)
	 */
	public void folderPropertyChanged(IFolderEvent e) {
		//don't care
	}

	/* (non-Javadoc)
	 * @see org.columba.mail.folder.event.IFolderListener#folderAdded(org.columba.mail.folder.event.IFolderEvent)
	 */
	public void folderAdded(IFolderEvent e) {
		if( isRecursive() && !(e.getChanges() instanceof VirtualFolder )) {
			AbstractMessageFolder folder = (AbstractMessageFolder) e.getChanges();
			folder.addFolderListener(this);
		}
	}

	/* (non-Javadoc)
	 * @see org.columba.mail.folder.event.IFolderListener#folderRemoved(org.columba.mail.folder.event.IFolderEvent)
	 */
	public void folderRemoved(IFolderEvent e) {
		AbstractMessageFolder folder = (AbstractMessageFolder) e.getChanges();
		folder.removeFolderListener(this);
	}

	/**
	 * 
	 */
	public void activate() throws Exception {		
		getMessageFolderInfo().reset();
		applySearch();
		registerWithSource();		
		active = true;
	}
	
	public void deactivate() {
		active = false;
		headerList.clear();
		getMessageFolderInfo().reset();
		
		unregisterWithSource();
	}
	
	/* (non-Javadoc)
	 * @see org.columba.mail.folder.IMailFolder#removeFolder()
	 */
	public void removeFolder() throws Exception {
		if(active) {
			deactivate();
		}
		super.removeFolder();
	}
	
}