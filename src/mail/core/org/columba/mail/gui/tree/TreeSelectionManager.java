// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Library General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

package org.columba.mail.gui.tree;

import java.util.Vector;

import org.columba.core.command.DefaultCommandReference;
import org.columba.core.gui.util.SelectionManager;
import org.columba.core.logging.ColumbaLogger;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.folder.FolderTreeNode;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class TreeSelectionManager extends SelectionManager {

	protected FolderTreeNode folder;
	
	protected Vector treeListenerList;
	
	/**
	 * Constructor for TreeSelectionManager.
	 */
	public TreeSelectionManager() {
		super();
		treeListenerList = new Vector();
	}
	
	public FolderTreeNode getFolder() {
		return folder;
	}
	
	public void addFolderSelectionListener(FolderSelectionListener listener) {
		treeListenerList.add(listener);
	}
	
	public void fireFolderSelectionEvent(
		FolderTreeNode oldFolder,
		FolderTreeNode newFolder) {
		folder = newFolder;

		for (int i = 0; i < treeListenerList.size(); i++) {
			FolderSelectionListener l =
				(FolderSelectionListener) treeListenerList.get(i);
			l.folderSelectionChanged(newFolder);
		}
	}
	
	public DefaultCommandReference[] getSelection()
	{
		ColumbaLogger.log.info("folder="+folder);
		
		FolderCommandReference[] references = new FolderCommandReference[1];
		references[0] = new FolderCommandReference(folder);

		return references;
	}

}
