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

import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import org.columba.addressbook.folder.Folder;
import org.columba.addressbook.folder.HeaderItem;
import org.columba.addressbook.gui.frame.AddressbookFrameMediator;
import org.columba.addressbook.gui.table.util.TableModelFilteredView;
import org.columba.addressbook.gui.table.util.TableModelSorter;

/**
 * 
 * @author fdietz 
 */
public class TableController implements TreeSelectionListener {
	private TableView view;
	private AddressbookFrameMediator mediator;

	private AddressbookTableModel addressbookModel;
	private TableModelFilteredView filteredView;
	private TableModelSorter sorter;

	private FilterToolbar toolbar;
	/**
	 *  
	 */
	public TableController(AddressbookFrameMediator mediator) {
		super();

		this.mediator = mediator;

		addressbookModel = new AddressbookTableModel();

		filteredView = new TableModelFilteredView(addressbookModel);
		sorter = new TableModelSorter(addressbookModel);

		addressbookModel.registerPlugin(filteredView);
		addressbookModel.registerPlugin(sorter);

		view = new TableView(this);
		view.setModel(addressbookModel);
		
		// TODO: move outside TableController
		toolbar = new FilterToolbar(this);
		

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
		Object o = e.getPath().getLastPathComponent();

		if (o == null) {
			return;
		}

		if (o instanceof Folder) {
			getView().setFolder((Folder) o);
		} else {
			getView().setFolder(null);
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

		HeaderItem item;

		for (int i = 0; i < rows.length; i++) {
			item = addressbookModel.getHeaderItem(rows[i]);

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

	/**
	 * @return Returns the filteredView.
	 */
	public TableModelFilteredView getFilteredView() {
		return filteredView;
	}

	/**
	 * @return Returns the sorter.
	 */
	public TableModelSorter getSorter() {
		return sorter;
	}

}
