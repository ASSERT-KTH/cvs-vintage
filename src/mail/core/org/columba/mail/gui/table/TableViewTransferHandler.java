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

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.InputEvent;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import org.columba.api.gui.frame.IFrameMediator;
import org.columba.core.command.CommandProcessor;
import org.columba.core.facade.DialogFacade;
import org.columba.mail.command.IMailFolderCommandReference;
import org.columba.mail.folder.IMailbox;
import org.columba.mail.folder.command.CopyMessageCommand;
import org.columba.mail.folder.command.MoveMessageCommand;
import org.columba.mail.gui.frame.MailFrameMediator;

/**
 * A transfer handler for the TableView control.
 * <p>
 * For now the transfer handler supports only moving or copying messages from
 * this control. ie it can only export messages.
 * 
 * @author redsolo
 */
public class TableViewTransferHandler extends TransferHandler {
	private IFrameMediator frameController;

	/** JDK 1.4+ logging framework logger, used for logging. */
	private static final Logger LOG = Logger
			.getLogger("org.columba.mail.gui.table");

	/**
	 * true, if operation is a drag'n'drop operation
	 */
	private boolean isDragOperation;

	/**
	 * true, if operation is a cut/copy/paste accelerator key operation using
	 * the clipboard
	 */
	private boolean isClipboardOperation;

	/**
	 * TransferHandler action
	 */
	private int action;

	/**
	 * Creates a TransferHandle for a table view.
	 * 
	 * @param cont
	 *            the fram controller, its used to get the selected messages.
	 */
	public TableViewTransferHandler(IFrameMediator cont) {
		frameController = cont;

		isDragOperation = false;
		isClipboardOperation = false;

	}

	/** {@inheritDoc} */
	public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
		return false;
	}

	/** {@inheritDoc} */
	protected Transferable createTransferable(JComponent c) {
		MessageReferencesTransfer transferable = null;

		if (c instanceof TableView) {
			transferable = new MessageReferencesTransfer(
					((MailFrameMediator) frameController).getTableSelection());

			transferable.setClipboardOperation(isClipboardOperation);
			transferable.setDragOperation(isDragOperation);
			transferable.setAction(action);
		}

		return transferable;
	}

	/** {@inheritDoc} */
	protected void exportDone(JComponent source, Transferable data, int action) {

		/*
		 * if (data instanceof MessageReferencesTransfer) {
		 * MessageReferencesTransfer messageTransfer =
		 * (MessageReferencesTransfer) data; messageTransfer.setAction(action); }
		 */

//		if (data instanceof MessageReferencesTransfer) {
//
//			MessageReferencesTransfer messageTransfer = (MessageReferencesTransfer) data;
//
//		}

		/*
		 * if ((action == TransferHandler.MOVE) && (data instanceof
		 * MessageReferencesTransfer) && (source instanceof TableView)) { //
		 * Remove the moved messages. MessageReferencesTransfer messageTransfer =
		 * (MessageReferencesTransfer) data; IMailFolderCommandReference
		 * messageRefs = messageTransfer .getFolderReferences();
		 * 
		 * messageRefs.setMarkVariant(MarkMessageCommand.MARK_AS_EXPUNGED);
		 * 
		 * MarkMessageCommand markCommand = new MarkMessageCommand(messageRefs);
		 * ExpungeFolderCommand expungeCommand = new ExpungeFolderCommand(
		 * messageRefs);
		 * 
		 * CompoundCommand command = new CompoundCommand();
		 * command.add(markCommand); command.add(expungeCommand);
		 * CommandProcessor.getInstance().addOp(command); }
		 */

	}

	/** {@inheritDoc} */
	public int getSourceActions(JComponent c) {
		int action = TransferHandler.NONE;

		if (c instanceof TableView) {
			action = TransferHandler.COPY_OR_MOVE;
		}

		return action;
	}

	/** {@inheritDoc} */
	public boolean importData(JComponent source, Transferable transferProxy) {
		boolean dataWasImported = false;

		if (source instanceof TableView) {
			TableView tableView = (TableView) source;

			try {
				DataFlavor[] dataFlavors = transferProxy
						.getTransferDataFlavors();

				for (int i = 0; (i < dataFlavors.length) && (!dataWasImported); i++) {
					if (dataFlavors[i].equals(MessageReferencesTransfer.FLAVOR)) {
						MessageReferencesTransfer messageTransferable = (MessageReferencesTransfer) transferProxy
								.getTransferData(MessageReferencesTransfer.FLAVOR);
						dataWasImported = importMessageReferences(tableView,
								messageTransferable);
					}
				}
			} catch (Exception e) { // UnsupportedFlavorException, IOException
				DialogFacade.showExceptionDialog(e);
			}
		}

		return dataWasImported;
	}

	/**
	 * Try to import the message references. This method copies the messages to
	 * the new folder. Note that it will not delete them, since this is done by
	 * the transferhandler that initiated the drag.
	 * 
	 * @param treeView
	 *            the tree view to import data into.
	 * @param transferable
	 *            the message references.
	 * @return true if the messages could be imported; false otherwise.
	 */
	private boolean importMessageReferences(TableView tableView,
			MessageReferencesTransfer transferable) {
		boolean dataWasImported = false;

		/*
		 * TreeController treeController = (TreeController) ((TreeViewOwner)
		 * frameController) .getTreeController();
		 * 
		 * TreeView treeView = treeController.getView();
		 */
		IMailbox destFolder = (IMailbox) ((MailFrameMediator) frameController)
				.getTableSelection().getSourceFolder();
		/*
		 * AbstractMessageFolder destFolder = (AbstractMessageFolder) treeView
		 * .getDropTargetFolder();
		 */

		IMailFolderCommandReference result = transferable.getFolderReferences();
		result.setDestinationFolder(destFolder);

		if (transferable.getAction() == TransferHandler.MOVE) {
			// move
			MoveMessageCommand command = new MoveMessageCommand(result);
			CommandProcessor.getInstance().addOp(command);
		} else {
			// copy
			CopyMessageCommand command = new CopyMessageCommand(result);
			CommandProcessor.getInstance().addOp(command);
		}
		dataWasImported = true;

		return dataWasImported;
	}

	/**
	 * Called when user starts a drag'n'drop operation using the mouse only.
	 * 
	 * @see javax.swing.TransferHandler#exportAsDrag(javax.swing.JComponent,
	 *      java.awt.event.InputEvent, int)
	 */
	public void exportAsDrag(JComponent comp, InputEvent e, int action) {
		this.isDragOperation = true;
		this.action = action;

		super.exportAsDrag(comp, e, action);
	}

	/**
	 * Called when the user calls cut/copy shortcuts to export the selected data
	 * into the clipboard.
	 * 
	 * @see javax.swing.TransferHandler#exportToClipboard(javax.swing.JComponent,
	 *      java.awt.datatransfer.Clipboard, int)
	 */
	public void exportToClipboard(JComponent comp, Clipboard clip, int action)
			throws IllegalStateException {

		this.isClipboardOperation = true;
		this.action = action;

		if (action == TransferHandler.MOVE) {
			LOG
					.info("Selected messages will be moved, when selecting \"Paste\"");
			frameController
					.fireStatusMessageChanged("Selected messages will be moved, when selecting \"Paste\"");
		} else if (action == TransferHandler.COPY) {
			LOG
					.info("Selected messages will be copied, when selecting \"Paste\"");
			frameController
					.fireStatusMessageChanged("Selected messages will be copied, when selecting \"Paste\"");
		}

		super.exportToClipboard(comp, clip, action);
	}
}