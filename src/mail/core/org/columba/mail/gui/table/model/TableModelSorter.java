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

import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.table.TableColumnModel;

import org.columba.core.config.DefaultItem;
import org.columba.core.config.HeaderItem;
import org.columba.core.config.TableItem;
import org.columba.core.config.WindowItem;
import org.columba.core.gui.util.AscendingIcon;
import org.columba.core.gui.util.DescendingIcon;
import org.columba.core.xml.XmlElement;
import org.columba.mail.config.FolderItem;
import org.columba.mail.main.MailInterface;
import org.columba.mail.gui.table.SortingStateObservable;
import org.columba.mail.gui.table.TableView;
import org.columba.mail.message.HeaderList;

/**
 * 
 *
 * Extends <class>BasicTableModelSorter</class> with Columba specific stuff.
 * <p>
 * Sorting order and column are initially loaded/saved from an xml
 * configuration file.
 * <p>
 * It especially implements <interface>TableModelModifier</interface>.
 *
 * @author fdietz
 */
public class TableModelSorter extends BasicTableModelSorter {
    protected WindowItem config;
    protected SortingStateObservable sortingStateObservable;

    public TableModelSorter(TreeTableModelInterface tableModel) {
        super(tableModel);

        /*
        XmlElement tableElement = FolderItem.getGlobalOptions();
        DefaultItem item = new DefaultItem(tableElement);

        String c = item.get("sorting_column");
        if ( c == null ) c = "Date";
        */
        
        setSortingColumn("Date");

        //setSortingOrder(item.getBoolean("sorting_order", false));
        setSortingOrder(true);
        
        // observable connects the sorting table with the sort menu (View->Sort
        // Messages)
        sortingStateObservable = new SortingStateObservable();
        sortingStateObservable.setSortingState(
            getSortingColumn(),
            getSortingOrder());
    }

    /**
     * @return
     */
    public SortingStateObservable getSortingStateObservable() {
        return sortingStateObservable;
    }

    /*
    public void saveConfig() {
        TableItem tableItem = (TableItem) MailInterface.config.getMainFrameOptionsConfig()
                                                    .getTableItem();
    
        boolean ascending = getSortingOrder();
        String sortingColumn = getSortingColumn();
    
        tableItem.set("ascending", ascending);
        tableItem.set("selected", sortingColumn);
    }
    */

    /**
     *
     * This method is used by <class>SortMessagesMenu</class> to generate the
     * available menuitem entries
     *
     * @return array of visible columns
     */
    public Object[] getColumns() {
        
        XmlElement tableElement = FolderItem.getGlobalOptions();
        
        
        XmlElement columns = tableElement.getElement("columns");

        Vector v = new Vector();
        for (int i = 0; i < columns.count(); i++) {
            XmlElement column = columns.getElement(i);

            String name = column.getAttribute("name");
            v.add(name);
        }
        

        /*
        TableItem tableItem = (TableItem) MailInterface.config.getMainFrameOptionsConfig()
                                                    .getTableItem();
        
        Vector v = new Vector();
        
        for (int i = 0; i < tableItem.count(); i++) {
            HeaderItem headerItem = tableItem.getHeaderItem(i);
        
            if (headerItem.getBoolean("enabled")) {
                v.add((String) headerItem.get("name"));
            }
        }
        */

        Object[] result = new String[v.size()];
        result = v.toArray();

        return result;
    }

    public void loadConfig(TableView view) {
        /*
        String column = getSortingColumn();
        int columnNumber = getSortInt();
        ImageIcon icon = null;

        if (getSortingOrder() == true) {
            icon = new AscendingIcon();
        } else {
            icon = new DescendingIcon();
        }

        TableColumnModel columnModel = view.getColumnModel();
        JLabel renderer =
            (JLabel) columnModel.getColumn(columnNumber).getHeaderRenderer();

        renderer.setIcon(icon);
        */
    }

    /*
    public void setSortingColumn(String str) {
        sort = str;
    }

    public void setSortingOrder(boolean b) {
        ascending = b;
    }
    */

    /**
     * ***************************** implements TableModelModifier
     * ******************
     */

    /*
     * (non-Javadoc)
     *
     * @see org.columba.mail.gui.table.model.TableModelModifier#modify(java.lang.Object[])
     */
    public void modify(Object[] uids) {
        super.modify(uids);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.columba.mail.gui.table.model.TableModelModifier#remove(java.lang.Object[])
     */
    public void remove(Object[] uids) {
        super.remove(uids);
    }

    public void sort() {
        super.sort();

        // notify tree
        getRealModel().getTreeModel().nodeStructureChanged(getRootNode());

        // notify table
        getRealModel().fireTableDataChanged();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.columba.mail.gui.table.model.TableModelModifier#update()
     */
    public void update() {
        super.update();

        // sort table model data
        sort();

        // notify tree
        getRealModel().getTreeModel().nodeStructureChanged(getRootNode());

        // notify table
        getRealModel().fireTableDataChanged();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.columba.mail.gui.table.model.TreeTableModelInterface#set(org.columba.mail.message.HeaderList)
     */
    public void set(HeaderList headerList) {
        super.set(headerList);

        if ((headerList != null) && (headerList.count() != 0)) {
            update();
        } else {
            // messagelist is empty
            //		notify tree
            getRealModel().getTreeModel().nodeStructureChanged(getRootNode());

            // notify table
            getRealModel().fireTableDataChanged();
        }
    }
}
