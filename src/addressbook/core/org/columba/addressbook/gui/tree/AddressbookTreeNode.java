// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Library General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

package org.columba.addressbook.gui.tree;

import java.lang.reflect.Method;
import java.util.Hashtable;

import javax.swing.ImageIcon;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.columba.addressbook.config.FolderItem;
import org.columba.addressbook.folder.Folder;
import org.columba.core.command.WorkerStatusController;
import org.columba.core.gui.util.ImageLoader;
import org.columba.core.util.Lock;
import org.columba.core.xml.XmlElement;

public abstract class AddressbookTreeNode extends DefaultMutableTreeNode {
	

	protected ImageIcon icon = ImageLoader.getSmallImageIcon("stock_book-16.png");
	
	
	private String name;

	protected FolderItem node;

	protected final static ImageIcon collapsedIcon =
		ImageLoader.getSmallImageIcon("folder.png");

	protected Lock myLock;

	private static int nextUid = 0;

	private final Class[] FOLDER_ITEM_ARG = new Class[] { FolderItem.class };
	private final Class[] STRING_ARG = new Class[] { String.class };

	public AddressbookTreeNode(FolderItem item) {
		super();

		setNode(item);
	}

	public AddressbookTreeNode(String name) {
		super(name);

		this.name = name;
	}
	
	public FolderItem getFolderItem()
	{
		return node;
	
	}
	
	
	
	public ImageIcon getIcon()
		{
			return icon;
		}

	public final static FolderItem getDefaultItem(
		String className,
		XmlElement props) {
		XmlElement defaultElement = new XmlElement("folder");
		defaultElement.addAttribute("class", className);
		defaultElement.addAttribute("uid", Integer.toString(nextUid++));

		if (props != null)
			defaultElement.addElement(props);

		// FAILURE!!!

		/*
		XmlElement filter = new XmlElement("filter");
		defaultElement.addElement(filter);
		*/

		return new FolderItem(defaultElement);
	}

	public TreePath getSelectionTreePath() {
		//TreeNodeList list = new TreeNodeList( getTreePath() );
		TreeNode[] treeNodes = getPathToRoot(this, 0);
		TreePath path = new TreePath(treeNodes[0]);

		for (int i = 1; i < treeNodes.length; i++) {
			Folder folder = (Folder) treeNodes[i];
			path.pathByAddingChild(folder);
		}

		return path;
	}

	public static XmlElement getDefaultProperties() {
		return null;
	}

	public ImageIcon getCollapsedIcon() {
		return collapsedIcon;
	}

	public ImageIcon getExpandedIcon() {
		return collapsedIcon;
	}

	public void setName(String s) {
		name = s;
	}

	public String getName() {
		return name;
	}

	public int getUid() {
		return node.getInteger("uid");
	}

	public boolean tryToGetLock() {
		return myLock.tryToGetLock(null);
	}

	public void releaseLock() {
		myLock.release();
	}

	/**
	 * Returns the node.
	 * @return FolderItem
	 */
	public XmlElement getNode() {
		return node.getRoot();
	}

	/**
	 * Sets the node.
	 * @param node The node to set
	 */
	public void setNode(FolderItem node) {
		this.node = node;
	}

	//public abstract Class getDefaultChild();
	public  Class getDefaultChild()
	{
		return null;
	}

	final public Hashtable getAttributes() {
		return node.getElement("property").getAttributes();
	}

	public abstract void createChildren(WorkerStatusController worker);

	public void addFolder(String name, Class childClass) throws Exception {
		Method m_getDefaultProperties =
			childClass.getMethod("getDefaultProperties", null);

		XmlElement props =
			(XmlElement) m_getDefaultProperties.invoke(null, null);
		FolderItem childNode = getDefaultItem(childClass.getName(), props);

		childNode.set("property", "name", name);
		// Put properties that should be copied from parent here

		Folder subFolder =
			(Folder) childClass.getConstructor(FOLDER_ITEM_ARG).newInstance(
				new Object[] { childNode });
		addWithXml(subFolder);
	}

	public void addFolder(String name) throws Exception {
		addFolder(name, getDefaultChild());
	}

	public void addWithXml(AddressbookTreeNode folder) {
		add(folder);
		this.getNode().addElement(folder.getNode());
	}

}
