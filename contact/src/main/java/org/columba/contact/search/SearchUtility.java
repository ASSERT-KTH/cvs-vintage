package org.columba.contact.search;

import java.util.List;
import java.util.Vector;

import org.columba.addressbook.folder.AddressbookFolder;
import org.columba.addressbook.gui.tree.AddressbookTreeModel;

public class SearchUtility {

	public static List<AddressbookFolder> createContactFolderList() {
		List<AddressbookFolder> v = new Vector<AddressbookFolder>();
		AddressbookTreeModel treeModel = AddressbookTreeModel.getInstance();
		v.add((AddressbookFolder) treeModel.getFolder("101"));
		v.add((AddressbookFolder) treeModel.getFolder("102"));
		return v;
	}

}
