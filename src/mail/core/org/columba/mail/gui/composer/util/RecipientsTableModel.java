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
package org.columba.mail.gui.composer.util;

import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.table.AbstractTableModel;

import org.columba.addressbook.model.ContactItemMap;
import org.columba.addressbook.model.HeaderItem;
import org.columba.addressbook.model.HeaderItemList;


/**
 * TableModel encapsulates {@link ContactItemMap} used in composer.
 *
 * @author fdietz
 */
public class RecipientsTableModel extends AbstractTableModel {

    /** JDK 1.4+ logging framework logger, used for logging. */
    private static final Logger LOG = Logger.getLogger("org.columba.mail.gui.composer.util");

    private static final String[] COLUMNS = { "field", "displayname" };
    private HeaderItemList list;

    public RecipientsTableModel() {
    }

    public int getSize() {
        return getHeaderList().count();
    }

    public void removeItems(Object[] items) {
        for (int i = 0; i < items.length; i++) {
            getHeaderList().remove((HeaderItem) items[i]);
        }

        // recreate whole tablemodel
        fireTableDataChanged();
    }

    public void remove(int index) {
        getHeaderList().remove(index);

        fireTableDataChanged();
    }

    public HeaderItemList getHeaderList() {
        return list;
    }

    public HeaderItem get(int index) {
        return getHeaderList().get(index);
    }

    public void addItem(HeaderItem item) throws Exception {
        int count = 0;

        if (getHeaderList() != null) {
            count = getHeaderList().count();
        }

        if (count == 0) {
            list = new HeaderItemList();

            // first message
            getHeaderList().add(item);

            fireTableDataChanged();
        } else {
            getHeaderList().add(item);

            fireTableDataChanged();
        }
    }

    public void setHeaderList(HeaderItemList l) {
        if (l == null) {
            LOG.fine("list == null");
            list = new HeaderItemList();

            fireTableDataChanged();

            return;
        }

        LOG.fine("list size=" + l.count());

        /*
         * FIXME
        List clone = (Vector) ((Vector) list.getList()).clone();
        list = new HeaderItemList(clone);
        */
        list = l;

        fireTableDataChanged();
    }

    public void setHeaderItem(int row, HeaderItem item) {
        list.replace(row, item);

        fireTableDataChanged();
    }

    public int getColumnCount() {
        return COLUMNS.length;
    }

    public int getRowCount() {
        if (list == null) {
            return 0;
        } else {
            return list.count();
        }
    }

    public String getColumnName(int col) {
        return COLUMNS[col];
    }

    public int getColumnNumber(String str) {
        for (int i = 0; i < getColumnCount(); i++) {
            if (str.equals(getColumnName(i))) {
                return i;
            }
        }

        return -1;
    }

    public Object getValueAt(int row, int col) {
        if (list == null) {
            return null;
        }

        HeaderItem item = list.get(row);

        /*
if (col == 0)
        return item.get("field");
if (col == 1)
        return item.get("displayname");
*/
        return item;

        //return null;
    }

    public Class getColumnClass(int c) {
        if (list == null) {
            return null;
        }

        return getValueAt(0, c).getClass();
    }

    public boolean isCellEditable(int row, int col) {
        if ((col == 0) || (col == 1)) {
            return true;
        }

        return false;
    }

    public void setValueAt(Object value, int row, int col) {
        if (col == 1) {
            HeaderItem item = list.get(row);

            item.setDisplayName((String)value);
            
            fireTableCellUpdated(row, col);
        } else if (col == 0) {
            HeaderItem item = list.get(row);
            item.setHeader((String)value);
            fireTableCellUpdated(row, col);
        }
    }
}
