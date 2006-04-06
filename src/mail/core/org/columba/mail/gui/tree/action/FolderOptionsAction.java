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

import org.columba.api.gui.frame.IFrameMediator;
import org.columba.api.selection.ISelectionListener;
import org.columba.api.selection.SelectionChangedEvent;
import org.columba.core.command.CommandProcessor;
import org.columba.core.gui.action.AbstractColumbaAction;
import org.columba.core.resourceloader.ImageLoader;
import org.columba.mail.command.MailFolderCommandReference;
import org.columba.mail.folder.IMailFolder;
import org.columba.mail.folder.IMailbox;
import org.columba.mail.gui.config.folder.FolderOptionsDialog;
import org.columba.mail.gui.frame.AbstractMailFrameController;
import org.columba.mail.gui.frame.MailFrameMediator;
import org.columba.mail.gui.tree.command.MailboxSizeCommand;
import org.columba.mail.gui.tree.selection.TreeSelectionChangedEvent;
import org.columba.mail.util.MailResourceLoader;

/**
 * Opens AbstractMessageFolder Options Dialog.
 * 
 * @author fdietz
 */
public class FolderOptionsAction extends AbstractColumbaAction implements
		ISelectionListener {
	/**
	 * @param frameMediator
	 * @param name
	 */
	public FolderOptionsAction(IFrameMediator frameMediator) {
		super(frameMediator, MailResourceLoader.getString("menu", "mainframe",
				"menu_folder_folderoptions"));

		// icon for menu
//		putValue(SMALL_ICON, ImageLoader
//				.getSmallImageIcon("16_configure_folder.png"));

		setEnabled(false);

		((MailFrameMediator) frameMediator).registerTreeSelectionListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt) {
		// it is safe here to cast to AbstractMailFrameControlller
		MailFolderCommandReference r = (MailFolderCommandReference) ((AbstractMailFrameController) frameMediator)
				.getTreeSelection();

		// only use the first selected folder
		IMailFolder folder = (IMailFolder) r.getSourceFolder();

		// cast to Folder
		FolderOptionsDialog dialog = new FolderOptionsDialog((IMailbox) folder,
				true, (AbstractMailFrameController) frameMediator);

		// calculate mailbox size in background worker
		CommandProcessor.getInstance().addOp(new MailboxSizeCommand(r, dialog));
	}

	public void selectionChanged(SelectionChangedEvent e) {
		IMailFolder[] r = ((TreeSelectionChangedEvent) e).getSelected();

		if ((r.length > 0) && r[0] instanceof IMailbox) {
			setEnabled(true);
		} else {
			setEnabled(false);
		}
	}
}