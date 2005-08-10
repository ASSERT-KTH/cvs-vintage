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

package org.columba.mail.composer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.columba.api.command.IWorkerStatusController;
import org.columba.core.logging.Logging;
import org.columba.core.versioninfo.VersionInfo;
import org.columba.core.xml.XmlElement;
import org.columba.mail.config.AccountItem;
import org.columba.mail.config.Identity;
import org.columba.mail.config.MailConfig;
import org.columba.mail.config.SecurityItem;
import org.columba.mail.gui.composer.ComposerModel;
import org.columba.mail.message.PGPMimePart;
import org.columba.mail.message.SendableHeader;
import org.columba.mail.parser.ListBuilder;
import org.columba.mail.parser.ListParser;
import org.columba.mail.parser.text.HtmlParser;
import org.columba.ristretto.coder.EncodedWord;
import org.columba.ristretto.composer.MimeTreeRenderer;
import org.columba.ristretto.io.CharSequenceSource;
import org.columba.ristretto.message.Address;
import org.columba.ristretto.message.LocalMimePart;
import org.columba.ristretto.message.MessageDate;
import org.columba.ristretto.message.MessageIDGenerator;
import org.columba.ristretto.message.MimeHeader;
import org.columba.ristretto.message.MimePart;
import org.columba.ristretto.message.StreamableMimePart;
import org.columba.ristretto.parser.ParserException;

public class MessageComposer {
	/** JDK 1.4+ logging framework logger, used for logging. */
	private static final Logger LOG = Logger
			.getLogger("org.columba.mail.composer");

	private static final Charset headerCharset = Charset.forName("UTF-8");
	
	private ComposerModel model;

	private int accountUid;

	public MessageComposer(ComposerModel model) {
		this.model = model;
	}

	protected SendableHeader initHeader() {
		SendableHeader header = new SendableHeader();
		
		// RFC822 - Header
		if (model.getToList() != null) {
			String s = ListParser.createStringFromList(ListBuilder
					.createFlatList(model.getToList()));
			if ( s.length() != 0 )
				header.set("To",EncodedWord.encode(s,
						headerCharset, EncodedWord.QUOTED_PRINTABLE).toString() );
		}

		if (model.getCcList() != null) {
			String s = ListParser.createStringFromList(ListBuilder
					.createFlatList(model.getCcList()));
			if ( s.length() != 0 )
			header.set("Cc",EncodedWord.encode(s,
					headerCharset, EncodedWord.QUOTED_PRINTABLE).toString() );
		}


		header.getAttributes().put("columba.subject", model.getSubject());

		//header.set("Subject",
		//	EncodedWord.encode(model.getSubject(),
		//		Charset.forName(model.getCharsetName()),
		//		EncodedWord.QUOTED_PRINTABLE).toString());
		header.set("Subject", EncodedWord.encode(model.getSubject(),
				headerCharset, EncodedWord.QUOTED_PRINTABLE).toString());

		AccountItem item = model.getAccountItem();
		Identity identity = item.getIdentity();

		//mod: 20040629 SWITT for redirecting feature
		//If FROM value was set, take this as From, else take Identity
		if (model.getMessage().getHeader().getHeader().get("From") != null) {
			header.set("From", EncodedWord.encode(model.getMessage().getHeader().getHeader().get(
					"From"), headerCharset, EncodedWord.QUOTED_PRINTABLE).toString());
		} else {
			header.set("From", EncodedWord.encode(identity.getAddress().toString(),
					headerCharset, EncodedWord.QUOTED_PRINTABLE).toString());
		}
		
		

		header.set("X-Priority", model.getPriority());

		/*
		 * String priority = controller.getModel().getPriority();
		 * 
		 * if (priority != null) { header.set("columba.priority", new
		 * Integer(priority)); } else { header.set("columba.priority", new
		 * Integer(3)); }
		 */
		header.set("Mime-Version", "1.0");

		String organisation = identity.getOrganisation();

		if (organisation != null && organisation.length() > 0) {
			header.set("Organisation", organisation);
		}

		// reply-to
		Address replyAddress = identity.getReplyToAddress();

		if (replyAddress != null) {
			header.set("Reply-To", EncodedWord.encode(replyAddress.getMailAddress(),
					headerCharset, EncodedWord.QUOTED_PRINTABLE).toString());
		}

		String messageID = MessageIDGenerator.generate();
		header.set("Message-ID", messageID);

		String inreply = model.getHeaderField("In-Reply-To");

		if (inreply != null) {
			header.set("In-Reply-To", EncodedWord.encode(inreply,
					headerCharset, EncodedWord.QUOTED_PRINTABLE).toString());
		}

		String references = model.getHeaderField("References");

		if (references != null) {
			header.set("References", references);
		}

		header.set("X-Mailer", "Columba ("
				+ VersionInfo.getVersion() + ")");

		header.getAttributes().put("columba.from", identity.getAddress());

		// date
		Date date = new Date();
		header.getAttributes().put("columba.date", date);
		header.set("Date", MessageDate.toString(date));

		//attachments
		header.getAttributes().put("columba.attachment", new Boolean(model.getAttachments().size() > 0));
		
		// copy flags
		header.setFlags(model.getMessage().getHeader().getFlags());
		
		return header;
	}

