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

import org.columba.core.config.WindowItem;
import org.columba.core.xml.XmlElement;
import org.columba.mail.config.FolderItem;
import org.columba.mail.gui.table.SortingStateObservable;
import org.columba.mail.gui.table.TableView;
import org.columba.mail.message.HeaderList;

/**
 * 
 * 
 * Extends <class>BasicTableModelSorter </class> with Columba specific stuff.
 * <p>
 * Sorting order and column are initially loaded/saved from an xml configuration
 * file.
 * <p>
 * It especially implements <interface>TableModelModifier </interface>.
 * 
 * @author fdietz
 */
public class TableModelSorter extends BasicTableModelSorter {

    protected WindowItem config;

    protected SortingStateObservable sortingStateObservable;

    public TableModelSorter(TreeTableModelInterface tableModel) {
        super(tableModel);

        setSortingColumn("Date");

        setSortingOrder(true);

        // observable connects the sorting table with the sort menu (View->Sort
        // Messages)
        sortingStateObservable = new SortingStateObservable();
        sortingStateObservable.setSortingState(getSortingColumn(),
                getSortingOrder());
    }

    /**
     * @return
     */
    public SortingStateObservable getSortingStateObservable() {
        return sortingStateObservable;
    }

    /**
     * 
     * This method is used by <class>SortMessagesMenu </class> to generate the
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

        Object[] result = new String[v.size()];
        result = v.toArray();

        return result;
    }

    public void loadConfig(TableView view) {

    }

    /**
     * ***************************** implements TableModelModifier
     * ******************
     */

    public void sort() {
        super.sort();

        // notify tree
        getRealModel().getTreeModel().nodeStructureChanged(getRootNode());

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

        }
    }
}