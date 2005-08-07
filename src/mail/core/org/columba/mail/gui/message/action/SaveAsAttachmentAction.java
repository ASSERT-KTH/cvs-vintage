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

import org.columba.api.gui.frame.IFrameMediator;
import org.columba.core.command.CommandProcessor;
import org.columba.core.gui.action.AbstractColumbaAction;
import org.columba.core.resourceloader.ImageLoader;
import org.columba.mail.gui.frame.MessageViewOwner;
import org.columba.mail.gui.message.MessageController;
import org.columba.mail.gui.message.command.SaveAttachmentAsCommand;
import org.columba.mail.util.MailResourceLoader;

/**
 * @author frdietz
 */
public class SaveAsAttachmentAction extends AbstractColumbaAction {

	/**
	 * Creates a SaveAs Action.
	 * 
	 * @param frameMediator
	 *            frame mediator.
	 */
	public SaveAsAttachmentAction(IFrameMediator frameMediator) {
		super(frameMediator, MailResourceLoader.getString("menu", "mainframe",
				"attachmentsaveas"));

		// tooltip text
		putValue(SHORT_DESCRIPTION, MailResourceLoader.getString("menu",
				"mainframe", "attachmentsaveas_tooltip").replaceAll("&", ""));

		// icons
		putValue(SMALL_ICON, ImageLoader
				.getSmallImageIcon("stock_save_as-16.png"));
		putValue(LARGE_ICON, ImageLoader.getImageIcon("stock_save_as.png"));

	}

	/** {@inheritDoc} */
	public void actionPerformed(ActionEvent evt) {
		CommandProcessor.getInstance().addOp(
				new SaveAttachmentAsCommand(
						((MessageController) ((MessageViewOwner) frameMediator)
								.getMessageController())
								.getAttachmentSelectionReference()));
	}

}