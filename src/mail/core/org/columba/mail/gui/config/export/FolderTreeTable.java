/*
 * Created on 07.09.2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.columba.mail.gui.config.export;

import java.util.HashMap;
import java.util.Map;

import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultTreeModel;

import org.columba.core.gui.util.treetable.Tree;
import org.columba.core.gui.util.treetable.TreeTable;
import org.columba.core.main.MainInterface;
import org.columba.mail.folder.FolderTreeNode;

/**
 * TreeTable displaying a tree view and a checkbox column.
 *
 * 
 * @author fdietz
 */
public class FolderTreeTable extends TreeTable {

	final static String[] columns = { "Name", "Export" };

	FolderTreeTableModel model;
	
	/**
	 * save enabled state for every treenode
	 */
	Map map;

	public FolderTreeTable() {
		
		map = new HashMap();
		
		model = new FolderTreeTableModel(columns);
		model.setTree((Tree) getTree());
		((DefaultTreeModel) model.getTree().getModel()).setAsksAllowsChildren(
			true);
		
		initTree();
		
		setModel(model);
		
		getTree().setCellRenderer(new FolderTreeCellRenderer());

		// make "Export" column fixed size
		TableColumn tc = getColumn(columns[1]);
		tc.setCellRenderer(new EnabledRenderer(map));
		
		tc.setCellEditor(new EnabledEditor(map));
		
		tc.setMaxWidth(80);
		tc.setMinWidth(80);
	}

	protected void initTree() {
		FolderTreeNode root =
			(FolderTreeNode) MainInterface.treeModel.getRoot();

		//model.setTreeModel( new FolderTreeModel(new FolderNode(root)) );
		
		model.set(root);
	}

	/**
	 * @return
	 */
	public Map getMap() {
		return map;
	}

}
