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
package org.columba.addressbook.gui.action;

import java.awt.event.ActionEvent;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import org.columba.addressbook.folder.AbstractFolder;
import org.columba.addressbook.folder.GroupFolder;
import org.columba.addressbook.gui.frame.AddressbookFrameMediator;
import org.columba.addressbook.gui.table.TableController;
import org.columba.addressbook.gui.tree.AddressbookTreeNode;
import org.columba.addressbook.gui.tree.TreeController;
import org.columba.addressbook.main.AddressbookInterface;
import org.columba.addressbook.util.AddressbookResourceLoader;
import org.columba.core.gui.focus.FocusOwner;
import org.columba.core.gui.frame.FrameMediator;
import org.columba.core.gui.util.ImageLoader;
import org.columba.core.main.MainInterface;

/**
 * Delete selected contact or group item.
 * 
 * @author fdietz
 */
public class RemoveCardAction extends DefaultTableAction implements
		TreeSelectionListener {
	public RemoveCardAction(FrameMediator frameController) {
		super(frameController, AddressbookResourceLoader.getString("menu",
				"mainframe", "menu_file_remove"));

		// tooltip text
		putValue(SHORT_DESCRIPTION, AddressbookResourceLoader.getString("menu",
				"mainframe", "menu_file_remove_tooltip").replaceAll("&", ""));

		putValue(TOOLBAR_NAME, AddressbookResourceLoader.getString("menu",
				"mainframe", "menu_file_remove_toolbar"));

		// icons
		putValue(SMALL_ICON, ImageLoader
				.getSmallImageIcon("stock_delete-16.png"));
		putValue(LARGE_ICON, ImageLoader.getImageIcon("stock_delete.png"));

		setEnabled(false);

		//		 register interest on tree selection changes
		((AddressbookFrameMediator) frameMediator)
				.addTreeSelectionListener(this);
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt) {
		AddressbookFrameMediator mediator = (AddressbookFrameMediator) frameMediator;

		FocusOwner focusOwner = MainInterface.focusManager.getCurrentOwner();

		TableController table = ((AddressbookFrameMediator) frameMediator)
				.getTable();

		boolean tableHasFocus = false;
		if (table.equals(focusOwner))
			tableHasFocus = true;

		if (tableHasFocus) {

			// get selected contact/group card
			Object[] uids = mediator.getTable().getUids();

			// get selected folder
			AbstractFolder folder = (AbstractFolder) mediator.getTree()
					.getSelectedFolder();

			// remove contacts/group cards from folder
			for (int i = 0; i < uids.length; i++) {
				try {
					folder.remove(uids[i]);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			if (folder instanceof GroupFolder)
				//		 re-select folder
				mediator.getTree().setSelectedFolder(folder);
		} else {
			// tree has focus
			
			// get selected folder
			AbstractFolder folder = (AbstractFolder) mediator.getTree()
					.getSelectedFolder();
			
			// get parent
			AddressbookTreeNode parent = (AddressbookTreeNode) folder.getParent();
			
			// remove folder from parent
			folder.removeFromParent();
			
			// notify model
			AddressbookInterface.addressbookTreeModel.nodeStructureChanged(parent);

			mediator.getTree().setSelectedFolder((AbstractFolder) parent);
		}
	}

	/**
	 * Enable or disable action on selection change.
	 * 
	 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
	 */
	public void valueChanged(ListSelectionEvent event) {
		// return if selection change is in flux
		if (event.getValueIsAdjusting()) {
			return;
		}

		FocusOwner focusOwner = MainInterface.focusManager.getCurrentOwner();

		TableController table = ((AddressbookFrameMediator) frameMediator)
				.getTable();

		if (table.equals(focusOwner)) {

			// table has focus

			Object[] uids = ((AddressbookFrameMediator) frameMediator)
					.getTable().getUids();

			if (uids.length > 0) {
				setEnabled(true);
				return;
			}
		}

		setEnabled(false);

	}

	public void valueChanged(TreeSelectionEvent e) {
		TreePath path = e.getNewLeadSelectionPath();

		FocusOwner focusOwner = MainInterface.focusManager.getCurrentOwner();

		TreeController tree = ((AddressbookFrameMediator) frameMediator)
				.getTree();

		if (tree.equals(focusOwner)) {
			// tree has focus

			AddressbookTreeNode treeNode = null;
			// remember last selected folder treenode
			if (path != null) {
				treeNode = (AddressbookTreeNode) path.getLastPathComponent();
			}

			// enable, if more than zero treenodes selected
			if ((path != null) && (treeNode instanceof GroupFolder)) {
				setEnabled(true);
			} else
				setEnabled(false);

		} else

			setEnabled(false);
	}
}