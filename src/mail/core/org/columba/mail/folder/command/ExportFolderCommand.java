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
package org.columba.mail.folder.command;

import org.columba.core.command.DefaultCommandReference;
import org.columba.core.command.Worker;
import org.columba.core.gui.frame.FrameMediator;

import org.columba.mail.command.FolderCommand;
import org.columba.mail.command.FolderCommandAdapter;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.folder.Folder;
import org.columba.mail.util.MailResourceLoader;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.text.MessageFormat;

import javax.swing.JOptionPane;


/**
 * Export all selected folders to a single MBOX mailbox file.
 *
 * MBOX mailbox format:
 *  http://www.qmail.org/qmail-manual-html/man5/mbox.html
 *
 * @author fdietz
 */
public class ExportFolderCommand extends FolderCommand {
    protected FolderCommandAdapter adapter;
    protected Object[] destUids;

    /**
     * @param references
     */
    public ExportFolderCommand(DefaultCommandReference[] references) {
        super(references);
    }

    /**
     * @param frame
     * @param references
     */
    public ExportFolderCommand(FrameMediator frame,
        DefaultCommandReference[] references) {
        super(frame, references);
    }

    /* (non-Javadoc)
     * @see org.columba.core.command.Command#execute(org.columba.core.command.Worker)
     */
    public void execute(Worker worker) throws Exception {
        // get references
        FolderCommandReference[] references = (FolderCommandReference[]) getReferences();

        // use wrapper class
        adapter = new FolderCommandAdapter(references);

        // get source references
        FolderCommandReference[] r = adapter.getSourceFolderReferences();

        OutputStream os = null;

        try {
            // create output stream
            os = new BufferedOutputStream(new FileOutputStream(
                        r[0].getDestFile()));

            int counter = 0;
            Folder srcFolder;
            Object[] uids;
            InputStream in;
            int read;
            byte[] buffer = new byte[1024];

            // for each source folder
            for (int i = 0; (i < r.length) && !worker.cancelled(); i++) {
                // get source folder
                srcFolder = (Folder) r[i].getFolder();

                // update status message
                worker.setDisplayText(MessageFormat.format(
                        MailResourceLoader.getString("statusbar", "message",
                            "export_messages"),
                        new Object[] {
                            srcFolder.getName(), Integer.toString(i + 1),
                            Integer.toString(r.length)
                        }));

                // get array of message UIDs
                uids = srcFolder.getUids();

                // initialize progressbar with total number of messages
                worker.setProgressBarMaximum(uids.length);
                worker.setProgressBarValue(0);

                // for each message in folder i
                for (int j = 0; (j < uids.length) && !worker.cancelled();
                        j++) {
                    // get message source from folder
                    in = new BufferedInputStream(srcFolder.getMessageSourceStream(
                                uids[j]));

                    // prepend From line
                    os.write(new String("From \r\n").getBytes());

                    // write message source to file
                    while ((read = in.read(buffer, 0, buffer.length)) > 0) {
                        os.write(buffer, 0, read);
                    }

                    try {
                        in.close();
                    } catch (IOException ioe_) {
                    }

                    // append newline
                    os.write(new String("\r\n").getBytes());

                    os.flush();

                    worker.setProgressBarValue(j);
                    counter++;
                }
            }

            // update status message
            if (worker.cancelled()) {
                worker.setDisplayText(MailResourceLoader.getString(
                        "statusbar", "message", "export_messages_cancelled"));
            } else {
                worker.setDisplayText(MessageFormat.format(
                        MailResourceLoader.getString("statusbar", "message",
                            "export_messages_success"),
                        new Object[] { Integer.toString(counter) }));
            }
        } catch (IOException ioe) {
            JOptionPane.showMessageDialog(null,
                MailResourceLoader.getString("statusbar", "message",
                    "err_export_messages_msg"),
                MailResourceLoader.getString("statusbar", "messages",
                    "err_export_messages_title"), JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                //close output stream
                if (os != null) {
                    os.close();
                }
            } catch (IOException ioe) {
            }
        }
    }
}
