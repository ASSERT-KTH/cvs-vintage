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
package org.columba.mail.gui.table;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import org.columba.core.command.CompoundCommand;
import org.columba.core.gui.frame.FrameMediator;
import org.columba.core.main.MainInterface;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.folder.command.ExpungeFolderCommand;
import org.columba.mail.folder.command.MarkMessageCommand;
import org.columba.mail.gui.frame.MailFrameMediator;


/**
 * A transfer handler for the TableView control.
 * <p>
 * For now the transfer handler supports only moving or copying messages
 * from this control. ie it can only export messages.
 *
 * @author redsolo
 */
public class TableViewTransferHandler extends TransferHandler {
    private FrameMediator frameController;

    /**
 * Creates a TransferHandle for a table view.
 * @param cont the fram controller, its used to get the selected messages.
 */
    public TableViewTransferHandler(FrameMediator cont) {
        frameController = cont;
    }

    /** {@inheritDoc} */
    public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
        return false;
    }

    /** {@inheritDoc} */
    protected Transferable createTransferable(JComponent c) {
        Transferable transferable = null;

        if (c instanceof TableView) {
            transferable = new MessageReferencesTransfer(((MailFrameMediator)frameController).getTableSelection());
        }

        return transferable;
    }

    /** {@inheritDoc} */
    protected void exportDone(JComponent source, Transferable data, int action) {
        if ((action == TransferHandler.MOVE) &&
                (data instanceof MessageReferencesTransfer) &&
                (source instanceof TableView)) {
            // Remove the moved messages.
            MessageReferencesTransfer messageTransfer = (MessageReferencesTransfer) data;
            FolderCommandReference messageRefs = messageTransfer.getFolderReferences();

            messageRefs.setMarkVariant(MarkMessageCommand.MARK_AS_EXPUNGED);

            MarkMessageCommand markCommand = new MarkMessageCommand(messageRefs);
            ExpungeFolderCommand expungeCommand = new ExpungeFolderCommand(messageRefs);

            CompoundCommand command = new CompoundCommand();
            command.add(markCommand);
            command.add(expungeCommand);
            MainInterface.processor.addOp(command);
        }
    }

    /** {@inheritDoc} */
    public int getSourceActions(JComponent c) {
        int action = TransferHandler.NONE;

        if (c instanceof TableView) {
            action = TransferHandler.COPY_OR_MOVE;
        }

        return action;
    }
}
