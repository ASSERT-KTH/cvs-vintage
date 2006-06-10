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

import org.columba.api.command.ICommandReference;
import org.columba.api.selection.ISelectionListener;
import org.columba.api.selection.SelectionChangedEvent;
import org.columba.core.selection.SelectionHandler;
import org.columba.mail.command.MailFolderCommandReference;
import org.columba.mail.folder.IMailFolder;
import org.columba.mail.gui.table.IMessageNode;
import org.columba.mail.gui.table.ITableController;
import org.columba.mail.gui.table.model.MessageNode;
import org.columba.mail.gui.tree.selection.TreeSelectionChangedEvent;

/**
 * TableSelectionHandler adds another abstraction layer to the swing JTable
 * selection model.
 * <p>
 * It is responsible for providing a mapping between swing table rows or tree
 * nodes into message UIDs and back.
 * <p>
 * Additionally it is able to encapsulate a message object transparently for
 * every action in Columba. This means actions don't need to care about if this
 * message is actually from a folder-/table-selection, as it usually is
 * (example: user selects a message in the table and does a move operation on
 * it), or if it is just a temporary message ( example: pgp-decrypted message ).
 * <p>
 * For this reason it uses a temporary folder to save such a message and provide
 * actions with the correctly mapped MailFolderCommandReference[] object.
 * 
 * 
 * 
 * @author fdietz
 */
public class TableSelectionHandler extends SelectionHandler implements
		ListSelectionListener, ISelectionListener {
	public static final String HANDLER_ID = "mail.table";


	private LinkedList<IMessageNode> messages;

	private IMailFolder folder;

	// if this is set to true, we use the local selection, instead
	// of using the table selection
	private boolean useLocalSelection;

	private MailFolderCommandReference local;

	private ITableController tableController;
	/**
	 * @param id
	 */
	public TableSelectionHandler(ITableController tableController) {
		super(TableSelectionHandler.HANDLER_ID);

		this.tableController = tableController;

		tableController.getListSelectionModel().addListSelectionListener(this);

		messages = new LinkedList<IMessageNode>();

		useLocalSelection = false;
	}

	/**
	 * 
	 * @see org.columba.core.gui.util.SelectionHandler#getSelection()
	 */
	public ICommandReference getSelection() {
		if (useLocalSelection == true) {
			return local;
		}

		MailFolderCommandReference reference = new MailFolderCommandReference(
				folder, getUidArray());

		return reference;
	}

	/**
	 * 
	 * @see org.columba.core.gui.util.SelectionHandler#setSelection(ICommandReference)
	 */
	public void setSelection(ICommandReference selection) {
		MailFolderCommandReference ref = (MailFolderCommandReference) selection;

		folder = (IMailFolder) ref.getSourceFolder();

		useLocalSelection = false;

		/*
		 * if (ref.getUids() != null) { messages.clear(); for (int i = 0; i <
		 * ref.getUids().length; i++) { Object uid = ref.getUids()[i];
		 * messages.add(tableController.getMessageNode(uid)); } }
		 */

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

	/**
	 * 
	 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
	 */
	public void valueChanged(ListSelectionEvent e) {

		useLocalSelection = false;

		// user is still manipulating the selection
		if (e.getValueIsAdjusting() == true) {
			return;
		}

		messages = new LinkedList<IMessageNode>();

		ListSelectionModel lsm = (ListSelectionModel) e.getSource();

		if (lsm.isSelectionEmpty()) {
			// no rows are selected

		} else {
			int[] rows = tableController.getSelectedRows();

			for (int i = 0; i < rows.length; i++) {
				TreePath path = tableController.getPathForRow(rows[i]);
				if ( path == null ) continue;
				
				IMessageNode node = (IMessageNode) path.getLastPathComponent();
				messages.add(node);
			}
		}

		fireSelectionChanged(new TableSelectionChangedEvent(folder,
				getUidArray()));

	}

	public void setLocalReference(MailFolderCommandReference r) {
		this.local = r;

		useLocalSelection = true;
	}

	/**
	 * 
	 * @see org.columba.core.gui.util.ISelectionListener#connectionChanged(org.columba.core.gui.util.SelectionChangedEvent)
	 */
	public void selectionChanged(SelectionChangedEvent e) {
		if (((TreeSelectionChangedEvent) e).getSelected().length > 0) {
			folder = ((TreeSelectionChangedEvent) e).getSelected()[0];
		} else {
			folder = null;
		}
	}
}