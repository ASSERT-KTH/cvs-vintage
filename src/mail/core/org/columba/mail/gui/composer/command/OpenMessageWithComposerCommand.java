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

import org.columba.ristretto.message.BasicHeader;
import org.columba.ristretto.message.Header;
import org.columba.ristretto.message.LocalMimePart;
import org.columba.ristretto.message.Message;
import org.columba.ristretto.message.io.CharSequenceSource;
import org.columba.ristretto.parser.MessageParser;


/**
 * Open message in composer.
 *
 * @author fdietz
 */
public class OpenMessageWithComposerCommand extends FolderCommand {
    protected ComposerController controller;
    protected ComposerModel model;

    /**
     * Constructor for OpenMessageInComposerCommand.
     *
     * @param frameMediator
     * @param references
     */
    public OpenMessageWithComposerCommand(DefaultCommandReference[] references) {
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
        // get selected folder
        Folder folder = (Folder) ((FolderCommandReference) getReferences()[0]).getFolder();

        // get first selected message
        Object[] uids = ((FolderCommandReference) getReferences()[0]).getUids();

        String source = folder.getMessageSource(uids[0]);

        model = new ComposerModel();

        Message message = MessageParser.parse(new CharSequenceSource(source));
        Header header = message.getHeader();
        BasicHeader basicHeader = new BasicHeader(header);

        // copy every headerfield the original message contains
        model.setHeader(header);

        model.setTo(header.get("To"));

        // try to good guess the correct account
        Integer accountUid = (Integer) folder.getAttribute(uids[0],
                "columba.accountuid");
        String host = (String) folder.getAttribute(uids[0], "columba.host");
        String address = header.get("To");
        AccountItem accountItem = MessageBuilderHelper.getAccountItem(accountUid,
                host, address);
        model.setAccountItem(accountItem);

        model.setSubject(basicHeader.getSubject());

        LocalMimePart bodyPart = (LocalMimePart) message.getMimePartTree()
                                                        .getFirstTextPart("html");

        // No conversion needed - the composer now supports both html and text
        if (bodyPart.getHeader().getMimeType().getSubtype().equals("html")) {
            // html
            model.setHtml(true);
        } else {
            model.setHtml(false);
        }

        //model.setBodyText(bodyPart.getBody().toString());
        model.setBodyText(MessageBuilderHelper.createBodyText(bodyPart));
    }
}
