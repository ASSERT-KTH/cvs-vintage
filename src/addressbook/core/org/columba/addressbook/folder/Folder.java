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

package org.columba.addressbook.folder;

import java.io.File;
import java.util.Vector;

import javax.swing.tree.TreeNode;

import org.columba.addressbook.config.FolderItem;
import org.columba.addressbook.gui.tree.AddressbookTreeNode;
import org.columba.addressbook.main.AddressbookInterface;
import org.columba.core.command.WorkerStatusController;
import org.columba.core.config.ConfigPath;
import org.columba.core.io.DiskIO;
import org.columba.mail.folder.FolderTreeNode;

public abstract class Folder extends AddressbookTreeNode {


	protected Vector folderListeners;

	/**
	 *    Description of the Field
	 *
	 *@since
	 */
	protected Vector treeNodeListeners;

	/**
	 *  unique identification number
	 */
	private int uid;

	/**
	 * folder where we save everything
	 * name of folder is usually the UID-number
	 */
	protected File directoryFile;

	/**
	 * FolderItem keeps information about the folder
	 * for example: name, accessrights, type
	 */
	//protected FolderItem folderItem;

	protected AddressbookInterface addressbookInterface;

	public Folder(FolderItem item) {
		super(item);

		/*
		this.folderItem = item;
		this.uid = folderItem.getUid();
		this.addressbookInterface = addressbookInterface;
		
		String dir =
			ConfigPath.getConfigDirectory() + "/addressbook" + "/" + new Integer(uid).toString();
		
		directoryFile = new File(dir);
		if (directoryFile.exists() == false)
		{
			directoryFile.mkdir();
		}
		*/

		init();

		String dir =
			ConfigPath.getConfigDirectory() + "/addressbook/" + getUid();
		if (DiskIO.ensureDirectory(dir))
			directoryFile = new File(dir);
	}

	public Folder(String name) {
		super(name);

		children = new Vector();

		init();

		String dir = ConfigPath.getConfigDirectory() + "/addressbook/" + name;
		if (DiskIO.ensureDirectory(dir))
			directoryFile = new File(dir);

	}

	/**
		 * Do some initialization work both constructors share
		 *
		 */
	protected void init() {

		//messageFolderInfo = new MessageFolderInfo();

		//changed = false;

		folderListeners = new Vector();

		treeNodeListeners = new Vector();

	}

	public File getDirectoryFile() {
		return directoryFile;
	}

	/*
	public AddressbookTreeNode getRootFolder() {
		AddressbookTreeNode folderTreeNode = (AddressbookTreeNode) getParent();
		while (folderTreeNode != null) {

			if (folderTreeNode instanceof Root) {

				return (Root) folderTreeNode;
			}

			folderTreeNode = (AddressbookTreeNode) folderTreeNode.getParent();

		}

		return null;
	}
	*/

	public void createChildren(WorkerStatusController worker) {
	}

	

	/**
	 * return HeaderItemList
	 * 
	 * List of HeaderItem-Objects is needed for the table
	 * and therefore only keeps table-relevant information
	 * 
	 * to get *all* the data of a contact use the uidGet-method
	 */
	public abstract HeaderItemList getHeaderItemList();

	/**
	 * this method it ATM only needed for group-support
	 *  -> get the uid-list (the grouplist-members!) from the
	 *     grouplistcard-object
	 * 
	 *  -> this method return a HeaderItemList for use in a JList-object, etc.
	 */
	public abstract HeaderItemList getHeaderItemList(Object[] uids);

	/** 
	 * add ContactCard to Folder
	 */
	public abstract void add(DefaultCard item);

	public abstract void modify(DefaultCard item, Object uid);

	//public abstract void add(GroupListCard item);

	/**
	 * remove ContactCard/GroupList
	 */
	public abstract void remove(Object uid);

	/**
	 * this is for address-completion
	 */
	public abstract HeaderItemList searchPattern(String pattern);

	/**
	 * return ContactCard containing every information 
	 * we have,
	 * use this method for the EditContact-Dialog
	 */
	public abstract DefaultCard get(Object uid);

	public abstract boolean exists(String email);

	/**
	 * remove folder from parent
	 */
	/*
	public void removeFolder() {
		// remove treenode from xml-config-file
		FolderItem item = getFolderItem();
		AdapterNode rootNode = item.getRootNode();
		rootNode.remove();
	
		// remove treenode from JTree
		removeFromParent();
	}
	*/

	/**
	 * save header-cache (HeaderItemList)
	 */
	public abstract void save(WorkerStatusController worker) throws Exception;

	/**
	 * load header-cache (HeaderItemList)
	 */
	public abstract void load(WorkerStatusController worker) throws Exception;

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
	public String getTreePath() {
		TreeNode[] treeNode = getPathToRoot(this, 0);

		StringBuffer path = new StringBuffer();

		for (int i = 1; i < treeNode.length; i++) {
			FolderTreeNode folder = (FolderTreeNode) treeNode[i];
			path.append("/" + folder.getName());
		}

		return path.toString();
	}

	public boolean isParent(Folder folder) {

		Folder parent = (Folder) folder.getParent();
		if (parent == null)
			return false;

		//while ( parent.getUid() != 100 )
		while (parent.getFolderItem() != null) {

			if (parent.getUid() == getUid()) {

				return true;
			}

			parent = (Folder) parent.getParent();
		}

		return false;
	}
	
	

	public String getName() {
		String name = null;

		FolderItem item = getFolderItem();
		name = item.get("property", "name");

		return name;
	}

	/**
	 * @see org.columba.modules.mail.folder.FolderTreeNode#setName(String)
	 */
	public void setName(String newName) {

		FolderItem item = getFolderItem();
		item.set("property", "name", newName);

	}

	public String toString() {
		return getName();
	}
}
