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
package org.columba.mail.gui.table;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.columba.core.config.HeaderItem;
import org.columba.core.config.TableItem;
import org.columba.core.gui.util.treetable.CustomTreeTableCellRenderer;
import org.columba.core.gui.util.treetable.Tree;
import org.columba.mail.folder.Folder;
import org.columba.mail.folder.command.MarkMessageCommand;
import org.columba.mail.gui.table.util.MessageNode;
import org.columba.mail.gui.table.util.TableModelFilteredView;
import org.columba.mail.gui.table.util.TableModelPlugin;
import org.columba.mail.gui.table.util.TableModelSorter;
import org.columba.mail.gui.table.util.TableModelThreadedView;
import org.columba.mail.message.ColumbaHeader;
import org.columba.mail.message.HeaderInterface;
import org.columba.mail.message.HeaderList;
import org.columba.mail.message.MessageCollection;

public class HeaderTableModel extends AbstractTableModel {

	private TableItem item;

	private Folder folder;
	protected HeaderList headerList;

	protected Hashtable uidList;

	private MessageCollection messageCollection;

	private MessageNode root;

	private MessageNode selectedMessageNode;

	private Vector tableModelPlugins;

	private Tree tree;
	
	private boolean enableThreadedView;
	
	public HeaderTableModel(TableItem item) {
		//super(null);
		this.item = item;

		tableModelPlugins = new Vector();

		/*
		root = new MessageNode("root", null);
		super.setRoot(root);
		*/
		uidList = new Hashtable();

		//mutex = false;

	}

	public void setTree(Tree tree) {
		this.tree = tree;
		tree.setRootNode(new MessageNode(new ColumbaHeader(), "0"));
	}

	public void registerPlugin(TableModelPlugin plugin) {
		tableModelPlugins.add(plugin);
	}

	public Folder getFolder() {
		return folder;
	}

	public MessageCollection getMessageCollection() {
		return messageCollection;
	}

	public MessageNode getRootNode() {
		return root;
	}

	public MessageNode getSelectedMessageNode() {
		return selectedMessageNode;
	}

	public void setSelectedMessageNode(MessageNode node) {
		selectedMessageNode = node;
	}

	protected void removeNode(MessageNode messageNode) {
		getTreeModel().removeNodeFromParent(messageNode);

	}

	public void markHeader(Object[] uids, int subMode) {

		for (int i = 0; i < uids.length; i++) {

			MessageNode node = (MessageNode) uidList.get(uids[i]);

			if (node != null) {

				HeaderInterface header = node.getHeader();
				switch (subMode) {
					case MarkMessageCommand.MARK_AS_READ :
						{
							header.getFlags().setSeen(true);
							header.getFlags().setRecent(false);
							break;
						}
					case MarkMessageCommand.MARK_AS_FLAGGED :
						{
							header.getFlags().setFlagged(true);
							break;
						}
					case MarkMessageCommand.MARK_AS_EXPUNGED :
						{
							header.getFlags().setDeleted(true);
							break;
						}
					case MarkMessageCommand.MARK_AS_ANSWERED :
						{
							header.getFlags().setAnswered(true);
							break;
						}
				}

				if (uids.length < 100)
					getTreeModel().nodeChanged(node);
			} else {
				System.out.println("unable to find message");
			}
		}


		fireTableDataChanged();
		/*
		if (uids.length >= 100)
			update();
		*/
		
		
	}

	public DefaultTreeModel getTreeModel() {
		return (DefaultTreeModel) tree.getModel();
	}

	public void removeHeaderList(Object[] uids) {

		if (uids != null) {
			if (uids.length > 100) { // recreate whole tablemodel
				for (int i = 0; i < uids.length; i++) {
					//headerList.remove(uids[i]);
					uidList.remove(uids[i]);
				}

				//update();
			} else { // single operation per message
				for (int i = 0; i < uids.length; i++) {
					//headerList.remove(uids[i]);
					MessageNode node = (MessageNode) uidList.get(uids[i]);
					if (node != null && node.getParent() != null)
						removeNode(node);
				}
			}
		}

		fireTableDataChanged();
	}

	public void addHeaderList(HeaderInterface[] headerList) throws Exception {

		for (int i = 0; i < headerList.length; i++) {
			addHeader(headerList[i]);
		}

	}

