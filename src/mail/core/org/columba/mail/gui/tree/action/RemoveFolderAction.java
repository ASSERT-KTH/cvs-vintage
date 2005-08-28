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

import org.columba.api.gui.frame.IFrameMediator;
import org.columba.core.command.CommandProcessor;
import org.columba.core.gui.action.AbstractColumbaAction;
import org.columba.core.gui.selection.ISelectionListener;
import org.columba.core.gui.selection.SelectionChangedEvent;
import org.columba.core.resourceloader.ImageLoader;
import org.columba.mail.command.IMailFolderCommandReference;
import org.columba.mail.config.IFolderItem;
import org.columba.mail.folder.IMailFolder;
import org.columba.mail.folder.IMailbox;
import org.columba.mail.folder.command.RemoveFolderCommand;
import org.columba.mail.gui.frame.MailFrameMediator;
import org.columba.mail.gui.tree.selection.TreeSelectionChangedEvent;
import org.columba.mail.util.MailResourceLoader;

/**
 * @author frd
 * 
 * To change this generated comment go to Window>Preferences>Java>Code
 * Generation>Code and Comments
 */
public class RemoveFolderAction extends AbstractColumbaAction implements
		ISelectionListener {
	public RemoveFolderAction(IFrameMediator frameMediator) {
		super(frameMediator, MailResourceLoader.getString("menu", "mainframe",
				"menu_folder_removefolder"));

		// tooltip text
		putValue(SHORT_DESCRIPTION, MailResourceLoader.getString("menu",
				"mainframe", "menu_folder_removefolder").replaceAll("&", ""));

		// icons
		putValue(SMALL_ICON, ImageLoader
				.getSmallImageIcon("stock_delete-16.png"));
		putValue(LARGE_ICON, ImageLoader.getImageIcon("stock_delete.png"));

		setEnabled(false);

		((MailFrameMediator) frameMediator).registerTreeSelectionListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt) {
		IMailFolderCommandReference r = ((MailFrameMediator) getFrameMediator())
				.getTreeSelection();
		IMailFolder folder = (IMailFolder) r.getSourceFolder();

		if (!folder.isLeaf()) {
			// warn user
			JOptionPane.showMessageDialog(null,
					"Your can only remove leaf folders!");

			return;
		} else {
			// warn user in any other cases
			int n = JOptionPane.showConfirmDialog(null, MailResourceLoader
					.getString("tree", "tree", "folder_warning"),
					MailResourceLoader.getString("tree", "tree",
							"folder_warning_title"), JOptionPane.YES_NO_OPTION);

			if (n == JOptionPane.NO_OPTION) {
				return;
			}
		}

		CommandProcessor.getInstance().addOp(new RemoveFolderCommand(r));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.columba.core.gui.util.ISelectionListener#selectionChanged(org.columba.core.gui.util.SelectionChangedEvent)
	 */
	public void selectionChanged(SelectionChangedEvent e) {
		if (((TreeSelectionChangedEvent) e).getSelected().length > 0) {
			IMailFolder folder = ((TreeSelectionChangedEvent) e).getSelected()[0];

			if ((folder != null) && folder instanceof IMailbox) {
				IFolderItem item = folder.getConfiguration();

				if (item.getString("property", "accessrights").equals("user")) {
					setEnabled(true);
				} else {
					setEnabled(false);
				}
			}
		} else {
			setEnabled(false);
		}
	}
}