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
//
//$Log: Folder.java,v $
//
package org.columba.mail.folder;

import java.io.File;
import java.util.Vector;

import javax.swing.JDialog;
import javax.swing.tree.TreeNode;

import org.columba.core.command.WorkerStatusController;
import org.columba.core.config.ConfigPath;
import org.columba.core.io.DiskIO;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.config.FolderItem;
import org.columba.mail.filter.Filter;
import org.columba.mail.filter.FilterList;
import org.columba.mail.gui.config.filter.ConfigFrame;
import org.columba.mail.gui.frame.MailFrameController;
import org.columba.mail.message.AbstractMessage;
import org.columba.mail.message.ColumbaHeader;
import org.columba.mail.message.HeaderList;
import org.columba.mail.message.MimePart;
import org.columba.mail.message.MimePartTree;

/**
 *    Abstract Basic Folder class. Is subclasses by every folder
 *    class containing messages and therefore offering methods
 *    to alter the mailbox
 *
 *@author       freddy
 *@created      19. Juni 2001
 */
public abstract class Folder extends FolderTreeNode {

	/**
	 *
	 */
	protected MessageFolderInfo messageFolderInfo;

	/**
	 *
	 */
	protected FilterList filterList;

	/**
	 *
	 */
	protected boolean changed;

	/**
	 *
	 */
	protected Vector folderListeners;

	/**
	 *
	 */
	protected File directoryFile; // directory

	/**
	 *
	 */
	/**
	 *    Description of the Field
	 *
	 *@since
	 */
	protected Vector treeNodeListeners;

	/**
	 * @see org.columba.mail.folder.FolderTreeNode#FolderTreeNode(org.columba.mail.config.FolderItem)
	 */
	/**
	 * Standard constructor. 
	 * 
	 * @param node <class>AdapterNode</class> this connects the config
	 *             to the JTree TreeModel
	 * @param item <class>FolderItem</class> contains information about
	 *             the folder
	 */
	public Folder(FolderItem item) {
		super(item);

		children = new Vector();

		

		init();

		String dir = ConfigPath.getConfigDirectory() + "/mail/" + getUid();
		if (DiskIO.ensureDirectory(dir))
			directoryFile = new File(dir);
	}

	/**
	 * Method showFilterDialog.
	 * @param frameController
	 * @return JDialog
	 */
	public JDialog showFilterDialog(MailFrameController frameController) {
		return new ConfigFrame(this);
	}

	/**
	 * Method innerCopy.
	 * @param destFolder
	 * @param uids
	 * @param worker
	 * @throws Exception
	 */
	public void innerCopy(
		Folder destFolder,
		Object[] uids,
		WorkerStatusController worker)
		throws Exception {
	}

	/**
	 * Method getRootFolder.
	 * @return FolderTreeNode
	 */
	public FolderTreeNode getRootFolder() {
		FolderTreeNode folderTreeNode = (FolderTreeNode) getParent();
		while (folderTreeNode != null) {

			if (folderTreeNode instanceof Root) {

				return (Root) folderTreeNode;
			}

			folderTreeNode = (FolderTreeNode) folderTreeNode.getParent();

		}

		return null;
	}

	/**
	 * Method Folder.
	 * @param name
	 */
	/**
	 * Constructor for creating temporary-folders or other types
	 * which work only in memory and aren't visible in the <class>
	 * TreeView</class>
	 * 
	 * @param name Name of the folder
	 */
	// use this constructor only for tempfolders
	public Folder(String name) {
		super(null);

		children = new Vector();

		init();

		String dir = ConfigPath.getConfigDirectory() + "/mail/" + name;
		if (DiskIO.ensureDirectory(dir))
			directoryFile = new File(dir);

	}

	/**
	 * Method init.
	 */
	/**
	 * Do some initialization work both constructors share
	 * 
	 */
	protected void init() {

		messageFolderInfo = new MessageFolderInfo();

		changed = false;

		folderListeners = new Vector();

		treeNodeListeners = new Vector();

	}

	/**
	 * Method getDirectoryFile.
	 * @return File
	 */
	/**
	 * Method getDirectoryFile returns the the folder where messages
	 * are saved
	 * 
	 * @return File <class>File</class>-class representing the mailbox directory
	 */
	public File getDirectoryFile() {
		return directoryFile;
	}

	/**
	 * @see org.columba.mail.folder.FolderTreeNode#createChildren(org.columba.core.command.WorkerStatusController)
	 */
	/**
	 * @see org.columba.modules.mail.folder.FolderTreeNode#createChildren(WorkerStatusController)
	 */
	public void createChildren(WorkerStatusController worker) {
	}

	/**
	 * Method setChanged.
	 * @param b
	 */
	/**
	 * Method applyFilter.
	 * @param uids
	 * @return boolean
	 * @throws Exception
	 */
	/*
	public boolean applyFilter(Object[] uids) throws Exception {
		boolean result = false;
	
		result = getFilterList().processAll(uids);
	
		return result;
	}
	*/
	/**
	 * Method setChanged.
	 * @param b
	 */
	public void setChanged(boolean b) {
		changed = b;
	}

