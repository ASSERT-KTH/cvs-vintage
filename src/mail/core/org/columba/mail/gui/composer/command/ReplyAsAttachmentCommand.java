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
package org.columba.mail.gui.composer.command;

import org.columba.core.command.DefaultCommandReference;
import org.columba.core.command.Worker;
import org.columba.mail.command.FolderCommand;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.composer.MessageBuilderHelper;
import org.columba.mail.config.AccountItem;
import org.columba.mail.folder.Folder;
import org.columba.mail.gui.composer.ComposerController;
import org.columba.mail.gui.composer.ComposerModel;
import org.columba.mail.message.ColumbaMessage;
import org.columba.ristretto.coder.EncodedWord;
import org.columba.ristretto.message.Header;
import org.columba.ristretto.message.LocalMimePart;
import org.columba.ristretto.message.MimeHeader;
import org.columba.ristretto.message.MimePart;
import org.columba.ristretto.message.MimeTree;
import org.columba.ristretto.message.MimeType;

/**
 * Reply to message, while keeping the original message as attachment. In
 * comparison to quoting the bodytext inline.
 * 
 * @author fdietz
 */
public class ReplyAsAttachmentCommand extends FolderCommand {

	protected ComposerController controller;
	protected ComposerModel model;

	/**
	 * Constructor for ReplyCommand.
	 * 
	 * @param frameMediator
	 * @param references
	 */
	public ReplyAsAttachmentCommand(DefaultCommandReference[] references) {
		super(references);
	}

	public void updateGUI() throws Exception {
		// open composer frame
		controller = new ComposerController();

		// apply model
		controller.setComposerModel(model);

		// model->view update
		controller.updateComponents(true);

	}

	public void execute(Worker worker) throws Exception {
		// get selected foloder
		Folder folder =
			(Folder) ((FolderCommandReference) getReferences()[0]).getFolder();
		// get first selected message
		Object[] uids = ((FolderCommandReference) getReferences()[0]).getUids();

		// create new message object
		ColumbaMessage message = new ColumbaMessage();

		// get headerfields
		Header header =
			folder.getHeaderFields(
				uids[0],
				new String[] {
					"Subject",
					"To",
					"From",
					"Reply-To",
					"Message-ID",
					"In-Reply-To",
					"References" });
		message.setHeader(header);

		// get mimeparts
		MimeTree mimePartTree = folder.getMimePartTree(uids[0]);
		message.setMimePartTree(mimePartTree);

		// get message source
		String source = folder.getMessageSource(uids[0]);
		message.setStringSource(source);

		// create composer model
		model = new ComposerModel();

		// get bodypart
		MimePart bodyPart = message.getBodyPart();

		// set character set
		if (bodyPart != null) {
			String charset =
				bodyPart.getHeader().getContentParameter("charset");
			if (charset != null) {
				model.setCharsetName(charset);
			}
		}

		// set subject
		model.setSubject(
			MessageBuilderHelper.createReplySubject(header.get("Subject")));

		// original message is sent as attachment - model is setup according to
		// the stored option for html / text
		model.setHtml(MessageBuilderHelper.isHTMLEnabled());

		// decode To: headerfield
		String to = MessageBuilderHelper.createTo(header);

		if (to != null) {
			to = EncodedWord.decode(to).toString();
			model.setTo(to);

			// TODO: automatically add sender to addressbook
			// -> split to-headerfield, there can be more than only one
			// recipients!
			MessageBuilderHelper.addSenderToAddressbook(to);
		}

		// create In-Reply-To:, References: headerfields
		MessageBuilderHelper.createMailingListHeaderItems(header, model);

		// try to good guess the correct account
		Integer accountUid =
			(Integer) folder.getAttribute(uids[0], "columba.accountuid");
		String host = (String) folder.getAttribute(uids[0], "columba.host");
		String address = header.get("To");
		AccountItem accountItem =
			MessageBuilderHelper.getAccountItem(accountUid, host, address);
		model.setAccountItem(accountItem);

		//		original message is sent as attachment - model is setup according to
		//the stored option for html / text
		model.setHtml(MessageBuilderHelper.isHTMLEnabled());

		//	append message as mimepart
		if (message.getSource() != null) {
			// initialize MimeHeader as RFC822-compliant-message
			MimeHeader mimeHeader = new MimeHeader();
			mimeHeader.setMimeType(new MimeType("message", "rfc822"));

			// add mimepart to model
			model.addMimePart(
				new LocalMimePart(mimeHeader, message.getSource()));
		}
	}

}
