package org.columba.addressbook.folder;

import org.columba.addressbook.config.FolderItem;
import org.columba.addressbook.gui.tree.AddressbookTreeNode;
import org.columba.core.command.WorkerStatusController;
import org.columba.core.xml.XmlElement;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class Root extends AddressbookTreeNode {

	FolderItem item;
	public Root(XmlElement node) {
		super(new FolderItem(node));
	}

	/**
	 * @see org.columba.modules.mail.folder.FolderTreeNode#instanceNewChildNode(AdapterNode, FolderItem)
	 */
	public Class getDefaultChild() {
		return null;
	}

	public void createChildren(WorkerStatusController c) {
	}

}