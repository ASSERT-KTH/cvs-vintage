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
package org.columba.mail.gui.tree.selection;

import org.columba.core.command.DefaultCommandReference;
import org.columba.core.gui.selection.SelectionManager;
import org.columba.core.logging.ColumbaLogger;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.folder.FolderTreeNode;

/**
 * Manages tree selection.
 * <p>
 * Actions creating Commands and passing FolderCommandReference directly
 * ask this class for the current selection.
 * <p>
 * If an command wants to select a {@link FolderTreeNode} it should use
 * this class, too.
 * <p>
 * This way you don't have to deal with the swing JTree class anymore.
 *
 * @author fdietz, tstich
 */
public class TreeSelectionManager extends SelectionManager {

	protected FolderTreeNode folder;

	/**
	 * Constructor for TreeSelectionManager.
	 */
	public TreeSelectionManager() {
		super();

	}

	public FolderTreeNode getFolder() {
		return folder;
	}

	public DefaultCommandReference[] getSelection() {
		ColumbaLogger.log.info("folder=" + folder);

		FolderCommandReference[] references = new FolderCommandReference[1];
		references[0] = new FolderCommandReference(folder);

		return references;
	}

}
