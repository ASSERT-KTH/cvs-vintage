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
package org.columba.mail.facade;

import java.util.Enumeration;

import org.columba.mail.folder.IMailFolder;
import org.columba.mail.folder.IMailbox;
import org.columba.mail.gui.tree.FolderTreeModel;

/**
 * Provides access to mailbox folders.
 * 
 * @author fdietz
 */
public class FolderFacade implements IFolderFacade {

	/**
	 * Get folder with uid.
	 * 
	 * @param uid
	 *            folder uid
	 * @return folder
	 */
	public IMailFolder getFolder(int uid) {
		return (IMailFolder) FolderTreeModel.getInstance().getFolder(uid);
	}

	/**
	 * Get folder with name.
	 * 
	 * @param name
	 *            folder name
	 * @return folder
	 */
	public IMailFolder getFolder(String name) {
		return (IMailFolder) FolderTreeModel.getInstance().findFolder(
				(IMailFolder) FolderTreeModel.getInstance().getRoot(), name);
	}

	/**
	 * Get breadth-first enumeration of all mailbox folders.
	 * 
	 * @return	breadth-first enumeration of folders
	 */
	public Enumeration getBreadthFirstEnumeration() {
		IMailFolder root = (IMailFolder) FolderTreeModel.getInstance()
				.getRoot();
		return root.breadthFirstEnumeration();
	}

	/**
	 * Get depth-first enumeration of all mailbox folders.
	 * 
	 * @return	depth-first enumeration of folders
	 */
	public Enumeration getDepthFirstEnumeration() {
		IMailFolder root = (IMailFolder) FolderTreeModel.getInstance()
				.getRoot();
		return root.depthFirstEnumeration();
	}

	/**
	 * Get local Inbox folder.
	 * 
	 * @return		inbox folder
	 */
	public IMailbox getLocalInboxFolder() {
		return (IMailbox) FolderTreeModel.getInstance().getFolder(101);
	}

	/**
	 * Get local Trash folder.
	 * 
	 * @return		trash folder
	 */
	public IMailbox getLocalTrashFolder() {
		return (IMailbox) FolderTreeModel.getInstance().getFolder(105);
	}

	/**
	 * Get local Drafts folder.
	 * 
	 * @return	drafts folder
	 */
	public IMailbox getLocalDraftsFolder() {
		return (IMailbox) FolderTreeModel.getInstance().getFolder(102);
	}

	/**
	 * Get local Templates folder.
	 * 
	 * @return		templates folder
	 */
	public IMailbox getLocalTemplatesFolder() {
		return (IMailbox) FolderTreeModel.getInstance().getFolder(107);
	}

	
	/**
	 * Get local outbox folder.
	 * 
	 * @return		outbox folder
	 */
	public IMailbox getLocalOutboxFolder() {
		return (IMailbox) FolderTreeModel.getInstance().getFolder(103);
	}

	/**
	 * Get local sent folder.
	 * 
	 * @return		sent folder
	 */
	public IMailbox getLocalSentFolder() {
		return (IMailbox) FolderTreeModel.getInstance().getFolder(104);
	}

}