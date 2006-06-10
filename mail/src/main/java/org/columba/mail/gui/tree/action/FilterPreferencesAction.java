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
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.mail.gui.tree.action;

import java.awt.event.ActionEvent;

import org.columba.api.gui.frame.IFrameMediator;
import org.columba.api.selection.ISelectionListener;
import org.columba.api.selection.SelectionChangedEvent;
import org.columba.core.gui.action.AbstractColumbaAction;
import org.columba.mail.command.IMailFolderCommandReference;
import org.columba.mail.config.IFolderItem;
import org.columba.mail.folder.IMailbox;
import org.columba.mail.folder.virtual.VirtualFolder;
import org.columba.mail.gui.config.filter.ConfigFrame;
import org.columba.mail.gui.config.search.SearchFrame;
import org.columba.mail.gui.frame.AbstractMailFrameController;
import org.columba.mail.gui.frame.MailFrameMediator;
import org.columba.mail.gui.tree.selection.TreeSelectionChangedEvent;
import org.columba.mail.util.MailResourceLoader;

/**
 * @author frd
 * 
 * To change this generated comment go to Window>Preferences>Java>Code
 * Generation>Code and Comments
 */

public class FilterPreferencesAction extends AbstractColumbaAction implements
		ISelectionListener {

	public FilterPreferencesAction(IFrameMediator frameMediator) {
		super(frameMediator, MailResourceLoader.getString("menu", "mainframe",
				"menu_folder_filterconfig"));

		// tooltip text
		putValue(SHORT_DESCRIPTION, MailResourceLoader.getString("menu",
				"mainframe", "menu_folder_filterconfig").replaceAll("&", ""));

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
		IMailbox folder = (IMailbox) r.getSourceFolder();

		if (folder == null) {
			return;
		}

		IFolderItem item = folder.getConfiguration();

		if (item == null) {
			return;
		}

		if (folder instanceof VirtualFolder) {
			new SearchFrame((AbstractMailFrameController) frameMediator,
					(VirtualFolder) folder);
		} else {
			new ConfigFrame(frameMediator, folder);
		}
		// folder.showFilterDialog(((AbstractMailFrameController)
		// getFrameMediator()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.columba.core.gui.util.ISelectionListener#selectionChanged(org.columba.core.gui.util.SelectionChangedEvent)
	 */
	public void selectionChanged(SelectionChangedEvent e) {
		TreeSelectionChangedEvent treeEvent = (TreeSelectionChangedEvent) e;
		
		if (treeEvent.getSelected().length == 1 && treeEvent.getSelected()[0] instanceof IMailbox) {
			setEnabled(true);
		} else {
			setEnabled(false);
		}
	}
}
