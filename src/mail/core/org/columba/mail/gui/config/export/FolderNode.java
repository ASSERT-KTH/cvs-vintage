/*
 * Created on 07.09.2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.columba.mail.gui.config.export;

import javax.swing.tree.DefaultMutableTreeNode;

import org.columba.mail.folder.FolderTreeNode;

/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class FolderNode extends DefaultMutableTreeNode {

	boolean export;
	String name;

	FolderTreeNode treeNode;

	public FolderNode() {
		export = false;
	}

	public FolderNode(String name) {
		this.name = name;

		export = false;
	}

	public FolderNode(FolderTreeNode treeNode) {
		this.treeNode = treeNode;

		export = false;
	}

	/**
	 * @return
	 */
	public boolean isExport() {
		return export;
	}

	/**
	 * @param export
	 */
	public void setExport(boolean export) {
		this.export = export;
	}

	/**
	 * @return
	 */
	public String getName() {
		if (treeNode == null)
			return name;
		else
			return treeNode.getName();
	}

	/**
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/*
	public Enumeration children() {
		Vector list = new Vector();
		
		for (int i = 0; i < treeNode.getChildCount(); i++) {
			FolderTreeNode child = (FolderTreeNode) treeNode.getChildAt(i);
			if ((child instanceof VirtualFolder) == false) {
				list.add(child);
			}
		}
		
		return list.elements();
	}
	
	
	public TreeNode getChildAt(int index) {
		int pos = 0;
	
		for (int i = 0; i < treeNode.getChildCount(); i++) {
			FolderTreeNode child = (FolderTreeNode) treeNode.getChildAt(i);
	
			if ((child instanceof VirtualFolder) == false) {
				pos++;
			}
			
			if (index == pos ) return child;
		}
		
		return null;
	}
	
	
	public int getChildCount() {
		int sum = 0;
	
		for (int i = 0; i < treeNode.getChildCount(); i++) {
			FolderTreeNode child = (FolderTreeNode) treeNode.getChildAt(i);
			if ((child instanceof VirtualFolder) == false) {
				sum++;
			}
		}
		return super.getChildCount();
	}
	
	public int getIndex(TreeNode node) {
		int sum = 0;
	
		for (int i = 0; i < treeNode.getChildCount(); i++) {
			FolderTreeNode child = (FolderTreeNode) treeNode.getChildAt(i);
			if ((child instanceof VirtualFolder) == false) {
				sum++;
			}
			
			if ( child.equals(node )) return sum;
		}
		
		return -1;
	}
	
	public TreeNode getParent() {
		return treeNode.getParent();
	}
	
	*/

}
