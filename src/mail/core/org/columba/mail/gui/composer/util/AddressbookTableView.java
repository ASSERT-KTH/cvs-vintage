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
//All Rights Reserved.Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

package org.columba.mail.gui.composer.util;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;

import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableColumn;

import org.columba.addressbook.folder.HeaderItem;
import org.columba.addressbook.gui.table.AddressbookTableModel;
import org.columba.addressbook.util.AddressbookResourceLoader;

/**
 * @author frd
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class AddressbookTableView extends JTable {

	public JComboBox fieldComboBox;

	public AddressbookTableModel addressbookModel;

	public AddressComboBox comboBox;

	public AddressCellEditor addressEditor;

	public AddressbookTableView() {
		initComponents();

		initKeys();

	}

	protected void initKeys() {

	}

	protected void initComponents() {

		addressbookModel = new AddressbookTableModel();

		addressbookModel.editable = true;

		ComboBoxHeaderColumn c1 =
			new ComboBoxHeaderColumn(
				"field",
				AddressbookResourceLoader.getString("header", "field"));
		addressbookModel.addColumn(c1);

		DisplaynameHeaderColumn c2 =
			new DisplaynameHeaderColumn(
				"displayname",
				AddressbookResourceLoader.getString("header", "displayname"));
		addressbookModel.addColumn(c2);

		setModel(addressbookModel);

		addressEditor = new AddressCellEditor(this);

		getColumn("displayname").setCellEditor(addressEditor);

		getColumn("displayname").setCellRenderer(c2);

		TableColumn tc = getColumn("field");
		tc.setMaxWidth(80);
		tc.setMinWidth(80);
		fieldComboBox = new JComboBox();
		fieldComboBox.addItem("To");
		fieldComboBox.addItem("Cc");
		fieldComboBox.addItem("Bcc");
		FieldCellEditor editor = new FieldCellEditor(fieldComboBox, this);
		getColumn("field").setCellEditor(editor);
		getColumn("field").setCellRenderer(new FieldCellRenderer());

		setShowHorizontalLines(true);
		setShowVerticalLines(false);
		setIntercellSpacing(new Dimension(0, 2));
		setRowHeight(getRowHeight() + 4);

		setAutoResizeMode(AUTO_RESIZE_ALL_COLUMNS);
		setTableHeader(null);

	}

	public void initFocus(Component c) {
		(
			(JTextField) addressEditor
				.getEditor()
				.getEditorComponent())
				.setNextFocusableComponent(
			c);
			
		addressEditor.setNextFocusableComponent(
		c);
	}

	public AddressbookTableModel getAddressbookTableModel() {
		return addressbookModel;
	}

	protected void makeVisible(int row, int column) {
		Rectangle r = getCellRect(row, column, true);

		scrollRectToVisible(r);
	}

	protected void focusToTextField() {
		JComboBox box = (JComboBox) getEditorComponent();
		if (box == null)
			return;

		JTextField textfield =
			(JTextField) box.getEditor().getEditorComponent();

		textfield.requestFocus();

	}

	protected void addEmptyRow() {

		if (emptyRowExists() == true)
			return;

		HeaderItem item = new HeaderItem(HeaderItem.CONTACT);
		item.add("displayname", "");
		item.add("field", "To");

		try {
			addressbookModel.addItem(item);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		makeVisible(getRowCount() - 1, 1);

	}

	protected void editLastRow() {

		int row = getRowCount() - 1;

		boolean b = editCellAt(row, 1);

		if (b == true) {
			focusToTextField();
		}

	}

	protected boolean isEmpty(int row) {
		HeaderItem item1 = (HeaderItem) addressbookModel.getValueAt(row, 1);

		String value = (String) item1.get("displayname");

		if (value.length() == 0)
			return true;

		return false;
	}

	protected boolean emptyRowExists() {
		int rowCount = getRowCount();

		if (rowCount == 0)
			return false;

		HeaderItem item1 =
			(HeaderItem) addressbookModel.getValueAt(rowCount - 1, 1);

		String value = (String) item1.get("displayname");

		if (value.length() == 0)
			return true;

		return false;
	}

	public void setHeaderItem(HeaderItem item) {
		int row = getEditingRow();

		addressbookModel.setHeaderItem(row, item);
	}

	public void removeEditingRow() {
		int rowCount = getRowCount();

		int row = getEditingRow();
		int column = getEditingColumn();

		if (row == rowCount - 1)
			return;

		if ((row == -1) || (column == -1)) {
			return;
		} else {
			getCellEditor(row, column).stopCellEditing();

		}

		HeaderItem[] items = new HeaderItem[1];
		items[0] = addressbookModel.getHeaderItem(row);

		addressbookModel.removeItem(items);

		boolean b = false;

		if (row == 0)
			b = editCellAt(0, 1);
		else
			b = editCellAt(row - 1, 1);

		if (b == true) {
			focusToTextField();
		}
	}

	public void appendRow() {
		int rowCount = getRowCount();
		int selected = getSelectedRow();

		if (emptyRowExists() == false) {

			addEmptyRow();
			editLastRow();
		} else
			editLastRow();

	}

	public void cleanupHeaderItemList() {
		for (int i = getRowCount() - 1; i >= 0; i--) {
			boolean b = isEmpty(i);

			if (b == true) {
				HeaderItem[] items = new HeaderItem[1];
				items[0] = addressbookModel.getHeaderItem(i);

				addressbookModel.removeItem(items);
			}
		}
	}

}