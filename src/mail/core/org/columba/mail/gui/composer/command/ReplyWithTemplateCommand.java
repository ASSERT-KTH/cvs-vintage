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
package org.columba.mail.gui.composer.command;

import org.columba.core.command.DefaultCommandReference;
import org.columba.core.command.Worker;
import org.columba.core.gui.frame.AbstractFrameController;
import org.columba.core.main.MainInterface;
import org.columba.core.xml.XmlElement;
import org.columba.mail.command.FolderCommand;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.composer.MessageBuilder;
import org.columba.mail.config.MailConfig;
import org.columba.mail.folder.Folder;
import org.columba.mail.gui.composer.ComposerController;
import org.columba.mail.gui.composer.ComposerModel;
import org.columba.mail.gui.config.template.ChooseTemplateDialog;
import org.columba.mail.message.ColumbaHeader;
import org.columba.mail.message.ColumbaMessage;
import org.columba.mail.message.HeaderList;
import org.columba.ristretto.message.LocalMimePart;
import org.columba.ristretto.message.MimeHeader;
import org.columba.ristretto.message.MimePart;
import org.columba.ristretto.message.MimeTree;
import org.columba.ristretto.message.io.CharSequenceSource;

/**
 * Opens a dialog to ask the user which template to use
 *
 * @author fdietz
 */
public class ReplyWithTemplateCommand extends FolderCommand {

	/**
	 * @param references
	 */
	public ReplyWithTemplateCommand(DefaultCommandReference[] references) {
		super(references);
	}

	/**
	 * @param frame
	 * @param references
	 */
	public ReplyWithTemplateCommand(
		AbstractFrameController frame,
		DefaultCommandReference[] references) {
		super(frame, references);

	}

	/* (non-Javadoc)
	 * @see org.columba.core.command.Command#execute(org.columba.core.command.Worker)
	 */
	public void execute(Worker worker) throws Exception {
		Folder folder =
			(Folder) ((FolderCommandReference) getReferences()[0]).getFolder();
		Object[] uids = ((FolderCommandReference) getReferences()[0]).getUids();

		// template folder has uid=107
		Folder templateFolder = (Folder) MainInterface.treeModel.getFolder(107);

		// retrieve headerlist of tempate folder
		HeaderList list = templateFolder.getHeaderList();

		// choose template
		ChooseTemplateDialog d = new ChooseTemplateDialog(list);

		Object uid = null;
		if (d.isResult()) {
			// user pressed OK
			uid = d.getUid();
		} else {
			// user cancelled
			return;
		}

		ColumbaMessage message = new ColumbaMessage();

		ColumbaHeader header = (ColumbaHeader) folder.getMessageHeader(uids[0]);
		message.setHeader(header);
		MimeTree mimePartTree = folder.getMimePartTree(uids[0]);
		message.setMimePartTree(mimePartTree);

		XmlElement html =
			MailConfig.getMainFrameOptionsConfig().getRoot().getElement(
				"/options/html");
		boolean viewhtml =
			new Boolean(html.getAttribute("prefer")).booleanValue();

		// Which Bodypart shall be shown? (html/plain)
		MimePart bodyPart = null;

		if (viewhtml)
			bodyPart = mimePartTree.getFirstTextPart("html");
		else
			bodyPart = mimePartTree.getFirstTextPart("plain");

		if (bodyPart == null) {
			bodyPart = new LocalMimePart(new MimeHeader("text", "plain"));
			((LocalMimePart) bodyPart).setBody(
				new CharSequenceSource("<No Message-Text>"));
		} else
			bodyPart = folder.getMimePart(uids[0], bodyPart.getAddress());

		message.setBodyPart(bodyPart);

		// get bodytext of template message
		MimeTree tree = templateFolder.getMimePartTree(uid);
		MimePart mp = tree.getFirstTextPart("plain");
		LocalMimePart mimePart =
			(LocalMimePart) templateFolder.getMimePart(uid, mp.getAddress());

		String charset = bodyPart.getHeader().getContentParameter("charset");

		String templateBodytext = mimePart.getBody().toString();

		// open new message in composer
		ComposerModel model = new ComposerModel();
		ComposerController controller = new ComposerController();
		MessageBuilder.getInstance().createMessageFromTemplate(
			message,
			model,
			templateBodytext);
		controller.setComposerModel(model);

	}

}
