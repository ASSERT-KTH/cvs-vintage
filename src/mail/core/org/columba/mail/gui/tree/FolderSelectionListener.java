package org.columba.mail.gui.tree;

import org.columba.mail.folder.FolderTreeNode;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public interface FolderSelectionListener {
	public void folderSelectionChanged( FolderTreeNode newFolder );
}