	private boolean needQPEncoding(String input) {
		for (int i = 0; i < input.length(); i++) {
			if (input.charAt(i) > 127) {
				return true;
			}
		}

		return false;
	}

	/**
	 * gives the signature for this Mail back. This signature is NOT a
	 * pgp-signature but a real mail-signature.
	 * 
	 * @param item
	 *            The item wich holds the signature-file
	 * @return The signature for the mail as a String. The Signature is
	 *         character encoded with the caracter set from the model
	 */
	protected String getSignature(File file) {
		StringBuffer strbuf = new StringBuffer();

		try {
			BufferedReader in = new BufferedReader(new FileReader(file));

			/*
			 * BufferedReader in = new BufferedReader( new InputStreamReader(
			 * new FileInputStream(file), model.getCharsetName()));
			 */
			String str;

			while ((str = in.readLine()) != null) {
				strbuf.append(str + "\n");
			}

			in.close();
			
			return strbuf.toString();
		} catch (IOException ex) {
			ex.printStackTrace();

			return "";
		}
	}

	/**
	 * Composes a multipart/alternative mime part for the body of a message
	 * containing a text part and a html part. <br>
	 * This is to be used for sending html messages, when an alternative text
	 * part - to be read by users not able to read html - is required. <br>
	 * Pre-condition: It is assumed that the model contains a message in html
	 * format.
	 * 
	 * @return The composed mime part for the message body
	 * @author Karl Peder Olesen (karlpeder)
	 */
	private StreamableMimePart composeMultipartAlternativeMimePart(boolean appendSignature) {
		// compose text part
		StreamableMimePart textPart = composeTextMimePart(appendSignature);

		// compose html part
		StreamableMimePart htmlPart = composeHtmlMimePart(appendSignature);

		// merge mimeparts and return
		LocalMimePart bodyPart = new LocalMimePart(new MimeHeader("multipart",
				"alternative"));
		bodyPart.addChild(textPart);
		bodyPart.addChild(htmlPart);

		return bodyPart;
	}

