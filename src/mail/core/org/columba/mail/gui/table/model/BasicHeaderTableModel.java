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

import org.columba.core.gui.util.treetable.CustomTreeTableCellRenderer;
import org.columba.core.gui.util.treetable.Tree;

import org.columba.mail.message.ColumbaHeader;
import org.columba.mail.message.HeaderList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;


/**
 *
 *
 * <class>BasicHeaderTableModel</class> extends AbstractTableModel
 * and adds a <class>DefaultTreeModel</class>.
 * <p>
 * The TableModel uses the TreeModel data. You can say, that it just
 * wraps the TreeModel in the TableModel.
 * <p>
 * This is necessary to support a threaded view of messages, useful
 * when following discussions of mailing-lists or newsgroups.
 * <p>
 * Another possible scenario would be adding a grouping support
 * which also needs a tree-like structure.
 *
 * @author fdietz
 *
 */
public class BasicHeaderTableModel extends AbstractTableModel {
    /**
     * list of column IDs
     */
    private List columns;
    protected HeaderList headerList;
    protected Tree tree;

    /**
     *
    * We cache all <class>MessageNode</class> here.
    *
    * This is much faster than searching through the complete
    * <class>HeaderList</class> all the time.
    *
    */
    protected HashMap map;
    protected MessageNode root;
    private boolean enableThreadedView;

    public BasicHeaderTableModel() {
        map = new HashMap();

        columns = new Vector();
    }

    public BasicHeaderTableModel(String[] c) {
        columns = new Vector();

        // add array to vector
        for (int i = 0; i < c.length; i++) {
            columns.add(c[i]);
        }

        map = new HashMap();
    }

    /************************ getter/setter methods ****************************/
    public void enableThreadedView(boolean b) {
        enableThreadedView = b;
    }

    public MessageNode getRootNode() {
        return root;
    }

    public void setTree(Tree tree) {
        this.tree = tree;
        tree.setRootNode(new MessageNode(new ColumbaHeader(), "0"));
    }

    public HeaderList getHeaderList() {
        return headerList;
    }

    public void setHeaderList(HeaderList list) {
        headerList = list;
    }

    public MessageNode getMessageNode(Object uid) {
        return (MessageNode) map.get(uid);
    }

    /**
             * @return
             */
    public Map getMap() {
        return map;
    }

    public DefaultTreeModel getTreeModel() {
        return (DefaultTreeModel) tree.getModel();
    }

    /**
     * @return
     */
    public Tree getTree() {
        return tree;
    }

    /********************* AbstractTableModel implementation ********************/
    public int getColumnCount() {
        return columns.size();
    }

    public int getRowCount() {
        if (tree != null) {
            return tree.getRowCount();
        } else {
            return 0;
        }
    }

    public Object getValueAt(int row, int col) {
        TreePath treePath = tree.getPathForRow(row);

        return (MessageNode) treePath.getLastPathComponent();

        //if ( col == 0 ) return tree;
    }

    public String getColumnName(int column) {
        return (String) columns.get(column);
    }

    public int getColumnNumber(String name) {

        for (int i = 0; i < getColumnCount(); i++) {
            if (name.equals(getColumnName(i))) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Get the class which is responsible for renderering
     * this column.
     * <p>
     * If the threaded-view is enabled, return a custom
     * tree cell renderer.
     * <p>
     * @see org.columba.mail.gui.table.TableView#enableThreadedView
     */
    public Class getColumnClass(int column) {
        if (enableThreadedView) {
            return CustomTreeTableCellRenderer.class;
        } else {
            return getValueAt(0, column).getClass();
        }
    }

    public boolean isCellEditable(int row, int col) {
        String name = getColumnName(col);

        if (name.equalsIgnoreCase("Subject")) {
            return true;
        }

        return false;
    }

    /**
     * Set column IDs
     *
     * @param c   array of column IDs
     */
    public void setColumns(String[] c) {
        columns = new Vector();

        // add array to vector
        for (int i = 0; i < c.length; i++) {
            columns.add(c[i]);
        }
    }

    /**
     * Add column to table model.
     *
     * @param c     new column ID
     */
    public void addColumn(String c) {
        columns.add(c);
    }

    /**
     * Clear column list.
     *
     */
    public void clearColumns() {
        columns.clear();
    }
}
