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

import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import org.columba.addressbook.folder.AddressbookFolder;
import org.columba.addressbook.folder.AddressbookTreeNode;
import org.columba.addressbook.folder.IContactFolder;
import org.columba.addressbook.gui.dialog.contact.ContactEditorDialog;
import org.columba.addressbook.gui.frame.AddressbookFrameMediator;
import org.columba.addressbook.resourceloader.ContactImageLoader;
import org.columba.addressbook.resourceloader.IconKeys;
import org.columba.addressbook.util.AddressbookResourceLoader;
import org.columba.api.gui.frame.IFrameMediator;
import org.columba.core.gui.action.AbstractColumbaAction;
import org.columba.core.gui.dialog.ErrorDialog;
import org.columba.core.logging.Logging;

/**
 * Add new contact card to selected addressbook.
 * 
 * @author fdietz
 */
public class AddContactCardAction extends AbstractColumbaAction implements
		TreeSelectionListener {

	private AddressbookTreeNode treeNode;

	public AddContactCardAction(IFrameMediator frameController) {
		super(frameController, AddressbookResourceLoader.getString("menu",
				"mainframe", "menu_file_addcontact"));

		// tooltip text
		putValue(SHORT_DESCRIPTION, AddressbookResourceLoader.getString("menu",
				"mainframe", "menu_file_addcontact_tooltip")
				.replaceAll("&", ""));

		putValue(TOOLBAR_NAME, AddressbookResourceLoader.getString("menu",
				"mainframe", "menu_file_addcontact_toolbar"));

		// icons
		putValue(SMALL_ICON, ContactImageLoader
				.getSmallIcon(IconKeys.NEW_CONTACT));
		putValue(LARGE_ICON, ContactImageLoader.getIcon(IconKeys.NEW_CONTACT));

		if (frameController instanceof AddressbookFrameMediator) {
			// register interest on tree selection changes
			((AddressbookFrameMediator) frameMediator)
					.addTreeSelectionListener(this);

			setEnabled(false);
		} else
			setEnabled(false);
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt) {
		if (frameMediator instanceof AddressbookFrameMediator) {

			AddressbookFrameMediator mediator = (AddressbookFrameMediator) frameMediator;

			// get selected folder
			AddressbookFolder folder = (AddressbookFolder) mediator.getTree()
					.getSelectedFolder();

			ContactEditorDialog dialog = new ContactEditorDialog(mediator
					.getView().getFrame());

			if (dialog.getResult()) {
				try {
					// add contact to folder
					folder.add(dialog.getDestModel());
				} catch (Exception e) {
					if (Logging.DEBUG)
						e.printStackTrace();

					ErrorDialog.createDialog(e.getMessage(), e);
				}

			}
		} else {
			ContactEditorDialog dialog = new ContactEditorDialog(frameMediator
					.getView().getFrame());

//			if (dialog.getResult()) {
//				try {
//					IContactFolder folder = dialog.getFolder();
//					
//					// add contact to folder
//					folder.add(dialog.getDestModel());
//				} catch (Exception e) {
//					if (Logging.DEBUG)
//						e.printStackTrace();
//
//					ErrorDialog.createDialog(e.getMessage(), e);
//				}
//
//			}
		}
	}

	/**
	 * Enable or disable action on tree selection changes.
	 * <p>
	 * Actions should overwrite this method if they need more fine-grained
	 * control.
	 * 
	 */
	public void valueChanged(TreeSelectionEvent e) {
		TreePath path = e.getNewLeadSelectionPath();

		// remember last selected folder treenode
		if (path != null) {
			treeNode = (AddressbookTreeNode) path.getLastPathComponent();
		}

		// enable, if more than zero treenodes selected
		if ((path != null) && (treeNode instanceof AddressbookFolder)) {
			setEnabled(true);
		} else {
			setEnabled(false);
		}
	}
}