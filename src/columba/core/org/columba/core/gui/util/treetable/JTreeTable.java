/*
 * @(#)JTreeTable.java	1.2 98/10/27
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

import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.LookAndFeel;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeSelectionModel;

/**
 * This example shows how to create a simple JTreeTable component,
 * by using a JTree as a renderer (and editor) for the cells in a
 * particular column in the JTable.
 *
 * @version 1.2 10/27/98
 *
 * @author Philip Milne
 * @author Scott Violet
 */
public class JTreeTable extends JTable {
	/** A subclass of JTree. */
	protected TreeTableCellRenderer tree;

	protected boolean threadMode = false;

	protected TreeTableModelAdapter treeTableModelAdapter;

	public JTreeTable(TreeTableModel treeTableModel) {
		super();

		// Create the tree. It will be used as a renderer and editor.
		tree = new TreeTableCellRenderer(this, treeTableModel);

		// Install a tableModel representing the visible rows in the tree.
		treeTableModelAdapter = new TreeTableModelAdapter(treeTableModel, tree);
		super.setModel(treeTableModelAdapter);

		// Force the JTable and JTree to share their row selection models.
		ListToTreeSelectionModelWrapper selectionWrapper =
			new ListToTreeSelectionModelWrapper(tree);
		selectionWrapper.setSelectionMode(
			TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);

		tree.setSelectionModel(selectionWrapper);

		setSelectionModel(selectionWrapper.getListSelectionModel());

		// Install the tree  renderer and editor.
		setDefaultRenderer(TreeTableModel.class, tree);

		
		setDefaultEditor(
			TreeTableModel.class,
			new TreeTableCellEditor(this, tree));
		
		// No grid.
		setShowGrid(false);

		tree.expandRow(0);

		//expandRoot();

		tree.setToggleClickCount(1);

		tree.setShowsRootHandles(true);
		//

		tree.setRootVisible(false);
		//tree.putClientProperty("JTree.lineStyle", "None");

		// No intercell spacing
		setIntercellSpacing(new Dimension(0, 0));

		// And update the height of the trees row to match that of
		// the table.
		if (tree.getRowHeight() < 1) {
			// Metal looks better like this.
			setRowHeight(18);
		}

		sizeColumnsToFit(AUTO_RESIZE_NEXT_COLUMN);

		addMouseListener();

	}

	protected void addMouseListener() {

		final JTree treeView = getTree();

		MouseAdapter listMouseListener = new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {

				int index =
					treeView.getClosestRowForLocation(e.getX(), e.getY());

				treeView.expandRow(index);
			}
		};

		getTree().addMouseListener(listMouseListener);

	}

	public void setRowSelectionInterval(int index0, int index1) {

		getSelectionModel().setSelectionInterval(index0, index1);

	}

	public void setTreeCellRenderer(TreeCellRenderer renderer) {
		tree.setCellRenderer(renderer);
	}

	public void addTreeSelectionListener(TreeSelectionListener l) {
		tree.addTreeSelectionListener(l);
	}

	public void update() {
		//tree.treeDidChange();
	}

	/**
	 * Overridden to message super and forward the method to the tree.
	 * Since the tree is not actually in the component hieachy it will
	 * never receive this unless we forward it in this manner.
	 */
	public void updateUI() {
		
		
		super.updateUI();
		
		if (tree != null) {
			tree.updateUI();
		}
		// Use the tree's default foreground and background colors in the
		// table.
		LookAndFeel.installColorsAndFont(
			this,
			"Tree.background",
			"Tree.foreground",
			"Tree.font");
	}

	/* Workaround for BasicTableUI anomaly. Make sure the UI never tries to
	 * paint the editor. The UI currently uses different techniques to
	 * paint the renderers and editors and overriding setBounds() below
	 * is not the right thing to do for an editor. Returning -1 for the
	 * editing row in this case, ensures the editor is never painted.
	 */
	public int getEditingRow() {
		return (getColumnClass(editingColumn) == TreeTableModel.class)
			? -1
			: editingRow;
	}

	/**
	 * Overridden to pass the new rowHeight to the tree.
	 */
	public void setRowHeight(int rowHeight) {
		super.setRowHeight(rowHeight);
		if (tree != null && tree.getRowHeight() != rowHeight) {
			tree.setRowHeight(getRowHeight());
		}
	}

	/**
	 * Returns the tree that is being shared between the model.
	 */
	public JTree getTree() {
		return tree;
	}

}