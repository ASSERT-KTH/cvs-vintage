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
package org.columba.mail.folder.virtual;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JDialog;

import org.columba.core.command.WorkerStatusController;
import org.columba.core.gui.util.ImageLoader;
import org.columba.core.main.MainInterface;
import org.columba.core.xml.XmlElement;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.config.FolderItem;
import org.columba.mail.filter.Filter;
import org.columba.mail.filter.FilterCriteria;
import org.columba.mail.folder.DefaultSearchEngine;
import org.columba.mail.folder.Folder;
import org.columba.mail.gui.config.search.SearchFrame;
import org.columba.mail.gui.frame.MailFrameController;
import org.columba.mail.message.AbstractMessage;
import org.columba.mail.message.ColumbaHeader;
import org.columba.mail.message.HeaderInterface;
import org.columba.mail.message.HeaderList;
import org.columba.mail.message.MimePart;
import org.columba.mail.message.MimePartTree;

public class VirtualFolder extends Folder {

	protected final static ImageIcon virtualIcon =
		ImageLoader.getSmallImageIcon("virtualfolder.png");
	//private MainInterface mainInterface;

	//private Search searchFilter;

	protected int nextUid;
	protected HeaderList headerList;

	public VirtualFolder(FolderItem item) {
		super(item);

		headerList = new HeaderList();

		XmlElement filterElement = node.getElement("filter");
		if (filterElement == null) {
			/*
			filterElement = new XmlElement("filter");
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

			getFolderItem().getRoot().addElement(f.getRoot());
		}

		//searchFilter = new Search(this);
	}

	public ImageIcon getCollapsedIcon() {
		return virtualIcon;
	}

	public ImageIcon getExpandedIcon() {
		return virtualIcon;
	}

	protected Object generateNextUid() {
		return new Integer(nextUid++);
	}

	public void setNextUid(int next) {
		nextUid = next;
	}

	public JDialog showFilterDialog(MailFrameController frameController) {
		return new SearchFrame(frameController, this);
	}

	public boolean exists(Object uid, WorkerStatusController worker)
		throws Exception {
		return headerList.containsKey(uid);
	}

	public HeaderList getHeaderList(WorkerStatusController worker)
		throws Exception {

		headerList.clear();
		getMessageFolderInfo().clear();

		applySearch(worker);

		addSearchToHistory();

		return headerList;

	}

	public void addSearchToHistory() throws Exception {

		
		VirtualFolder folder =
			(VirtualFolder) MainInterface.treeModel.getFolder(106);

		// only create new subfolders if we used the default "Search Folder"
		if (!folder.equals(this))
			return;

		// we only want 10 subfolders
		// -> if more children exist remove them
		if (folder.getChildCount() >= 10) {
			Folder child = (Folder) folder.getChildAt(0);
			child.removeFromParent();
		}

		// create new subfolder
		String name = "search result";
		VirtualFolder newFolder = null;
		try {

			newFolder = (VirtualFolder) addFolder(name, "VirtualFolder");
		} catch (Exception ex) {
			ex.printStackTrace();

			return;
		}

		// if creation failed
		if (newFolder == null)
			return;


		// copy all properties to the subfolder
		int uid = getFolderItem().getInteger("property", "source_uid");
		boolean includes =
			getFolderItem().getBoolean("property", "include_subfolders");

		FolderItem newFolderItem = newFolder.getFolderItem();
		newFolderItem.set("property", "source_uid", uid);
		newFolderItem.set("property", "include_subfolders", includes);

		newFolderItem.getElement("filter").removeFromParent();
		newFolderItem.getRoot().addElement(
			(XmlElement) getFolderItem().getElement("filter").clone());

		FilterCriteria newc =
					new Filter(getFolderItem().getElement("filter"))
						.getFilterRule()
						.get(
						0);
						
		/*
		FilterCriteria c =
			new Filter(getFolderItem().getElement("filter"))
				.getFilterRule()
				.get(
				0);

		FilterCriteria newc =
			new Filter(getFolderItem().getElement("filter"))
				.getFilterRule()
				.get(
				0);
		newc.setCriteria(c.getCriteriaString());
		newc.setHeaderItem(c.getHeaderItemString());
		newc.setPattern(c.getPattern());
		newc.setType(c.getType());
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
		

		newFolder.renameFolder(buf.toString());
		
		// update tree-view
		MainInterface.treeModel.nodeStructureChanged(folder);
		
		// update tree-node (for renaming the new folder)
		MainInterface.treeModel.nodeChanged(newFolder);

	}

	protected void applySearch(WorkerStatusController worker)
		throws Exception {

		int uid = getFolderItem().getInteger("property", "source_uid");
		Folder srcFolder = (Folder) MainInterface.treeModel.getFolder(uid);

		boolean result = false;

		XmlElement filter = getFolderItem().getRoot().getElement("filter");

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
			getFolderItem().getRoot().addElement(filter);
		}

		Filter f = new Filter(getFolderItem().getRoot().getElement("filter"));

		applySearch(srcFolder, f, worker);

		VirtualFolder folder =
			(VirtualFolder) MainInterface.treeModel.getFolder(106);

	}

	protected void applySearch(
		Folder parent,
		Filter filter,
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

		boolean isInclude =
			(new Boolean(getFolderItem()
				.get("property", "include_subfolders")))
				.booleanValue();

		if (isInclude == true) {
			for (Enumeration e = parent.children(); e.hasMoreElements();) {
				folder = (Folder) e.nextElement();
				if (folder instanceof VirtualFolder)
					continue;

				applySearch(folder, filter, worker);
			}
		}

	}

	public DefaultSearchEngine getSearchEngine() {
		return null;
	}

	public Filter getFilter() {
		return new Filter(getFolderItem().getRoot().getElement("filter"));
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
	public void expungeFolder(Object[] uids, WorkerStatusController worker)
		throws Exception {

		for (int i = 0; i < uids.length; i++) {
			Object uid = uids[i];

			if (exists(uid, worker) == false)
				continue;

			ColumbaHeader h = getMessageHeader(uid, worker);
			Boolean expunged = (Boolean) h.get("columba.flags.expunged");

			//ColumbaLogger.log.debug("expunged=" + expunged);

			if (expunged.equals(Boolean.TRUE)) {
				// move message to trash

				//ColumbaLogger.log.info("moving message with UID " + uid + " to trash");

				// remove message
				removeMessage(uid, worker);

			}
		}
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
	public void removeMessage(Object uid, WorkerStatusController worker)
		throws Exception {

		ColumbaHeader header = (ColumbaHeader) getMessageHeader(uid, worker);

		if (header.get("columba.flags.seen").equals(Boolean.FALSE))
			getMessageFolderInfo().decUnseen();
		if (header.get("columba.flags.recent").equals(Boolean.TRUE))
			getMessageFolderInfo().decRecent();

		headerList.remove(uid);
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

		return (ColumbaHeader) headerList.get(uid);
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

	public FolderCommandReference[] getCommandReference(FolderCommandReference[] r) {

		FolderCommandReference[] newReference = null;

		Object[] uids = r[0].getUids();
		if (uids == null)
			return r;

		Hashtable list = new Hashtable();

		for (int i = 0; i < uids.length; i++) {
			VirtualHeader virtualHeader =
				(VirtualHeader) headerList.get(uids[i]);
			Folder srcFolder = virtualHeader.getSrcFolder();
			Object srcUid = virtualHeader.getSrcUid();

			if (list.containsKey(srcFolder)) {
				// bucket for this folder exists already
			} else {
				// create new bucket for this folder
				list.put(srcFolder, new Vector());
			}

			Vector v = (Vector) list.get(srcFolder);
			v.add(srcUid);
		}

		newReference = new FolderCommandReference[list.size() + 2];
		int i = 0;
		for (Enumeration e = list.keys(); e.hasMoreElements();) {
			Folder srcFolder = (Folder) e.nextElement();
			Vector v = (Vector) list.get(srcFolder);

			int size = 1;

			/*
			// check if we need a destination folder 
			if (r.length > 1)
				newReference = new FolderCommandReference[2];
			else
				newReference = new FolderCommandReference[1];
			*/

			newReference[i] = new FolderCommandReference(srcFolder);
			Object[] uidArray = new Object[v.size()];
			v.copyInto(uidArray);
			newReference[i].setUids(uidArray);
			newReference[i].setMarkVariant(r[0].getMarkVariant());
			newReference[i].setMessage(r[0].getMessage());

			i++;
		}

		if (r.length > 1)
			newReference[i] =
				new FolderCommandReference((Folder) r[1].getFolder());
		else
			newReference[i] = null;

		newReference[i + 1] = r[0];

		return newReference;
	}

	/**
	 * @see org.columba.mail.folder.FolderTreeNode#releaseLock()
	 */
	public void releaseLock() {
		super.releaseLock();
	}

	/**
	 * @see org.columba.mail.folder.FolderTreeNode#tryToGetLock(java.lang.Object)
	 */
	public boolean tryToGetLock(Object locker) {
		return super.tryToGetLock(locker);
	}

}
