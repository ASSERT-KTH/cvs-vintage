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

import javax.swing.JOptionPane;

import org.columba.core.action.FrameAction;
import org.columba.core.gui.frame.FrameMediator;
import org.columba.core.gui.selection.SelectionChangedEvent;
import org.columba.core.gui.selection.SelectionListener;
import org.columba.core.gui.util.ImageLoader;
import org.columba.core.main.MainInterface;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.config.FolderItem;
import org.columba.mail.folder.Folder;
import org.columba.mail.folder.FolderTreeNode;
import org.columba.mail.folder.command.RemoveFolderCommand;
import org.columba.mail.gui.frame.AbstractMailFrameController;
import org.columba.mail.gui.tree.selection.TreeSelectionChangedEvent;
import org.columba.mail.util.MailResourceLoader;

/**
 * @author frd
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class RemoveFolderAction
	extends FrameAction
	implements SelectionListener {

	public RemoveFolderAction(FrameMediator frameController) {
		super(
				frameController,
				MailResourceLoader.getString(
					"menu", "mainframe", "menu_folder_removefolder"));

		// tooltip text
		setTooltipText(
				MailResourceLoader.getString(
					"menu", "mainframe", "menu_folder_removefolder"));

		// icons
		setSmallIcon(ImageLoader.getSmallImageIcon("stock_delete-16.png"));
		setLargeIcon(ImageLoader.getImageIcon("stock_delete.png"));
					
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
		FolderCommandReference[] r =
			(FolderCommandReference[]) frameMediator
				.getSelectionManager()
				.getSelection(
				"mail.tree");
		FolderTreeNode folder = r[0].getFolder();

		if (!folder.isLeaf()) {

			// warn user
			JOptionPane.showMessageDialog(
				null,
				"Your can only remove leaf folders!");
			return;
		} else {
		    // warn user in any other cases
            int n = JOptionPane.showConfirmDialog(
                    null,
                    MailResourceLoader.getString("tree", "tree",  "folder_warning"),
                    MailResourceLoader.getString("tree", "tree",  "folder_warning_title"),
                    JOptionPane.YES_NO_OPTION);
            if (n == JOptionPane.NO_OPTION) {
                return;
            }
        }

		MainInterface.processor.addOp(new RemoveFolderCommand(r));
	}
	/* (non-Javadoc)
					 * @see org.columba.core.gui.util.SelectionListener#selectionChanged(org.columba.core.gui.util.SelectionChangedEvent)
					 */
	public void selectionChanged(SelectionChangedEvent e) {
		if (((TreeSelectionChangedEvent) e).getSelected().length > 0) {
			FolderTreeNode folder = ((TreeSelectionChangedEvent) e).getSelected()[0];

			if (folder != null && folder instanceof Folder ) {

				FolderItem item = folder.getFolderItem();
				if (item.get("property", "accessrights").equals("user"))
					setEnabled(true);
				else
					setEnabled(false);
			}
		} else
			setEnabled(false);

	}
}
