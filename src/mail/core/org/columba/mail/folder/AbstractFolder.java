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

import java.util.logging.Logger;

import javax.swing.event.EventListenerList;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.columba.core.util.Lock;
import org.columba.core.xml.XmlElement;
import org.columba.mail.config.FolderItem;
import org.columba.mail.folder.event.FolderEvent;
import org.columba.mail.folder.event.FolderEventDelegator;
import org.columba.mail.folder.event.FolderListener;

/**
 * Represents a treenode and is the abstract class every folder extends.
 * <p>
 * See tree.xml configuration file.
 * 
 * @author fdietz
 */
public abstract class AbstractFolder extends DefaultMutableTreeNode implements IFolder {

	/** JDK 1.4+ logging framework logger, used for logging. */
	private static final Logger LOG = Logger
			.getLogger("org.columba.mail.folder");

	// the next new folder will get this UID
	private static int nextUid = 0;

	// folderitem wraps xml configuration from tree.xml
	protected FolderItem node;

	// locking mechanism
	protected Lock myLock = new Lock();

	// Root folder cache
	private AbstractFolder rootFolder;

	protected EventListenerList listenerList = new EventListenerList();

	public AbstractFolder(String name, String type) {
		super();

		XmlElement defaultElement = new XmlElement("folder");
		defaultElement.addAttribute("type", type);
		defaultElement.addAttribute("uid", Integer.toString(nextUid++));
		defaultElement.addElement(new XmlElement("property"));

		setConfiguration(new FolderItem(defaultElement));
		try {
			setName(name);
		} catch (Exception e) {
			LOG.severe(e.getMessage());
		}

		// register interest on tree node changes
		addFolderListener(FolderEventDelegator.getInstance());
	}

	public AbstractFolder() {
		super();

		// register interest on tree node changes
		addFolderListener(FolderEventDelegator.getInstance());
	}

