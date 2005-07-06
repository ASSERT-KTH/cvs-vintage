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
package org.columba.mail.gui.message.action;

import java.awt.event.ActionEvent;

import org.columba.core.action.AbstractColumbaAction;
import org.columba.core.command.CommandProcessor;
import org.columba.core.gui.frame.FrameMediator;
import org.columba.core.gui.util.ImageLoader;
import org.columba.core.io.ColumbaDesktop;
import org.columba.mail.command.MailFolderCommandReference;
import org.columba.mail.gui.frame.MessageViewOwner;
import org.columba.mail.gui.message.MessageController;
import org.columba.mail.gui.message.command.OpenAttachmentCommand;
import org.columba.mail.util.MailResourceLoader;

/**
 * @author frd
 * 
 * To change this generated comment go to Window>Preferences>Java>Code
 * Generation>Code and Comments
 */
public class OpenAttachmentAction extends AbstractColumbaAction {

	public OpenAttachmentAction(FrameMediator frameMediator) {
		super(frameMediator, MailResourceLoader.getString("menu", "mainframe",
				"attachmentopen"));

		// tooltip text
		putValue(SHORT_DESCRIPTION, MailResourceLoader.getString("menu",
				"mainframe", "attachmentopen_tooltip").replaceAll("&", ""));

		// icons
		putValue(SMALL_ICON, ImageLoader.getSmallImageIcon("folder-open.png"));
		putValue(LARGE_ICON, ImageLoader.getSmallImageIcon("folder-open.png"));
		
		setEnabled(ColumbaDesktop.getInstance().supportsOpen());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt) {
		// bug #999990 (fdietz) check if attachment is really selected
		MailFolderCommandReference ref = ((MessageController) ((MessageViewOwner) frameMediator)
				.getMessageController()).getAttachmentSelectionReference();

		if (ref.getAddress() != null)
			CommandProcessor.getInstance()
					.addOp(new OpenAttachmentCommand(ref));
	}

}