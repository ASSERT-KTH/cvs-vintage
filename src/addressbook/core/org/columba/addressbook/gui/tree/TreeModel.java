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

import java.lang.reflect.Constructor;
import java.util.Enumeration;

import javax.swing.tree.DefaultTreeModel;

import org.columba.addressbook.config.FolderItem;
import org.columba.core.config.DefaultXmlConfig;
import org.columba.core.xml.XmlElement;
import org.columba.mail.folder.Root;

class TreeModel extends DefaultTreeModel {
	//private AddressbookTreeNode rootNode;

	protected DefaultXmlConfig folderXmlConfig;
	protected Root rootNode;

	private final Class[] FOLDER_ITEM_ARG = new Class[] { FolderItem.class };

	public TreeModel(DefaultXmlConfig xmlConfig) {
		super(null);
		//rootNode = root;

		rootNode = new Root(xmlConfig.getRoot().getElement("tree"));
		setRoot(rootNode);
		//System.out.println("root-uid=" + rootNode.getUid());
		this.folderXmlConfig = xmlConfig;

		//System.out.println("root1=" + getRoot().toString());
		createDirectories(
			((AddressbookTreeNode) getRoot()).getNode(),
			(AddressbookTreeNode) getRoot());
	}

	public Object getRoot() {
		return rootNode;
	}

	public void createDirectories(
		XmlElement parentTreeNode,
		AddressbookTreeNode parentFolder) {
		int count = parentTreeNode.count();

		XmlElement child;

		if (count > 0) {
			for (int i = 0; i < count; i++) {

				child = parentTreeNode.getElement(i);
				String name = child.getName();
				//XmlElement nameNode = child.getName();

				//                System.out.println( "node: "+child );
				//                System.out.println( "nodename: "+nameNode.getValue());

				/*
				if ((name.equals("tree")) || (name.equals("folder"))) {
					FolderTreeNode folder = add(child, parentFolder);
					if (folder != null)
						createDirectories(child, folder);
				}
				*/
				if (name.equals("folder")) {
					AddressbookTreeNode folder = add(child, parentFolder);
					if (folder != null)
						createDirectories(child, folder);
				}

			}
		}
	}

	public AddressbookTreeNode add(
		XmlElement childNode,
		AddressbookTreeNode parentFolder) {

		FolderItem item = new FolderItem(childNode);

		if (item == null)
			return null;

		// i18n stuff

		String name = null;

		//XmlElement.printNode(item.getRoot(), "");

		int uid = item.getInteger("uid");

		/*
		try {
			if (uid == 100)
				name = MailResourceLoader.getString("tree", "localfolders");
			else if (uid == 101)
				name = MailResourceLoader.getString("tree", "inbox");
		
			else if (uid == 102)
				name = MailResourceLoader.getString("tree", "drafts");
		
			else if (uid == 103)
				name = MailResourceLoader.getString("tree", "outbox");
		
			else if (uid == 104)
				name = MailResourceLoader.getString("tree", "sent");
		
			else if (uid == 105)
				name = MailResourceLoader.getString("tree", "trash");
		
			else if (uid == 106)
				name = MailResourceLoader.getString("tree", "search");
			else if (uid == 107)
				name = MailResourceLoader.getString("tree", "templates");
		
			else
				name = item.get("property", "name");
		
			item.set("property", "name", name);
		
		} catch (MissingResourceException ex) {
			name = item.get("property", "name");
		}
		*/

		name = item.get("property", "name");

		// now instanciate the folder classes

		String className = item.get("class");
		ClassLoader loader = ClassLoader.getSystemClassLoader();
		try {
			Class actClass = loader.loadClass(className);
			//System.out.println("superclass="+actClass.getSuperclass().getName());

			/*
			if (actClass
				.getSuperclass()
				.getName()
				.equals("org.columba.mail.folder.Folder")) {
			
				Folder folder = (Folder) actClass.newInstance();
			}
			*/

			Constructor c = actClass.getConstructor(FOLDER_ITEM_ARG);

			if (c != null) {
				Object[] args = { item };

				AddressbookTreeNode folder =
					(AddressbookTreeNode) c.newInstance(args);
				parentFolder.add(folder);

				return folder;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		/*
		if (item.getType().equals("columba")) {
			//ColumbaFolder f = new ColumbaFolder(childNode, item);
			CachedMHFolder f = new CachedMHFolder(childNode, item);
		
			FilterList list = new FilterList(f);
			parentFolder.add(f);
		
			return f;
		} else if (item.getType().equals("virtual")) {
		
			VirtualFolder f = new VirtualFolder(childNode, item);
			Search search = new Search(childNode, f);
			parentFolder.add(f);
		
			return f;
		} else if (item.getType().equals("outbox")) {
		
			OutboxFolder f = new OutboxFolder(childNode, item);
			parentFolder.add(f); // Do never exchange with line below!!
		
			return f;
		
		} else if (item.getType().equals("imap")) {
			AccountItem accountItem =
				MailConfig.getAccountList().uidGet(item.getAccountUid());
		
			ImapItem item2 = accountItem.getImapItem();
		
			IMAPRootFolder imapRootFolder = null;
		
			IMAPFolder f =
				new IMAPFolder(childNode, item, item2, imapRootFolder);
			FilterList list = new FilterList(f);
			parentFolder.add(f);
		
			return f;
		
		} else if (item.getType().equals("imaproot")) {
		
			AccountItem accountItem =
				MailConfig.getAccountList().uidGet(item.getAccountUid());
		
			ImapItem item2 = accountItem.getImapItem();
		
			IMAPRootFolder f =
				new IMAPRootFolder(
					childNode,
					item,
					item2,
					item.getAccountUid());
			f.setName(accountItem.getName());
			parentFolder.add(f);
		
			return f;
		}
		*/
		return null;
	}

	public AddressbookTreeNode getFolder(int uid) {
		AddressbookTreeNode root = (AddressbookTreeNode) getRoot();

		for (Enumeration e = root.breadthFirstEnumeration();
			e.hasMoreElements();
			) {
			AddressbookTreeNode node = (AddressbookTreeNode) e.nextElement();

			int id = node.getUid();

			if (uid == id) {

				return node;
			}

		}
		return null;

	}

	/* ===================================================================== */
	// methods for TreeModel implementation

	/*
	public Object getRoot() {
		return rootNode;
	}
	
	public boolean isLeaf(Object aNode) {
		AddressbookTreeNode node = (AddressbookTreeNode) aNode;
		if (node.getChildCount() > 0)
			return false;
		return true;
	}
	
	public int getChildCount(Object parent) {
		AddressbookTreeNode node = (AddressbookTreeNode) parent;
		return node.getChildCount();
	}
	
	public Object getChild(Object parent, int index) {
		AddressbookTreeNode node = (AddressbookTreeNode) parent;
		return node.getChildAt(index);
	}
	
	public int getIndexOfChild(Object parent, Object child) {
		AddressbookTreeNode node = (AddressbookTreeNode) parent;
		return node.getIndex((AddressbookTreeNode) child);
	}
	*/
}
