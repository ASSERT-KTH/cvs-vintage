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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Iterator;

import org.columba.core.command.DefaultCommandReference;
import org.columba.core.command.WorkerStatusController;
import org.columba.core.io.StreamUtils;
import org.columba.core.xml.XmlElement;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.composer.MessageBuilderHelper;
import org.columba.mail.folder.MessageFolder;
import org.columba.mail.gui.composer.ComposerModel;
import org.columba.mail.gui.composer.util.QuoteFilterInputStream;
import org.columba.mail.gui.util.AddressListRenderer;
import org.columba.mail.main.MailInterface;
import org.columba.mail.parser.text.HtmlParser;
import org.columba.mail.util.MailResourceLoader;
import org.columba.ristretto.coder.Base64DecoderInputStream;
import org.columba.ristretto.coder.CharsetDecoderInputStream;
import org.columba.ristretto.coder.QuotedPrintableDecoderInputStream;
import org.columba.ristretto.message.Address;
import org.columba.ristretto.message.BasicHeader;
import org.columba.ristretto.message.Header;
import org.columba.ristretto.message.InputStreamMimePart;
import org.columba.ristretto.message.MimeHeader;
import org.columba.ristretto.message.MimePart;
import org.columba.ristretto.message.MimeTree;

/**
 * Forward message inline, which is the same as replying to someone who is not
 * the original sender.
 * 
 * @author fdietz
 */
public class ForwardInlineCommand extends ForwardCommand {

	protected final String[] headerfields = new String[] { "Subject", "Date",
			"From", "To" };

	/**
	 * Constructor for ForwardInlineCommand.
	 * 
	 * @param frameMediator
	 * @param references
	 */
	public ForwardInlineCommand(DefaultCommandReference[] references) {
		super(references);
	}

	public void execute(WorkerStatusController worker) throws Exception {
		// create composer model
		model = new ComposerModel();

		// get selected folder
		MessageFolder folder = (MessageFolder) ((FolderCommandReference) getReferences()[0])
				.getFolder();

		// get first selected message
		Object[] uids = ((FolderCommandReference) getReferences()[0]).getUids();

		//      ->set source reference in composermodel
		// when replying this is the original sender's message
		// you selected and replied to
		FolderCommandReference[] ref = new FolderCommandReference[1];
		ref[0] = new FolderCommandReference(folder, uids);
		model.setSourceReference(ref);

		// setup to, references and account
		initHeader(folder, uids);

		// get mimeparts
		MimeTree mimePartTree = folder.getMimePartTree(uids[0]);

		XmlElement html = MailInterface.config.getMainFrameOptionsConfig()
				.getRoot().getElement("/options/html");

		// Which Bodypart shall be shown? (html/plain)
		MimePart bodyPart = null;
		Integer[] bodyPartAddress = null;
		if (Boolean.valueOf(html.getAttribute("prefer")).booleanValue()) {
			bodyPart = mimePartTree.getFirstTextPart("html");
		} else {
			bodyPart = mimePartTree.getFirstTextPart("plain");
		}

		if (bodyPart != null) {
			// setup charset and html
			initMimeHeader(bodyPart);

			StringBuffer bodyText;
			bodyPartAddress = bodyPart.getAddress();

			String quotedBodyText = createQuotedBody(bodyPart.getHeader(),
					folder, uids, bodyPartAddress);

			/*
			 * *20040210, karlpeder* Remove html comments - they are not
			 * displayed properly in the composer
			 */
			if (bodyPart.getHeader().getMimeType().getSubtype().equals("html")) {
				quotedBodyText = HtmlParser.removeComments(quotedBodyText);
			}

			model.setBodyText(quotedBodyText);
		}

		//  add all attachments
		MimeTree mt = folder.getMimePartTree(uids[0]);
		Iterator it = mt.getAllLeafs().iterator();
		while (it.hasNext()) {
			MimePart mp = (MimePart) it.next();
			Integer[] address = mp.getAddress();
			// skip if bodypart (already added as quoted text)
			if (Arrays.equals(address, bodyPartAddress))
				continue;

			// add attachment
			InputStream bodyStream = folder.getMimePartBodyStream(uids[0], address);
        	int encoding = mp.getHeader().getContentTransferEncoding();

            switch (encoding) {
                case MimeHeader.QUOTED_PRINTABLE:
                    bodyStream = new QuotedPrintableDecoderInputStream(bodyStream);
                    break;

                case MimeHeader.BASE64:
                    bodyStream = new Base64DecoderInputStream(bodyStream);
                    break;
                default:
            }

            model.addMimePart(new InputStreamMimePart(mp.getHeader(), bodyStream));
		}

	}

