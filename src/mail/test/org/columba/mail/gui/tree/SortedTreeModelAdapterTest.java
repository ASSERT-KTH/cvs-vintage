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

import junit.framework.TestCase;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeModel;


/**
 * @author Erik Mattsson
 */
public class SortedTreeModelAdapterTest extends TestCase {
    private DefaultMutableTreeNode orgRootNode;
    private DefaultTreeModel orgModel;
    private TreeModel sortedModel;

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        orgRootNode = new DefaultMutableTreeNode();
        orgModel = new DefaultTreeModel(orgRootNode);

        DefaultMutableTreeNode bChild = new DefaultMutableTreeNode("BB");
        bChild.add(new DefaultMutableTreeNode("9"));
        bChild.add(new DefaultMutableTreeNode("7"));
        bChild.add(new DefaultMutableTreeNode("3"));
        bChild.add(new DefaultMutableTreeNode("8"));

        orgModel.insertNodeInto(bChild, orgRootNode, 0);
        orgModel.insertNodeInto(new DefaultMutableTreeNode("AA"), orgRootNode, 1);
        orgModel.insertNodeInto(new DefaultMutableTreeNode("FF"), orgRootNode, 2);
        sortedModel = new SortedTreeModelAdapter(orgModel);
    }

    /**
 * Test to sort a simple one level tree.
 */
    public void testGetChild() {
        assertSame("Expected the same object at index 0 in the sorted list",
            orgModel.getChild(orgRootNode, 1),
            sortedModel.getChild(orgRootNode, 0));
        assertSame("Expected the same object at index 1 in the sorted list",
            orgModel.getChild(orgRootNode, 0),
            sortedModel.getChild(orgRootNode, 1));
        assertSame("Expected the same object at index 2 in the sorted list",
            orgModel.getChild(orgRootNode, 2),
            sortedModel.getChild(orgRootNode, 2));
    }

    /**
 * Test to sort a simple two level tree.
 */
    public void testSort() {
        Object orgBChild = orgModel.getChild(orgRootNode, 0);
        Object expBChild = sortedModel.getChild(orgRootNode, 1);
        assertSame("The b child was not the same object", orgBChild, expBChild);

        Object bChild = expBChild;
        assertSame("Expected the same object at index 0 in the sorted list",
            orgModel.getChild(bChild, 2), sortedModel.getChild(bChild, 0));
        assertSame("Expected the same object at index 1 in the sorted list",
            orgModel.getChild(bChild, 1), sortedModel.getChild(bChild, 1));
        assertSame("Expected the same object at index 2 in the sorted list",
            orgModel.getChild(bChild, 3), sortedModel.getChild(bChild, 2));
        assertSame("Expected the same object at index 3 in the sorted list",
            orgModel.getChild(bChild, 0), sortedModel.getChild(bChild, 3));
    }

    /**
 * Tests the isLeaf() method.
 */
    public void testIsLeaf() {
        Object orgBChild = orgModel.getChild(orgRootNode, 0);
        assertEquals("The B child is not a child as it is in the original model.",
            orgModel.isLeaf(orgBChild), sortedModel.isLeaf(orgBChild));
    }

    /**
 * Tests the getChildCount() method.
 */
    public void testGetChildCount() {
        assertEquals("The number of childs of the root is not correct",
            orgModel.getChildCount(orgRootNode),
            sortedModel.getChildCount(orgRootNode));

        Object orgBChild = orgModel.getChild(orgRootNode, 0);
        assertEquals("The number of childs of the B child is not correct",
            orgModel.getChildCount(orgBChild),
            sortedModel.getChildCount(orgBChild));
    }

    /**
 * Tests the getIndexOfChild() method.
 */
    public void testGetIndexOfChild() {
        assertEquals("The child should be the middle child in the sorted list.",
            1,
            sortedModel.getIndexOfChild(orgRootNode,
                orgModel.getChild(orgRootNode, 0)));
        assertEquals("The child should be the first child in the sorted list.",
            0,
            sortedModel.getIndexOfChild(orgRootNode,
                orgModel.getChild(orgRootNode, 1)));
        assertEquals("The child should be the last child in the sorted list.",
            2,
            sortedModel.getIndexOfChild(orgRootNode,
                orgModel.getChild(orgRootNode, 2)));
    }

    /**
 * Tests the addTreeModeListener() method.
 */
    public void testAddListener() {
        DummyTreeListener listener = new DummyTreeListener();
        sortedModel.addTreeModelListener(listener);

        orgModel.insertNodeInto(orgRootNode, new DefaultMutableTreeNode("aaa"),
            0);
        assertNotNull("The insert event wasnt fired correctly",
            listener.treeNodesInsertedEvent);
        listener.clearEvents();

        /*orgModel.removeNodeFromParent(orgRootNode.getFirstLeaf());
assertNotNull("The remove event wasnt fired correctly", listener.treeNodesRemovedEvent);
listener.clearEvents();*/
        orgModel.setRoot(new DefaultMutableTreeNode("NEW ROOT"));
        assertNotNull("The tree structure change event wasnt fired correctly",
            listener.treeStructureChangedEvent);
    }

    /**
 * Tests the removeTreeModelListener() method.
 */
    public void testRemoveListener() {
        DummyTreeListener listener = new DummyTreeListener();
        sortedModel.addTreeModelListener(listener);

        orgModel.insertNodeInto(orgRootNode, new DefaultMutableTreeNode("aaa"),
            0);
        assertNotNull("The insert event wasnt fired correctly",
            listener.treeNodesInsertedEvent);
        listener.clearEvents();
        sortedModel.removeTreeModelListener(listener);

        orgModel.insertNodeInto(orgRootNode, new DefaultMutableTreeNode("aaa"),
            0);
        assertNull("The listener received the inserted event",
            listener.treeNodesInsertedEvent);
        listener.clearEvents();

        /*orgModel.removeNodeFromParent(orgRootNode.getFirstLeaf());
assertNull("The listener received the removed event", listener.treeNodesRemovedEvent);
listener.clearEvents();*/
        orgModel.setRoot(new DefaultMutableTreeNode("NEW ROOT"));
        assertNull("The listener received the tree structure change event",
            listener.treeStructureChangedEvent);
    }

    /**
 * Tests that the tree nodes inserted event is thrown and with the correct indexes.
 */
    public void testNodesInsertedEvent() {
        DummyTreeListener listener = new DummyTreeListener();
        sortedModel.addTreeModelListener(listener);

        DefaultMutableTreeNode newNode = new DefaultMutableTreeNode("DD");
        orgModel.insertNodeInto(newNode, orgRootNode, 1);

        Object[] paths = listener.treeNodesInsertedEvent.getPath();
        int[] newChildIndexes = listener.treeNodesInsertedEvent.getChildIndices();
        assertSame("The event did not occur with the root node as the parent",
            orgRootNode, paths[0]);
        assertEquals("The number of new childs was not correct.", 1,
            newChildIndexes.length);
        assertEquals("The new node's index was not sorted in the model.", 2,
            newChildIndexes[0]);
        assertSame("The model has a different object for the event's index",
            newNode, sortedModel.getChild(orgRootNode, newChildIndexes[0]));

        listener.clearEvents();

        MutableTreeNode orgBChild = (MutableTreeNode) orgModel.getChild(orgRootNode,
                0);
        newNode = new DefaultMutableTreeNode("5");
        orgModel.insertNodeInto(newNode, orgBChild, 0);
        paths = listener.treeNodesInsertedEvent.getPath();
        newChildIndexes = listener.treeNodesInsertedEvent.getChildIndices();
        assertSame("The event did not occur with the B node as the parent",
            orgBChild, paths[1]);
        assertEquals("The number of new childs was not correct.", 1,
            newChildIndexes.length);
        assertEquals("The new node's index was not sorted in the model.", 1,
            newChildIndexes[0]);
        assertSame("The model has a different object for the event's index",
            newNode, sortedModel.getChild(orgBChild, newChildIndexes[0]));
    }

    /**
 * Tests that the tree nodes removed event is thrown and with the correct indexes.
 */
    public void testNodesRemovedEvent() {
        DummyTreeListener listener = new DummyTreeListener();
        sortedModel.addTreeModelListener(listener);

        MutableTreeNode orgBChild = (MutableTreeNode) orgModel.getChild(orgRootNode,
                0);

        orgModel.removeNodeFromParent(orgBChild);

        Object[] paths = listener.treeStructureChangedEvent.getPath();
        assertNull("The index list should be null",
            listener.treeStructureChangedEvent.getChildIndices());
        assertNull("The children list should be null",
            listener.treeStructureChangedEvent.getChildren());
        assertSame("The event did not occur with the root node as the parent",
            orgRootNode, paths[0]);

        /*
Object[] paths = listener.treeNodesRemovedEvent.getPath();
int[] removedIndexes = listener.treeNodesRemovedEvent.getChildIndices();
assertEquals("The number removed nodes is not correct", 1, removedIndexes.length);
assertEquals("The removed node's index was not the correct one", 1, removedIndexes[0]);
assertSame("The event did not occur with the root node as the parent", orgRootNode, paths[0]);
assertSame("The removed object from the event is not the one that was removed.", orgBChild, listener.treeNodesRemovedEvent.getChildren()[0]);
*/
    }

    /**
 * Tests that the tree nodes changed event is thrown and with the correct indexes.
 */
    public void atestNodesChangedEvent() {
        DummyTreeListener listener = new DummyTreeListener();
        sortedModel.addTreeModelListener(listener);

        DefaultMutableTreeNode orgBChild = (DefaultMutableTreeNode) orgModel.getChild(orgRootNode,
                0);

        orgModel.nodeChanged(orgBChild);

        Object[] paths = listener.treeNodeChangedEvent.getPath();
        int[] changedIndexes = listener.treeNodeChangedEvent.getChildIndices();
        assertEquals("The number changed nodes is not correct", 1,
            changedIndexes.length);
        assertEquals("The changed node's index was not the correct one", 1,
            changedIndexes[0]);
        assertSame("The event did not occur with the root node as the parent",
            orgRootNode, paths[0]);
        assertSame("The changed object from the event is not the one that was changed.",
            orgBChild, listener.treeNodeChangedEvent.getChildren()[0]);
    }

    /**
 * Tests that the tree nodes inserted event is thrown and with the correct indexes.
 */
    public void testTreeStructureChangedEvent() {
        DummyTreeListener listener = new DummyTreeListener();
        sortedModel.addTreeModelListener(listener);

        DefaultMutableTreeNode orgBChild = (DefaultMutableTreeNode) orgModel.getChild(orgRootNode,
                0);

        orgModel.nodeStructureChanged(orgBChild);

        Object[] paths = listener.treeStructureChangedEvent.getPath();
        assertNull("The index list should be null",
            listener.treeStructureChangedEvent.getChildIndices());
        assertNull("The children list should be null",
            listener.treeStructureChangedEvent.getChildren());
        assertSame("The event did not occur with the root node as the parent",
            orgRootNode, paths[0]);
        assertSame("The event did not occur with the root node as the parent",
            orgBChild, paths[1]);
    }

    /**
 * A dummy listener that stores all the latest events.
 * @author redsolo
 */
    private static class DummyTreeListener implements TreeModelListener {
        private TreeModelEvent treeNodeChangedEvent;
        private TreeModelEvent treeNodesInsertedEvent;
        private TreeModelEvent treeNodesRemovedEvent;
        private TreeModelEvent treeStructureChangedEvent;

        /** {@inheritDoc} */
        public void treeNodesChanged(TreeModelEvent e) {
            treeNodeChangedEvent = e;
        }

        /** {@inheritDoc} */
        public void treeNodesInserted(TreeModelEvent e) {
            treeNodesInsertedEvent = e;
        }

        /** {@inheritDoc} */
        public void treeNodesRemoved(TreeModelEvent e) {
            treeNodesRemovedEvent = e;
        }

        /** {@inheritDoc} */
        public void treeStructureChanged(TreeModelEvent e) {
            treeStructureChangedEvent = e;
        }

        /**
 * Clears all tree node events.
 */
        public void clearEvents() {
            treeNodeChangedEvent = null;
            treeNodesInsertedEvent = null;
            treeNodesRemovedEvent = null;
            treeStructureChangedEvent = null;
        }
    }
}
