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

package org.columba.mail.gui.tree.action;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import org.columba.core.action.FrameAction;
import org.columba.core.gui.frame.FrameMediator;
import org.columba.core.gui.selection.SelectionChangedEvent;
import org.columba.core.gui.selection.SelectionListener;
import org.columba.core.gui.util.ImageLoader;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.folder.FolderFactory;
import org.columba.mail.gui.frame.AbstractMailFrameController;
import org.columba.mail.gui.tree.selection.TreeSelectionChangedEvent;
import org.columba.mail.gui.tree.util.CreateFolderDialog;
import org.columba.mail.main.MailInterface;
import org.columba.mail.util.MailResourceLoader;

/**
 * @author frd
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class CreateVirtualFolderAction
	extends FrameAction
	implements SelectionListener {

	public CreateVirtualFolderAction(FrameMediator frameController) {
		super(frameController, MailResourceLoader.getString(
			"menu", "mainframe", "menu_folder_newvirtualfolder"));
		
		// tooltip text
		putValue(SHORT_DESCRIPTION, MailResourceLoader.getString(
			"menu",
                        "mainframe",
                        "menu_folder_newvirtualfolder").replaceAll("&", ""));

		// icons
		putValue(SMALL_ICON, ImageLoader.getSmallImageIcon("virtualfolder.png"));
		putValue(LARGE_ICON, ImageLoader.getImageIcon("virtualfolder.png"));
		
		// shortcut key
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(
					KeyEvent.VK_V, ActionEvent.ALT_MASK));

		setEnabled(false);
		(
			(
				AbstractMailFrameController) frameController)
					.registerTreeSelectionListener(
			this);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt) {
		CreateFolderDialog dialog = new CreateFolderDialog(null);
		dialog.showDialog();

		String name;

		if (dialog.success()) {
			// ok pressed
			name = dialog.getName();

			try {
				FolderCommandReference[] r =
					(FolderCommandReference[]) frameMediator
						.getSelectionManager()
						.getSelection(
						"mail.tree");
				FolderFactory.getInstance().createChild( r[0].getFolder(), name, "VirtualFolder");

				FolderCommandReference[] reference =
					(FolderCommandReference[])
						((AbstractMailFrameController) getFrameMediator())
						.getTreeSelection();
				MailInterface.treeModel.nodeStructureChanged(
					reference[0].getFolder());

			} catch (Exception ex) {
				ex.printStackTrace();
			}

		} else {
			// cancel pressed
			return;
		}
	}

	/* (non-Javadoc)
         * @see org.columba.core.gui.util.SelectionListener#selectionChanged(org.columba.core.gui.util.SelectionChangedEvent)
         */
	public void selectionChanged(SelectionChangedEvent e) {
		if (((TreeSelectionChangedEvent) e).getSelected().length > 0)
			setEnabled(true);
		else
			setEnabled(false);
	}
}
