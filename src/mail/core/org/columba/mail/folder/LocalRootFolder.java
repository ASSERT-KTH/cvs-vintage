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

import org.columba.core.gui.util.ImageLoader;
import org.columba.mail.config.FolderItem;

/**
 * Top-level folder of all local folders. 
 * <p>
 * Only purpose of this folder is to allow for a better structure
 * of the folder hierachy, where local and remote folders are
 * very easy to distinct.
 *
 *  @author fdietz
 */
public class LocalRootFolder extends FolderTreeNode implements RootFolder {

	protected final static ImageIcon rootIcon =
		ImageLoader.getSmallImageIcon("localhost.png");
	//ImageLoader.getSmallImageIcon("16_computer.png");

	public LocalRootFolder(FolderItem item) {
		super(item);	
	}


	public ImageIcon getCollapsedIcon() {
		return rootIcon;
	}

	public ImageIcon getExpandedIcon() {
		return rootIcon;
	}


	/* (non-Javadoc)
	 * @see org.columba.mail.folder.RootFolder#getTrashFolder()
	 */
	public FolderTreeNode getTrashFolder() {
		return findChildWithUID(105,false);
	}

	/* (non-Javadoc)
	 * @see org.columba.mail.folder.RootFolder#getInbox()
	 */
	public FolderTreeNode getInboxFolder() {
		return findChildWithUID(101,false);
	}

}
