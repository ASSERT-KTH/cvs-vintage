package org.columba.mail.folder;

import java.util.Hashtable;

import org.columba.core.command.WorkerStatusController;
import org.columba.core.config.AdapterNode;
import org.columba.mail.config.FolderItem;

/**
 * @author Timo Stich (tstich@users.sourceforge.net)
 * 
 */
public class Root extends FolderTreeNode {

	/**
	 * Constructor for Root.
	 * @param node
	 */
	public Root(AdapterNode node) {
		super(node);
	}

	/**
	 * @see org.columba.modules.mail.folder.FolderTreeNode#instanceNewChildNode(AdapterNode, FolderItem)
	 */
	public Folder instanceNewChildNode(AdapterNode node, FolderItem item) {
		return null;
	}

	/**
	 * @see org.columba.modules.mail.folder.FolderTreeNode#getParameter()
	 */
	public Hashtable getAttributes() {
		return null;
	}
	
	public void createChildren( WorkerStatusController c )
	{
	}
	
	

}
