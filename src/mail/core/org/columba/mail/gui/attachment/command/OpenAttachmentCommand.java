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
package org.columba.mail.gui.attachment.command;

import org.columba.core.command.Command;
import org.columba.core.command.DefaultCommandReference;
import org.columba.core.io.TempFileStore;
import org.columba.core.main.MainInterface;

import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.folder.temp.TempFolder;
import org.columba.mail.gui.message.command.ViewMessageCommand;
import org.columba.mail.gui.messageframe.MessageFrameController;
import org.columba.mail.gui.mimetype.MimeTypeViewer;
import org.columba.mail.main.MailInterface;

import org.columba.ristretto.message.LocalMimePart;
import org.columba.ristretto.message.MimeHeader;

import java.io.File;
import java.util.logging.Logger;


/**
 * @author freddy
 */
public class OpenAttachmentCommand extends SaveAttachmentCommand {
    private static final Logger LOG = Logger.getLogger("org.columba.mail.gui.attachment.command");
    //private StreamableMimePart part;
    private LocalMimePart part;
    private File tempFile;

    // true, if showing a message as attachment
    private boolean inline = false;
    private TempFolder tempFolder;
    private Object tempMessageUid;

    /**
     * Constructor for OpenAttachmentCommand.
     * @param references command parameters
     */
    public OpenAttachmentCommand(DefaultCommandReference[] references) {
        super(references);

        priority = Command.REALTIME_PRIORITY;
        commandType = Command.NORMAL_OPERATION;
    }

    /**
     * @see org.columba.core.command.Command#updateGUI()
     */
    public void updateGUI() throws Exception {
        MimeHeader header = part.getHeader();

        if (header.getMimeType().getType().toLowerCase().indexOf("message") != -1) {
            MessageFrameController c = new MessageFrameController();

            FolderCommandReference[] r = new FolderCommandReference[1];
            Object[] uidList = new Object[1];
            uidList[0] = tempMessageUid;

            r[0] = new FolderCommandReference(tempFolder, uidList);

            c.setTreeSelection(r);
            c.setTableSelection(r);

            MainInterface.processor.addOp(new ViewMessageCommand(c, r));

            //inline = true;
            //openInlineMessage(part, tempFile);
        } else {
            //inline = false;
            MimeTypeViewer viewer = new MimeTypeViewer();
            viewer.open(header, tempFile);
        }
    }

    /** {@inheritDoc} */
    protected File getDestinationFile(LocalMimePart mimepart) {
        part = mimepart;
        tempFile = null;

        if (part.getHeader().getMimeType().getType().equals("message")) {

            tempFolder = MailInterface.treeModel.getTempFolder();
            try {
                tempMessageUid = tempFolder.addMessage(part.getInputStream());
            } catch (Exception e) {
                LOG.warning("Could not create temporary email from the attachment.");
            }
            inline = true;

        } else {

            String filename = mimepart.getHeader().getFileName();
            if (filename != null) {
                tempFile = TempFileStore.createTempFile(filename);
            } else {
                tempFile = TempFileStore.createTempFile();
            }
            inline = false;
        }

        return tempFile;
    }
}