	/**
	 * Composes a text/html mime part from the body contained in the composer
	 * model. This could be for a pure html message or for the html part of a
	 * multipart/alternative. <br>
	 * If a signature is defined, it is added to the body. <br>
	 * Pre-condition: It is assumed that the model contains a html message.
	 * 
	 * @return The composed text/html mime part
	 * @author Karl Peder Olesen (karlpeder)
	 */
	private StreamableMimePart composeHtmlMimePart(boolean appendSignature) {
		// Init Mime-Header with Default-Values (text/html)
		LocalMimePart bodyPart = new LocalMimePart(new MimeHeader("text",
				"html"));

		// Set Default Charset or selected
		String charsetName = model.getCharset().name();


		StringBuffer buf = new StringBuffer();
		String body = model.getBodyText();

		// insert link tags for urls and email addresses
		body = HtmlParser.substituteURL(body, false);
		body = HtmlParser.substituteEmailAddress(body, false);

		String lcase = body.toLowerCase(); // for text comparisons

		// insert document type decl.
		if (lcase.indexOf("<!doctype") == -1) {
			// FIXME (@author karlpeder): Is 3.2 the proper version of html to refer to?
			buf.append("<!DOCTYPE HTML PUBLIC "
					+ "\"-//W3C//DTD HTML 3.2//EN\">\r\n");
		}

		// insert head section with charset def.
		String meta = "<meta " + "http-equiv=\"Content-Type\" "
				+ "content=\"text/html; charset=" + charsetName + "\">";
		int pos = lcase.indexOf("<head");
		int bodyStart;

		if (pos == -1) {
			// add <head> section
			pos = lcase.indexOf("<html") + 6;
			buf.append(body.substring(0, pos));
			buf.append("<head>");
			buf.append(meta);
			buf.append("</head>");

			bodyStart = pos;
		} else {
			// replace <head> section
			pos = lcase.indexOf('>', pos) + 1;
			buf.append(body.substring(0, pos));
			buf.append(meta);

			// TODO (@author karlpeder): If existing meta tags are to be kept, code changes are
			// necessary
			bodyStart = lcase.indexOf("</head");
		}

		// add rest of body until start of </body>
		int bodyEnd = lcase.indexOf("</body");
		buf.append(body.substring(bodyStart, bodyEnd));

		// add signature if defined
		AccountItem item = model.getAccountItem();
		Identity identity = item.getIdentity();
		File signatureFile = identity.getSignature();

		if (signatureFile != null) {
			String signature = getSignature(signatureFile);

			if (signature != null) {
				buf.append("\r\n\r\n");

				// TODO: Should we take some action to ensure signature is valid
				// html?
				buf.append(signature);
			}
		}

		// add the rest of the original body - and transfer back to body var.
		buf.append(body.substring(bodyEnd));
		body = buf.toString();

		// add encoding if necessary
		if (needQPEncoding(body)) {
			bodyPart.getHeader().setContentTransferEncoding("quoted-printable");

			// check if the charset is US-ASCII then there is something wrong
			// -> switch to UTF-8 and write to log-file
			if( charsetName.equalsIgnoreCase("us-ascii")){
				charsetName = "UTF-8";
				LOG.info("Charset was US-ASCII but text has 8-bit chars -> switched to UTF-8");
			}
		}

		bodyPart.getHeader().putContentParameter("charset", charsetName);

		// to allow empty messages
		if (body.length() == 0) {
			body = " ";
		}

		bodyPart.setBody(new CharSequenceSource(body));

		return bodyPart;
	}

	/**
	 * Composes a text/plain mime part from the body contained in the composer
	 * model. This could be for a pure text message or for the text part of a
	 * multipart/alternative. <br>
	 * If the model contains a html message, tags are stripped to get plain
	 * text. <br>
	 * If a signature is defined, it is added to the body.
	 * @param appendSignature 
	 * 
	 * @return The composed text/plain mime part
	 */
	private StreamableMimePart composeTextMimePart(boolean appendSignature) {
		// Init Mime-Header with Default-Values (text/plain)
		LocalMimePart bodyPart = new LocalMimePart(new MimeHeader("text",
				"plain"));

		// Set Default Charset or selected
		String charsetName = model.getCharset().name();


		String body = model.getBodyText();

		/*
		 * *20030918, karlpeder* Tags are stripped if the model contains a html
		 * message (since we are composing a plain text message here.
		 */
		if (model.isHtml()) {
			body = HtmlParser.htmlToText(body);
		}

		AccountItem item = model.getAccountItem();
		Identity identity = item.getIdentity();
		File signatureFile = identity.getSignature();

		if (appendSignature && signatureFile != null) {
			String signature = getSignature(signatureFile);

			if (signature != null) {
				body = body + "\r\n\r\n" + signature;
			}
		}

		if (needQPEncoding(body)) {
			bodyPart.getHeader().setContentTransferEncoding("quoted-printable");
			
			// check if the charset is US-ASCII then there is something wrong
			// -> switch to UTF-8 and write to log-file
			if( charsetName.equalsIgnoreCase("us-ascii")){
				charsetName = "UTF-8";
				LOG.info("Charset was US-ASCII but text has 8-bit chars -> switched to UTF-8");
			}
		}
		
		// write charset to header
		bodyPart.getHeader().putContentParameter("charset", charsetName);
		
		// to allow empty messages
		if (body.length() == 0) {
			body = " ";
		}

		bodyPart.setBody(new CharSequenceSource(body));

		return bodyPart;
	}

	

