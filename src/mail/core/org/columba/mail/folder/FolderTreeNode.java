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
package org.columba.mail.folder;

import javax.swing.ImageIcon;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.columba.core.gui.util.ImageLoader;
import org.columba.core.logging.ColumbaLogger;
import org.columba.core.util.Lock;
import org.columba.core.xml.XmlElement;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.config.FolderItem;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public abstract class FolderTreeNode extends DefaultMutableTreeNode {

	protected final static ImageIcon collapsedIcon =
		ImageLoader.getSmallImageIcon("folder-closed.png");

	protected final static ImageIcon expandedIcon =
		ImageLoader.getSmallImageIcon("folder-open.png");

	protected FolderItem node;
	protected Lock myLock;

	private static int nextUid = 0;

	//private final Class[] FOLDER_ITEM_ARG = new Class[] { FolderItem.class };
	//private final Class[] STRING_ARG = new Class[] { String.class };

	public FolderTreeNode(String name, String type) {
		super();

		XmlElement defaultElement = new XmlElement("folder");
		defaultElement.addAttribute("type", type);
		defaultElement.addAttribute("uid", Integer.toString(nextUid++));
		defaultElement.addElement(new XmlElement("property"));

		setNode(new FolderItem(defaultElement));
		setName(name);

		myLock = new Lock();
	}

	public FolderTreeNode() {
		super();

		myLock = new Lock();
	}

	public FolderTreeNode(FolderItem node) {
		super();
		if (node != null)
			setNode(node);
		myLock = new Lock();
	}

	/**
		 * Method getSelectionTreePath.
		 * @return TreePath
		 */
	public TreePath getSelectionTreePath() {
		//TreeNodeList list = new TreeNodeList( getTreePath() );
		/*
		TreeNode[] treeNodes = getPathToRoot(this, 0);
		TreePath path = new TreePath(treeNodes[0]);
		
		for (int i = 1; i < treeNodes.length; i++) {
			Folder folder = (Folder) treeNodes[i];
			path.pathByAddingChild(folder);
		}
		return path;
		*/
		return new TreePath(getPathToRoot(this, 0));
	}

	public int getUid() {
		return node.getInteger("uid");
	}

	public ImageIcon getCollapsedIcon() {
		return collapsedIcon;
	}

	public ImageIcon getExpandedIcon() {
		return expandedIcon;
	}

	public boolean tryToGetLock(Object locker) {
		return myLock.tryToGetLock(locker);
	}

	public void releaseLock() {
		myLock.release();
	}

	public XmlElement getNode() {
		return node.getRoot();
	}

	public FolderItem getFolderItem() {
		return node;
	}

	public void insert(FolderTreeNode newFolder, int newIndex) {

		FolderTreeNode oldParent = (FolderTreeNode) newFolder.getParent();
		int oldIndex = oldParent.getIndex(newFolder);
		oldParent.remove(oldIndex);

		XmlElement oldParentNode = oldParent.getFolderItem().getRoot();
		XmlElement newChildNode = newFolder.getFolderItem().getRoot();
		oldParentNode.removeElement(newChildNode);

		newFolder.setParent(this);
		children.insertElementAt(newFolder, newIndex);

		XmlElement newParentNode = getFolderItem().getRoot();

		int j = -1;
		boolean inserted = false;
		for (int i = 0; i < newParentNode.count(); i++) {
			XmlElement n = newParentNode.getElement(i);
			String name = n.getName();

			if (name.equals("folder")) {
				j++;
			}

			if (j == newIndex) {
				newParentNode.insertElement(newChildNode, i);
				inserted = true;
				System.out.println("------> adapternode insert correctly");
			}
		}

		if (inserted == false) {
			if (j + 1 == newIndex) {
				newParentNode.append(newChildNode);
				System.out.println("------> adapternode appended correctly");
			}
		}

		//oldParent.fireTreeNodeStructureUpdate();
		//fireTreeNodeStructureUpdate();
	}

	/*
	public void removeFromParent() {
		AdapterNode childAdapterNode = getNode();
		childAdapterNode.remove();
	
		super.removeFromParent();
	}
	*/

	public void removeFolder() throws Exception {
		// remove XmlElement
		getFolderItem().getRoot().getParent().removeElement(
			getFolderItem().getRoot());

		// remove DefaultMutableTreeNode
		removeFromParent();
	}

	public void addSubfolder(FolderTreeNode child) throws Exception {
		add(child);
		getNode().addElement(child.getNode());
	}

	/*
	public void remove(FolderTreeNode childNode) {
		FolderTreeNode childFolder = (FolderTreeNode) childNode;
		AdapterNode childAdapterNode = childFolder.getNode();
		childAdapterNode.remove();
	
		int index = getIndex(childFolder);
		children.removeElementAt(index);
		//fireTreeNodeStructureUpdate();
	
		//return childFolder;
	}
	*/

	public FolderTreeNode findChildWithName(String str, boolean recurse) {
		for (int i = 0; i < getChildCount(); i++) {
			FolderTreeNode child = (FolderTreeNode) getChildAt(i);
			String name = child.getName();

			if (name.equalsIgnoreCase(str)) {
				return child;
			} else if( recurse ){
				FolderTreeNode subchild = child.findChildWithName(str,true);
				if( subchild != null ) {
					return subchild;
				}
			}
		}
		return null;
	}

	public FolderTreeNode findChildWithUID(int uid, boolean recurse ) {
		for (int i = 0; i < getChildCount(); i++) {
			FolderTreeNode child = (Folder) getChildAt(i);
			int childUid = child.getUid();

			if (uid == childUid) {
				return child;
			} else if (recurse) {
				FolderTreeNode subchild = child.findChildWithUID(uid,true);
				if( subchild != null ) {
					return subchild;
				}				
			}
		}
		return null;
	}

	/*
	public void append(Folder newFolder) {
		Folder oldParent = (Folder) newFolder.getParent();
		int oldIndex = oldParent.getIndex(newFolder);
		oldParent.remove(oldIndex);
	
		AdapterNode oldParentNode = oldParent.getNode();
		AdapterNode newChildNode = newFolder.getNode();
		oldParentNode.removeChild(newChildNode);
	
		newFolder.setParent(this);
		children.add(newFolder);
	
		AdapterNode newParentNode = node;
		newParentNode.appendChild(newChildNode);
	
		// oldParent.fireTreeNodeStructureUpdate();
		// fireTreeNodeStructureUpdate();
	}
	*/

	/**
	 * Sets the node.
	 * @param node The node to set
	 */
	public void setNode(FolderItem node) {
		this.node = node;

		try {
			if (node.getInteger("uid") >= nextUid)
				nextUid = node.getInteger("uid") + 1;
		} catch (NumberFormatException ex) {
			node.set("uid", nextUid++);
		}
	}

	/**
		 * Method isParent.
		 * @param folder
		 * @return boolean
		 */

	/*
	public boolean isParent(FolderTreeNode folder) {
	
		FolderTreeNode parent = (FolderTreeNode) folder.getParent();
		if (parent == null)
			return false;
	
		//while ( parent.getUid() != 100 )
		while (parent.getFolderItem() != null) {
	
			if (parent.getUid() == getUid()) {
	
				return true;
			}
	
			parent = (FolderTreeNode) parent.getParent();
		}
	
		return false;
	}
	*/

	/**
	 * 
	 * FolderTreeNode wraps XmlElement
	 * 
	 * all treenode manipulation is passed to the corresponding XmlElement
	 */
	public void append(FolderTreeNode child) {
		ColumbaLogger.log.debug("child=" + child);

		// remove child from parent
		child.removeFromParent();
		// do the same for the XmlElement node
		ColumbaLogger.log.debug(
			"xmlelement=" + child.getFolderItem().getRoot().getName());

		child.getFolderItem().getRoot().removeFromParent();

		// add child to this node
		add(child);
		// do the same for the XmlElement of child
		getFolderItem().getRoot().addElement(child.getFolderItem().getRoot());

	}

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
	 * @see org.columba.modules.mail.folder.FolderTreeNode#setName(String)
	 */
	public void setName(String newName) {

		FolderItem item = getFolderItem();
		item.set("property", "name", newName);

	}

	/**
	 * Method getCommandReference.
	 * 
	 * @param r
	 * @return FolderCommandReference[]
	 */
	public FolderCommandReference[] getCommandReference(FolderCommandReference[] r) {
		return r;
	}

	/*
	public void insert( FolderTreeNode child, int index )
	{
		
		
		super.insert(child, index );
		
		getFolderItem().getRoot().insertElement(child.getFolderItem().getRoot(), index );
		
		
	}
	*/
}
