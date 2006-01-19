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
import org.columba.core.desktop.ColumbaDesktop;
import org.columba.core.gui.action.AbstractColumbaAction;
import org.columba.core.resourceloader.ImageLoader;
import org.columba.mail.command.IMailFolderCommandReference;
import org.columba.mail.command.MailFolderCommandReference;
import org.columba.mail.gui.frame.MessageViewOwner;
import org.columba.mail.gui.message.MessageController;
import org.columba.mail.gui.message.command.OpenAttachmentCommand;
import org.columba.mail.gui.message.viewer.AttachmentsViewer;
import org.columba.mail.util.MailResourceLoader;

/**
 * Open Attachment action.
 * 
 * @author fdietz
 */
public class OpenAttachmentAction extends AbstractColumbaAction {

	private Integer[] address;

	private AttachmentsViewer attachmentViewer;
	
	public OpenAttachmentAction(IFrameMediator frameMediator, Integer[] address) {
		super(frameMediator, MailResourceLoader.getString("menu", "mainframe",
				"attachmentopen"));

		this.address = address;
		
		// tooltip text
		putValue(SHORT_DESCRIPTION, MailResourceLoader.getString("menu",
				"mainframe", "attachmentopen_tooltip").replaceAll("&", ""));

		// icons
		putValue(SMALL_ICON, ImageLoader.getSmallImageIcon("folder-open.png"));
		putValue(LARGE_ICON, ImageLoader.getSmallImageIcon("folder-open.png"));

		setEnabled(ColumbaDesktop.getInstance().supportsOpen());
		
	}
	
	public OpenAttachmentAction(IFrameMediator frameMediator, AttachmentsViewer attachmentViewer) {
		super(frameMediator, MailResourceLoader.getString("menu", "mainframe",
				"attachmentopen"));

		this.attachmentViewer = attachmentViewer;
		
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
		IMailFolderCommandReference ref = ((MessageController) ((MessageViewOwner) frameMediator)
				.getMessageController()).getReference();

		if ( attachmentViewer != null ) {
			address = attachmentViewer.getSelected();
		}
		
		CommandProcessor.getInstance().addOp(
				new OpenAttachmentCommand(new MailFolderCommandReference(ref
						.getSourceFolder(), ref.getUids(), address)));
	}

}