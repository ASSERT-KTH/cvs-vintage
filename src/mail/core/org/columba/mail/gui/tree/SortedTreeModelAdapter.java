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
package org.columba.mail.gui.tree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;


/**
 * This is an tree model adapter that sorts the underlying tree model.
 * This adapter is to be used between the JTree view and the original tree model if the
 * original tree model should not be sorted.
 * <p>
 * This adapter listens on the original tree model in order to get all events that
 * has changed the original tree model. The events gets transformed to correlate
 * to the sorted tree model and sent onwards to the real listeners.
 * <p>
 * This implementation has not fixed the problem when receiving a nodes removed event
 * from the original model. This event has the indicies and bojects of the nodes that
 * has been removed. Since this class does not store a copy of nodes before they were
 * removed, it is impossible to convert the old indicies to an indicies list for the
 * sorted model. The current solution is that the treeStructureChanged() event is
 * thrown instead.
 *
 * @author redsolo
 */
public class SortedTreeModelAdapter implements TreeModel, TreeModelListener {
    /** The original tree model that has all the actual data that this adapter uses. */
    private TreeModel originalModel;

    /** All listeners of this adapter. */
    private List listeners;

    /** A cached list of children. */
    private List cachedChildList;

    /** The cached children list has this object as its parent. */
    private Object cachedChildListParent;

    /**
 * Creates a sorted TreeModel that retrieves the data from the original tree model.
 * @param original the mode that is to be displayed as sorted.
 */
    public SortedTreeModelAdapter(TreeModel original) {
        originalModel = original;
        listeners = new LinkedList();
        original.addTreeModelListener(this);
    }

    /** {@inheritDoc} */
    public Object getChild(Object parent, int index) {
        return getChildList(parent).get(index);
    }

    /** {@inheritDoc} */
    public int getIndexOfChild(Object parent, Object child) {
        int index = -1;
        List childs = getChildList(parent);
        int childCount = childs.size();

        for (int i = 0; (i < childCount) && (index == -1); i++) {
            if (childs.get(i).equals(child)) {
                index = i;
            }
        }

        return index;
    }

    /**
 * {@inheritDoc}
 * <p>
 * Current implementation invokes this method on the original tree model without any changes.
 */
    public Object getRoot() {
        return originalModel.getRoot();
    }

    /**
 * {@inheritDoc}
 * <p>
 * Current implementation invokes this method on the original tree model without any changes.
 */
    public int getChildCount(Object parent) {
        return originalModel.getChildCount(parent);
    }

    /**
 * {@inheritDoc}
 * <p>
 * Current implementation invokes this method on the original tree model without any changes.
 */
    public boolean isLeaf(Object node) {
        return originalModel.isLeaf(node);
    }

    /** {@inheritDoc} */
    public void addTreeModelListener(TreeModelListener l) {
        listeners.add(l);
    }

    /** {@inheritDoc} */
    public void removeTreeModelListener(TreeModelListener l) {
        listeners.remove(l);
    }

    /**
 * {@inheritDoc}
 * <p>
 * Current implementation invokes this method on the original tree model without any changes.
 */
    public void valueForPathChanged(TreePath path, Object newValue) {
        originalModel.valueForPathChanged(path, newValue);
    }

    /**
 * Returns the parent node for the event.
 * @param event the event.
 * @return the parent node for the event.
 */
    private MutableTreeNode getParentNodeFromEvent(TreeModelEvent event) {
        Object[] paths = event.getPath();

        return (MutableTreeNode) paths[paths.length - 1];
    }

    /**
 * Returns a new TreeEvent that has the correct changed children indexes and objects.
 * This method takes the indicies array from the old event, changes the indexing so it
 * corresponds to the sorted model list, and then sorts the indicies in ascending order.
 * @param oldEvent the event from the original model.
 * @return a new TreeEvent for this sorted tree model.
 */
    private TreeModelEvent getSortedTreeEvent(TreeModelEvent oldEvent) {
        int numberOfChilds = oldEvent.getChildIndices().length;
        MutableTreeNode parentNode = getParentNodeFromEvent(oldEvent);

        int[] oldEventIndicies = oldEvent.getChildIndices();
        Object[] oldEventChilds = oldEvent.getChildren();
        int[] newEventIndicies = new int[oldEventIndicies.length];
        Object[] newEventChilds = new Object[numberOfChilds];

        for (int i = 0; i < oldEventIndicies.length; i++) {
            int oldIndex = oldEventIndicies[i];
            int newIndex = getIndexOfChild(parentNode, oldEventChilds[i]);
            newEventIndicies[i] = newIndex;
        }

        Arrays.sort(newEventIndicies);

        for (int i = 0; i < numberOfChilds; i++) {
            newEventChilds[i] = getChild(parentNode, newEventIndicies[i]);
        }

        return new TreeModelEvent(this, oldEvent.getPath(), newEventIndicies,
            newEventChilds);
    }

