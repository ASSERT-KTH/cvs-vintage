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
import org.columba.core.command.StatusObservableImpl;
import org.columba.core.command.WorkerStatusController;

import org.columba.mail.command.FolderCommand;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.folder.MessageFolder;
import org.columba.mail.util.MailResourceLoader;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;


/**
 * Defines command for saving message source to file
 *
 * @author Karl Peder Olesen (karlpeder), 20030615
 *
 */
public class SaveMessageSourceAsCommand extends FolderCommand {

    /** JDK 1.4+ logging framework logger, used for logging. */
    private static final Logger LOG = Logger.getLogger("org.columba.mail.folder.command");

    /**
     * Constructor for SaveMessageSourceAsCommand.
     * @param frameMediator
     * @param references
     */
    public SaveMessageSourceAsCommand(DefaultCommandReference[] references) {
        super(references);
    }

    /**
     * Implementation-specific: This method does anything
     * @see org.columba.core.command.Command#updateGUI()
     */
    public void updateGUI() throws Exception {
    }

    /**
     * Executes the command, i.e. saves source for selected
     * messages.
     * @see org.columba.core.command.Command#execute(Worker)
     */
    public void execute(WorkerStatusController worker)
        throws Exception {
        FolderCommandReference[] r = (FolderCommandReference[]) getReferences();
        Object[] uids = r[0].getUids(); // uid for messages to save
        MessageFolder srcFolder = (MessageFolder) r[0].getFolder();

        //	register for status events
        ((StatusObservableImpl) srcFolder.getObservable()).setWorker(worker);

        JFileChooser fileChooser = new JFileChooser();

        // Save message source for each selected message
        for (int j = 0; j < uids.length; j++) {
            Object uid = uids[j];
            LOG.info("Saving UID=" + uid);

            //setup save dialog
            String subject = (String) srcFolder.getMessageHeader(uid).get("Subject");
            String defaultName = getValidFilename(subject, false);

            if (defaultName.length() == 0) {
                //TODO: use org.columba.ristretto.message.Address
                // to parse "From" headerfield and use getDisplayName()
                // as defaultName
            }

            fileChooser.setSelectedFile(new File(defaultName));
            fileChooser.setDialogTitle(MailResourceLoader.getString("dialog",
                    "saveas", "save_msg_source"));

            // show dialog
            if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                File f = fileChooser.getSelectedFile();

                if (f.exists()) {
                    // file exists, user needs to confirm overwrite
                    int confirm = JOptionPane.showConfirmDialog(null,
                            MailResourceLoader.getString("dialog", "saveas",
                                "overwrite_existing_file"),
                            MailResourceLoader.getString("dialog", "saveas",
                                "file_exists"), JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE);

                    if (confirm == JOptionPane.NO_OPTION) {
                        j--;

                        continue;
                    }
                }

                InputStream in = null;
                OutputStream out = null;

                try {
                    // save message source under selected filename
                    in = new BufferedInputStream(srcFolder.getMessageSourceStream(uid));
                    out = new BufferedOutputStream(new FileOutputStream(f));

                    byte[] buffer = new byte[1024];
                    int read;

                    while ((read = in.read(buffer, 0, buffer.length)) > 0) {
                        out.write(buffer, 0, read);
                    }
                } catch (IOException ioe) {
                    LOG.severe("Error saving msg source to file: " + ioe.getMessage());
                    JOptionPane.showMessageDialog(null,
                        MailResourceLoader.getString("dialog", "saveas",
                            "err_save_msg"),
                        MailResourceLoader.getString("dialog", "saveas",
                            "err_save_title"), JOptionPane.ERROR_MESSAGE);
                } finally {
                    try {
                        in.close();
                    } catch (IOException ioe) {
                    }

                    try {
                        if (out != null) {
                            out.close();
                        }
                    } catch (IOException ioe) {
                    }
                }
            }
        }

        // end of loop over selected messages
    }

    /**
     * Private utility to extract a valid filename from a message
     * subject or another string.<br>
     * This means remove the chars: / \ : , \n \t
     * NB: If the input string is null, an empty string is returned
     * @param        subj                Message subject
     * @param        replSpaces        If true, spaces are replaced by _
     * @return        A valid filename without the chars mentioned
     */
    private String getValidFilename(String subj, boolean replSpaces) {
        if (subj == null) {
            return "";
        }

        StringBuffer buf = new StringBuffer();

        for (int i = 0; i < subj.length(); i++) {
            char c = subj.charAt(i);

            if ((c == '\\') || (c == '/') || (c == ':') || (c == ',')
                    || (c == '\n') || (c == '\t') || (c == '[') || (c == ']')) {
                // dismiss char
            } else if ((c == ' ') && (replSpaces)) {
                buf.append('_');
            } else {
                buf.append(c);
            }
        }

        return buf.toString();
    }
}
