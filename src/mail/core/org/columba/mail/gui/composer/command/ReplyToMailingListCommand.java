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

import org.columba.core.command.DefaultCommandReference;
import org.columba.core.command.Worker;
import org.columba.core.io.StreamUtils;
import org.columba.core.xml.XmlElement;
import org.columba.mail.command.FolderCommand;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.composer.MessageBuilderHelper;
import org.columba.mail.config.AccountItem;
import org.columba.mail.main.MailInterface;
import org.columba.mail.folder.Folder;
import org.columba.mail.gui.composer.ComposerController;
import org.columba.mail.gui.composer.ComposerModel;
import org.columba.mail.gui.composer.util.QuoteFilterInputStream;
import org.columba.ristretto.message.Address;
import org.columba.ristretto.message.BasicHeader;
import org.columba.ristretto.message.Header;
import org.columba.ristretto.message.MimeHeader;
import org.columba.ristretto.message.MimePart;
import org.columba.ristretto.message.MimeTree;

/**
 * Reply to mailinglist.
 * <p>
 * Uses the X-BeenThere: headerfield to determine the To: address
 *
 * @author fdizet
 */
public class ReplyToMailingListCommand extends FolderCommand {
    private static final String[] headerfields =
    new String[] {
			   "Subject", "From", "To", "Reply-To", "Message-ID",
			   "In-Reply-To", "References", "X-Beenthere", "X-BeenThere"
    };
    
    
    protected ComposerController controller;
    protected ComposerModel model;

    /**
     * Constructor for ReplyToMailingListCommand.
     *
     * @param frameMediator
     * @param references
     */
    public ReplyToMailingListCommand(DefaultCommandReference[] references) {
        super(references);
    }

    public void updateGUI() throws Exception {
        // open composer frame
        controller = new ComposerController();
        controller.openView();

        // apply model
        controller.setComposerModel(model);

        // model->view update
        controller.updateComponents(true);
    }
    
    
    public void execute(Worker worker) throws Exception {
        // create composer model
        model = new ComposerModel();

        // get selected folder
        Folder folder =
        (Folder) ((FolderCommandReference) getReferences()[0]).getFolder();

        // get first selected message
        Object[] uids = ((FolderCommandReference) getReferences()[0]).getUids();

        // setup to, references and account
        initHeader(folder, uids);

        // get mimeparts
        MimeTree mimePartTree = folder.getMimePartTree(uids[0]);

        XmlElement html =
        MailInterface.config.getMainFrameOptionsConfig().getRoot().getElement(
        "/options/html");

        // Which Bodypart shall be shown? (html/plain)
        MimePart bodyPart = null;

        if (Boolean.valueOf(html.getAttribute("prefer")).booleanValue()) {
            bodyPart = mimePartTree.getFirstTextPart("html");
        } else {
            bodyPart = mimePartTree.getFirstTextPart("plain");
        }

        if (bodyPart != null) {
            // setup charset and html
            initMimeHeader(bodyPart);

            StringBuffer bodyText;
            Integer[] address = bodyPart.getAddress();

            String quotedBodyText = createQuotedBody(folder, uids, address);

            model.setBodyText(quotedBodyText);
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

    private void initHeader(Folder folder, Object[] uids) throws Exception {
        // get headerfields
        Header header = folder.getHeaderFields(uids[0], headerfields);

        BasicHeader rfcHeader = new BasicHeader(header);
        // set subject
        model.setSubject(
                MessageBuilderHelper.createReplySubject(rfcHeader.getSubject()));

        // Use reply-to field if given, else use from
        Address to = rfcHeader.getBeenThere();
        if( to == null ) {
            Address[] replyTo = rfcHeader.getReplyTo();
            if( replyTo.length > 0)
                to = replyTo[0];
        }
        
        if( to == null ) {
            to = rfcHeader.getFrom();
        }
        
        MessageBuilderHelper.addAddressesToAddressbook(new Address[] { to });
        model.setTo(new Address[] { to });
        
        // create In-Reply-To:, References: headerfields
        MessageBuilderHelper.createMailingListHeaderItems(header, model);

        // select the account this mail was received from
        Integer accountUid =
        (Integer) folder.getAttribute(uids[0], "columba.accountuid");
        AccountItem accountItem =
        MessageBuilderHelper.getAccountItem(accountUid);
        model.setAccountItem(accountItem);
    }
    
    private String createQuotedBody(
            Folder folder,
			Object[] uids,
			Integer[] address)
    throws IOException, Exception {

        InputStream  bodyStream = folder.getMimePartBodyStream(uids[0], address);
        /*
         * original message is sent "inline" - model is setup according to the
         * type of the original message. NB: If the original message was plain
         * text, the message type seen here is always text. If the original
         * message contained html, the message type seen here will depend on
         * the "prefer html" option.
         */
        if( model.isHtml() ) {
            // TODO Quote with HTML
            return StreamUtils.readInString(bodyStream).toString();                        
        } else {
            return StreamUtils.readInString(new QuoteFilterInputStream(bodyStream)).toString();            
        }
    }
    
}
