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
import org.columba.ristretto.message.Address;
import org.columba.ristretto.message.BasicHeader;
import org.columba.ristretto.message.Header;
import org.columba.ristretto.message.InputStreamMimePart;
import org.columba.ristretto.message.MimeHeader;
import org.columba.ristretto.message.MimeType;

/**
 * Reply to message, while keeping the original message as attachment. In
 * comparison to quoting the bodytext inline.
 * 
 * @author fdietz
 */
public class ReplyAsAttachmentCommand extends FolderCommand {
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
        
        // initialize MimeHeader as RFC822-compliant-message
        MimeHeader mimeHeader = new MimeHeader();
        mimeHeader.setMimeType(new MimeType("message", "rfc822"));

        // add mimepart to model
        model.addMimePart(new InputStreamMimePart(mimeHeader, folder.getMessageSourceStream(uids[0])));
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
