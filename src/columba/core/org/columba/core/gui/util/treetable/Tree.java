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

package org.columba.core.gui.util.treetable;

import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

public class Tree extends JTree {
    private TreeTable table = null;

    public Tree() {
        super();

        //setEditable(true);

        setRootVisible(false);

        setShowsRootHandles(true);
        
		//putClientProperty("JTree.lineStyle", "None");

        setCellRenderer(new CustomTreeCellRenderer());
    }

    public void setRootNode(DefaultMutableTreeNode root) {
        //		Create a default tree model with root as tree root
        DefaultTreeModel model = new DefaultTreeModel(root);

        // set the model
        setModel(model);
    }

    /**
     * updateUI is overridden to set the colors of the Tree's renderer
     * to match that of the table.
     */
    public void updateUI() {
        super.updateUI();

        CustomTreeCellRenderer jtcr = new CustomTreeCellRenderer();
        setCellRenderer(jtcr);

        // The tree should use the table's selection colors 
        if (table != null) {
            jtcr.setTextSelectionColor(UIManager.getColor(
                    "Table.selectionForeground"));
            jtcr.setBackgroundSelectionColor(UIManager.getColor(
                    "Table.selectionBackground"));
        }
    }

    /**
     * Sets the row height of the tree
     * and forwards it to the table.
     */
    public void setRowHeight(int rowHeight) {
        if (rowHeight > 0) {
            super.setRowHeight(rowHeight);

            if ((table != null) && (table.getRowHeight() != rowHeight)) {
                table.setRowHeight(getRowHeight());
            }
        }
    }

    public void setTable(TreeTable table) {
        this.table = table;
    }
}
