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

import java.util.Vector;

import org.columba.core.command.DefaultCommandReference;
import org.columba.core.logging.ColumbaLogger;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.folder.Folder;
import org.columba.mail.folder.FolderTreeNode;
import org.columba.mail.gui.tree.selection.TreeSelectionListener;
import org.columba.mail.gui.tree.selection.TreeSelectionManager;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class TableSelectionManager extends TreeSelectionManager implements TreeSelectionListener{

	// these uids are MessageNode[] !!!!!
	protected Object[] uids;
	protected Object[] oldUids;
	
	protected Vector messageListenerList;
	
	/**
	 * Constructor for TableSelectionManager.
	 */
	public TableSelectionManager() {
		super();
		messageListenerList = new Vector();
	}
	
	public void addMessageSelectionListener(MessageSelectionListener listener) {
		messageListenerList.add(listener);
	}

	

	public void fireMessageSelectionEvent(
		Object[] oldUidList,
		Object[] newUidList) {
		oldUids = oldUidList;
		uids = newUidList;

		for (int i = 0; i < messageListenerList.size(); i++) {
			MessageSelectionListener l =
				(MessageSelectionListener) messageListenerList.get(i);
			l.messageSelectionChanged(uids);
		}
	}
	
	public Object[] getUids() {
		return uids;
	}
	
	public DefaultCommandReference[] getSelection()
	{
		FolderCommandReference[] references = new FolderCommandReference[1];
		references[0] = new FolderCommandReference((Folder) getFolder(), uids);

		return references;
	}
	
	public void folderSelectionChanged( FolderTreeNode treeNode )
	{
		ColumbaLogger.log.debug("new folder selection:"+treeNode.toString());
		
		fireFolderSelectionEvent( folder, treeNode );
		
		folder = treeNode;
	}

	/**
	 * @return Object[]
	 */
	public Object[] getOldUids() {
		return oldUids;
	}

}
