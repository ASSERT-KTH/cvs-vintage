/*
 * @(#)AbstractTreeTableModel.java	1.2 98/10/27
 *
 * Copyright 1997, 1998 by Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Sun Microsystems, Inc. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Sun.
 */

package org.columba.core.gui.util.treetable;

import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 * @version 1.2 10/27/98
 * An abstract implementation of the TreeTableModel interface, handling the list
 * of listeners.
 * @author Philip Milne
 */

public abstract class AbstractTreeTableModel implements TreeTableModel {
    protected Object root;
    protected EventListenerList listenerList = new EventListenerList();

    public AbstractTreeTableModel(Object root) {
        this.root = root;
    }

    public AbstractTreeTableModel()
    {
    }

    public void setRoot( Object o )
    {
	this.root = o;
    }

    //
    // Default implmentations for methods in the TreeModel interface.
    //

    public Object getRoot() {
        return root;
    }

    public boolean isLeaf(Object node) {
        return getChildCount(node) == 0;
    }

    public void valueForPathChanged(TreePath path, Object newValue) {}

    // This is not called in the JTree's default mode: use a naive implementation.
    public int getIndexOfChild(Object parent, Object child) {
        for (int i = 0; i < getChildCount(parent); i++) {
	    if (getChild(parent, i).equals(child)) {
	        return i;
	    }
        }
	return -1;
    }




    /************************** listener methods ************************/




    public void addTreeModelListener(TreeModelListener l) {
        listenerList.add(TreeModelListener.class, l);
    }

    public void removeTreeModelListener(TreeModelListener l) {
        listenerList.remove(TreeModelListener.class, l);
    }

