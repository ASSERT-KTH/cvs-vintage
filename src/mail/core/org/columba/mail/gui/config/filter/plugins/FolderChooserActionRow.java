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
package org.columba.mail.gui.config.filter.plugins;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import org.columba.mail.filter.FilterAction;
import org.columba.mail.folder.Folder;
import org.columba.mail.gui.config.filter.ActionList;
import org.columba.mail.gui.tree.util.SelectFolderDialog;
import org.columba.mail.gui.tree.util.TreeNodeList;
import org.columba.core.main.MainInterface;

public class FolderChooserActionRow extends DefaultActionRow implements ActionListener {

	private JButton treePathButton;

	public FolderChooserActionRow(ActionList list, FilterAction action) {
		super(list, action);

	}

	public void updateComponents(boolean b) {
		super.updateComponents(b);

		if (b) {
			int uid = filterAction.getUid();
			Folder folder = (Folder) MainInterface.treeModel.getFolder(uid);
			String treePath = folder.getTreePath();

			treePathButton.setText(treePath);
		} else {
			String treePath = treePathButton.getText();
			TreeNodeList list = new TreeNodeList(treePath);
			Folder folder = (Folder) MainInterface.treeModel.getFolder(list);
			int uid = folder.getUid();
			filterAction.setUid(uid);
		}

	}

	public void initComponents() {
		super.initComponents();

		treePathButton = new JButton();
		treePathButton.addActionListener(this);
		treePathButton.setActionCommand("TREEPATH");
		
		addComponent(treePathButton);

	}

	public void actionPerformed(ActionEvent e) {
		String action = e.getActionCommand();

		if (action.equals("TREEPATH")) {
			SelectFolderDialog dialog =
				MainInterface
					.treeModel
					.getSelectFolderDialog();
			

			if (dialog.success()) {
				Folder folder = dialog.getSelectedFolder();

				String treePath = folder.getTreePath();

				treePathButton.setText(treePath);
			}

		} 
	}

}