	public AbstractFolder(FolderItem node) {
		super();
		setConfiguration(node);

		// register interest on tree node changes
		addFolderListener(FolderEventDelegator.getInstance());
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
	 * Propagates an event to all registered listeners notifying them that this
	 * folder has been renamed.
	 */
	protected void fireFolderPropertyChanged() {
		FolderEvent e = new FolderEvent(this);
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();

		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == FolderListener.class) {
				((FolderListener) listeners[i + 1]).folderPropertyChanged(e);
			}
		}
	}

	/**
	 * Propagates an event to all registered listeners notifying them that a
	 * subfolder has been added to this folder.
	 */
	protected void fireFolderAdded(AbstractFolder folder) {
		FolderEvent e = new FolderEvent(this, folder);
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();

		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == FolderListener.class) {
				((FolderListener) listeners[i + 1]).folderAdded(e);
			}
		}
	}

	/**
	 * Propagates an event to all registered listeners notifying them that this
	 * folder has been removed from its parent folder. This method removes all
	 * registered listeners.
	 */
	protected void fireFolderRemoved() {
		FolderEvent e = new FolderEvent(this, this);
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();

		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == FolderListener.class) {
				((FolderListener) listeners[i + 1]).folderRemoved(e);
				listenerList.remove(FolderListener.class,
						(FolderListener) listeners[i + 1]);
			}
		}
	}

	/**
	 * Method getSelectionTreePath.
	 * 
	 * @return TreePath
	 */
	public TreePath getSelectionTreePath() {
		return new TreePath(getPathToRoot(this, 0));
	}

	/**
	 * Returns the folder's UID.
	 */
	public int getUid() {
		return node.getInteger("uid");
	}

	/**
	 * Returns the folder's configuration.
	 */
	public FolderItem getConfiguration() {
		return node;
	}

	/**
	 * Sets the folder's configuration.
	 */
	public void setConfiguration(FolderItem node) {
		this.node = node;

		try {
			if (node.getInteger("uid") >= nextUid) {
				nextUid = node.getInteger("uid") + 1;
			}
		} catch (NumberFormatException ex) {
			node.set("uid", nextUid++);
		}
	}

	/**
	 * Returns the folder's name.
	 */
	public String getName() {
		String name = null;

		FolderItem item = getConfiguration();
		name = item.get("property", "name");

		return name;
	}

	public String toString() {
		return getName();
	}

	/**
	 * Sets the folder's name. This method notifies registered FolderListeners.
	 */
	public void setName(String newName) throws Exception {
		FolderItem item = getConfiguration();
		item.set("property", "name", newName);
		fireFolderPropertyChanged();
	}
	
	/*
	public FolderCommandReference getCommandReference(FolderCommandReference r) {
		return r;
	}
	*/

	/**
	 * ******************************** locking mechanism
	 * ***************************
	 */
	public boolean tryToGetLock(Object locker) {
		return myLock.tryToGetLock(locker);
	}

	public void releaseLock(Object locker) {
		myLock.release(locker);
	}

	/**
	 * ************************** treenode management
	 * ******************************
	 */
	public void insert(AbstractFolder newFolder, int newIndex) {
		AbstractFolder oldParent = (AbstractFolder) newFolder.getParent();
		int oldIndex = oldParent.getIndex(newFolder);
		oldParent.remove(oldIndex);

		XmlElement oldParentNode = oldParent.getConfiguration().getRoot();
		XmlElement newChildNode = newFolder.getConfiguration().getRoot();
		oldParentNode.removeElement(newChildNode);

		newFolder.setParent(this);
		children.insertElementAt(newFolder, newIndex);

		XmlElement newParentNode = getConfiguration().getRoot();

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
			}
		}

		if (!inserted) {
			if ((j + 1) == newIndex) {
				newParentNode.append(newChildNode);
			}
		}
	}

	/**
	 * Removes this folder from its parent. This method will notify registered
	 * FolderListeners.
	 */
	public void removeFolder() throws Exception {
		// remove XmlElement
		getConfiguration().getRoot().getParent().removeElement(
				getConfiguration().getRoot());

		// notify listeners
		fireFolderRemoved();
		
	}

	/**
	 * Adds a child folder to this folder. This method will notify registered
	 * FolderListeners.
	 */
	public void addSubfolder(AbstractFolder child) throws Exception {
		getConfiguration().getRoot().addElement(
				child.getConfiguration().getRoot());
		fireFolderAdded(child);
	}

	public AbstractFolder findChildWithName(String str, boolean recurse) {
		for (int i = 0; i < getChildCount(); i++) {
			AbstractFolder child = (AbstractFolder) getChildAt(i);
			String name = child.getName();

			if (name.equalsIgnoreCase(str)) {
				return child;
			} else if (recurse) {
				AbstractFolder subchild = child.findChildWithName(str, true);

				if (subchild != null) {
					return subchild;
				}
			}
		}

		return null;
	}

	public AbstractFolder findChildWithUID(int uid, boolean recurse) {
		for (int i = 0; i < getChildCount(); i++) {
			AbstractFolder child = (AbstractMessageFolder) getChildAt(i);
			int childUid = child.getUid();

			if (uid == childUid) {
				return child;
			} else if (recurse) {
				AbstractFolder subchild = child.findChildWithUID(uid, true);

				if (subchild != null) {
					return subchild;
				}
			}
		}

		return null;
	}

	/**
	 * 
	 * AbstractFolder wraps XmlElement
	 * 
	 * all treenode manipulation is passed to the corresponding XmlElement
	 */
	public void moveTo(AbstractFolder newParent) throws Exception {
		// do the same for the XmlElement node
		getConfiguration().getRoot().removeFromParent();	
		
		// do the same for the XmlElement of child
		newParent.getConfiguration().getRoot().addElement(
				getConfiguration().getRoot());

		newParent.fireFolderAdded(this);
	}

	/** ******************* capabilities ************************************* */
	/**
	 * Does this treenode support adding messages?
	 * 
	 * @return true, if this folder is able to contain messages, false otherwise
	 *  
	 */
	public boolean supportsAddMessage() {
		return false;
	}

	/**
	 * Returns true if this folder can have sub folders of the specified type;
	 * false otherwise.
	 * 
	 * @param newFolder
	 *            the folder that is going to be inserted as a child.
	 * @return true if this folder can have sub folders; false otherwise.
	 */
	public boolean supportsAddFolder(AbstractFolder newFolder) {
		return false;
	}

	/**
	 * Returns true if this folder type can be moved around in the folder tree.
	 * 
	 * @return true if this folder type can be moved around in the folder tree.
	 */
	public boolean supportsMove() {
		return false;
	}

	/**
	 * Return the root folder of this folder.
	 * <p>
	 * This is especially useful when using IMAP. IMAP has a root folder which
	 * is labelled with the account name.
	 * 
	 * @return root parent folder of this folder
	 */
	public AbstractFolder getRootFolder() {
		// If rootFolder is not cached traverse the tree
		if (rootFolder == null) {
			AbstractFolder parent = (AbstractFolder) getParent();

			// There is no parent
			if (parent == null) {
				return this;
			}

			if (parent instanceof RootFolder) {
				rootFolder = parent;
			} else {
				rootFolder = parent.getRootFolder();
			}
		}

		return rootFolder;
	}

}