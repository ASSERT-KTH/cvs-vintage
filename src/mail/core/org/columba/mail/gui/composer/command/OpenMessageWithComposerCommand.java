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

package org.columba.mail.gui.composer.command;

import org.columba.core.command.DefaultCommandReference;
import org.columba.core.command.WorkerStatusController;
import org.columba.core.main.MainInterface;
import org.columba.core.xml.XmlElement;

import org.columba.mail.command.FolderCommand;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.composer.MessageBuilderHelper;
import org.columba.mail.config.AccountItem;
import org.columba.mail.folder.MessageFolder;
import org.columba.mail.gui.composer.ComposerController;
import org.columba.mail.gui.composer.ComposerModel;
import org.columba.mail.main.MailInterface;

import org.columba.ristretto.message.BasicHeader;
import org.columba.ristretto.message.Header;
import org.columba.ristretto.message.LocalMimePart;
import org.columba.ristretto.message.Message;
import org.columba.ristretto.message.MimePart;
import org.columba.ristretto.message.MimeTree;
import org.columba.ristretto.message.StreamableMimePart;
import org.columba.ristretto.message.io.Source;
import org.columba.ristretto.message.io.TempSourceFactory;
import org.columba.ristretto.parser.MessageParser;

import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

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
        controller = (ComposerController)
                MainInterface.frameModel.openView("Composer");

        // apply model
        controller.setComposerModel(model);

        // model->view update
        controller.updateComponents(true);
    }

    public void execute(WorkerStatusController worker)
        throws Exception {
        model = new ComposerModel();

        // get selected folder
        MessageFolder folder = (MessageFolder) ((FolderCommandReference) getReferences()[0]).getFolder();

        // get first selected message
        Object[] uids = ((FolderCommandReference) getReferences()[0]).getUids();

        //TODO keep track of progress here

        InputStream messageSourceStream = folder.getMessageSourceStream(uids[0]);
        Source tempSource = TempSourceFactory.createTempSource(messageSourceStream, -1, null);
        messageSourceStream.close();

        Message message = MessageParser.parse(tempSource);

        initHeader(message);

        // select the account this mail was received from
        Integer accountUid = (Integer) folder.getAttribute(uids[0],
                "columba.accountuid");
        AccountItem accountItem = MessageBuilderHelper.getAccountItem(accountUid);
        model.setAccountItem(accountItem);

        XmlElement html = MailInterface.config.getMainFrameOptionsConfig()
                                              .getRoot().getElement("/options/html");

        boolean preferHtml = Boolean.valueOf(html.getAttribute("prefer"))
                                    .booleanValue();

        initBody(message, preferHtml);
    }

    private void initBody(Message message, boolean preferHtml) {
        MimeTree mimeTree = message.getMimePartTree();

        // Which Bodypart shall be shown? (html/plain)
        LocalMimePart bodyPart = null;

        if (preferHtml) {
            bodyPart = (LocalMimePart) mimeTree.getFirstTextPart("html");
        } else {
            bodyPart = (LocalMimePart) mimeTree.getFirstTextPart("plain");
        }

        if (bodyPart != null) {
            if (bodyPart.getHeader().getMimeType().getSubtype().equals("html")) {
                // html
                model.setHtml(true);
            } else {
                model.setHtml(false);
            }

            model.setBodyText(bodyPart.getBody().toString());
        }

        initAttachments(mimeTree, bodyPart);
    }

    private void initHeader(Message message) {
        Header header = message.getHeader();

        BasicHeader rfcHeader = new BasicHeader(header);

        // set subject
        model.setSubject(rfcHeader.getSubject());

        model.setTo(rfcHeader.getTo());

        // copy every headerfield the original message contains
        model.setHeader(header);
    }

    private void initAttachments(MimeTree collection, MimePart bodyPart) {
        // Get all MimeParts
        List displayedMimeParts = collection.getAllLeafs();

        if (bodyPart != null) {
            MimePart bodyParent = bodyPart.getParent();

            if (bodyParent != null) {
                if (bodyParent.getHeader().getMimeType().getSubtype().equals("alternative")) {
                    List bodyParts = bodyParent.getChilds();
                    displayedMimeParts.removeAll(bodyParts);
                } else {
                    displayedMimeParts.remove(bodyPart);
                }
            }

            Iterator it = displayedMimeParts.iterator();

            while (it.hasNext()) {
                model.addMimePart((StreamableMimePart) it.next());
            }
        }
    }
}
