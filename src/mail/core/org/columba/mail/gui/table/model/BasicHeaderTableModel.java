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
package org.columba.mail.gui.table.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.columba.mail.message.HeaderList;
import org.frappucino.treetable.AbstractTreeTableModel;
import org.frappucino.treetable.CustomTreeTableCellRenderer;

/**
 * 
 * 
 * <class>BasicHeaderTableModel </class> extends AbstractTableModel and adds a
 * <class>DefaultTreeModel </class>.
 * <p>
 * The TableModel uses the TreeModel data. You can say, that it just wraps the
 * TreeModel in the TableModel.
 * <p>
 * This is necessary to support a threaded view of messages, useful when
 * following discussions of mailing-lists or newsgroups.
 * <p>
 * Another possible scenario would be adding a grouping support which also needs
 * a tree-like structure.
 * 
 * @author fdietz
 *  
 */
public class BasicHeaderTableModel extends AbstractTreeTableModel {
    
    /**
     * list of column IDs
     */
    private List columns;

    protected HeaderList headerList;

    /**
     * 
     * We cache all <class>MessageNode </class> here.
     * 
     * This is much faster than searching through the complete <class>HeaderList
     * </class> all the time.
     *  
     */
    protected HashMap map;

    protected MessageNode root;

    private boolean enableThreadedView;

    public BasicHeaderTableModel() {
        super();
        
        map = new HashMap();

        columns = new Vector();
    }

    public BasicHeaderTableModel(String[] c) {
        super();
        
        columns = new Vector();

        // add array to vector
        for (int i = 0; i < c.length; i++) {
            columns.add(c[i]);
        }

        map = new HashMap();
    }

    /** ********************** getter/setter methods *************************** */
    public void enableThreadedView(boolean b) {
        enableThreadedView = b;
    }

    public MessageNode getRootNode() {
        return root;
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
        return (DefaultTreeModel) getTree().getModel();
    }

   

    /** ******************* AbstractTableModel implementation ******************* */
    public int getColumnCount() {
        return columns.size();
    }

    public int getRowCount() {
        if (getTree() != null) {
            return getTree().getRowCount();
        } else {
            return 0;
        }
    }

    public Object getValueAt(int row, int col) {
        //if ( col == 0 ) return tree;
        TreePath treePath = getTree().getPathForRow(row);

        return (MessageNode) treePath.getLastPathComponent();
    }

    public String getColumnName(int column) {
        return (String) columns.get(column);
    }

    public int getColumnNumber(String name) {
        for (int i = 0; i < getColumnCount(); i++) {
            if (name.equals(getColumnName(i))) { return i; }
        }

        return -1;
    }

    /**
     * Get the class which is responsible for renderering this column.
     * <p>
     * If the threaded-view is enabled, return a custom tree cell renderer.
     * <p>
     * 
     * @see org.columba.mail.gui.table.TableView#enableThreadedView
     */
    public Class getColumnClass(int column) {
        if (enableThreadedView) {
            if (getColumnName(column).equals("Subject")) { return CustomTreeTableCellRenderer.class; }
        }

        return getValueAt(0, column).getClass();
    }

    public boolean isCellEditable(int row, int col) {
        String name = getColumnName(col);

        if (name.equalsIgnoreCase("Subject")) { return true; }

        return false;
    }

    /**
     * Set column IDs
     * 
     * @param c
     *            array of column IDs
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
     * @param c
     *            new column ID
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