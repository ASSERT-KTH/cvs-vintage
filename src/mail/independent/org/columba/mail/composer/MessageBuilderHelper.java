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

import org.columba.addressbook.folder.ContactCard;
import org.columba.addressbook.parser.AddressParser;
import org.columba.addressbook.parser.ListParser;

import org.columba.core.io.StreamUtils;
import org.columba.core.xml.XmlElement;

import org.columba.mail.config.AccountItem;
import org.columba.mail.config.MailConfig;
import org.columba.mail.gui.composer.ComposerModel;
import org.columba.mail.parser.text.BodyTextParser;

import org.columba.ristretto.coder.Base64DecoderInputStream;
import org.columba.ristretto.coder.CharsetDecoderInputStream;
import org.columba.ristretto.coder.QuotedPrintableDecoderInputStream;
import org.columba.ristretto.message.Header;
import org.columba.ristretto.message.MimePart;
import org.columba.ristretto.message.StreamableMimePart;

import java.io.IOException;
import java.io.InputStream;

import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

import java.util.Iterator;
import java.util.List;


/**
 *
 * The <code>MessageBuilderHelper</code> class is responsible for creating
 * the information for the <code>ComposerModel</class>class.
 * <p>
 * It generates appropriate header-information, mimeparts and
 * quoted bodytext, etc.
 * <p>
 * These helper class is primarly used by the commands in org.columba.composer.command
 *
 *  @author fdietz, tstich
 */
public class MessageBuilderHelper {
    /**
     *
     * Check if the subject headerfield already starts with a pattern like
     * "Re:" or "Fwd:"
     *
     * @param subject
     *            A <code>String</code> containing the subject
     * @param pattern
     *            A <code>String</code> specifying the pattern to search for.
     */
    public static boolean isAlreadyReply(String subject, String pattern) {
        if (subject == null) {
            return false;
        }

        if (subject.length() == 0) {
            return false;
        }

        String str = subject.toLowerCase();

        // for example: "Re: this is a subject"
        if (str.startsWith(pattern) == true) {
            return true;
        }

        // for example: "[columba-users]Re: this is a subject"
        int index = str.indexOf(pattern);

        if (index != -1) {
            return true;
        }

        return false;
    }

    /**
     *
     * create subject headerfield in using the senders message subject and
     * prepending "Re:" if not already there
     *
     * @param header
     *            A <code>ColumbaHeader</code> which contains the
     *            headerfields of the message we want reply/forward.
     *
     * FIXME: we need to i18n this!
     */
    public static String createReplySubject(String subject) {
        // if subject doesn't start already with "Re:" prepend it
        if (!isAlreadyReply(subject, "re:")) {
            subject = "Re: " + subject;
        }

        return subject;
    }

    /**
     *
     * create Subject headerfield in using the senders message subject and
     * prepending "Fwd:" if not already there
     *
     * @param header
     *            A <code>ColumbaHeader</code> which contains the
     *            headerfields of the message we want reply/forward.
     *
     * FIXME: we need to i18n this!
     *
     */
    public static String createForwardSubject(String subject) {
        // if subject doesn't start already with "Fwd:" prepend it
        if (!isAlreadyReply(subject, "fwd:")) {
            subject = "Fwd: " + subject;
        }

        return subject;
    }

    /**
     *
     * create a To headerfield in using the senders message Reply-To or From
     * headerfield
     *
     * @param header
     *            A <code>Header</code> which contains the headerfields of
     *            the message we want reply/forward.
     *
     */
    public static String createTo(Header header) {
        String replyTo = (String) header.get("Reply-To");
        String from = (String) header.get("From");

        if (replyTo == null) {
            // Reply-To headerfield isn't specified, try to use From instead
            if (from != null) {
                return from;
            } else {
                return "";
            }
        } else {
            return replyTo;
        }
    }

