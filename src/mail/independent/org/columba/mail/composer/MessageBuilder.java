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

package org.columba.mail.composer;

import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.columba.addressbook.folder.ContactCard;
import org.columba.addressbook.parser.AddressParser;
import org.columba.addressbook.parser.ListParser;
import org.columba.mail.coder.CoderRouter;
import org.columba.mail.coder.Decoder;
import org.columba.mail.config.AccountItem;
import org.columba.mail.config.MailConfig;
import org.columba.mail.gui.composer.ComposerModel;
import org.columba.mail.message.ColumbaHeader;
import org.columba.mail.message.Message;
import org.columba.mail.message.MimeHeader;
import org.columba.mail.message.MimePart;
import org.columba.mail.parser.Rfc822Parser;
import org.columba.mail.parser.text.BodyTextParser;
import org.columba.mail.parser.text.HtmlParser;

/**
 * 
 * The <code>MessageBuilder</code> class is responsible for creating the
 * information for the <code>ComposerModel</class>class.
 * 
 * It generates appropriate header-information, mimeparts and
 * quoted bodytext 
 *
 * 
 */
public class MessageBuilder {

	public final static int REPLY = 0;
	public final static int REPLY_ALL = 1;
	public final static int REPLY_MAILINGLIST = 2;
	public final static int REPLY_AS_ATTACHMENT = 3;

	public final static int FORWARD = 4;
	public final static int FORWARD_INLINE = 5;

	public final static int OPEN = 6;

	private static MessageBuilder instance;

	public MessageBuilder() {

	}

	public static MessageBuilder getInstance() {
		if (instance == null)
			instance = new MessageBuilder();

		return instance;
	}

	/**
	 * 
	 * Check if the subject headerfield already starts with a pattern
	 * like "Re:" or "Fwd:"
	 * 
	 * @param subject  A <code>String</code> containing the subject
	 * @param pattern A <code>String</code> specifying the pattern
	 *                to search for.
	 **/
	public static boolean isAlreadyReply(String subject, String pattern) {

		if (subject == null)
			return false;

		if (subject.length() == 0)
			return false;

		String str = subject.toLowerCase();

		// for example: "Re: this is a subject"
		if (str.startsWith(pattern) == true)
			return true;

		// for example: "[columba-users]Re: this is a subject"
		int index = str.indexOf(pattern);
		if (index != -1)
			return true;

		return false;
	}

	/**
	 * 
	 * create subject headerfield in using the senders message
	 * subject and prepending "Re:" if not already there
	 * 
	 * @param header A <code>ColumbaHeader</code> which contains
	 *               the headerfields of the message we want
	 * 	             reply/forward.
	 * 
	 * FIXME: we need to i18n this!
	 **/
	private static String createReplySubject(ColumbaHeader header) {
		String subject = (String) header.get("Subject");

		// if subject doesn't start already with "Re:" prepend it
		if (isAlreadyReply(subject, "re:") == false)
			subject = "Re: " + subject;

		return subject;
	}

	/**
	 * 
	 * create Subject headerfield in using the senders message
	 * subject and prepending "Fwd:" if not already there
	 * 
	 * @param header A <code>ColumbaHeader</code> which contains
	 *        the headerfields of the message we want
	 * 	      reply/forward.
	 * 
	 * FIXME: we need to i18n this!
	 * 
	 **/
	private static String createForwardSubject(ColumbaHeader header) {
		String subject = (String) header.get("Subject");

		// if subject doesn't start already with "Fwd:" prepend it
		if (isAlreadyReply(subject, "fwd:") == false)
			subject = "Fwd: " + subject;

		return subject;
	}

	/**
	 * 
	 * create a To headerfield in using the senders message
	 * Reply-To or From headerfield
	 * 
	 * @param header A <code>ColumbaHeader</code> which contains
	 *               the headerfields of the message we want
	 * 	             reply/forward.
	 * 
	 * */
	private static String createTo(ColumbaHeader header) {
		String replyTo = (String) header.get("Reply-To");
		String from = (String) header.get("From");

		if (replyTo == null) {
			// Reply-To headerfield isn't specified, try to use From instead
			if (from != null)
				return from;
			else
				return "";
		} else
			return replyTo;
	}

	/**
	 * 
	 * This is for creating the "Reply To All recipients" 
	 * To headerfield.
	 * 
	 * It is different from the <code>createTo</code> method
	 * in that it also appends the recipients specified in the
	 * To headerfield
	 * 
	 * @param header A <code>ColumbaHeader</code> which contains
	 *               the headerfields of the message we want
	 * 	             reply/forward.
	 * 
	 **/
	private static String createToAll(ColumbaHeader header) {
		String sender = "";
		String replyTo = (String) header.get("Reply-To");
		String from = (String) header.get("From");
		String to = (String) header.get("To");
		String cc = (String) header.get("Cc");

		// if Reply-To headerfield isn't specified, try to use from
		if (replyTo == null) {
			sender = from;
		} else
			sender = replyTo;

		// create To headerfield
		StringBuffer buf = new StringBuffer();
		buf.append(sender);
		if (to != null) {

			buf.append(",");
			buf.append(to);
		}
		if (cc != null) {

			buf.append(",");
			buf.append(cc);
		}

		return buf.toString();
	}