	public SendableMessage compose(IWorkerStatusController workerStatusController, boolean appendSignature)
			throws Exception {
		this.accountUid = model.getAccountItem().getUid();

		workerStatusController.setDisplayText("Composing Message...");

		MimeTreeRenderer renderer = MimeTreeRenderer.getInstance();
		SendableMessage message = new SendableMessage();
		StringBuffer composedMessage = new StringBuffer();

		SendableHeader header = initHeader();
		MimePart root = null;

		/*
		 * *20030921, karlpeder* The old code was (accidentially!?) modifying
		 * the attachment list of the model. This affects the composing when
		 * called a second time for saving the message after sending!
		 */

		//List mimeParts = model.getAttachments();
		List attachments = model.getAttachments();
		List mimeParts = new ArrayList();
		Iterator ite = attachments.iterator();

		while (ite.hasNext()) {
			mimeParts.add(ite.next());
		}

		// *20030919, karlpeder* Added handling of html messages
		StreamableMimePart body;

		if (model.isHtml()) {
			// compose message body as multipart/alternative
			XmlElement composerOptions = MailConfig.getInstance()
					.getComposerOptionsConfig().getRoot()
					.getElement("/options");
			XmlElement html = composerOptions.getElement("html");

			if (html == null) {
				html = composerOptions.addSubElement("html");
			}

			String multipart = html.getAttribute("send_as_multipart", "true");

			if (multipart.equals("true")) {
				// send as multipart/alternative
				body = composeMultipartAlternativeMimePart(appendSignature);
			} else {
				// send as text/html
				body = composeHtmlMimePart(appendSignature);
			}
		} else {
			// compose message body as text/plain
			body = composeTextMimePart(appendSignature);
		}

		if (body != null) {
			mimeParts.add(0, body);
		}

		// Create Multipart/Mixed if necessary
		if (mimeParts.size() > 1) {
			root = new MimePart(new MimeHeader("multipart", "mixed"));

			for (int i = 0; i < mimeParts.size(); i++) {
				root.addChild((StreamableMimePart) mimeParts.get(i));
			}
		} else {
			root = (MimePart) mimeParts.get(0);
		}

		if (model.isSignMessage()) {
			SecurityItem item = model.getAccountItem().getPGPItem();
			String idStr = item.get("id");

			// if the id not currently set (for example in the security panel in
			// the account-config
			if ((idStr == null) || (idStr.length() == 0)) {
				//  Set id on from address
				item.setString("id", model.getAccountItem().getIdentity()
						.getAddress().getMailAddress());
			}

			PGPMimePart signPart = new PGPMimePart(new MimeHeader("multipart",
					"signed"), item);

			signPart.addChild(root);
			root = signPart;
		}

		if (model.isEncryptMessage()) {
			SecurityItem item = model.getAccountItem().getPGPItem();

			// Set recipients from the recipients vector
			List recipientList = model.getRCPTVector();
			StringBuffer recipientBuf = new StringBuffer();

			for (Iterator it = recipientList.iterator(); it.hasNext();) {
				recipientBuf.append((String) it.next());
			}

			item.setString("recipients", recipientBuf.toString());

			PGPMimePart signPart = new PGPMimePart(new MimeHeader("multipart",
					"encrypted"), item);

			signPart.addChild(root);
			root = signPart;
		}

		header.setRecipients(model.getRCPTVector());

		List headerItemList;

		headerItemList = model.getToList();

	
		if ( ( headerItemList != null ) && (headerItemList.size() > 0) ){
			Address adr = null;
			try {
				adr = Address.parse((String) headerItemList.get(0));
				header.getAttributes().put("columba.to", adr);
			} catch (ParserException e) {
				if (Logging.DEBUG)
					e.printStackTrace();
			}
		}

		headerItemList = model.getCcList();

		if ( ( headerItemList != null ) && (headerItemList.size() > 0) ) {
			Address adr = null;
			try {
				adr = Address.parse((String) headerItemList.get(0));
				header.getAttributes().put("columba.cc", adr);
			} catch (ParserException e) {
				if (Logging.DEBUG)
					e.printStackTrace();
			}

		}

		String composedBody;

		root.getHeader().getHeader().merge(header.getHeader());

		InputStream in = renderer.renderMimePart(root);

		// size
		int size = in.available() / 1024;
		header.getAttributes().put("columba.size", new Integer(size));

		message.setHeader(header);

		message.setAccountUid(accountUid);

		//Do not access the inputstream after this line!
		message.setSourceStream(in);

		return message;
	}
}