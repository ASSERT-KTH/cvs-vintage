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

import java.util.LinkedList;
import java.util.logging.Logger;

import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import org.columba.core.command.DefaultCommandReference;
import org.columba.core.gui.selection.SelectionHandler;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.folder.AbstractFolder;
import org.columba.mail.gui.tree.TreeView;

/**
 * Handles the tree selection.
 * <p>
 * Listens for swing tree selection events and translates TreePath selection to
 * FolderCommandReference.
 * <p>
 * Actions creating Commands and passing FolderCommandReference directly ask
 * {@link TreeSelectionManager}for the selection. They don't talk with the
 * swing JTree.
 * 
 * @author fdietz, tstich
 */
public class TreeSelectionHandler extends SelectionHandler implements
		TreeSelectionListener {

	/** JDK 1.4+ logging framework logger, used for logging. */
	private static final Logger LOG = Logger
			.getLogger("org.columba.mail.gui.tree.selection");

	private static final AbstractFolder[] FOLDER_ARRAY = { null };

	private TreeView view;

	private LinkedList selectedFolders;

	public TreeSelectionHandler(TreeView view) {
		super("mail.tree");
		this.view = view;
		view.addTreeSelectionListener(this);
		selectedFolders = new LinkedList();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.columba.core.gui.util.SelectionHandler#getSelection()
	 */
	public DefaultCommandReference getSelection() {
		if ( selectedFolders.size() == 0) return null;
		
		FolderCommandReference reference = new FolderCommandReference(
				(AbstractFolder) selectedFolders.get(0));

		return reference;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event.TreeSelectionEvent)
	 */
	public void valueChanged(TreeSelectionEvent e) {
		// BUGFIX but don't know why that bug occurs
		if (e.getPath() == null) {
			return;
		}

		// If the tree is in a DND action then we dont need to update all
		// listeners, since this only a temporary folder selection.
		if (view.isInDndAction()) {
			return;
		}

		for (int i = 0; i < e.getPaths().length; i++) {
			if (e.getPaths()[i].getLastPathComponent() instanceof AbstractFolder) {
				AbstractFolder folder = (AbstractFolder) e.getPaths()[i]
						.getLastPathComponent();

				if (e.isAddedPath(i)) {
					LOG.info("Folder added to Selection= " + folder.getName());
					selectedFolders.add(folder);
				} else {
					LOG.info("Folder removed from Selection= "
							+ folder.getName());
					selectedFolders.remove(folder);
				}
			}
		}

		fireSelectionChanged(new TreeSelectionChangedEvent(
				(AbstractFolder[]) selectedFolders.toArray(FOLDER_ARRAY)));
	}

	public void setSelection(DefaultCommandReference selection) {
		view.clearSelection();
		view.requestFocus();

		TreePath path = ((FolderCommandReference) selection).getFolder()
				.getSelectionTreePath();
		view.setLeadSelectionPath(path);
		view.setAnchorSelectionPath(path);
		view.expandPath(path);

		//view.setSelectionPaths(path);
	}
}