	/**
	 * 
	 * This method creates a To headerfield for the
	 * "Reply To MailingList" action. 
	 * It uses the List-Post headerfield and falls back
	 * to Reply-To or From if needed
	 * 
	 * @param header A <code>ColumbaHeader</code> which contains
	 *               the headerfields of the message we want
	 * 	             reply/forward.
	 * */
	private static String createToMailinglist(ColumbaHeader header) {

		// example: X-BeenThere: columba-devel@lists.sourceforge.net
		String sender = (String) header.get("X-BeenThere");

		if (sender == null)
			sender = (String) header.get("Reply-To");

		if (sender == null)
			sender = (String) header.get("From");

		return sender;
	}

	/**
	 * 
	 * Creates In-Reply-To and References headerfields. 
	 * These are useful for mailing-list threading.
	 * 
	 * @param header A <code>ColumbaHeader</code> which contains
	 *               the headerfields of the message we want
	 * 	             reply/forward.
	 * 
	 * @param model  The <code>ComposerModel</code> we want to 
	 *               pass the information to.
	 * 
	 * FIXME: if the References headerfield contains to many 
	 *        characters, we have to remove some of the first
	 *        References, before appending another one.
	 *        (RFC822 headerfields are not allowed to become  
	 *        that long)
	 * 
	 * */
	private static void createMailingListHeaderItems(
		ColumbaHeader header,
		ComposerModel model) {
		String messageId = (String) header.get("Message-ID");
		if (messageId == null)
			messageId = (String) header.get("Message-Id");

		if (messageId != null) {
			model.setHeaderField("In-Reply-To", messageId);

			String references = (String) header.get("References");
			if (references != null) {
				references = references + " " + messageId;
				model.setHeaderField("References", references);

			}
		}
	}

	/**
	 * 
	 * Search the correct Identity for replying to someone
	 * 
	 * @param header A <code>ColumbaHeader</code> which contains
	 *               the headerfields of the message we want
	 * 	             reply/forward.
	 */
	private static AccountItem getAccountItem(ColumbaHeader header) {
		String host = (String) header.get("columba.host");
		String address = (String) header.get("To");

		// if the Account/Identity is already defined in the columba.host headerfield
		// use it. Otherwise search through every account for the To/email-address
		AccountItem item =
			MailConfig.getAccountList().hostGetAccount(host, address);

		return item;
	}

	/**
	 * 
	 * create bodytext
	 * 
	 * @param message A <code>Message</code> which contains
	 *                the bodytext of the message we want
	 * 	              reply/forward.
	 */
	private static String createBodyText(Message message) {
		String bodyText = "";

		MimePart bodyPart = message.getBodyPart();

		String charset = bodyPart.getHeader().getContentParameter("charset");

		// init decoder with appropriate content-transfer-encoding
		Decoder decoder =
			CoderRouter.getDecoder(
				bodyPart.getHeader().contentTransferEncoding);

		// decode bodytext
		try {

			bodyText = decoder.decode(bodyPart.getBody(), charset);
		} catch (UnsupportedEncodingException e) {
		}

		return bodyText;
	}

	/**
	 * 
	 * prepend "> " characters to the bodytext to specify 
	 * we are quoting
	 * 
	 * @param message A <code>Message</code> which contains
	 *                the bodytext of the message we want
	 * 	              reply/forward.
	 * 
	 * FIXME: we should make this configureable
	 * 
	 */
	private static String createQuotedBodyText(Message message) {
		String bodyText = createBodyText(message);

		/*
		 * *20030621, karlpeder* tags are stripped
		 * if the message body part is html
		 */
		MimeHeader header = message.getBodyPart().getHeader();
		if (header.getContentSubtype().equals("html")) {
			bodyText = HtmlParser.htmlToText(bodyText);
		}

		String quotedBodyText = BodyTextParser.quote(bodyText);

		return quotedBodyText;

	}

