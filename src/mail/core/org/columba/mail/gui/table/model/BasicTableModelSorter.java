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
package org.columba.mail.gui.table.model;

import org.columba.mail.message.ColumbaHeader;

import org.columba.ristretto.message.Flags;

import java.text.Collator;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;


/**
 * 
 *
 * Adds sorting capability to the TableModel.
 * <p>
 * We support the following sorting orders:
 *  - In Order Received (no sorting applied)
 *  - Ascending
 *  - Descending
 * <p>
 * Sorting is applied on the selected column.
 * <p>
 * When inserting new headers we use insertion sort
 * to insert them in the already sorted list of headers
 *
 * @author fdietz
 */
public class BasicTableModelSorter extends TreeTableModelDecorator {
    protected boolean ascending = true;
    protected String sort = new String("Date");
    protected Collator collator;

    public BasicTableModelSorter(TreeTableModelInterface tableModel) {
        super(tableModel);

        collator = Collator.getInstance();
    }

    public String getSortingColumn() {
        return sort;
    }

    public boolean getSortingOrder() {
        return ascending;
    }

    public void setSortingColumn(String str) {
        sort = str;
    }

    public void setSortingOrder(boolean b) {
        ascending = b;
    }

    /**
 *
 * sort the table
 *
 * use selected column and sorting order
 *
 */
    public void sort() {
        String str = getSortingColumn();

        /*
if (str.equals("In Order Received")) {
    // do not sort the table, just use
    MessageNode rootNode = getRootNode();
} else {
*/
        MessageNode rootNode = getRootNode();

        // get a list of MessageNode objects of the first
        // hierachy level	
        List v = rootNode.getVector();
				if (v == null)
				  return;
				
        // do the sorting
        Collections.sort(v,
            new MessageHeaderComparator(getRealModel().getColumnNumber(getSortingColumn()),
                getSortingOrder()));

        //}
    }

    /**
 * sort the table
 *
 * @param column        index of column
 */
    public void sort(int column) {
        String c = getColumnName(column);

        // if the same column is selected we change the sorting order
        // between ascending/descending
        if (getSortingColumn().equals(c)) {
            if (getSortingOrder()) {
                setSortingOrder(false);
            } else {
                setSortingOrder(true);
            }
        }

        setSortingColumn(c);
        sort();
    }

    /**
 *
 *
 * @return        index of selected column
 */
    public int getSortInt() {
        return getRealModel().getColumnNumber(getSortingColumn());
    }

    class MessageHeaderComparator implements Comparator {
        protected int column;
        protected boolean ascending;

        public MessageHeaderComparator(int sortCol, boolean sortAsc) {
            column = sortCol;
            ascending = sortAsc;
        }

        public int compare(Object o1, Object o2) {
            MessageNode node1 = (MessageNode) o1;
            MessageNode node2 = (MessageNode) o2;

            ColumbaHeader header1 = (ColumbaHeader) node1.getUserObject();
            ColumbaHeader header2 = (ColumbaHeader) node2.getUserObject();

            if ((header1 == null) || (header2 == null)) {
                return 0;
            }

            int result = 0;

            String columnName = getRealModel().getColumnName(column);

            if (columnName.equals("Status")) {
                Flags flags1 = header1.getFlags();
                Flags flags2 = header2.getFlags();

                if ((flags1 == null) || (flags2 == null)) {
                    result = 0;
                } else if ((flags1.getSeen()) && (!flags2.getSeen())) {
                    result = -1;
                } else if ((!flags1.getSeen()) && (flags2.getSeen())) {
                    result = 1;
                } else {
                    result = 0;
                }
            } else if (columnName.equals("Flagged")) {
                Flags flags1 = header1.getFlags();
                Flags flags2 = header2.getFlags();

                boolean f1 = flags1.getFlagged();
                boolean f2 = flags2.getFlagged();

                if (f1 == f2) {
                    result = 0;
                } else if (f1) { // define false < true
                    result = 1;
                } else {
                    result = -1;
                }
            } else if (columnName.equals("Attachment")) {
                boolean f1 = ((Boolean) header1.get("columba.attachment")).booleanValue();
                boolean f2 = ((Boolean) header2.get("columba.attachment")).booleanValue();

                if (f1 == f2) {
                    result = 0;
                } else if (f1) { // define false < true
                    result = 1;
                } else {
                    result = -1;
                }
            } else if (columnName.equals("Date")) {
                Date d1 = (Date) header1.get("columba.date");
                Date d2 = (Date) header2.get("columba.date");

                if ((d1 == null) || (d2 == null)) {
                    result = 0;
                } else {
                    result = d1.compareTo(d2);
                }
            } else if (columnName.equals("Size")) {
                int i1 = ((Integer) header1.get("columba.size")).intValue();
                int i2 = ((Integer) header2.get("columba.size")).intValue();

                if (i1 == i2) {
                    result = 0;
                } else if (i1 > i2) {
                    result = 1;
                } else {
                    result = -1;
                }
            } else if (columnName.equals("Spam")) {
                boolean f1 = ((Boolean) header1.get("columba.spam")).booleanValue();
                boolean f2 = ((Boolean) header2.get("columba.spam")).booleanValue();

                if (f1 == f2) {
                    result = 0;
                } else if (f1) { // define false < true
                    result = 1;
                } else {
                    result = -1;
                }
            }else {
                Object item1 = header1.get(columnName);
                Object item2 = header2.get(columnName);

                if ((item1 != null) && (item2 == null)) {
                    result = 1;
                } else if ((item1 == null) && (item2 != null)) {
                    result = -1;
                } else if ((item1 == null) && (item2 == null)) {
                    result = 0;
                } else if (item1 instanceof String) {
                    result = collator.compare((String) item1, (String) item2);
                } else if (item1 instanceof Boolean) {
                    result = collator.compare((Boolean) item1, (Boolean) item2);
                }
            }

            if (!ascending) {
                result = -result;
            }

            return result;
        }

        public boolean equals(Object obj) {
            if (obj instanceof MessageHeaderComparator) {
                MessageHeaderComparator compObj = (MessageHeaderComparator) obj;

                return (compObj.column == column) &&
                (compObj.ascending == ascending);
            }

            return false;
        }
    }
}