	public void addHeader(HeaderInterface header) throws Exception {

		int count;
		if (headerList != null)
			count = getRootNode().getChildCount();
		else
			count = 0;
		if (count == 0) {

			headerList = new HeaderList();
			uidList = new Hashtable();

			Object uid = header.get("columba.uid");
			headerList.add(header, uid);

			MessageNode child = new MessageNode(header, uid);
			uidList.put(uid, child);

			getRootNode().add(child);

			getTreeModel().nodeStructureChanged(getRootNode());
			return;
		}
		try { // we don't need this here

			Object uid = header.get("columba.uid");
			MessageNode node = new MessageNode(header, uid);
			uidList.put(uid, node);

			setSelectedMessageNode(node);
			boolean result =
				getTableModelFilteredView().manipulateModel(
					TableModelPlugin.NODES_INSERTED);
			if (result == true) {
				boolean result2 =
					getTableModelThreadedView().manipulateModel(
						TableModelPlugin.NODES_INSERTED);
				if (result2 == true) {

				} else {
					int index =
						getTableModelSorter().getInsertionSortIndex(node);
					getTreeModel().insertNodeInto(node, getRootNode(), index);
					//hashtable.put( message.getUID(), node );
				}
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	/*
	public TreeNode[] getPathToRoot(TreeNode aNode, int depth) {
	
		TreeNode[] nodes = super.getPathToRoot(aNode, depth);
		return nodes;
	}
	*/

	public TableModelFilteredView getTableModelFilteredView() {
		return (TableModelFilteredView) tableModelPlugins.get(0);
	}

	public TableModelThreadedView getTableModelThreadedView() {
		return (TableModelThreadedView) tableModelPlugins.get(1);
	}

	public TableModelSorter getTableModelSorter() {
		return (TableModelSorter) tableModelPlugins.get(2);
	}

	public HeaderList getHeaderList() {
		return headerList;
	}

	public void update() {

		if (root == null)
			root = new MessageNode(new ColumbaHeader(), "0");

		root.removeAllChildren();

		uidList.clear();

		if (headerList == null) {
			tree.setRootNode(root);
			return;
		}

		if (headerList.count() == 0) {
			tree.setRootNode(root);
			return;
		}

		try {

			boolean result =
				getTableModelFilteredView().manipulateModel(
					TableModelPlugin.STRUCTURE_CHANGE);
			if (result == false) {
				for (Enumeration e = headerList.keys(); e.hasMoreElements();) {
					Object uid = e.nextElement();
					HeaderInterface header = headerList.getHeader(uid);
					MessageNode child = new MessageNode(header, uid);
					uidList.put(uid, child);
					root.add(child);
				}

			}

			getTableModelThreadedView().manipulateModel(
				TableModelPlugin.STRUCTURE_CHANGE);
			getTableModelSorter().manipulateModel(
				TableModelPlugin.STRUCTURE_CHANGE);

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		tree.setRootNode(root);
		fireTableDataChanged();
		//tree.revalidate();
		//tree.repaint();
	}

	public void setHeaderList(HeaderList list) {
		headerList = list;

	}
	/***************************** treemodel interface ********************************/ //
	// The TreeModel interface
	//

	/*
	public int getChildCount(Object node) {
	
		DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) node;
		int count = treeNode.getChildCount();
		return count;
	}
	
	public Object getChild(Object node, int i) {
	
		DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) node;
		DefaultMutableTreeNode child =
			(DefaultMutableTreeNode) treeNode.getChildAt(i);
		return child;
	}
	
	public boolean isLeaf(Object node) {
		//Message message = (Message) node;
		DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) node;
		boolean result;
		if (treeNode.getChildCount() == 0)
			result = true;
		else
			result = false;
		return result;
	}
		*/

	public int getColumnCount() {
		int count = 0;

		for (int i = 0; i < item.count(); i++) {
			HeaderItem headerItem = item.getHeaderItem(i);
			boolean enabled = headerItem.getBoolean("enabled");

			if (enabled == true)
				count++;
		}

		return count;
	}

	public String getColumnName(int column) {

		return item.getHeaderItem(column).get("name");
	}

	public int getColumnNumber(String name) {

		for (int i = 0;
			i < getColumnCount();
			i++) { //System.out.println("column name: "+ getColumnName(i) );
			if (name.indexOf(getColumnName(i)) != -1)
				return i;
		}

		return -1;
	}
	
	public void enableThreadedView( boolean b)
	{
		enableThreadedView = b;
	}

	public Class getColumnClass(int column) {
		
		if ( enableThreadedView )
		{
		
		String name = getColumnName(column);
		if (name.equalsIgnoreCase("Subject"))
			return CustomTreeTableCellRenderer.class;
		else
			return getValueAt(0, column).getClass();
		}
		else
		return getValueAt(0, column).getClass();

		//return null;

	}

	public int getRowCount() {
		return tree.getRowCount();
	}

	public Object getValueAt(int row, int col) {
		TreePath treePath = tree.getPathForRow(row);
		return (MessageNode) treePath.getLastPathComponent();

		//if ( col == 0 ) return tree;
	}

	/*
	public Object getValueAt(Object node, int col) {
	
		MessageNode treeNode = (MessageNode) node;
		return treeNode;
	
	}
	*/

	public boolean isCellEditable(int row, int col) {
		String name = getColumnName(col);
		if (name.equalsIgnoreCase("Subject"))
			return true;

		return false;
	}
	public MessageNode getMessageNode(Object uid) {
		return (MessageNode) uidList.get(uid);
	}

}
