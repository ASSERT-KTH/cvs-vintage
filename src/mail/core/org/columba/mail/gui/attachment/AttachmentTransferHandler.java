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
package org.columba.mail.gui.attachment;

import java.awt.datatransfer.DataFlavor;

import javax.swing.JComponent;
import javax.swing.TransferHandler;
import org.frappucino.swing.DynamicFileTransferHandler;
import org.frappucino.swing.DynamicFileFactory;

/**
 * Transfer handler for the attachment view.
 *
 * The Sun DnD integration with the the native platform, requires that the file
 * is already exists on the disk, when a File DnD is issued. This TransferHandler
 * will create the files locally when the
 * @linkplain java.awt.datatransfer.Transferable#getTransferData(java.awt.datatransfer.DataFlavor)
 * method is called. That method is the last method called before the DnD has completed.
 * The actual extraction is done through the SaveAttachmentTemporaryCommand, and there
 * might be problems waiting for other commands to finish before it. The method does not
 * complete until the file has been created, ie locks up the DnD action.
 * <p>
 * @author redsolo
 * @see org.columba.mail.gui.attachment.command.SaveAttachmentTemporaryCommand
 * @see org.frappucino.swing.DynamicFileTransferHandler
 */
public class AttachmentTransferHandler extends DynamicFileTransferHandler {

    /**
     * Setup the dynamic transfer handler.
     * @param factory the factory that creates the file for the DnD action.
     */
    public AttachmentTransferHandler(DynamicFileFactory factory) {
        super(factory, DynamicFileTransferHandler.LATE_GENERATION);
    }

    /**
     * Returns the COPY action.
     * @param c ignored.
     * @return the @link TransferHandler#COPY COPY action.
     */
    public int getSourceActions(JComponent c) {
        return TransferHandler.COPY;
    }

    /**
     * Returns always false.
     * The attachment transfer handler can only export data flavors, and not
     * import them.
     * @param comp ignored.
     * @param transferFlavors ignored.
     * @return false.
     */
    public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
        return false;
    }
}
