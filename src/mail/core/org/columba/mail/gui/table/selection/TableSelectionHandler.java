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

import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.TreePath;

import org.columba.core.command.DefaultCommandReference;
import org.columba.core.gui.selection.SelectionHandler;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.folder.Folder;
import org.columba.mail.gui.table.TableView;
import org.columba.mail.gui.table.util.MessageNode;

public class TableSelectionHandler extends SelectionHandler
//implements TreeSelectionListener
implements ListSelectionListener {

	private TableView view;
	private LinkedList messages;
	private Folder folder;

	private final static MessageNode[] messageNodeArray = { null };

	/**
	 * @param id
	 */
	public TableSelectionHandler(TableView view) {
		super("mail.table");
		this.view = view;

		//view.getTree().addTreeSelectionListener(this);

		view.getSelectionModel().addListSelectionListener(this);

		messages = new LinkedList();
	}

	/* (non-Javadoc)
	 * @see org.columba.core.gui.util.SelectionHandler#getSelection()
	 */
	public DefaultCommandReference[] getSelection() {
		FolderCommandReference[] references = new FolderCommandReference[1];

		references[0] = new FolderCommandReference(folder, getUidArray());

		return references;
	}

	/* (non-Javadoc)
	 * @see org.columba.core.gui.util.SelectionHandler#setSelection(org.columba.core.command.DefaultCommandReference[])
	 */
	public void setSelection(DefaultCommandReference[] selection) {
		FolderCommandReference ref = (FolderCommandReference) selection[0];

		folder = (Folder) ref.getFolder();

	}

	/* (non-Javadoc)
	 * @see javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event.TreeSelectionEvent)
	 */

	/*
	public void valueChanged(TreeSelectionEvent e) {
	
		for (int i = 0; i < e.getPaths().length; i++) {
			if (e.getPaths()[i].getLastPathComponent()
				instanceof MessageNode) {
				MessageNode message =
					(MessageNode) e.getPaths()[i].getLastPathComponent();
				if (e.isAddedPath(i)) {
					ColumbaLogger.log.debug(
						"Message added to Selection= " + message.getUid());
					messages.add(message);
	
				} else {
					ColumbaLogger.log.debug(
						"Message removed from Selection= " + message.getUid());
					messages.remove(message);
				}
			}
		}
	
		fireSelectionChanged(
			new TableSelectionChangedEvent(folder, getUidArray()));
	
	}
	*/
	/**
	 * Sets the folder.
	 * @param folder The folder to set
	 */
	public void setFolder(Folder folder) {
		this.folder = folder;
	}

	private Object[] getUidArray() {
		Object[] result = new Object[messages.size()];
		ListIterator it = messages.listIterator();

		int i = 0;
		while (it.hasNext()) {
			result[i++] = ((MessageNode) it.next()).getUid();
		}

		return result;
	}
	/* (non-Javadoc)
	 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
	 */
	public void valueChanged(ListSelectionEvent e) {

		messages = new LinkedList();

		ListSelectionModel lsm = (ListSelectionModel) e.getSource();
		if (lsm.isSelectionEmpty()) {
			//no rows are selected
		} else {
			int[] rows = view.getSelectedRows();

			for (int i = 0; i < rows.length; i++) {
				TreePath path = view.getTree().getPathForRow(rows[i]);
				MessageNode node = (MessageNode) path.getLastPathComponent();
				messages.add(node);
			}
		}

		fireSelectionChanged(
			new TableSelectionChangedEvent(folder, getUidArray()));

	}

}
