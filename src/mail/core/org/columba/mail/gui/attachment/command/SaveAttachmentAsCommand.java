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

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.columba.core.command.DefaultCommandReference;
import org.columba.core.util.cFileChooser;
import org.columba.core.util.cFileFilter;
import org.columba.ristretto.message.MimeHeader;


/**
 * Save attachment command that asks the user where to save the attachment to.
 * @author freddy
 */
public class SaveAttachmentAsCommand extends SaveAttachmentCommand {
    private static File lastDir = null;

    /**
     * Constructor for SaveAttachmentCommand.
     *
     * @param references command references
     */
    public SaveAttachmentAsCommand(DefaultCommandReference reference) {
        super(reference);
    }

    /** {@inheritDoc} */
    protected File getDestinationFile(MimeHeader header) {
        cFileChooser fileChooser;

        if (lastDir == null) {
            fileChooser = new cFileChooser();
        } else {
            fileChooser = new cFileChooser(lastDir);
        }

        cFileFilter fileFilter = new cFileFilter();
        fileFilter.acceptFilesWithProperty(cFileFilter.FILEPROPERTY_FILE);

        fileChooser.setDialogTitle("Save Attachment as ...");

        String fileName = getFilename(header);
        if (fileName != null) {
            fileChooser.forceSelectedFile(new File(fileName));
        }

        fileChooser.setSelectFilter(fileFilter);
        File tempFile = null;

        while (true) {
            if (fileChooser.showSaveDialog(null) != JFileChooser.APPROVE_OPTION) {
                return null;
            }

            tempFile = fileChooser.getSelectedFile();
            lastDir = tempFile.getParentFile();

            if (tempFile.exists()) {
                if (JOptionPane.showConfirmDialog(null, "Overwrite File?",
                            "Warning", JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
                    break;
                }
            } else {
                break;
            }
        }
        return tempFile;
    }
}
