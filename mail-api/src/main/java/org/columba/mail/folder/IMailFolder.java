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
package org.columba.mail.folder;

import java.util.Enumeration;

import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.columba.api.plugin.IExtensionInterface;
import org.columba.core.folder.IFolder;
import org.columba.mail.config.IFolderItem;
import org.columba.mail.folder.event.IFolderListener;

/**
 * @author fdietz
 * 
 */
public interface IMailFolder extends MutableTreeNode, IFolder, IExtensionInterface {
	/**
	 * Adds a listener.
	 */
	void addFolderListener(IFolderListener l);

	/**
	 * Removes a previously registered listener.
	 */
	void removeFolderListener(IFolderListener l);

	/**
	 * Method getSelectionTreePath.
	 * 
	 * @return TreePath
	 */
	TreePath getSelectionTreePath();

	/**
	 * Returns the folder's configuration.
	 */
	IFolderItem getConfiguration();

	/**
	 * Sets the folder's configuration.
	 */
	void setConfiguration(IFolderItem node);

	/**
	 * Sets the folder's name. This method notifies registered FolderListeners.
	 */
	void setName(String newName) throws Exception;

	/**
	 * ************************** treenode management
	 * ******************************
	 */
	void insert(IMailFolder newFolder, int newIndex);

	/**
	 * Removes this folder from its parent. This method will notify registered
	 * FolderListeners.
	 */
	void removeFolder() throws Exception;

	/**
	 * Adds a child folder to this folder. This method will notify registered
	 * FolderListeners.
	 */
	void addSubfolder(IMailFolder child) throws Exception;

	/**
	 * 
	 * AbstractFolder wraps XmlElement
	 * 
	 * all treenode manipulation is passed to the corresponding XmlElement
	 */
	void moveTo(IMailFolder child);

	/** ******************* capabilities ************************************* */
	boolean supportsAddMessage();

	/**
	 * Returns true if this folder can have sub folders of the specified type;
	 * false otherwise.
	 * 
	 * @param newFolderType
	 *            the folder that is going to be inserted as a child.
	 * @return true if this folder can have sub folders; false otherwise.
	 */
	boolean supportsAddFolder(String newFolderType);

	/**
	 * Returns true if this folder type can be moved around in the folder tree.
	 * 
	 * @return true if this folder type can be moved around in the folder tree.
	 */
	boolean supportsMove();

	/**
	 * Return the root folder of this folder.
	 * <p>
	 * This is especially useful when using IMAP. IMAP has a root folder which
	 * is labelled with the account name.
	 * 
	 * @return root parent folder of this folder
	 */
	IMailFolder getRootFolder();

	void fireFolderPropertyChanged();

	void fireFolderAdded(IMailFolder folder);

	void fireFolderRemoved();

	TreeNode[] getPath();

	String getTreePath();
	
	Enumeration breadthFirstEnumeration();
	
	Enumeration depthFirstEnumeration();
	
	/**
	 * Add treenode as child to this parent node.
	 * 
	 * @param treeNode	new treenode
	 */
	void add(IMailFolder treeNode);
	
	
	public String getType();
}