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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTable;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.columba.addressbook.config.AdapterNode;
import org.columba.addressbook.config.FolderItem;
import org.columba.addressbook.folder.Folder;
import org.columba.addressbook.folder.HeaderItem;
import org.columba.addressbook.folder.HeaderItemList;
import org.columba.addressbook.gui.table.util.AddressbookCommonHeaderRenderer;
import org.columba.addressbook.gui.table.util.HeaderColumn;
import org.columba.addressbook.gui.table.util.HeaderColumnInterface;
import org.columba.addressbook.gui.table.util.TypeHeaderColumn;
import org.columba.addressbook.main.AddressbookInterface;
import org.columba.addressbook.util.AddressbookResourceLoader;
import org.columba.core.config.TableItem;

public class TableView extends JTable {

	private AdapterNode node;

	private TableController controller;

	private AddressbookTableModel addressbookModel;

	public TableView(TableController controller) {
		this.controller = controller;

		this.addressbookModel = controller.getAddressbookModel();

		setIntercellSpacing(new Dimension(0, 0));
		setShowGrid(false);
		setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

		addMouseListenerToHeaderInTable();

		setLayout(new BorderLayout());

	}

	public void setupColumn(String name) {
		try {
			TableColumn tc = getColumn(name);

			if (tc != null) {
				HeaderColumnInterface column =
					addressbookModel.getHeaderColumn(name);

				//System.out.println("renderer set:");
				tc.setCellRenderer((TableCellRenderer) column);

				if (column.getValueString() != null) {
					tc.setHeaderRenderer(
						new AddressbookCommonHeaderRenderer(
							column.getValueString(),
							controller.getSorter()));
				} else {
					tc.setHeaderRenderer(
						new AddressbookCommonHeaderRenderer(
							column.getName(),
							controller.getSorter()));
				}

				if (column.getColumnSize() != -1) {
					//System.out.println("size is locked:" +
					// column.getColumnSize());
					tc.setHeaderRenderer(
						new AddressbookCommonHeaderRenderer(
							"",
							controller.getSorter()));

					tc.setMaxWidth(column.getColumnSize());
					tc.setMinWidth(column.getColumnSize());
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	protected void addMouseListenerToHeaderInTable() {
		final JTable tableView = this;

		tableView.setColumnSelectionAllowed(false);

		MouseAdapter listMouseListener = new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				TableColumnModel columnModel = tableView.getColumnModel();
				int viewColumn = columnModel.getColumnIndexAtX(e.getX());
				int column = tableView.convertColumnIndexToModel(viewColumn);

				if ((e.getClickCount() == 1) && (column != -1)) {
					controller.getSorter().sort(column);
					addressbookModel.update();

					//mainInterface.mainFrame.getMenu().updateSortMenu();
				}
			}
		};

		JTableHeader th = tableView.getTableHeader();
		th.addMouseListener(listMouseListener);
	}

	public void setupRenderer() {
		TableItem headerItemList =
			new TableItem(
				AddressbookInterface.config.get("options").getElement(
					"/options/gui/table"));

		HeaderColumn c;

		for (int i = 0; i < headerItemList.count(); i++) {
			org.columba.core.config.HeaderItem e =
				headerItemList.getHeaderItem(i);

			String name = e.get("name");

			//System.out.println("name:" + name);
			int size = e.getInteger("size");
			int position = e.getInteger("position");
			boolean enabled = e.getBoolean("enabled");

			if (enabled == false) {
				continue;
			}

			int index = name.indexOf(";");

			if (index != -1) {
				String prefix =
					AddressbookResourceLoader.getString(
						"header",
						name.substring(0, index));
				String suffix =
					AddressbookResourceLoader.getString(
						"header",
						name.substring(index + 1, name.length()));

				c = new HeaderColumn(name, prefix + "(" + suffix + ")");
			} else if (name.equals("type")) {
				c =
					new TypeHeaderColumn(
						name,
						AddressbookResourceLoader.getString("header", name));

				//System.out.println("typeHeadercolumn created");
			} else {
				c =
					new HeaderColumn(
						name,
						AddressbookResourceLoader.getString("header", name));
			}

			addressbookModel.addColumn(c);
		}

		for (int i = 0; i < headerItemList.count(); i++) {
			org.columba.core.config.HeaderItem e =
				headerItemList.getHeaderItem(i);

			String name = e.get("name");
			boolean enabled = e.getBoolean("enabled");

			if (enabled == false) {
				continue;
			}

			setupColumn(name);
		}
	}

	public void setFolder(Folder folder) {
		Folder f = (Folder) folder;

		if (f == null) {
			addressbookModel.setHeaderList(null);

			return;
		}

		FolderItem item = f.getFolderItem();
		HeaderItemList list = folder.getHeaderItemList();

		addressbookModel.setHeaderList(list);

	}

	public void setHeaderItemList(HeaderItemList list) {
		addressbookModel.setHeaderList(list);
	}

	public void update() {
		addressbookModel.update();

	}

	public HeaderItem getSelectedItem() {
		int row = getSelectedRow();

		HeaderItem item = addressbookModel.getHeaderItem(row);

		return item;
	}

	public Object getSelectedUid() {
		//AdapterNode child = null;
		int row = getSelectedRow();

		if (row == -1) {
			return null;
		}

		HeaderItem item = addressbookModel.getHeaderItem(row);
		Object uid = item.getUid();

		return uid;
	}

}