	/**
	 * Method setMessageFolderInfo.
	 * @param i
	 */
	/**
	 * Method setMessageFolderInfo.
	 * @param i
	 */
	public void setMessageFolderInfo(MessageFolderInfo i) {
		messageFolderInfo = i;
	}

	/**
	 * Method setFilterList.
	 * @param list
	 */
	/**
	 * Method setFilterList.
	 * @param list
	 */
	public void setFilterList(FilterList list) {
		filterList = list;
	}

	/**
	 * Method getChanged.
	 * @return boolean
	 */
	/**
	 * Method getChanged.
	 * @return boolean
	 */
	public boolean getChanged() {
		return changed;
	}

	/**
	 * Method getMessageFolderInfo.
	 * @return MessageFolderInfo
	 */
	/**
	 * Method getMessageFolderInfo.
	 * @return MessageFolderInfo
	 */
	public MessageFolderInfo getMessageFolderInfo() {
		return messageFolderInfo;
	}

	/**
	 * Method getFilterList.
	 * @return FilterList
	 */
	/**
	 * Method getFilterList.
	 * @return FilterList
	 */
	public FilterList getFilterList() {
		return filterList;
	}

	/**
	 * Method hasFilters.
	 * @return boolean
	 */
	/**
	 * Method hasFilters.
	 * @return boolean
	 */
	public boolean hasFilters() {
		if (getFilterList() == null)
			return false;

		return getFilterList().count() > 0;
	}

	/**
	 * Method expungeFolder.
	 * @param uids
	 * @param worker
	 * @throws Exception
	 */
	public abstract void expungeFolder(
		WorkerStatusController worker)
		throws Exception;

	/**
	 * Method addMessage.
	 * @param message
	 * @param worker
	 * @return Object
	 * @throws Exception
	 */
	/**
	 * Method addMessage.
	 * 
	 * @param message Message object can be null 
	 * @param source  raw string of message 
	 * @return Object UID of message
	 * @throws Exception
	 */
	public abstract Object addMessage(
		AbstractMessage message,
		WorkerStatusController worker)
		throws Exception;

	/**
	 * Method addMessage.
	 * @param source
	 * @param worker
	 * @return Object
	 * @throws Exception
	 */
	public abstract Object addMessage(
		String source,
		WorkerStatusController worker)
		throws Exception;
	/**
	 * Method exists.
	 * @param uid
	 * @param worker
	 * @return boolean
	 * @throws Exception
	 */
	/**
	 * Method exists.
	 * 
	 * @param uid UID of message
	 * @return boolean true, if message exists
	 * @throws Exception
	 */
	public abstract boolean exists(Object uid, WorkerStatusController worker)
		throws Exception;

	/**
	 * Method getHeaderList.
	 * @param worker
	 * @return HeaderList
	 * @throws Exception
	 */
	/**
	 * Method getHeaderList.
	 * 
	 * @param worker for accessing the <class>StatusBar</class>
	 * @return HeaderList 
	 * @throws Exception
	 */
	public abstract HeaderList getHeaderList(WorkerStatusController worker)
		throws Exception;

	/**
	 * Method markMessage.
	 * @param uids
	 * @param variant
	 * @param worker
	 * @throws Exception
	 */
	/**
	 * Method mark.
	 * @param uid
	 * @param variant
	 * @param worker
	 * @throws Exception
	 */
	public abstract void markMessage(
		Object[] uids,
		int variant,
		WorkerStatusController worker)
		throws Exception;

	/**
	 * Method removeMessage.
	 * @param uid
	 * @param worker
	 * @throws Exception
	 */
	/**
	 * Method removeMessage.
	 * @param uid
	 * @throws Exception
	 */
	public abstract void removeMessage(
		Object uid,
		WorkerStatusController worker)
		throws Exception;

	/**
	 * Method getMimePart.
	 * @param uid
	 * @param address
	 * @param worker
	 * @return MimePart
	 * @throws Exception
	 */
	/**
	 * Method getMimePart.
	 * @param uid
	 * @param address
	 * @param worker
	 * @return MimePart
	 * @throws Exception
	 */
	public abstract MimePart getMimePart(
		Object uid,
		Integer[] address,
		WorkerStatusController worker)
		throws Exception;

	/**
	 * Method getMessageSource.
	 * @param uid
	 * @param worker
	 * @return String
	 * @throws Exception
	 */
	/**
	 * Method getMessageSource.
	 * @param uid
	 * @param worker
	 * @return String
	 * @throws Exception
	 */
	public abstract String getMessageSource(
		Object uid,
		WorkerStatusController worker)
		throws Exception;

	/**
	 * Method getMimePartTree.
	 * @param uid
	 * @param worker
	 * @return MimePartTree
	 * @throws Exception
	 */
	/**
	 * Method getMimePartTree.
	 * @param uid
	 * @param worker
	 * @return MimePartTree
	 * @throws Exception
	 */
	public abstract MimePartTree getMimePartTree(
		Object uid,
		WorkerStatusController worker)
		throws Exception;