    /*
     * Notify all listeners that have registered interest for
     * notification on this event type.  The event instance
     * is lazily created using the parameters passed into
     * the fire method.
     * @see EventListenerList
     */
    protected void fireTreeNodesChanged(Object source, Object[] path,
                                        int[] childIndices,
                                        Object[] children) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        TreeModelEvent e = null;
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==TreeModelListener.class) {
                // Lazily create the event:
                if (e == null)
                    e = new TreeModelEvent(source, path,
                                           childIndices, children);
                ((TreeModelListener)listeners[i+1]).treeNodesChanged(e);
            }
        }
    }

    /*
     * Notify all listeners that have registered interest for
     * notification on this event type.  The event instance
     * is lazily created using the parameters passed into
     * the fire method.
     * @see EventListenerList
     */
    protected void fireTreeNodesInserted(Object source, Object[] path,
                                        int[] childIndices,
                                        Object[] children) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        TreeModelEvent e = null;
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==TreeModelListener.class) {
                // Lazily create the event:
                if (e == null)
                    e = new TreeModelEvent(source, path,
                                           childIndices, children);
                ((TreeModelListener)listeners[i+1]).treeNodesInserted(e);
            }
        }
    }

    /*
     * Notify all listeners that have registered interest for
     * notification on this event type.  The event instance
     * is lazily created using the parameters passed into
     * the fire method.
     * @see EventListenerList
     */
    protected void fireTreeNodesRemoved(Object source, Object[] path,
                                        int[] childIndices,
                                        Object[] children) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        TreeModelEvent e = null;
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==TreeModelListener.class) {
                // Lazily create the event:
                if (e == null)
                    e = new TreeModelEvent(source, path,
                                           childIndices, children);
                ((TreeModelListener)listeners[i+1]).treeNodesRemoved(e);
            }
        }
    }

    /*
     * Notify all listeners that have registered interest for
     * notification on this event type.  The event instance
     * is lazily created using the parameters passed into
     * the fire method.
     * @see EventListenerList
     */
    protected void fireTreeStructureChanged(Object source, Object[] path,
                                        int[] childIndices,
                                        Object[] children) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        TreeModelEvent e = null;
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==TreeModelListener.class) {
                // Lazily create the event:
                if (e == null)
                    e = new TreeModelEvent(source, path,
                                           childIndices, children);
                ((TreeModelListener)listeners[i+1]).treeStructureChanged(e);
            }
        }
    }





    /*********************** some little helper methods ****************/


    // i've taken the following methods from javax.swing.DefaultTreeModel


    public TreeNode[] getPathToRoot(TreeNode aNode, int depth) {
        TreeNode[]              retNodes;
	// This method recurses, traversing towards the root in order
	// size the array. On the way back, it fills in the nodes,
	// starting from the root and working back to the original node.

        /* Check for null, in case someone passed in a null node, or
           they passed in an element that isn't rooted at root. */
        if(aNode == null) {
            if(depth == 0)
                return null;
            else
                retNodes = new TreeNode[depth];
        }
        else {
            depth++;
            if(aNode == root)
                retNodes = new TreeNode[depth];
            else
                retNodes = getPathToRoot(aNode.getParent(), depth);
            retNodes[retNodes.length - depth] = aNode;
        }
        return retNodes;
    }



    /**
     * Invoked this to insert newChild at location index in parents children.
     * This will then message nodesWereInserted to create the appropriate
     * event. This is the preferred way to add children as it will create
     * the appropriate event.
     */
    public void insertNodeInto(MutableTreeNode newChild,
                               MutableTreeNode parent, int index){
        parent.insert(newChild, index);

        int[]           newIndexs = new int[1];

        newIndexs[0] = index;
        nodesWereInserted(parent, newIndexs);
    }

    /**
     * Message this to remove node from its parent. This will message
     * nodesWereRemoved to create the appropriate event. This is the
     * preferred way to remove a node as it handles the event creation
     * for you.
     */
    public void removeNodeFromParent(MutableTreeNode node) {
        MutableTreeNode         parent = (MutableTreeNode)node.getParent();

        if(parent == null)
            throw new IllegalArgumentException("node does not have a parent.");

        int[]            childIndex = new int[1];
        Object[]         removedArray = new Object[1];

        childIndex[0] = parent.getIndex(node);
        parent.remove(childIndex[0]);
        removedArray[0] = node;
        nodesWereRemoved(parent, childIndex, removedArray);
    }

    /**
      * Invoke this method after you've changed how node is to be
      * represented in the tree.
      */
    public void nodeChanged(TreeNode node) {
        if(listenerList != null && node != null) {
            TreeNode         parent = node.getParent();

            if(parent != null) {
                int        anIndex = parent.getIndex(node);
                if(anIndex != -1) {
                    int[]        cIndexs = new int[1];

                    cIndexs[0] = anIndex;
                    nodesChanged(parent, cIndexs);
                }
            }
	    else if (node == getRoot()) {
		nodesChanged(node, null);
	    }
        }
    }



    /**
      * Invoke this method after you've inserted some TreeNodes into
      * node.  childIndices should be the index of the new elements and
      * must be sorted in ascending order.
      */
    public void nodesWereInserted(TreeNode node, int[] childIndices) {
        if(listenerList != null && node != null && childIndices != null
           && childIndices.length > 0) {
            int               cCount = childIndices.length;
            Object[]          newChildren = new Object[cCount];

            for(int counter = 0; counter < cCount; counter++)
                newChildren[counter] = node.getChildAt(childIndices[counter]);
            fireTreeNodesInserted(this, getPathToRoot(node,0), childIndices,
                                  newChildren);
        }
    }


    /**
      * Invoke this method after you've removed some TreeNodes from
      * node.  childIndices should be the index of the removed elements and
      * must be sorted in ascending order. And removedChildren should be
      * the array of the children objects that were removed.
      */
    public void nodesWereRemoved(TreeNode node, int[] childIndices,
                                 Object[] removedChildren) {
        if(node != null && childIndices != null) {
            fireTreeNodesRemoved(this, getPathToRoot(node,0), childIndices,
                                 removedChildren);
        }
    }

    /**
      * Invoke this method after you've changed how the children identified by
      * childIndicies are to be represented in the tree.
      */
    public void nodesChanged(TreeNode node, int[] childIndices) {
        if(node != null) {
	    if (childIndices != null) {
		int            cCount = childIndices.length;

		if(cCount > 0) {
		    Object[]       cChildren = new Object[cCount];

		    for(int counter = 0; counter < cCount; counter++)
			cChildren[counter] = node.getChildAt
			    (childIndices[counter]);
		    fireTreeNodesChanged(this, getPathToRoot(node,0),
					 childIndices, cChildren);
		}
	    }
	    else if (node == getRoot()) {
		fireTreeNodesChanged(this, getPathToRoot(node,0), null, null);
	    }
        }
    }


    /**
      * Invoke this method if you've totally changed the children of
      * node and its childrens children...  This will post a
      * treeStructureChanged event.
      */
    public void nodeStructureChanged(TreeNode node) {
        if(node != null) {
           fireTreeStructureChanged(this, getPathToRoot(node,0), null, null);
        }
    }



















    //
    // Default impelmentations for methods in the TreeTableModel interface.
    //

    public Class getColumnClass(int column) { return Object.class; }

   /** By default, make the column with the Tree in it the only editable one.
    *  Making this column editable causes the JTable to forward mouse
    *  and keyboard events in the Tree column to the underlying JTree.
    */
    public boolean isCellEditable(Object node, int column) {
         return getColumnClass(column) == TreeTableModel.class;
    }

    public void setValueAt(Object aValue, Object node, int column) {}


    // Left to be implemented in the subclass:

    /*
     *   public Object getChild(Object parent, int index)
     *   public int getChildCount(Object parent)
     *   public int getColumnCount()
     *   public String getColumnName(Object node, int column)
     *   public Object getValueAt(Object node, int column)
     */
}
