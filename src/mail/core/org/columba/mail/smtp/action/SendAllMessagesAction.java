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
package org.columba.mail.smtp.action;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import org.columba.core.action.AbstractColumbaAction;
import org.columba.core.gui.frame.FrameMediator;
import org.columba.core.gui.util.ImageLoader;
import org.columba.core.main.ConnectionStateImpl;
import org.columba.core.main.MainInterface;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.folder.outbox.OutboxFolder;
import org.columba.mail.main.MailInterface;
import org.columba.mail.smtp.command.SendAllMessagesCommand;
import org.columba.mail.util.MailResourceLoader;

/**
 * @author fdietz
 * 
 * This action is responsible for starting the command which does the actual
 * work. It is visually represented with a menuentry and a toolbar.
 *  
 */
public class SendAllMessagesAction extends AbstractColumbaAction {
	/**
	 * @param controller
	 */
	public SendAllMessagesAction(FrameMediator controller) {
		super(controller, MailResourceLoader.getString("menu", "mainframe",
				"menu_file_sendunsentmessages"));

		// tooltip text
		putValue(SHORT_DESCRIPTION, MailResourceLoader.getString("menu",
				"mainframe", "menu_file_sendunsentmessages_tooltip")
				.replaceAll("&", ""));

		// icon
		putValue(LARGE_ICON, ImageLoader.getImageIcon("send-24.png"));

		// shortcut key
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0));
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt) {
		setEnabled(false);
		// check if we are online
		if (ConnectionStateImpl.getInstance().isOnline() == false) {
			// offline -> go online
			ConnectionStateImpl.getInstance().setOnline(true);
		}

		// get outbox folder
		OutboxFolder folder = (OutboxFolder) MailInterface.treeModel
				.getFolder(103);

		// create referenc
		FolderCommandReference r = new FolderCommandReference(folder);

		// start command
		SendAllMessagesCommand c = new SendAllMessagesCommand(this,
				frameMediator, r);

		MainInterface.processor.addOp(c);
	}
}