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

import java.awt.Dimension;

import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import org.columba.addressbook.gui.table.renderer.DefaultHeaderRenderer;
import org.columba.addressbook.util.AddressbookResourceLoader;


/**
 * JTable customized with renderers.
 *
 * @author fdietz
 */
public class TableView extends JTable {
   
    private TableController controller;
    private TableModel tableModel;

    public TableView(TableController controller, TableModel tableModel) {
        super(tableModel);

        this.controller = controller;

        //this.addressbookModel = addressbookModel;
        setIntercellSpacing(new Dimension(0, 0));
        setShowGrid(false);
        setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        // set renderers for columns
        TableColumn tc = getColumnModel().getColumn(0);
        /*
        tc.setHeaderRenderer(new DefaultHeaderRenderer(""));
        tc.setCellRenderer(new TypeRenderer());
        tc.setMaxWidth(23);
        */
        
        tc = getColumnModel().getColumn(0);
        tc.setHeaderRenderer(createHeader("displayname"));

        tc = getColumnModel().getColumn(1);
        tc.setHeaderRenderer(createHeader("email;internet"));

        tc = getColumnModel().getColumn(2);
        tc.setHeaderRenderer(createHeader("url"));
    }

    /**
     * Create table header renderer. Names use semicolon ";" character to
     * define subtypes.
     * <p>
     * For example:email;internet or: tel;fax
     * <p>
     * Method searches for a semicolon and creates a string representation,
     * setting the subtype in braces.
     *
     * @param name
     * @return
     */
    private DefaultHeaderRenderer createHeader(String name) {
        int index = name.indexOf(";");

        if (index != -1) {
            String prefix = AddressbookResourceLoader.getString("header",
                    name.substring(0, index));
            String suffix = AddressbookResourceLoader.getString("header",
                    name.substring(index + 1, name.length()));

            return new DefaultHeaderRenderer(prefix + "(" + suffix + ")");
        }

        return new DefaultHeaderRenderer(name);
    }
}
