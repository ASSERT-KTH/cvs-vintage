/*
 * Created on 11.03.2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
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
package org.columba.mail.gui.table.action;

import java.awt.event.ActionEvent;

import org.columba.core.action.FrameAction;
import org.columba.core.gui.frame.AbstractFrameController;
import org.columba.core.gui.selection.SelectionChangedEvent;
import org.columba.core.gui.selection.SelectionListener;
import org.columba.core.main.MainInterface;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.folder.command.CreateVFolderOnMessageCommand;
import org.columba.mail.gui.frame.AbstractMailFrameController;
import org.columba.mail.gui.table.selection.TableSelectionChangedEvent;
import org.columba.mail.util.MailResourceLoader;

/**
 * @author frd
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class CreateVFolderOnToAction
	extends FrameAction
	implements SelectionListener {

	/*
	 * @param frameController
	 * @param name
	 * @param longDescription
	 * @param actionCommand
	 * @param small_icon
	 * @param big_icon
	 * @param mnemonic
	 * @param keyStroke
	 */
	public CreateVFolderOnToAction(AbstractFrameController frameController) {
		super(
			frameController,
			MailResourceLoader.getString(
				"menu",
				"mainframe",
				"menu_message_vfolderonto"),
			MailResourceLoader.getString(
				"menu",
				"mainframe",
				"menu_message_vfolderonto_tooltip"),
			"VFOLDER_ON_TO",
			null,
			null,
			'0',
			null);
		setEnabled(false);
		// *20030621, karlpeder*
		((AbstractMailFrameController) frameController)
				.registerTableSelectionListener(this);
	}

	/**
	 * Called for execution of this action
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt) {
		/*
		 * *20030621, karlpeder* Action has been implemented
		 */

		// get selected stuff
		FolderCommandReference[] r =
				((AbstractMailFrameController) getFrameController()).
					getTableSelection();

		// add command for execution
		CreateVFolderOnMessageCommand c =
				new CreateVFolderOnMessageCommand(
						getFrameController(),
						r,
						CreateVFolderOnMessageCommand.VFOLDER_ON_TO);
						
		MainInterface.processor.addOp(c);

	}

	/**
	 * Called when selection changes in message table
	 * @see org.columba.core.gui.util.SelectionListener#selectionChanged(org.columba.core.gui.util.SelectionChangedEvent)
	 */
	public void selectionChanged(SelectionChangedEvent e) {

		if (((TableSelectionChangedEvent) e).getUids().length > 0)
			setEnabled(true);
		else
			setEnabled(false);

	}
}