	/**
	 * Method getMessageHeader.
	 * @param uid
	 * @param worker
	 * @return ColumbaHeader
	 * @throws Exception
	 */
	/**
	 * Method getHeader.
	 * @param uid
	 * @param worker
	 * @return ColumbaHeader
	 * @throws Exception
	 */
	public abstract ColumbaHeader getMessageHeader(
		Object uid,
		WorkerStatusController worker)
		throws Exception;

	/**
	 * Method addFolderListener.
	 * @param listener
	 */
	/**
	 * Method getMessage.
	 * @param uid
	 * @param worker
	 * @return AbstractMessage
	 * @throws Exception
	 */

	//protected abstract AbstractMessage getMessage(Object uid, WorkerStatusController worker) throws Exception;

	/**
	 * Method addFolderListener.
	 * @param listener
	 */
	public void addFolderListener(FolderChangeListener listener) {
		folderListeners.add(listener);
	}

	/**
	 * Method removeFolderListener.
	 * @param listener
	 */
	/**
	 * Method removeFolderListener.
	 * @param listener
	 */
	public void removeFolderListener(FolderChangeListener listener) {
		folderListeners.remove(listener);
	}

	/**
	 * @see javax.swing.tree.DefaultMutableTreeNode#getPathToRoot(javax.swing.tree.TreeNode, int)
	 */
	/**
	 * @see javax.swing.tree.DefaultMutableTreeNode#getPathToRoot(TreeNode, int)
	 */
	protected TreeNode[] getPathToRoot(TreeNode aNode, int depth) {
		TreeNode[] retNodes;

		if (aNode == null) {
			if (depth == 0)
				return null;
			else
				retNodes = new TreeNode[depth];
		} else {
			depth++;
			retNodes = getPathToRoot(aNode.getParent(), depth);
			retNodes[retNodes.length - depth] = aNode;
		}
		return retNodes;
	}

	/**
	 * Method getTreePath.
	 * @return String
	 */
	/**
	 * Method getTreePath.
	 * @return String
	 */
	public String getTreePath() {
		TreeNode[] treeNode = getPathToRoot(this, 0);

		StringBuffer path = new StringBuffer();

		for (int i = 1; i < treeNode.length; i++) {
			FolderTreeNode folder = (FolderTreeNode) treeNode[i];
			path.append("/" + folder.getName());
		}

		return path.toString();
	}

	

	/**
	 * Method isParent.
	 * @param folder
	 * @return boolean
	 */
	/**
	 * @see org.columba.modules.mail.folder.FolderTreeNode#getChild(String)
	 */
	/************************************ treenode implementation ***********/

	/*
	public TreeNode getChild(String str) {
		for (int i = 0; i < getChildCount(); i++) {
			Folder child = (Folder) getChildAt(i);
			String name = child.getName();

			if (name.equalsIgnoreCase(str))
				return child;
		}
		return null;
	}
	*/

	/**
	 * @see org.columba.modules.mail.folder.FolderTreeNode#getName()
	 */
	public String getName() {
		String name = null;

		FolderItem item = getFolderItem();
		name = item.get("property", "name");

		return name;
	}

	/**
	 * @see org.columba.mail.folder.FolderTreeNode#setName(java.lang.String)
	 */
	/**
	 * @see org.columba.modules.mail.folder.FolderTreeNode#setName(String)
	 */
	public void setName(String newName) {

		FolderItem item = getFolderItem();
		item.set("property", "name", newName);

	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return getName();
	}

	/**
	 * Method renameFolder.
	 * @param name
	 * @return boolean
	 * @throws Exception
	 */
	/**
	 * Method renameFolder.
	 * @param name
	 * @return boolean
	 * @throws Exception
	 */
	public boolean renameFolder(String name) throws Exception {
		setName(name);

		return true;
	}

	/**
	 * Method removeAll.
	 */
	/**
	 * Method removeAll.
	 */
	public void removeAll() {
	}

	/**
	 * Method getUids.
	 * @param worker
	 * @return Object[]
	 * @throws Exception
	 */
	/**
	 * Method getUids.
	 * @return Object[]
	 */
	public Object[] getUids(WorkerStatusController worker) throws Exception {
		return null;
	}
	
	/**
	 * Method size.
	 * @return int
	 */
	public int size() {
		return 0;
	}

	/**
	 * Method searchMessages.
	 * @param filter
	 * @param uids
	 * @param worker
	 * @return Object[]
	 * @throws Exception
	 */
	public abstract Object[] searchMessages(
		Filter filter,
		Object[] uids,
		WorkerStatusController worker)
		throws Exception;

	/**
	 * Method searchMessages.
	 * @param filter
	 * @param worker
	 * @return Object[]
	 * @throws Exception
	 */
	public abstract Object[] searchMessages(
		Filter filter,
		WorkerStatusController worker)
		throws Exception;

	/**
	 * Method getCommandReference.
	 * @param r
	 * @return FolderCommandReference[]
	 */
	public FolderCommandReference[] getCommandReference(FolderCommandReference[] r) {
		return r;
	}

}