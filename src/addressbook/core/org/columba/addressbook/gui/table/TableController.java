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

import org.columba.addressbook.folder.Folder;
import org.columba.addressbook.folder.HeaderItem;
import org.columba.addressbook.gui.frame.AddressbookFrameMediator;
import org.columba.addressbook.gui.table.model.AddressbookTableModel;
import org.columba.addressbook.gui.table.model.SortDecorator;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTable;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;


/**
 * @author fdietz
 */
public class TableController implements TreeSelectionListener {
    private TableView view;
    private AddressbookFrameMediator mediator;
    private AddressbookTableModel addressbookModel;
    private FilterToolbar toolbar;
    private SortDecorator sortDecorator;

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
        Object o = e.getPath().getLastPathComponent();

        if (o == null) {
            return;
        }

        if (o instanceof Folder) {
            sortDecorator.setHeaderList(((Folder) o).getHeaderItemList());
        } else {
            sortDecorator.setHeaderList(null);
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
            item = (HeaderItem) sortDecorator.getHeaderItem(rows[i]);

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

    public HeaderItem getSelectedItem() {
        int row = getView().getSelectedRow();

        // we use the SortDecorator, because the indices are sorted
        HeaderItem item = (HeaderItem) sortDecorator.getHeaderItem(row);

        return item;
    }
}