	private void initMimeHeader(MimePart bodyPart) {
		MimeHeader bodyHeader = bodyPart.getHeader();

		if (bodyHeader.getMimeType().getSubtype().equals("html")) {
			model.setHtml(true);
		} else {
			model.setHtml(false);
		}

		// Select the charset of the original message
		String charset = bodyHeader.getContentParameter("charset");

		if (charset != null) {
			model.setCharset(Charset.forName(charset));
		}
	}

	private void initHeader(MessageFolder folder, Object[] uids)
			throws Exception {
		// get headerfields
		Header header = folder.getHeaderFields(uids[0],
				new String[] { "Subject" });

		BasicHeader rfcHeader = new BasicHeader(header);

		// set subject
		model.setSubject(MessageBuilderHelper.createForwardSubject(rfcHeader
				.getSubject()));
	}

	protected String createQuotedBody(MimeHeader header, MessageFolder folder,
			Object[] uids, Integer[] address) throws IOException, Exception {
		InputStream bodyStream = folder.getMimePartBodyStream(uids[0], address);

		// Do decoding stuff
		switch (header.getContentTransferEncoding()) {
		case MimeHeader.QUOTED_PRINTABLE: {
			bodyStream = new QuotedPrintableDecoderInputStream(bodyStream);
			break;
		}

		case MimeHeader.BASE64: {
			bodyStream = new Base64DecoderInputStream(bodyStream);
		}
		}
		String charset = header.getContentParameter("charset");
		if (charset != null) {
			bodyStream = new CharsetDecoderInputStream(bodyStream, Charset
					.forName(charset));
		}

		String quotedBody;
		// Quote original message - different methods for text and html
		if (model.isHtml()) {
			// Html: Insertion of text before and after original message
			// get necessary headerfields
			BasicHeader rfcHeader = new BasicHeader(folder.getHeaderFields(
					uids[0], headerfields));
			String subject = rfcHeader.getSubject();
			String date = DateFormat.getDateTimeInstance(DateFormat.LONG,
					DateFormat.MEDIUM).format(rfcHeader.getDate());
			String from = AddressListRenderer.renderToHTMLWithLinks(
					new Address[] { rfcHeader.getFrom() }).toString();
			String to = AddressListRenderer.renderToHTMLWithLinks(
					rfcHeader.getTo()).toString();

			// build "quoted" message
			StringBuffer buf = new StringBuffer();
			buf.append("<html><body><p>");
			buf.append(MailResourceLoader.getString("dialog", "composer",
					"original_message_start"));
			buf.append("<br>"
					+ MailResourceLoader.getString("header", "header",
							"subject") + ": " + subject);
			buf.append("<br>"
					+ MailResourceLoader.getString("header", "header", "date")
					+ ": " + date);
			buf.append("<br>"
					+ MailResourceLoader.getString("header", "header", "from")
					+ ": " + from);
			buf.append("<br>"
					+ MailResourceLoader.getString("header", "header", "to")
					+ ": " + to);
			buf.append("</p>");
			buf.append(HtmlParser.removeComments( // comments are not displayed
					// correctly in composer
					HtmlParser.getHtmlBody(StreamUtils.readInString(bodyStream)
							.toString())));
			buf.append("<p>");
			buf.append(MailResourceLoader.getString("dialog", "composer",
					"original_message_end"));
			buf.append("</p></body></html>");

			quotedBody = buf.toString();
		} else {
			// Text: Addition of > before each line
			quotedBody = StreamUtils.readInString(
					new QuoteFilterInputStream(bodyStream)).toString();
		}

		bodyStream.close();
		return quotedBody;
	}
}