	/** 
	 * 
	 * Fill the <code>ComposerModel</code> with headerfields,
	 * bodytext and mimeparts
	 * 
	 * @param message   A <code>Message</code> which contains
	 *                  the headerfield/bodytext/mimeparts of 
	 *                  of the message we want to reply/forward.
	 * 
	 * @param model     The <code>ComposerModel</code> we want to 
	 *                  pass the information to.
	 * 
	 * @param operation an int value specifying the operation-type
	 *                  (for example: MessageBuilder.REPLY, .REPLY_TO_ALL)
	 * 
	 */
	public void createMessage(
		Message message,
		ComposerModel model,
		int operation) {

		ColumbaHeader header = (ColumbaHeader) message.getHeader();

		MimePart bodyPart = message.getBodyPart();

		if (bodyPart != null) {
			String charset =
				bodyPart.getHeader().getContentParameter("charset");
			if (charset != null) {
				model.setCharsetName(charset);
			}
		}

		if ((operation == FORWARD) || (operation == FORWARD_INLINE)) {
			model.setSubject(createForwardSubject(header));
		} else {
			model.setSubject(createReplySubject(header));
		}

		String to = null;
		if (operation == REPLY)
			to = createTo(header);
		else if (operation == REPLY_ALL)
			to = createToAll(header);
		else if (operation == REPLY_MAILINGLIST)
			to = createToMailinglist(header);

		if (to != null) {
			model.setTo(to);
			addSenderToAddressbook(to);
		}

		if (operation != FORWARD)
			createMailingListHeaderItems(header, model);

		if ((operation != FORWARD) && (operation != FORWARD_INLINE)) {
			AccountItem accountItem = getAccountItem(header);
			model.setAccountItem(accountItem);
		}

		if ((operation == REPLY_AS_ATTACHMENT) || (operation == FORWARD)) {
			// append message as mimepart
			if (message.getSource() != null) {
				// initialize MimeHeader as RFC822-compliant-message
				MimeHeader mimeHeader = new MimeHeader();
				mimeHeader.contentType = new String("Message");
				mimeHeader.contentSubtype = new String("Rfc822");

				model.addMimePart(
					new MimePart(mimeHeader, message.getSource()));
			}
		} else {
			// prepend "> " to every line of the bodytext
			String bodyText = createQuotedBodyText(message);
			if (bodyText == null) {
				bodyText = "<Error parsing bodytext>";
			}
			model.setBodyText(bodyText);
		}

	}

	/** 
	 * 
	 * Fill the <code>ComposerModel</code> with headerfields,
	 * bodytext and mimeparts.
	 * 
	 * This is a special method for the "Open Message in Composer"
	 * action.
	 * 
	 * @param message   The <code>Message</code> we want to edit
	 *                  as new message.
	 * 
	 * @param model     The <code>ComposerModel</code> we want to 
	 *                  pass the information to.
	 * 
	 */
	public static void openMessage(Message message, ComposerModel model) {
		ColumbaHeader header = (ColumbaHeader) message.getHeader();

		// copy every headerfield the original message contains
		Hashtable hashtable = header.getHashtable();
		for (Enumeration e = hashtable.keys(); e.hasMoreElements();) {
			Object key = e.nextElement();

			try {
				model.setHeaderField(
					(String) key,
					(String) header.get((String) key));
			} catch (ClassCastException ex) {
				System.out.println("skipping header item");
			}

		}

		model.setTo((String) header.get("To"));

		AccountItem accountItem = getAccountItem(header);
		model.setAccountItem(accountItem);

		/* 
		   parse the whole message. this is for:
		    -> creating the MimePart-objects
		    -> decoding the bodytext
		        -> in ordner to add MimeParts to the ComposerModel
		           we need to decode them. the MimeParts become
		           encoded before sending them (quoted-printable
		           or base64). Not decoding them here, would make
		           them become encoded twice times.
		*/
		Message parsedMessage =
			new Rfc822Parser().parse(
				message.getSource(),
				(ColumbaHeader) message.getHeader());

		int count = parsedMessage.getMimePartTree().count();
		Decoder decoder;
		for (int i = 0; i < count; i++) {
			MimePart mp = parsedMessage.getMimePartTree().get(i);
			MimeHeader mimeHeader = mp.getHeader();
			decoder =
				CoderRouter.getDecoder(mimeHeader.contentTransferEncoding);

			String str = "";
			try {
				str = decoder.decode(mp.getBody(), null);

			} catch (UnsupportedEncodingException e) {
			}

			// first MimePart is the bodytext of the message
			if (i == 0) {
				/*
				 * *20030621, karlpeder* tags are stripped if the 
				 * message body part is html
				 */
				if (mimeHeader.getContentSubtype().equals("html")) {
					model.setBodyText(HtmlParser.htmlToText(str));
				} else {
					model.setBodyText(str);
				}
				//model.setBodyText(str);
			} else {
				mp.setBody(str);
				model.addMimePart(mp);
			}

		}

	}

	/********************** addressbook stuff ***********************/

	/**
	 *
	 * add automatically every person we'll send a message to
	 * the "Collected Addresses" Addressbook
	 * 
	 * FIXME: this should be moved outside independent
	 *        -> should be in core, or even better addressbook
	 *
	 */
	public void addSenderToAddressbook(String sender) {

		if (sender != null) {
			if (sender.length() > 0) {

				org.columba.addressbook.folder.Folder selectedFolder =
					org
						.columba
						.addressbook
						.facade
						.FolderFacade
						.getCollectedAddresses();

				// this can be a list of recipients
				List list = ListParser.parseString(sender);
				Iterator it = list.iterator();
				while (it.hasNext()) {
					String address =
						AddressParser.getAddress((String) it.next());
					System.out.println("address:" + address);

					if (!selectedFolder.exists(address)) {
						ContactCard card = new ContactCard();

						String fn = AddressParser.getDisplayname(sender);
						System.out.println("fn=" + fn);

						card.set("fn", fn);
						card.set("displayname", fn);
						card.set("email", "internet", address);

						selectedFolder.add(card);
					}

				}

			}
		}

	}

}