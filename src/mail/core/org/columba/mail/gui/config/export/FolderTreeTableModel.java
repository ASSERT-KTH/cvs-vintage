/*
 * Created on 07.09.2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.columba.mail.gui.config.export;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.columba.core.gui.util.treetable.AbstractTreeTableModel;

/**
 * Model combining a Tree- and a Table-Model. 
 *
 * @author fdietz
 */
public class FolderTreeTableModel extends AbstractTreeTableModel {

	FolderNode root;

	/**
		 * @param tree
		 * @param columns
		 */
	public FolderTreeTableModel(String[] columns) {
		super(columns);
	}

	public Class getColumnClass(int c) {
		// first column is a tree
		if (c == 0)
			return tree.getClass();

		else
			// second column is a JCheckBox column
			return Boolean.class;
	}

	public Object getValueAt(int row, int col) {
		DefaultMutableTreeNode node =
			(DefaultMutableTreeNode) tree.getPathForRow(row).getLastPathComponent();

		//if ( col == 1 ) return new Boolean(false);
		
		return node;
	}

	
	
	
	public void set(DefaultMutableTreeNode root) {
		tree.setRootNode(root);

		((DefaultTreeModel) tree.getModel()).nodeStructureChanged(root);

		fireTableDataChanged();
	}
	
	
	public void setValueAt(Object value, int row, int col) {

		if (col == 1) {

			// checkbox pressed

		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#isCellEditable(int, int)
	 */
	public boolean isCellEditable(int row, int col) {
		// enabled/disabled checkbox must be editable
		if (col == 1)
			return true;

		// tree must be editable, otherwise you can't collapse/expand tree nodes
		if (col == 0)
			return true;

		return false;
	}
}
