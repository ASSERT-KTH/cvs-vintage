// The contents of this file are subject to the Mozilla Public License Version
// 1.1
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
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.addressbook.gui.table;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;

import org.columba.addressbook.folder.AbstractFolder;
import org.columba.addressbook.folder.ContactStorage;
import org.columba.addressbook.folder.FolderEvent;
import org.columba.addressbook.folder.FolderListener;
import org.columba.addressbook.gui.frame.AddressbookFrameMediator;
import org.columba.addressbook.gui.table.model.AddressbookTableModel;
import org.columba.addressbook.gui.table.model.SortDecorator;
import org.columba.addressbook.gui.tree.AddressbookTreeNode;
import org.columba.addressbook.model.ContactItem;
import org.columba.core.gui.focus.FocusOwner;
import org.columba.core.gui.util.ErrorDialog;
import org.columba.core.main.MainInterface;

/**
 * @author fdietz
 */
public class TableController implements TreeSelectionListener, FolderListener,
		FocusOwner {

	private TableView view;

	private AddressbookFrameMediator mediator;

	private AddressbookTableModel addressbookModel;

	private FilterToolbar toolbar;

	private SortDecorator sortDecorator;

	private AddressbookTreeNode selectedFolder;

	/**
	 *  
	 */
	public TableController(AddressbookFrameMediator mediator) {
		super();

		this.mediator = mediator;

		addressbookModel = new AddressbookTableModel();

		sortDecorator = new SortDecorator(addressbookModel);

		/*
		 * filteredView = new TableModelFilteredView(addressbookModel); sorter =
		 * new TableModelSorter(addressbookModel);
		 * 
		 * addressbookModel.registerPlugin(filteredView);
		 * addressbookModel.registerPlugin(sorter);
		 */
		view = new TableView(this, sortDecorator);

		addMouseListenerToHeaderInTable();

		//view.setModel(addressbookModel);
		// TODO: move outside TableController
		//toolbar = new FilterToolbar(this);

		// register as focus owner
		MainInterface.focusManager.registerComponent(this);
	}

	/**
	 * Add MouseListener to JTableHeader to sort table based on clicked column
	 * header.
	 *  
	 */
	protected void addMouseListenerToHeaderInTable() {
		final JTable tableView = getView();

		tableView.setColumnSelectionAllowed(false);

		MouseAdapter listMouseListener = new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				TableColumnModel columnModel = tableView.getColumnModel();
				int viewColumn = columnModel.getColumnIndexAtX(e.getX());
				int column = tableView.convertColumnIndexToModel(viewColumn);

				if ((e.getClickCount() == 1) && (column != -1)) {
					//controller.getSorter().sort(column);
					sortDecorator.sort(column);

					/*
					 * sortDecorator.tableChanged( new
					 * TableModelEvent(addressbookModel));
					 */

					//addressbookModel.update();
					//mainInterface.mainFrame.getMenu().updateSortMenu();
				}
			}
		};

		JTableHeader th = tableView.getTableHeader();
		th.addMouseListener(listMouseListener);
	}

	/**
	 * @return AddressbookFrameController
	 */
	public AddressbookFrameMediator getMediator() {
		return mediator;
	}

	/**
	 * @return TableView
	 */
	public TableView getView() {
		return view;
	}

	/**
	 * Update table on tree selection changes.
	 */
	public void valueChanged(TreeSelectionEvent e) {
		AddressbookTreeNode node = (AddressbookTreeNode) e.getPath()
				.getLastPathComponent();

		if (node == null) {
			return;
		}

		if (node instanceof ContactStorage) {
			try {

				((AbstractFolder) node).removeFolderListener(this);

				selectedFolder = node;
				((AbstractFolder) node).addFolderListener(this);

				sortDecorator
						.setContactItemMap(((AbstractFolder) selectedFolder)
								.getContactItemMap());
			} catch (Exception e1) {
				if ( MainInterface.DEBUG)
					e1.printStackTrace();
				
				new ErrorDialog(e1.getMessage(), e1);
			}
		} else {
			sortDecorator.setContactItemMap(null);
		}
	}

	/**
	 * Get selected uids.
	 * 
	 * @return
	 */
	public Object[] getUids() {
		int[] rows = getView().getSelectedRows();
		Object[] uids = new Object[rows.length];

		ContactItem item;

		for (int i = 0; i < rows.length; i++) {
			item = (ContactItem) sortDecorator.getContactItem(rows[i]);

			Object uid = item.getUid();
			uids[i] = uid;
		}

		return uids;
	}

	/**
	 * @return Returns the addressbookModel.
	 */
	public AddressbookTableModel getAddressbookModel() {
		return addressbookModel;
	}

	public ContactItem getSelectedItem() {
		int row = getView().getSelectedRow();

		// we use the SortDecorator, because the indices are sorted
		ContactItem item = (ContactItem) sortDecorator.getContactItem(row);

		return item;
	}
	

	/** ********************** FolderListener ***************** */

	/**
	 * @see org.columba.addressbook.folder.FolderListener#itemAdded(org.columba.addressbook.folder.FolderEvent)
	 */
	public void itemAdded(FolderEvent e) {
		getAddressbookModel().update();

	}

	/**
	 * @see org.columba.addressbook.folder.FolderListener#itemChanged(org.columba.addressbook.folder.FolderEvent)
	 */
	public void itemChanged(FolderEvent e) {
		getAddressbookModel().update();

	}

	/**
	 * @see org.columba.addressbook.folder.FolderListener#itemRemoved(org.columba.addressbook.folder.FolderEvent)
	 */
	public void itemRemoved(FolderEvent e) {
		getAddressbookModel().update();

	}

	/** ************* FocusOwner Implementation ****************** */

	/**
	 * @see org.columba.core.gui.focus.FocusOwner#copy()
	 */
	public void copy() {

	}

	/**
	 * @see org.columba.core.gui.focus.FocusOwner#cut()
	 */
	public void cut() {

	}

	/**
	 * @see org.columba.core.gui.focus.FocusOwner#delete()
	 */
	public void delete() {

	}

	/**
	 * @see org.columba.core.gui.focus.FocusOwner#getComponent()
	 */
	public JComponent getComponent() {
		return getView();
	}

	/**
	 * @see org.columba.core.gui.focus.FocusOwner#isCopyActionEnabled()
	 */
	public boolean isCopyActionEnabled() {

		return false;
	}

	/**
	 * @see org.columba.core.gui.focus.FocusOwner#isCutActionEnabled()
	 */
	public boolean isCutActionEnabled() {

		return false;
	}

	/**
	 * @see org.columba.core.gui.focus.FocusOwner#isDeleteActionEnabled()
	 */
	public boolean isDeleteActionEnabled() {

		return false;
	}

	/**
	 * @see org.columba.core.gui.focus.FocusOwner#isPasteActionEnabled()
	 */
	public boolean isPasteActionEnabled() {

		return false;
	}

	/**
	 * @see org.columba.core.gui.focus.FocusOwner#isRedoActionEnabled()
	 */
	public boolean isRedoActionEnabled() {

		return false;
	}

	/**
	 * @see org.columba.core.gui.focus.FocusOwner#isSelectAllActionEnabled()
	 */
	public boolean isSelectAllActionEnabled() {
		if (getView().getRowCount() > 0) {
			return true;
		}

		return false;
	}

	/**
	 * @see org.columba.core.gui.focus.FocusOwner#isUndoActionEnabled()
	 */
	public boolean isUndoActionEnabled() {

		return false;
	}

	/**
	 * @see org.columba.core.gui.focus.FocusOwner#paste()
	 */
	public void paste() {

	}

	/**
	 * @see org.columba.core.gui.focus.FocusOwner#redo()
	 */
	public void redo() {

	}

	/**
	 * @see org.columba.core.gui.focus.FocusOwner#selectAll()
	 */
	public void selectAll() {
		 getView().selectAll();
	}

	/**
	 * @see org.columba.core.gui.focus.FocusOwner#undo()
	 */
	public void undo() {

	}
}