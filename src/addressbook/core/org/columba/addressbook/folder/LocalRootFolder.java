package org.columba.addressbook.folder;

import org.columba.addressbook.config.FolderItem;
import org.columba.addressbook.gui.tree.AddressbookTreeNode;
import org.columba.core.command.WorkerStatusController;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class LocalRootFolder extends AddressbookTreeNode {
	
	public LocalRootFolder(FolderItem item) {
		super(item);
	}
		
	/**
	 * @see org.columba.addressbook.gui.tree.AddressbookTreeNode#createChildren(org.columba.core.command.WorkerStatusController)
	 */
	public void createChildren(WorkerStatusController worker) {
	}

}
