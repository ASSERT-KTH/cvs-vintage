// The contents of this file are subject to the Mozilla Public License Version
// 1.1
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
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.addressbook.gui.tree;

import java.util.Enumeration;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

import org.columba.addressbook.config.AddressbookConfig;
import org.columba.addressbook.config.FolderItem;
import org.columba.addressbook.folder.GroupFolder;
import org.columba.addressbook.folder.Root;
import org.columba.addressbook.gui.tree.util.SelectAddressbookFolderDialog;
import org.columba.addressbook.plugin.FolderPluginHandler;
import org.columba.core.config.DefaultXmlConfig;
import org.columba.core.gui.util.NotifyDialog;
import org.columba.core.plugin.PluginHandlerNotFoundException;
import org.columba.core.plugin.PluginManager;
import org.columba.core.xml.XmlElement;

public class AddressbookTreeModel extends DefaultTreeModel implements TreeModel {

	protected DefaultXmlConfig folderXmlConfig;

	private final Class[] FOLDER_ITEM_ARG = new Class[] { FolderItem.class };

	private static AddressbookTreeModel instance = new AddressbookTreeModel(
			AddressbookConfig.getInstance().get("tree").getElement("/tree"));

	public AddressbookTreeModel(XmlElement root) {
		super(new Root(root));

		createDirectories(((AddressbookTreeNode) getRoot()).getNode(),
				(AddressbookTreeNode) getRoot());
	}

	public static AddressbookTreeModel getInstance() {
		return instance;
	}

	public SelectAddressbookFolderDialog getSelectAddressbookFolderDialog() {
		SelectAddressbookFolderDialog dialog = new SelectAddressbookFolderDialog(
				this);

		return dialog;
	}

	public void createDirectories(XmlElement parentTreeNode,
			AddressbookTreeNode parentFolder) {
		int count = parentTreeNode.count();

		XmlElement child;

		for (int i = 0; i < count; i++) {
			child = parentTreeNode.getElement(i);

			String name = child.getName();

			if (name.equals("folder")) {
				AddressbookTreeNode folder = add(child, parentFolder);

				if (folder != null) {
					createDirectories(child, folder);
				}
			}
		}
	}

	public AddressbookTreeNode add(XmlElement childNode,
			AddressbookTreeNode parentFolder) {
		FolderItem item = new FolderItem(childNode);

		if (item == null) {
			return null;
		}

		// i18n stuff
		String name = item.get("property", "name");

		//XmlElement.printNode(item.getRoot(), "");
		int uid = item.getInteger("uid");

		if (AddressbookTreeNode.getNextFolderUid() <= uid)
			AddressbookTreeNode.setNextFolderUid(uid + 1);

		// now instanciate the folder classes
		String type = item.get("type");

		Object[] args = { item };

		FolderPluginHandler handler = null;

		try {
			handler = (FolderPluginHandler) PluginManager.getInstance()
					.getHandler("org.columba.addressbook.folder");
		} catch (PluginHandlerNotFoundException ex) {
			NotifyDialog d = new NotifyDialog();
			d.showDialog(ex);
		}

		AddressbookTreeNode folder = null;

		try {
			folder = (AddressbookTreeNode) handler.getPlugin(type, args);
			parentFolder.add(folder);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return folder;

	}

	public AddressbookTreeNode getFolder(int uid) {
		AddressbookTreeNode root = (AddressbookTreeNode) getRoot();

		for (Enumeration e = root.breadthFirstEnumeration(); e
				.hasMoreElements();) {
			AddressbookTreeNode node = (AddressbookTreeNode) e.nextElement();

			if (node instanceof Root) {
				continue;
			}

			int id = node.getUid();

			if (uid == id) {
				return node;
			}
		}

		return null;
	}

	public GroupFolder getGroupFolder(String name) {
		AddressbookTreeNode root = (AddressbookTreeNode) getRoot();

		for (Enumeration e = root.breadthFirstEnumeration(); e
				.hasMoreElements();) {
			AddressbookTreeNode node = (AddressbookTreeNode) e.nextElement();

			if (node instanceof GroupFolder) {
				GroupFolder groupFolder = (GroupFolder) node;
				if (groupFolder.getName().equals(name))
					return groupFolder;
			}
		}

		return null;
	}

}