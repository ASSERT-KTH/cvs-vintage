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

import java.util.Date;

import junit.framework.TestCase;

import org.columba.mail.message.ColumbaHeader;
import org.columba.mail.message.HeaderList;
import org.frappucino.treetable.Tree;
import org.frappucino.treetable.TreeTable;

/**
 * @author fdietz
 *  
 */
public class HeaderTableModelTest extends TestCase {

    public static String[] columns = { "Subject", "From", "columba.date"};

    protected HeaderList createHeaderList() {
        HeaderList list = new HeaderList();
        ColumbaHeader h = new ColumbaHeader();
        h.set("columba.uid", new Integer(0));
        h.set("Subject", "Test1");
        h.set("From", "test@test.de");
        h.set("columba.date", new Date());
        list.add(h, new Integer(0));

        h = new ColumbaHeader();
        h.set("columba.uid", new Integer(1));
        h.set("Subject", "Test2");
        h.set("From", "test@test.de");
        h.set("columba.date", new Date());
        list.add(h, new Integer(1));

        return list;
    }

    /**
     * Test number of columns after init
     *  
     */
    public void testColumns() {
        HeaderTableModel model = new HeaderTableModel(columns);

        // 3 columns table
        assertEquals(3, model.getColumnCount());
    }

    public void testSet() {
        HeaderTableModel model = new HeaderTableModel(columns);

        TreeTable treetable = new TreeTable();
        treetable.setModel(model);
        model.setTree((Tree) treetable.getTree());

        // create sample headerlist
        HeaderList list = createHeaderList();

        model.set(list);

        // check number of tree nodes
        assertEquals(2, model.getRootNode().getChildCount());

        // check number of cached MessageNodes
        //assertEquals(2, model.getMap().size());

        // check number of JTable rows
        assertEquals(2, model.getRowCount());

        // check number of JTree rows
        assertEquals(2, model.getTree().getRowCount());

    }

    public void testRemove() {
        HeaderTableModel model = new HeaderTableModel(columns);
        Tree tree = new Tree();
        model.setTree(tree);

        HeaderList list = createHeaderList();

        model.set(list);

        // remove MessageNode with uid=0
        model.remove(new Object[] { new Integer(0)});

        // check number of tree nodes
        assertEquals(1, model.getRootNode().getChildCount());
        // check number of cached MessageNodes
        //assertEquals(1, model.getMap().size());

        // check number of JTable rows
        assertEquals(1, model.getRowCount());

        // check number of JTree rows
        assertEquals(1, model.getTree().getRowCount());
    }

    public void testModify() {
        HeaderTableModel model = new HeaderTableModel(columns);
        Tree tree = new Tree();
        model.setTree(tree);

        HeaderList list = createHeaderList();

        model.set(list);
    }
}