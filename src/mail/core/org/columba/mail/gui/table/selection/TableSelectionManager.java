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
package org.columba.mail.gui.table.selection;

import org.columba.core.command.DefaultCommandReference;
import org.columba.core.gui.selection.SelectionChangedEvent;
import org.columba.core.gui.selection.SelectionListener;
import org.columba.core.gui.selection.SelectionManager;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.folder.Folder;
import org.columba.mail.gui.tree.selection.TreeSelectionChangedEvent;

/**
 * Manager of table selection.
 * <p>
 * Note: TableSelectionManager listens for TreeSelectionChanges for itself
 *
 * @author fdietz
 */
public class TableSelectionManager
	extends SelectionManager
	implements SelectionListener {

	// these uids are MessageNode[] !!!!!
	private Object[] uids;
	private Object[] oldUids;
	
	private Folder folder;

	/**
	 * Constructor for TableSelectionManager.
	 */
	public TableSelectionManager() {
		super();

	}

	public Object[] getUids() {
		return uids;
	}

	public DefaultCommandReference[] getSelection() {
		FolderCommandReference[] references = new FolderCommandReference[1];
		references[0] = new FolderCommandReference((Folder) folder, uids);

		return references;
	}

	/*
	public void folderSelectionChanged(FolderTreeNode treeNode) {
		ColumbaLogger.log.debug("new folder selection:" + treeNode.toString());
	
		// FIXME
		//fireFolderSelectionEvent( folder, treeNode );
	
		folder = treeNode;
	}
	*/


	public void selectionChanged(SelectionChangedEvent e) {
		TreeSelectionChangedEvent treeEvent = (TreeSelectionChangedEvent) e;

		// we are only interested in folders containing messages 
		// meaning of instance Folder and not of instance FolderTreeNode
		// -> casting here to Folder
		if (treeEvent.getSelected()[0] != null)
			folder = (Folder) treeEvent.getSelected()[0];
	}

	/**
	 * @return Object[]
	 */
	public Object[] getOldUids() {
		return oldUids;
	}

}
