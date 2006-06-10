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
package org.columba.addressbook.gui.base;

import java.awt.Component;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;

import org.columba.addressbook.folder.IContactFolder;
import org.columba.addressbook.folder.IFolder;
import org.columba.addressbook.gui.tree.AddressbookTreeModel;

public class FolderComboBox extends JComboBox {

	public FolderComboBox(boolean showRootFolders) {
		super();

		AddressbookTreeModel model = AddressbookTreeModel.getInstance();
		Vector<IFolder> v = new Vector<IFolder>();

		Object parent = model.getRoot();

		getChildren(model, parent, v);

		Iterator<IFolder> it = v.listIterator();

		while (it.hasNext()) {
			IFolder folder = it.next();
			if (!showRootFolders) {
				if (folder instanceof IContactFolder)
					addItem(folder);
			}
		}

		setRenderer(new MyListCellRenderer());
	}

	private void getChildren(AddressbookTreeModel model, Object parent,
			Vector<IFolder> v) {
		int childCount = model.getChildCount(parent);
		for (int i = 0; i < childCount; i++) {
			Object child = model.getChild(parent, i);
			v.add((IFolder) child);

			getChildren(model, child, v);
		}
	}

	class MyListCellRenderer extends DefaultListCellRenderer {

		MyListCellRenderer() {

		}

		/**
		 * @see javax.swing.DefaultListCellRenderer#getListCellRendererComponent(javax.swing.JList,
		 *      java.lang.Object, int, boolean, boolean)
		 */
		@Override
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {

			super.getListCellRendererComponent(list, value, index, isSelected,
					cellHasFocus);

			IFolder folder = (IFolder) value;

			setText(folder.getName());
			setIcon(folder.getIcon());

			return this;
		}

	}
}
