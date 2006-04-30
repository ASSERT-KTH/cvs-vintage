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
package org.columba.addressbook.folder;

import java.io.File;
import java.util.Iterator;

import javax.swing.event.EventListenerList;

import org.columba.addressbook.config.FolderItem;
import org.columba.addressbook.model.ContactItem;
import org.columba.addressbook.model.GroupItem;
import org.columba.addressbook.model.HeaderItemList;
import org.columba.addressbook.model.IContactItem;
import org.columba.addressbook.model.IContactItemMap;
import org.columba.addressbook.model.IContactModel;
import org.columba.addressbook.model.IHeaderItemList;
import org.columba.api.command.IWorkerStatusController;
import org.columba.core.config.Config;
import org.columba.core.io.DiskIO;

/**
 * Abstract base class for every contact folder.
 * 
 * @author fdietz
 *
 */
public abstract class AbstractFolder extends AddressbookTreeNode implements
		IContactStorage, IContactFolder {



	protected EventListenerList listenerList = new EventListenerList();

	protected int nextMessageUid;
	
	/**
	 * folder where we save everything name of folder is usually the UID-number
	 */
	private File directoryFile;

	/**
	 * FolderItem keeps information about the folder for example: name,
	 * accessrights, type
	 */

	private ContactItemCacheStorage cacheStorage;

	public AbstractFolder(String name, String dir) {
		super(name);

		if (DiskIO.ensureDirectory(dir)) {
			directoryFile = new File(dir);
		}

		cacheStorage = new ContactItemCacheStorageImpl(this);
	}

	public AbstractFolder(FolderItem item) {
		super(item);

		String dir = Config.getInstance().getConfigDirectory()
				+ "/addressbook/" + getUid();

		if (DiskIO.ensureDirectory(dir)) {
			directoryFile = new File(dir);
		}

		cacheStorage = new ContactItemCacheStorageImpl(this);
	}

	/**
	 * Adds a listener.
	 */
	public void addFolderListener(FolderListener l) {
		listenerList.add(FolderListener.class, l);
	}

	/**
	 * Removes a previously registered listener.
	 */
	public void removeFolderListener(FolderListener l) {
		listenerList.remove(FolderListener.class, l);
	}

	/**
	 * Propagates an event to all registered listeners notifying them of a item
	 * addition.
	 */
	protected void fireItemAdded(Object uid) {

		IFolderEvent e = new FolderEvent(this, null);
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();

		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == FolderListener.class) {
				((FolderListener) listeners[i + 1]).itemAdded(e);
			}
		}
	}

	/**
	 * Propagates an event to all registered listeners notifying them of a item
	 * removal.
	 */
	protected void fireItemRemoved(Object uid) {

		IFolderEvent e = new FolderEvent(this, null);
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();

		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == FolderListener.class) {
				((FolderListener) listeners[i + 1]).itemRemoved(e);
			}
		}
	}

	/**
	 * Propagates an event to all registered listeners notifying them of a item
	 * change.
	 */
	protected void fireItemChanged(Object uid) {

		IFolderEvent e = new FolderEvent(this, null);
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();

		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == FolderListener.class) {
				((FolderListener) listeners[i + 1]).itemChanged(e);
			}
		}
	}

	public File getDirectoryFile() {
		return directoryFile;
	}

	public void createChildren(IWorkerStatusController worker) {
	}

	/**
	 * @see org.columba.addressbook.folder.IContactStorage#getHeaderItemList()
	 */
	public IContactItemMap getContactItemMap() throws StoreException {
		return cacheStorage.getContactItemMap();
	}

	/**
	 * Check if contact exists with email or displayname.
	 * 
	 * @param contact		given contact email or displayname
	 * @return				contact UID, if exists. Otherwise, null
	 * @throws Exception
	 */
	public Object exists(String contact) throws StoreException{
		Iterator it = getContactItemMap().iterator();
		while ( it.hasNext()) {
			ContactItem item = (ContactItem) it.next();
			String address = item.getAddress();
			String displayname = item.getDisplayName();
			
			if ( address.equalsIgnoreCase(contact)) return item.getUid();
			if ( displayname.equalsIgnoreCase(contact)) return item.getUid();
			
		}
		return null;
	}
	

	/**
	 * save header-cache (HeaderItemList)
	 */
	public void save() throws StoreException {

	}

	/**
	 * load header-cache (HeaderItemList)
	 */
	public void load() throws StoreException {

	}

	/**
	 * @see javax.swing.tree.DefaultMutableTreeNode#getPathToRoot(TreeNode, int)
	 */
	/*
	 * protected TreeNode[] getPathToRoot(TreeNode aNode, int depth) {
	 * TreeNode[] retNodes;
	 * 
	 * if (aNode == null) { if (depth == 0) { return null; } else { retNodes =
	 * new TreeNode[depth]; } } else { depth++; retNodes =
	 * getPathToRoot(aNode.getParent(), depth); retNodes[retNodes.length -
	 * depth] = aNode; }
	 * 
	 * return retNodes; }
	 */

	/**
	 * Method getTreePath.
	 * 
	 * @return String
	 */
	/*
	 * public String getTreePath() { TreeNode[] treeNode = getPathToRoot(this,
	 * 0);
	 * 
	 * StringBuffer path = new StringBuffer();
	 * 
	 * for (int i = 1; i < treeNode.length; i++) { AddressbookTreeNode folder =
	 * (AddressbookTreeNode) treeNode[i]; path.append("/" + folder.getName()); }
	 * 
	 * return path.toString(); }
	 */

	/**
	 * @see org.columba.addressbook.folder.IContactStorage#add(IContactModel)
	 */
	public Object add(IContactModel contact) throws StoreException {
		Object uid = generateNextMessageUid();

		IContactItem item = new ContactItem(contact);
		item.setUid(uid);

		cacheStorage.add(uid, item);

		fireItemAdded(uid);

		return uid;
	}

	/**
	 * 
	 * @see org.columba.addressbook.folder.IContactStorage#modify(java.lang.Object,
	 *      IContactModel)
	 */
	public void modify(Object uid, IContactModel contact) throws StoreException {

		IContactItem item = new ContactItem(contact);

		item.setUid(uid);

		cacheStorage.modify(uid, item);

		fireItemChanged(uid);
	}

	/**
	 * @see org.columba.addressbook.folder.IContactStorage#remove(java.lang.Object)
	 */
	public void remove(Object uid) throws StoreException {
		cacheStorage.remove(uid);

		fireItemRemoved(uid);
	}

	/**
	 * @see org.columba.addressbook.folder.IContactStorage#get(java.lang.Object)
	 */
	public abstract IContactModel get(Object uid) throws StoreException;

	/**
	 * @see org.columba.addressbook.folder.IContactStorage#count()
	 */
	public int count()  throws StoreException {
		return cacheStorage.count();
	}

	/**
	 * @see org.columba.addressbook.folder.IContactStorage#exists(java.lang.Object)
	 */
	public boolean exists(Object uid) throws StoreException  {
		return cacheStorage.exists(uid);
	}

	/**
	 * Get all contact *and* group items of this folder.
	 * <p>
	 * GroupItems are retrieved accessing the subfolders of this folder.
	 * <p>
	 * 
	 * @return
	 */
	public IHeaderItemList getHeaderItemList() throws StoreException {
		// create list containing all contact item of this folder
		IHeaderItemList list = new HeaderItemList(getContactItemMap());

		// add group items
		for (int i = 0; i < getChildCount(); i++) {
			IGroupFolder groupFolder = (IGroupFolder) getChildAt(i);
			GroupItem groupItem = new GroupItem(groupFolder.getGroup());
			list.add(groupItem);
		}

		return list;
	}
	
	/**
	 * @return Returns the messageUid.
	 */
	public int getNextMessageUid() {
		return nextMessageUid;
	}
	/**
	 * @param messageUid The messageUid to set.
	 */
	public void setNextMessageUid(int messageUid) {
		this.nextMessageUid = messageUid;
	}
	
	public Integer generateNextMessageUid() {
		return new Integer(nextMessageUid++);
	}
}