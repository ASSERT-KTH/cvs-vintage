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
import org.columba.core.gui.frame.FrameMediator;
import org.columba.core.xml.XmlElement;
import org.columba.mail.command.FolderCommand;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.composer.MessageBuilderHelper;
import org.columba.mail.config.AccountItem;
import org.columba.mail.config.MailConfig;
import org.columba.mail.folder.Folder;
import org.columba.mail.gui.composer.ComposerController;
import org.columba.mail.gui.composer.ComposerModel;
import org.columba.mail.gui.config.template.ChooseTemplateDialog;
import org.columba.mail.main.MailInterface;
import org.columba.mail.message.ColumbaHeader;
import org.columba.mail.message.ColumbaMessage;
import org.columba.mail.message.HeaderList;
import org.columba.mail.parser.text.HtmlParser;
import org.columba.ristretto.coder.EncodedWord;
import org.columba.ristretto.message.Header;
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

	protected ComposerController controller;
	protected ComposerModel model;

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
		FrameMediator frame,
		DefaultCommandReference[] references) {
		super(frame, references);

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
		Folder folder =
			(Folder) ((FolderCommandReference) getReferences()[0]).getFolder();
		Object[] uids = ((FolderCommandReference) getReferences()[0]).getUids();

		// template folder has uid=107
		Folder templateFolder = (Folder) MailInterface.treeModel.getFolder(107);

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

		// get headerfields
		Header header =
			folder.getHeaderFields(
				uids[0],
				new String[] {
					"Subject",
					"From",
					"To",
					"Reply-To",
					"Message-ID",
					"In-Reply-To",
					"References" });
		message.setHeader(header);

		message.setHeader(header);
		MimeTree mimePartTree = folder.getMimePartTree(uids[0]);
		message.setMimePartTree(mimePartTree);

		XmlElement html =
			MailConfig.getMainFrameOptionsConfig().getRoot().getElement(
				"/options/html");

		// Which Bodypart shall be shown? (html/plain)
		MimePart bodyPart = null;

		boolean viewhtml =
			Boolean.valueOf(html.getAttribute("prefer")).booleanValue();
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
		// *20030926, karlpeder* Added html support
		//MimePart mp = tree.getFirstTextPart("plain");
		MimePart mp;
		if (viewhtml) {
			mp = tree.getFirstTextPart("html");
		} else {
			mp = tree.getFirstTextPart("text");
		}
		LocalMimePart mimePart =
			(LocalMimePart) templateFolder.getMimePart(uid, mp.getAddress());
		boolean htmlTemplate;
		if (mimePart.getHeader().getMimeType().getSubtype().equals("html")) {
			htmlTemplate = true;
		} else {
			htmlTemplate = false;
		}

		// open new message in composer
		model = new ComposerModel();

		ColumbaHeader columbaHeader = (ColumbaHeader) message.getHeader();

		MimePart bp = message.getBodyPart();
		String templateBody = MessageBuilderHelper.createBodyText(bp);

		if (bp != null) {
			String charset =
				bodyPart.getHeader().getContentParameter("charset");
			if (charset != null) {
				model.setCharsetName(charset);
			}
		}

		model.setSubject(
			MessageBuilderHelper.createReplySubject(
				(String) header.get("Subject")));

		String to = MessageBuilderHelper.createTo(header);

		if (to != null) {
			to = EncodedWord.decode(to).toString();
			model.setTo(to);
			MessageBuilderHelper.addSenderToAddressbook(to);
		}

		MessageBuilderHelper.createMailingListHeaderItems(
			header,
			model);

		// try to good guess the correct account
		Integer accountUid = null;
		if (folder.getAttribute(uids[0], "columba.accountuid") != null)
			accountUid =
				(Integer) folder.getAttribute(uids[0], "columba.accountuid");
		String host = null;
		if (folder.getAttribute(uids[0], "columba.host") != null)
			host = (String) folder.getAttribute(uids[0], "columba.host");
		String address = (String) header.get("To");
		AccountItem accountItem =
			MessageBuilderHelper.getAccountItem(accountUid, host, address);
		model.setAccountItem(accountItem);

		// Initialisation of model to html or text
		MimeHeader bodyHeader = message.getBodyPart().getHeader();
		if (bodyHeader.getMimeType().getSubtype().equals("html")) {
			model.setHtml(true);
		} else {
			model.setHtml(false);
		}

		// prepend "> " to every line of the bodytext
		String bodyText =
			MessageBuilderHelper.createQuotedBodyText(
				message.getBodyPart(),
				model.isHtml());
		if (bodyText == null) {
			bodyText = "[Error parsing bodytext]";
		}

		if (!htmlTemplate && model.isHtml()) {
			// conversion to html necessary
			templateBody = HtmlParser.textToHtml(templateBody, null, null);
		} else if (htmlTemplate && !model.isHtml()) {
			// conversion to text necessary
			templateBody = HtmlParser.htmlToText(templateBody);
		}

		StringBuffer buf;
		if (model.isHtml()) {
			// insert template just before ending body tag
			String lcase = bodyText.toLowerCase();
			int pos = lcase.indexOf("</body>");
			if (pos < 0) {
				pos = bodyText.length();
			}
			buf = new StringBuffer(bodyText.substring(0, pos));
			buf.append(HtmlParser.getHtmlBody(templateBody));
			buf.append("</body></html>");
		} else {
			// just insert template at end (text mode)
			buf = new StringBuffer(bodyText);
			buf.append(templateBody);
		}

		model.setBodyText(buf.toString());
	}

}