    /**
     *
     * This is for creating the "Reply To All recipients" To headerfield.
     *
     * It is different from the <code>createTo</code> method in that it also
     * appends the recipients specified in the To headerfield
     *
     * @param header
     *            A <code>ColumbaHeader</code> which contains the
     *            headerfields of the message we want reply/forward.
     *
     */
    public static String createToAll(Header header) {
        String sender = "";
        String replyTo = (String) header.get("Reply-To");
        String from = (String) header.get("From");
        String to = (String) header.get("To");
        String cc = (String) header.get("Cc");

        // if Reply-To headerfield isn't specified, try to use from
        if (replyTo == null) {
            sender = from;
        } else {
            sender = replyTo;
        }

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
     * This method creates a To headerfield for the "Reply To MailingList"
     * action. It uses the X-BeenThere headerfield and falls back to Reply-To
     * or From if needed
     *
     * @param header
     *            A <code>Header</code> which contains the headerfields of
     *            the message we want reply/forward.
     */
    public static String createToMailinglist(Header header) {
        // example: X-BeenThere: columba-devel@lists.sourceforge.net
        String sender = (String) header.get("X-BeenThere");

        if (sender == null) {
            sender = (String) header.get("X-Beenthere");
        }

        if (sender == null) {
            sender = (String) header.get("Reply-To");
        }

        if (sender == null) {
            sender = (String) header.get("From");
        }

        return sender;
    }

    /**
     *
     * Creates In-Reply-To and References headerfields. These are useful for
     * mailing-list threading.
     *
     * @param header
     *            A <code>Header</code> which contains the headerfields of
     *            the message we want reply/forward.
     *
     * @param model
     *            The <code>ComposerModel</code> we want to pass the
     *            information to.
     *
     * FIXME: if the References headerfield contains to many characters, we
     * have to remove some of the first References, before appending another
     * one. (RFC822 headerfields are not allowed to become that long)
     *
     */
    public static void createMailingListHeaderItems(Header header,
        ComposerModel model) {
        String messageId = (String) header.get("Message-ID");

        if (messageId == null) {
            messageId = (String) header.get("Message-Id");
        }

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
     * <p>
     * TODO: use accountUid for better identification
     *
     */
    public static AccountItem getAccountItem(Integer accountUid, String host,
        String to) {
        // if the Account/Identity is already defined in the columba.host
        // headerfield
        // use it. Otherwise search through every account for the
        // To/email-address
        AccountItem item = MailConfig.getAccountList().hostGetAccount(host, to);

        return item;
    }

    /**
     *
     * create bodytext
     *
     * @param message
     *            A <code>Message</code> which contains the bodytext of the
     *            message we want reply/forward.
     */
    public static String createBodyText(MimePart mimePart)
        throws IOException {
        CharSequence bodyText = "";

        StreamableMimePart bodyPart = (StreamableMimePart) mimePart;
        String charsetName = bodyPart.getHeader().getContentParameter("charset");
        String encoding = bodyPart.getHeader().getContentTransferEncoding();

        InputStream body = bodyPart.getInputStream();

        if (encoding != null) {
            if (encoding.equals("quoted-printable")) {
                body = new QuotedPrintableDecoderInputStream(body);
            } else if (encoding.equals("base64")) {
                body = new Base64DecoderInputStream(body);
            }
        }

        if (charsetName != null) {
            Charset charset;

            try {
                charset = Charset.forName(charsetName);
            } catch (UnsupportedCharsetException e) {
                charset = Charset.forName(System.getProperty("file.encoding"));
            }

            body = new CharsetDecoderInputStream(body, charset);
        }

        return StreamUtils.readInString(body).toString();
    }

    /**
     *
     * prepend "> " characters to the bodytext to specify we are quoting
     *
     * @param message
     *            A <code>Message</code> which contains the bodytext of the
     *            message we want reply/forward.
     * @param html
     *            True for html messages (a different quoting is necessary)
     *
     * FIXME: we should make this configureable
     *
     */
    public static String createQuotedBodyText(MimePart mimePart, boolean html)
        throws IOException {
        String bodyText = createBodyText(mimePart);

        // Quote according model type (text/html)
        String quotedBodyText;

        if (html) {
            // html - quoting is done by inserting a div around the
            // message formattet with a blue line at left edge
            // TODO: Implement quoting (font color, stylesheet, blockquote???)

            /*
             * String lcase = bodyText.toLowerCase(); StringBuffer buf = new
             * StringBuffer(); String quoteStart = " <blockquote> "; String
             * quoteEnd = " </blockquote> ";
             *
             * int pos = lcase.indexOf(" <body"); pos = lcase.indexOf("> ",
             * pos) + 1; buf.append(bodyText.substring(0, pos));
             * buf.append(quoteStart); int end = lcase.indexOf(" </body");
             * buf.append(bodyText.substring(pos, end)); buf.append(quoteEnd);
             * buf.append(bodyText.substring(end));
             *
             * ColumbaLogger.log.debug("Source:\n" + bodyText);
             * ColumbaLogger.log.debug("Result:\n" + buf.toString());
             *
             * quotedBodyText = buf.toString();
             */
            quotedBodyText = bodyText;
        } else {
            // plain text
            quotedBodyText = BodyTextParser.quote(bodyText);
        }

        return quotedBodyText;
    }

    /**
     * Check if HTML support should be enabled in model.
     *
     * @return true, if enabled. false, otherwise
     */
    public static boolean isHTMLEnabled() {
        // get configuration
        XmlElement optionsElement = MailConfig.get("composer_options")
                                              .getElement("/options");
        XmlElement htmlElement = optionsElement.getElement("html");

        // create html element, if it doesn't exist
        if (htmlElement == null) {
            htmlElement = optionsElement.addSubElement("html");
        }

        // get enable attribute
        String enableHtml = htmlElement.getAttribute("enable", "false");

        return Boolean.valueOf(enableHtml).booleanValue();
    }

    /** ******************** addressbook stuff ********************** */
    /**
     *
     * add automatically every person we'll send a message to the "Collected
     * Addresses" Addressbook
     *
     * TODO: this should be moved outside independent -> should be in core, or
     * even better addressbook
     *
     * TODO: split sender, there can be more than only one recipients!
     *
     */
    public static void addSenderToAddressbook(String sender) {
        if (sender != null) {
            if (sender.length() > 0) {
                org.columba.addressbook.folder.Folder selectedFolder = org.columba.addressbook.facade.FolderFacade.getCollectedAddresses();

                // this can be a list of recipients
                List list = ListParser.parseString(sender);
                Iterator it = list.iterator();

                while (it.hasNext()) {
                    String address = AddressParser.getAddress((String) it.next());
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
