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

import org.columba.core.command.CommandCancelledException;
import org.columba.core.command.DefaultCommandReference;
import org.columba.core.command.Worker;
import org.columba.core.gui.frame.FrameMediator;
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
import org.columba.mail.gui.config.template.ChooseTemplateDialog;
import org.columba.mail.main.MailInterface;
import org.columba.mail.message.HeaderList;
import org.columba.ristretto.message.Address;
import org.columba.ristretto.message.BasicHeader;
import org.columba.ristretto.message.Header;
import org.columba.ristretto.message.MimeHeader;
import org.columba.ristretto.message.MimePart;
import org.columba.ristretto.message.MimeTree;

/**
 * Opens a dialog to ask the user which template to use
 * 
 * @author fdietz
 */
public class ReplyWithTemplateCommand extends FolderCommand {
    private static final String[] headerfields =
        new String[] {
            "Subject",
            "From",
            "To",
            "Reply-To",
            "Message-ID",
            "In-Reply-To",
            "References" };
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

            // get answer from template
            String templateBody = getTemplateBody();

            model.setBodyText(quotedBodyText + templateBody);
        } else {
            model.setBodyText(getTemplateBody());
        }
    }

    private String getTemplateBody()
        throws Exception, CommandCancelledException, IOException {
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
            throw new CommandCancelledException();
        }

        // get bodytext of template message
        MimeTree tree = templateFolder.getMimePartTree(uid);

        // *20030926, karlpeder* Added html support
        //MimePart mp = tree.getFirstTextPart("plain");
        MimePart mp;

        if (model.isHtml()) {
            mp = tree.getFirstTextPart("html");
        } else {
            mp = tree.getFirstTextPart("text");
        }

        InputStream bodyStream =
            templateFolder.getMimePartBodyStream(uid, mp.getAddress());

        return StreamUtils.readInString(bodyStream).toString();
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
            model.setCharsetName(charset);
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
        Address[] to = rfcHeader.getReplyTo();
        if (to.length == 0) {
            to = new Address[] { rfcHeader.getFrom()};
        }

        // Add addresses to the addressbook
        MessageBuilderHelper.addAddressesToAddressbook(to);
        model.setTo(to);

        // create In-Reply-To:, References: headerfields
        MessageBuilderHelper.createMailingListHeaderItems(header, model);

        // select the account this mail was received from
        Integer accountUid =
            (Integer) folder.getAttribute(uids[0], "columba.accountuid");
        AccountItem accountItem =
            MessageBuilderHelper.getAccountItem(accountUid);
        model.setAccountItem(accountItem);
    }
}
