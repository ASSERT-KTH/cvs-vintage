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

import javax.swing.AbstractAction;

import org.columba.core.command.CommandProcessor;
import org.columba.core.resourceloader.IconKeys;
import org.columba.core.resourceloader.ImageLoader;
import org.columba.mail.command.IMailFolderCommandReference;
import org.columba.mail.command.MailFolderCommandReference;
import org.columba.mail.gui.message.IMessageController;
import org.columba.mail.gui.message.command.SaveAttachmentAsCommand;
import org.columba.mail.gui.message.viewer.AttachmentsViewer;
import org.columba.mail.util.MailResourceLoader;

/**
 * Save attachment to file.
 * 
 * @author frdietz
 */
public class SaveAsAttachmentAction extends AbstractAction {

	private Integer[] address;

	private AttachmentsViewer attachmentViewer;

	private IMessageController mediator;

	public SaveAsAttachmentAction(IMessageController mediator, Integer[] address) {
		super(MailResourceLoader.getString("menu", "mainframe",
				"attachmentsaveas"));
		this.mediator = mediator;
		this.address = address;

		// tooltip text
		putValue(SHORT_DESCRIPTION, MailResourceLoader.getString("menu",
				"mainframe", "attachmentsaveas_tooltip").replaceAll("&", ""));

		// icons
		putValue(SMALL_ICON, ImageLoader
				.getSmallIcon(IconKeys.DOCUMENT_SAVE_AS));
		// putValue(LARGE_ICON, ImageLoader.getIcon(IconKeys.DOCUMENT_SAVE_AS));

	}

	public SaveAsAttachmentAction(IMessageController mediator,
			AttachmentsViewer attachmentViewer) {
		super(MailResourceLoader.getString("menu", "mainframe",
				"attachmentsaveas"));
		this.mediator = mediator;
		this.attachmentViewer = attachmentViewer;

		// tooltip text
		putValue(SHORT_DESCRIPTION, MailResourceLoader.getString("menu",
				"mainframe", "attachmentsaveas_tooltip").replaceAll("&", ""));

		// icons
		putValue(SMALL_ICON, ImageLoader
				.getSmallIcon(IconKeys.DOCUMENT_SAVE_AS));
		// putValue(LARGE_ICON, ImageLoader.getIcon(IconKeys.DOCUMENT_SAVE_AS));

	}

	/** {@inheritDoc} */
	public void actionPerformed(ActionEvent evt) {
		IMailFolderCommandReference ref = mediator.getSelectedReference();

		if (attachmentViewer != null)
			address = attachmentViewer.getSelected();

		CommandProcessor.getInstance().addOp(
				new SaveAttachmentAsCommand(new MailFolderCommandReference(ref
						.getSourceFolder(), ref.getUids(), address)));

	}

}