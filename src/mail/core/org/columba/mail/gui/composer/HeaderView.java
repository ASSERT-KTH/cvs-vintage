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
//All Rights Reserved.ndation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
package org.columba.mail.gui.composer;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;

import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableColumn;

import org.columba.addressbook.model.ContactItem;
import org.columba.addressbook.model.HeaderItem;
import org.columba.mail.gui.composer.util.AddressComboBox;
import org.columba.mail.gui.composer.util.DisplaynameEditor;
import org.columba.mail.gui.composer.util.DisplaynameRenderer;
import org.columba.mail.gui.composer.util.FieldEditor;
import org.columba.mail.gui.composer.util.FieldRenderer;
import org.columba.mail.gui.composer.util.RecipientsTableModel;


/**
 * JTable including a nested JComboBox.
 * <p>
 * Table contains two column. The first for choosing To:, Cc: or Bcc:,
 * the second column for recipients;
 * <p>
 * TODO: HeaderView should extend AddressbookTableView !
 *
 * @author fdietz
 */
public class HeaderView extends JTable {
    private HeaderController controller;
    private JComboBox fieldComboBox;
    private RecipientsTableModel model;
    private AddressComboBox comboBox;
    private DisplaynameEditor addressEditor;

    public HeaderView(HeaderController controller) {
        super();

        this.controller = controller;

        model = new RecipientsTableModel();

        setModel(model);

        addressEditor = new DisplaynameEditor(this);

        getColumn("displayname").setCellEditor(addressEditor);
        getColumn("displayname").setCellRenderer(new DisplaynameRenderer());

        TableColumn tc = getColumn("field");
        tc.setMaxWidth(80);
        tc.setMinWidth(80);
        fieldComboBox = new JComboBox();
        fieldComboBox.addItem("To");
        fieldComboBox.addItem("Cc");
        fieldComboBox.addItem("Bcc");

        getColumn("field").setCellEditor(new FieldEditor(fieldComboBox));

        //getColumn("field").setCellEditor(new DefaultCellEditor(fieldComboBox));
        getColumn("field").setCellRenderer(new FieldRenderer());

        setShowHorizontalLines(true);
        setShowVerticalLines(false);
        setIntercellSpacing(new Dimension(0, 2));
        setRowHeight(getRowHeight() + 6);

        setAutoResizeMode(AUTO_RESIZE_ALL_COLUMNS);
        setTableHeader(null);
    }

    public void removeSelected() {
        int[] indices = getSelectedRows();
        HeaderItem[] items = new HeaderItem[indices.length];

        for (int i = 0; i < indices.length; i++) {
            items[i] = getAddressbookTableModel().get(indices[i]);
        }

        getAddressbookTableModel().removeItems(items);
    }

    public void initFocus(Component c) {
        ((JTextField) addressEditor.getEditor().getEditorComponent()).setNextFocusableComponent(c);

        addressEditor.setNextFocusableComponent(c);
    }

    public RecipientsTableModel getAddressbookTableModel() {
        return model;
    }

    private void makeVisible(int row, int column) {
        Rectangle r = getCellRect(row, column, true);

        scrollRectToVisible(r);
    }

    public void focusToTextField() {
        JComboBox box = (JComboBox) getEditorComponent();

        if (box == null) {
            return;
        }

        JTextField textfield = (JTextField) box.getEditor().getEditorComponent();

        textfield.requestFocus();
    }

    private void addEmptyRow() {
        /*
if (emptyRowExists())
                return;
*/
        ContactItem item = new ContactItem();
        item.setDisplayName("");
        item.setHeader("To");
        
        try {
            model.addItem(item);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        makeVisible(getRowCount() - 1, 1);
    }

    public void editLastRow() {
        int row = getRowCount() - 1;

        if (editCellAt(row, 1)) {
            focusToTextField();
        }
    }

    private boolean isEmpty(int row) {
        HeaderItem item1 = (HeaderItem) model.getValueAt(row, 1);

        String value = (String) item1.getDisplayName();

        return value.length() == 0;
    }

    private boolean emptyRowExists() {
        int rowCount = getRowCount();

        if (rowCount == 0) {
            return false;
        }

        HeaderItem item1 = (HeaderItem) model.getValueAt(rowCount - 1, 1);

        String value = (String) item1.getDisplayName();

        return value.length() == 0;
    }

    public void setHeaderItem(HeaderItem item) {
        int row = getEditingRow();

        if (row != -1) {
            // be sure that the old field value (to:, cc:, bcc:) is
            // properly set 
            HeaderItem old = model.get(row);
            String oldField = (String) old.getHeader();

            String newField = (String) item.getHeader();

            if (newField == null) {
                item.setHeader( oldField);
            } else if (newField.length() == 0) {
                item.setHeader( oldField);
            }
        }

        model.setHeaderItem(row, item);
    }

    /**
 * Remove selected row.
 *
 */
    public void removeEditingRow() {
        int rowCount = getRowCount();

        int row = getEditingRow();
        int column = getEditingColumn();

        if (row == 0) {
            // one row should always be available
            return;
        }

        if ((row != -1) && (column != -1)) {
            getCellEditor(row, column).stopCellEditing();
        }

        model.remove(row);

        boolean b;

        if (row == 0) {
            b = editCellAt(0, 1);
        } else {
            b = editCellAt(row - 1, 1);
        }

        if (b) {
            focusToTextField();
        }
    }

    public void appendRow() {
        if (isEditing()) {
            // cancel editor, if necessary
            // (if this is not happening we can't add another row
            // without loosing table data
            removeEditor();
        }

        // if not available
        if (!emptyRowExists()) {
            addEmptyRow();
        }

        // start editing in last row
        editLastRow();
    }

    /**
 * Remove all empty rows.
 *
 */
    public void cleanupHeaderItemList() {
        for (int i = getRowCount() - 1; i >= 0; i--) {
            boolean b = isEmpty(i);

            if (b) {
                HeaderItem[] items = new HeaderItem[1];
                items[0] = model.get(i);

                model.removeItems(items);
            }
        }
    }
}
