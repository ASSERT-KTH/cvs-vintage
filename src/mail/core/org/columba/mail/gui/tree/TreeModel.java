package org.columba.mail.gui.tree;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.MissingResourceException;

import javax.swing.tree.DefaultTreeModel;

import org.columba.core.config.AdapterNode;
import org.columba.mail.config.AccountItem;
import org.columba.mail.config.FolderItem;
import org.columba.mail.config.FolderXmlConfig;
import org.columba.mail.config.ImapItem;
import org.columba.mail.config.MailConfig;
import org.columba.mail.filter.FilterList;
import org.columba.mail.filter.Search;
import org.columba.mail.folder.Folder;
import org.columba.mail.folder.FolderTreeNode;
import org.columba.mail.folder.Root;
import org.columba.mail.folder.imap.IMAPFolder;
import org.columba.mail.folder.imap.IMAPRootFolder;
import org.columba.mail.folder.mh.CachedMHFolder;
import org.columba.mail.folder.outbox.OutboxFolder;
import org.columba.mail.folder.virtual.VirtualFolder;
import org.columba.mail.gui.tree.util.TreeNodeList;
import org.columba.mail.util.MailResourceLoader;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class TreeModel extends DefaultTreeModel {
	protected FolderXmlConfig folderXmlConfig;
	protected Root rootNode;

	public TreeModel(FolderXmlConfig folderConfig) {
		super(null);

		rootNode = new Root(folderConfig.getRootNode());
		setRoot(rootNode);
		System.out.println("root-uid=" + rootNode.getUid());
		this.folderXmlConfig = folderConfig;

		System.out.println("root1=" + getRoot().toString());
		createDirectories(
			((FolderTreeNode) getRoot()).getNode(),
			(FolderTreeNode) getRoot());

		System.out.println("root2=" + getRoot());

	}

	public Object getRoot() {
		return rootNode;
	}

	public void createDirectories(
		AdapterNode parentTreeNode,
		FolderTreeNode parentFolder) {
		int count = parentTreeNode.getChildCount();
		AdapterNode child;

		if (count > 0) {
			for (int i = 0; i < count; i++) {

				child = parentTreeNode.getChild(i);
				String name = child.getName();
				AdapterNode nameNode = child.getChild("name");

				//                System.out.println( "node: "+child );
				//                System.out.println( "nodename: "+nameNode.getValue());
				if ((name.equals("tree")) || (name.equals("folder"))) {
					FolderTreeNode folder = add(child, parentFolder);
					if (folder != null)
						createDirectories(child, folder);
				}
			}
		}
	}

	public FolderItem getItem(AdapterNode node) {
		return folderXmlConfig.getFolderItem(node);
	}

	public AdapterNode addFolder(
		FolderTreeNode parentFolder,
		Hashtable folderAttributes) {
		AdapterNode adapterNode =
			MailConfig.getFolderConfig().addFolderNode(
				parentFolder.getNode(),
				(String) folderAttributes.get("name"),
				(String) folderAttributes.get("accessrights"),
				(String) folderAttributes.get("messagefolder"),
				(String) folderAttributes.get("type"),
				(String) folderAttributes.get("subfolder"),
				(String) folderAttributes.get("add"),
				(String) folderAttributes.get("remove"),
				(Integer) folderAttributes.get("accountuid"));

		return adapterNode;
	}

	/*
	public void addUserFolder(FolderTreeNode parentFolder, String name) {
		AdapterNode adapterNode =
			MailConfig.getFolderConfig().addFolderNode(
				parentFolder.getNode(),
				name,
				"user",
				"true",
				"columba",
				"true",
				"true",
				"true",new Integer(-1));
	
		FolderItem item = folderXmlConfig.getFolderItem(adapterNode);
	
	}
	*/

	/*
	public FolderTreeNode addVirtualFolder(
		FolderTreeNode parentFolder,
		String name) {
		AdapterNode adapterNode =
			MailConfig.getFolderConfig().addFolderNode(
				parentFolder.getNode(),
				name,
				"user",
				"true",
				"virtual",
				"false",
				"false",
				"true",
				new Integer(-1));
	
		FolderItem item = folderXmlConfig.getFolderItem(adapterNode);
	
		VirtualFolder newFolder = new VirtualFolder(adapterNode, item);
		Search search = new Search(adapterNode, newFolder);
		// newFolder.addTreeNodeListener( this );
		parentFolder.add(newFolder);
	
		return newFolder;
	}
	*/

	/*
	public FolderTreeNode addImapFolder(
		FolderTreeNode parentFolder,
		String name,
		ImapItem imapItem,
		FolderTreeNode rootFolder,
		int uid) {
		AdapterNode adapterNode =
			MailConfig.getFolderConfig().addFolderNode(
				parentFolder.getNode(),
				name,
				"user",
				"true",
				"imap",
				"true",
				"true",
				"true",new Integer(-1)
				);
	
		FolderItem item = folderXmlConfig.getFolderItem(adapterNode);
	
		IMAPFolder newFolder =
			new IMAPFolder(
				adapterNode,
				item,
				imapItem,
				(IMAPRootFolder) rootFolder);
		//newFolder.addTreeNodeListener( this );
		parentFolder.add(newFolder);
	
		return newFolder;
	}
	*/
	public FolderTreeNode addImapRootFolder(
		String name,
		ImapItem imapItem,
		int uid) {
		FolderTreeNode parentFolder = (FolderTreeNode) getRoot();
		AdapterNode adapterNode =
			MailConfig.getFolderConfig().addFolderNode(
				parentFolder.getNode(),
				name,
				"system",
				"false",
				"imaproot",
				"true",
				"false",
				"false",
				new Integer(uid));

		FolderItem item = folderXmlConfig.getFolderItem(adapterNode);
		if (item.getName() == null)
			item.setName(name);

		IMAPRootFolder newFolder =
			new IMAPRootFolder(adapterNode, item, imapItem, uid);
		//newFolder.addTreeNodeListener( this );
		parentFolder.add(newFolder);

		return newFolder;
	}

	public FolderTreeNode add(
		AdapterNode childNode,
		FolderTreeNode parentFolder) {

		FolderItem item = MailConfig.getFolderConfig().getFolderItem(childNode);
		if (item == null)
			return null;

		// i18n stuff

		String name = null;

		int uid = item.getUid();

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
				name = item.getName();

			item.setName(name);

		} catch (MissingResourceException ex) {
			name = item.getName();
		}

		// now instanciate the folder classes

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

		return null;
	}

	public FolderTreeNode getFolder(int uid) {
		FolderTreeNode root = (FolderTreeNode) getRoot();

		for (Enumeration e = root.breadthFirstEnumeration();
			e.hasMoreElements();
			) {
			FolderTreeNode node = (FolderTreeNode) e.nextElement();

			int id = node.getUid();

			if (uid == id) {

				return node;
			}

		}
		return null;

	}

	public FolderTreeNode getTrashFolder() {
		return getFolder(105);
	}

	public FolderTreeNode getImapFolder(int accountUid) {

		FolderTreeNode root = (FolderTreeNode) getRoot();

		for (Enumeration e = root.breadthFirstEnumeration();
			e.hasMoreElements();
			) {
			FolderTreeNode node = (FolderTreeNode) e.nextElement();

			if (node instanceof Folder) {
				Folder folder = (Folder) node;

				FolderItem item = folder.getFolderItem();
				if (item == null)
					continue;

				if (item.getType().equals("imaproot")) {
					int account = item.getAccountUid();

					if (account == accountUid) {
						int uid = item.getUid();

						return getFolder(uid);
					}

				}
			}

		}
		return null;

	}

	public FolderTreeNode getFolder(TreeNodeList list) {

		FolderTreeNode folder = null;

		FolderTreeNode parentFolder = (FolderTreeNode) getRoot();

		if (list == null)
			return parentFolder;

		if (list.count() == 0) {
			System.out.println("list count == null ");

			return parentFolder;
		}

		FolderTreeNode child = parentFolder;
		for (int i = 0; i < list.count(); i++) {
			String str = list.get(i);
			System.out.println("str: " + str);
			child = findFolder(child, str);
		}

		return child;

	}

	public FolderTreeNode findFolder(FolderTreeNode parentFolder, String str) {

		int count = parentFolder.getChildCount();
		FolderTreeNode child;
		FolderTreeNode folder;

		for (Enumeration e = parentFolder.children(); e.hasMoreElements();) {
			child = (FolderTreeNode) e.nextElement();

			if (child.getName().equals(str))
				return child;
			/*
			if (child instanceof Folder) {
				Folder f = (Folder) child;
				//System.out.println( "child: "+child.getName());
				if (f.getName().equals(str))
					return child;
			}
			*/
		}

		return null;
	}

}