    /** {@inheritDoc} */
    public void treeNodesInserted(TreeModelEvent e) {
        //the new event needs the new indicies
        resetCachedChildList();

        TreeModelEvent newEvent = getSortedTreeEvent(e);

        for (Iterator iter = listeners.iterator(); iter.hasNext();) {
            TreeModelListener listener = (TreeModelListener) iter.next();
            listener.treeNodesInserted(newEvent);
        }
    }

    /** {@inheritDoc} */
    public void treeNodesChanged(TreeModelEvent e) {
        // TODO fix this one as well
        //.... since the tree nodes name can change this will
        // make it impossible to find the old index to fire the events to/from
        //
        MutableTreeNode parent = getParentNodeFromEvent(e);
        int[] allIndicies = new int[getChildCount(parent)];
        Object[] allObjects = new Object[getChildCount(parent)];

        for (int i = 0; i < allIndicies.length; i++) {
            allIndicies[i] = i;
            allObjects[i] = getChild(parent, i);
        }

        TreeModelEvent newEvent = new TreeModelEvent(this, e.getPath(),
                allIndicies, allObjects);

        for (Iterator iter = listeners.iterator(); iter.hasNext();) {
            TreeModelListener listener = (TreeModelListener) iter.next();
            listener.treeNodesChanged(newEvent);
        }

        //the new event needs the new indicies

        /*resetCachedChildList();

TreeModelEvent newEvent = getSortedTreeEvent(e);
for (Iterator iter = listeners.iterator(); iter.hasNext();) {
    TreeModelListener listener = (TreeModelListener) iter.next();
    listener.treeNodesChanged(newEvent);
}*/
        /*System.out.println("cached parent=" + cachedChildListParent);
MutableTreeNode parent = getParentNodeFromEvent(e);

boolean eventWasFired = false;

int[] oldIndicies = e.getChildIndices();
if ((parent == cachedChildListParent) && (oldIndicies.length == 1)) {
    List oldChildList = cachedChildList;
    resetCachedChildList();
    List newChildList = getChildList(parent);

    Object oldChangedChild = e.getChildren()[0];
    if (oldChildList.indexOf(oldChangedChild) == newChildList.indexOf(oldChangedChild)) {
        System.out.println("changing same place");
        TreeModelEvent newEvent = getSortedTreeEvent(e);
        for (Iterator iter = listeners.iterator(); iter.hasNext();) {
            TreeModelListener listener = (TreeModelListener) iter.next();
            listener.treeNodesChanged(newEvent);
        }
        eventWasFired = true;
    }
}
if (!eventWasFired) {
    TreeModelEvent newEvent = new TreeModelEvent(this, e.getPath(), null, null);
    treeStructureChanged(newEvent);
}
System.out.println("tree nodes changed");*/
    }

    /** {@inheritDoc} */
    public void treeNodesRemoved(TreeModelEvent e) {
        // TODO quickfix for removed events, firing the tree structure changed event instead.

        /*
resetCachedChildList();
TreeModelEvent newEvent = getSortedTreeEvent(e);
for (Iterator iter = listeners.iterator(); iter.hasNext();) {
    TreeModelListener listener = (TreeModelListener) iter.next();
    listener.treeNodesRemoved(newEvent);
}*/
        TreeModelEvent newEvent = new TreeModelEvent(this, e.getPath(), null,
                null);
        treeStructureChanged(newEvent);
    }

    /**
 * {@inheritDoc}
 * <p>
 * Current implementation invokes this method on the original tree model without any changes.
 */
    public void treeStructureChanged(TreeModelEvent e) {
        resetCachedChildList();

        // This event is only on affected nodes and downward, so we dont need to create a new event.
        for (Iterator iter = listeners.iterator(); iter.hasNext();) {
            TreeModelListener listener = (TreeModelListener) iter.next();
            listener.treeStructureChanged(e);
        }
    }

    /**
 * Resets the cached child list.
 */
    private void resetCachedChildList() {
        cachedChildListParent = null;
        cachedChildList = null;
    }

    /**
 * Returns the children for the treenode as a sorted list.
 * @param parent the parent tree node to get all children for.
 * @return a sorted List containing all children for the parent.
 */
    private List getChildList(Object parent) {
        List childList;

        if (parent == cachedChildListParent) {
            childList = cachedChildList;
        } else {
            childList = new ArrayList();

            int childCount = originalModel.getChildCount(parent);

            for (int i = 0; i < childCount; i++) {
                childList.add(originalModel.getChild(parent, i));
            }

            Collections.sort(childList, new ChildComparator());

            cachedChildList = childList;
            cachedChildListParent = parent;
        }

        return childList;
    }

    /**
 * Comparator to use when sorting the tree model list.
 * This uses the String.compareTo() method to do the actual work, note that
 * it is done using lowercase.
 * @author redsolo
 */
    private static class ChildComparator implements Comparator {
        /** {@inheritDoc} */
        public int compare(Object o1, Object o2) {
            return o1.toString().toLowerCase().compareTo(o2.toString()
                                                           .toLowerCase());
        }
    }
}
