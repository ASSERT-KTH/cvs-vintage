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

import org.columba.mail.message.ColumbaHeader;
import org.columba.mail.message.HeaderList;

import java.util.Enumeration;

public class HeaderTableModel extends BasicHeaderTableModel implements
        TreeTableModelInterface {

    public HeaderTableModel() {
        super();
    }

    public HeaderTableModel(String[] columns) {
        super(columns);
    }

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
        for (int i = 0; i < uids.length; i++) {
            MessageNode node = (MessageNode) map.get(uids[i]);

            if (node != null) {
                // update treemodel
                getTreeModel().nodeChanged(node);
            }
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.columba.mail.gui.table.model.TableModelModifier#remove(java.lang.Object[])
     */
    public void remove(Object[] uids) {
        if (uids != null) {
            for (int i = 0; i < uids.length; i++) {
                MessageNode node = (MessageNode) map.get(uids[i]);

                if (node != null) {
                    map.remove(node);

                    if (node.getParent() != null) {
                        getTreeModel().removeNodeFromParent(node);
                    }
                }
            }

        }
    }

    public void update() {
        if (root == null) {
            root = new MessageNode(new ColumbaHeader(), "0");
        }

        // remove all children from tree
        root.removeAllChildren();

        // clear messagenode cache
        map.clear();

        if ((headerList == null) || (headerList.count() == 0)) {
        // table is empty
        // -> just display empty table
        return; }

        // add every header from HeaderList to the table as MessageNode
        for (Enumeration e = headerList.keys(); e.hasMoreElements();) {
            // get unique id
            Object uid = e.nextElement();

            // get header
            ColumbaHeader header = headerList.get(uid);

            // create MessageNode
            MessageNode child = new MessageNode(header, uid);

            // add this node to cache
            map.put(uid, child);

            // add node to tree
            root.add(child);
        }

        tree.setRootNode(root);
    }

    public void clear() {
        root = new MessageNode(new ColumbaHeader(), "0");
        tree.setRootNode(root);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.columba.mail.gui.table.model.TableModelModifier#set(org.columba.mail.message.HeaderList)
     */
    public void set(HeaderList headerList) {
        this.headerList = headerList;

        update();
    }
}