package org.columba.mail.folder;

import java.lang.reflect.Method;
import java.util.Hashtable;

import javax.swing.ImageIcon;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.columba.core.command.WorkerStatusController;
import org.columba.core.gui.util.ImageLoader;
import org.columba.core.main.MainInterface;
import org.columba.core.util.Lock;
import org.columba.core.xml.XmlElement;
import org.columba.mail.config.FolderItem;
import org.columba.mail.plugin.FolderPluginHandler;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public abstract class FolderTreeNode
	extends DefaultMutableTreeNode
	implements TreeNodeInterface {

	protected final static ImageIcon collapsedIcon =
		ImageLoader.getSmallImageIcon("folder-closed.png");
		
		protected final static ImageIcon expandedIcon =
				ImageLoader.getSmallImageIcon("folder-open.png");

	protected FolderItem node;
	protected Lock myLock;

	private static int nextUid = 0;

	private final Class[] FOLDER_ITEM_ARG = new Class[] { FolderItem.class };
	private final Class[] STRING_ARG = new Class[] { String.class };

	public FolderTreeNode(FolderItem node) {
		super();
		setNode(node);
		myLock = new Lock();
	}

	public final static FolderItem getDefaultItem(
		String type,
		XmlElement props) {
		XmlElement defaultElement = new XmlElement("folder");
		defaultElement.addAttribute("type", type);
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

	/**
		 * Method getSelectionTreePath.
		 * @return TreePath
		 */
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

	public int getUid() {
		return node.getInteger("uid");
	}

	public ImageIcon getCollapsedIcon() {
		return collapsedIcon;
	}

	public ImageIcon getExpandedIcon() {
		return expandedIcon;
	}

	public CapabilityList getSupportedActions() {
		CapabilityList v = CapabilityList.getDefaultFolderCapabilities();

		Hashtable table = getAttributes();

		if (table.contains("accessrights")) {
			String accessrights = (String) table.get("accessrights");

			if (accessrights.equals("user")) {
				v.add(CapabilityList.RENAME_FOLDER_ACTION);
				v.add(CapabilityList.REMOVE_FOLDER_ACTION);
			}
		}

		if (table.contains("messagefolder")) {
			String messagefolder = (String) table.get("messagefolder");

			if (messagefolder.equals("true")) {
				v.add(CapabilityList.FOLDER_SHOW_HEADERLIST_ACTION);
			}
		}

		return v;

	}

	public boolean tryToGetLock(Object locker) {
		return myLock.tryToGetLock(locker);
	}

	public void releaseLock() {
		myLock.release();
	}

	public XmlElement getNode() {
		return node.getRoot();
	}

	public FolderItem getFolderItem() {
		return node;
	}

	public String getName() {
		return toString();
	}

	public void setName(String s) {
		this.setUserObject(s);
	}

	public String toString() {
		return (String) this.getUserObject();
	}

	/*
	public void insert(Folder newFolder, int newIndex) {
	
		Folder oldParent = (Folder) newFolder.getParent();
		int oldIndex = oldParent.getIndex(newFolder);
		oldParent.remove(oldIndex);
	
		AdapterNode oldParentNode = oldParent.getNode();
		AdapterNode newChildNode = newFolder.getNode();
		oldParentNode.removeChild(newChildNode);
	
		newFolder.setParent(this);
		children.insertElementAt(newFolder, newIndex);
	
		AdapterNode newParentNode = node;
	
		int j = -1;
		boolean inserted = false;
		for (int i = 0; i < newParentNode.getChildCount(); i++) {
			AdapterNode n = newParentNode.getChildAt(i);
			String name = n.getName();
	
			if (name.equals("folder")) {
				j++;
			}
	
			if (j == newIndex) {
				newParentNode.insert(newChildNode, i);
				inserted = true;
				System.out.println("------> adapternode insert correctly");
			}
		}
	
		if (inserted == false) {
			if (j + 1 == newIndex) {
				newParentNode.appendChild(newChildNode);
				System.out.println("------> adapternode appended correctly");
			}
		}
	
		//oldParent.fireTreeNodeStructureUpdate();
		//fireTreeNodeStructureUpdate();
	}
	*/

	/*
	public void removeFromParent() {
		AdapterNode childAdapterNode = getNode();
		childAdapterNode.remove();
	
		super.removeFromParent();
	}
	*/

	public void removeFolder() throws Exception {
		// remove XmlElement
		getFolderItem().getRoot().getParent().removeElement(
			getFolderItem().getRoot());

		// remove DefaultMutableTreeNode
		removeFromParent();
	}

	/*
	public void remove(FolderTreeNode childNode) {
		FolderTreeNode childFolder = (FolderTreeNode) childNode;
		AdapterNode childAdapterNode = childFolder.getNode();
		childAdapterNode.remove();
	
		int index = getIndex(childFolder);
		children.removeElementAt(index);
		//fireTreeNodeStructureUpdate();
	
		//return childFolder;
	}
	*/

	public abstract String getDefaultChild();

	final public Hashtable getAttributes() {
		return node.getElement("property").getAttributes();
	}

	public abstract void createChildren(WorkerStatusController worker);

	public void addFolder(String name, String type) throws Exception {
		FolderPluginHandler handler =
					(FolderPluginHandler) MainInterface.pluginManager.getHandler(
						"folder");
		
		Class childClass = handler.getPluginClass(type);
		
		Method m_getDefaultProperties =
			childClass.getMethod("getDefaultProperties", null);

		XmlElement props =
			(XmlElement) m_getDefaultProperties.invoke(null, null);

		/*			
		XmlElement props = getDefaultProperties();
		*/
		//XmlElement props = new XmlElement("property");
		props.addAttribute("name", name);

		FolderItem childNode = getDefaultItem(type, props);

		Object[] args = { childNode };

		FolderTreeNode folder = null;
		try {

			folder = (FolderTreeNode) handler.getPlugin(type, args);
			addWithXml(folder);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		/*
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
		*/
	}

	public void addFolder(String name) throws Exception {
		addFolder(name, getDefaultChild());
	}

	public void addWithXml(FolderTreeNode folder) {
		add(folder);
		this.getNode().addElement(folder.getNode());
	}

	/*
	public void removeFromParent()
	{
		
		Folder parentFolder = (Folder) getParent();
		parentFolder.remove(this);
		
	}
	*/

	/*
	public void moveUp()
	{
		Folder parentFolder = (Folder) getParent();
		if (parentFolder == null)
			return;
	
		AdapterNode parentNode = parentFolder.getNode();
	
		int childCount = parentFolder.getChildCount();
	
		int childIndex = parentFolder.getIndex((Folder) this);
		if (childIndex == -1)
			return;
		if (childIndex == 0)
			return;
	
		parentFolder.insert(this, childIndex - 1);
	
	}
	*/

	/*
	public void moveDown()
	{
		Folder parentFolder = (Folder) getParent();
		if (parentFolder == null)
			return;
	
		AdapterNode parentNode = parentFolder.getNode();
	
		int childCount = parentFolder.getChildCount();
	
		int childIndex = parentFolder.getIndex((Folder) this);
		if (childIndex == -1)
			return;
		if (childIndex >= childCount - 1)
			return;
	
		parentFolder.insert(this, childIndex + 1);
	}
	*/

	public TreeNode getChild(String str) {
		for (int i = 0; i < getChildCount(); i++) {
			Folder child = (Folder) getChildAt(i);
			String name = child.getName();

			if (name.equalsIgnoreCase(str))
				return child;
		}
		return null;
	}

	/*
	public void append(Folder newFolder) {
		Folder oldParent = (Folder) newFolder.getParent();
		int oldIndex = oldParent.getIndex(newFolder);
		oldParent.remove(oldIndex);
	
		AdapterNode oldParentNode = oldParent.getNode();
		AdapterNode newChildNode = newFolder.getNode();
		oldParentNode.removeChild(newChildNode);
	
		newFolder.setParent(this);
		children.add(newFolder);
	
		AdapterNode newParentNode = node;
		newParentNode.appendChild(newChildNode);
	
		// oldParent.fireTreeNodeStructureUpdate();
		// fireTreeNodeStructureUpdate();
	}
	*/

	/**
	 * Sets the node.
	 * @param node The node to set
	 */
	public void setNode(FolderItem node) {
		this.node = node;

		try {
			if (node.getInteger("uid") >= nextUid)
				nextUid = node.getInteger("uid") + 1;
		} catch (NumberFormatException ex) {
			node.set("uid", nextUid++);
		}
	}

}
