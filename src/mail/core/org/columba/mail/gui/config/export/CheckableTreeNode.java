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
package org.columba.mail.gui.config.export;

import java.util.Enumeration;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.tree.TreeNode;

import org.columba.core.gui.checkabletree.CheckableItem;
import org.columba.mail.folder.FolderTreeNode;

/**
 * @author fdietz
 */
public class CheckableTreeNode  implements CheckableItem {

    private CheckableTreeNode parent;
    private Vector children;
    
    private Icon icon;
    private boolean selected;
    private String name;
    
    private FolderTreeNode node;
    
    /**
     * 
     */
    public CheckableTreeNode(String name) {
        this.name = name;
        
        children = new Vector();
        
    }

    public void addChild( CheckableTreeNode child ) {
        children.add(child);
        
        child.setParent(this);
    }
    /**
     * @see org.columba.core.gui.checkabletree.CheckableItem#isSelected()
     */
    public boolean isSelected() {
        
        return selected;
    }

    /**
     * @see org.columba.core.gui.checkabletree.CheckableItem#setSelected(boolean)
     */
    public void setSelected(boolean b) {
        selected = b;

    }

    /**
     * @see org.columba.core.gui.checkabletree.CheckableItem#getIcon()
     */
    public Icon getIcon() {
        
        return icon;
    }

    /**
     * @see javax.swing.tree.TreeNode#getChildCount()
     */
    public int getChildCount() {
       
        return children.size();
    }

    /**
     * @see javax.swing.tree.TreeNode#getAllowsChildren()
     */
    public boolean getAllowsChildren() {
      
        return true;
    }

    /**
     * @see javax.swing.tree.TreeNode#isLeaf()
     */
    public boolean isLeaf() {
        
        return getChildCount() == 0;
    }

    /**
     * @see javax.swing.tree.TreeNode#children()
     */
    public Enumeration children() {
      
        return children.elements();
    }

    /**
     * @see javax.swing.tree.TreeNode#getParent()
     */
    public TreeNode getParent() {
       
        return parent;
    }
    
    public void setParent(CheckableTreeNode parent) {
        this.parent = parent;
    }
    

    /**
     * @see javax.swing.tree.TreeNode#getChildAt(int)
     */
    public TreeNode getChildAt(int arg0) {
       
        return (CheckableTreeNode) children.get(arg0);
    }

    /**
     * @see javax.swing.tree.TreeNode#getIndex(javax.swing.tree.TreeNode)
     */
    public int getIndex(TreeNode arg0) {
        
        return children.indexOf(arg0);
    }

   
    /**
     * @see org.columba.core.gui.checkabletree.CheckableItem#setIcon(javax.swing.Icon)
     */
    public void setIcon(Icon icon) {
        this.icon = icon;

    }

    /**
     * @see org.columba.core.gui.checkabletree.CheckableItem#getName()
     */
    public String getName() {
        return name;
    }

    /**
     * @see org.columba.core.gui.checkabletree.CheckableItem#setName(java.lang.String)
     */
    public void setName(String s) {
       this.name = s;

    }

    /**
     * @return
     */
    public FolderTreeNode getNode() {
        return node;
    }

    /**
     * @param node
     */
    public void setNode(FolderTreeNode node) {
        this.node = node;
    }

}
