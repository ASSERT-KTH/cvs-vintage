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
package org.columba.mail.gui.action;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import org.columba.core.action.AbstractColumbaAction;
import org.columba.core.gui.frame.FrameMediator;
import org.columba.core.gui.util.ImageLoader;
import org.columba.mail.smtp.action.SendAllMessagesAction;
import org.columba.mail.util.MailResourceLoader;

public class ReceiveSendAction extends AbstractColumbaAction {
	public ReceiveSendAction(FrameMediator controller) {
		super(controller, MailResourceLoader.getString("menu", "mainframe",
				"menu_file_receivesend"));

		// tooltip text
		putValue(SHORT_DESCRIPTION, MailResourceLoader.getString("menu",
				"mainframe", "menu_file_receivesend_tooltip").replaceAll("&",
				""));

		// toolbar text
		putValue(TOOLBAR_NAME, MailResourceLoader.getString("menu",
				"mainframe", "menu_file_receivesend_toolbar"));

		// small icon for menu
		putValue(SMALL_ICON, ImageLoader.getSmallImageIcon("send-receive.png"));

		// large icon for toolbar
		putValue(LARGE_ICON, ImageLoader.getImageIcon("send-24-receive.png"));

		// shortcut key
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt) {
		// check for new messages
		new ReceiveMessagesAction(getFrameMediator()).actionPerformed(null);

		// send all unsent messages found in Outbox
		new SendAllMessagesAction(getFrameMediator()).actionPerformed(null);
	}
}