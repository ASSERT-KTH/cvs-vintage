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
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.config.FolderItem;
import org.columba.mail.folder.Folder;
import org.columba.mail.folder.FolderTreeNode;
import org.columba.mail.gui.config.folder.FolderOptionsDialog;
import org.columba.mail.gui.frame.AbstractMailFrameController;
import org.columba.mail.gui.frame.MailFrameMediator;
import org.columba.mail.gui.tree.selection.TreeSelectionChangedEvent;
import org.columba.mail.util.MailResourceLoader;

/**
 * Action used to popup a folder renaming dialog to the user.
 *  
 * @author Frederik
 */
public class RenameFolderAction
	extends FrameAction
	implements SelectionListener {

	public RenameFolderAction(FrameMediator frameMediator) {
		super(
			frameMediator,
			MailResourceLoader.getString(
				"menu",
				"mainframe",
				"menu_folder_renamefolder"));

		// tooltip text
		putValue(
			SHORT_DESCRIPTION,
			MailResourceLoader
				.getString("menu", "mainframe", "menu_folder_renamefolder")
				.replaceAll("&", ""));

		// shortcut key
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));

		setEnabled(false);

		((MailFrameMediator) frameMediator).registerTreeSelectionListener(this);
	}

	public void actionPerformed(ActionEvent evt) {
		FolderCommandReference[] r =
			(FolderCommandReference[])
				((AbstractMailFrameController) frameMediator)
				.getTreeSelection();

		new FolderOptionsDialog((Folder) r[0].getFolder(), true);
	}

	public void selectionChanged(SelectionChangedEvent evt) {
		if (((TreeSelectionChangedEvent) evt).getSelected().length > 0) {
			FolderTreeNode folder =
				((TreeSelectionChangedEvent) evt).getSelected()[0];

			if (folder != null && folder instanceof Folder) {
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
