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
package org.columba.mail.gui.tree;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

import javax.swing.JComponent;
import javax.swing.TransferHandler;
import javax.swing.tree.TreePath;

import org.columba.core.command.CommandProcessor;
import org.columba.core.facade.DialogFacade;
import org.columba.mail.command.MailFolderCommandReference;
import org.columba.mail.command.IMailFolderCommandReference;
import org.columba.mail.folder.AbstractFolder;
import org.columba.mail.folder.AbstractMessageFolder;
import org.columba.mail.folder.command.CopyMessageCommand;
import org.columba.mail.folder.command.MoveFolderCommand;
import org.columba.mail.folder.command.MoveMessageCommand;
import org.columba.mail.gui.table.MessageReferencesTransfer;

/**
 * A Transferhandler for the TreeView. This handler will only work with a
 * treeview component. The only type of folders that can be moved in the
 * treeview are <code>VirtualFolder</code> and <code>AbstractLocalFolder</code>
 * <p>
 * The handler supports
 * <ul>
 * <li>Moving a folder into other folders. If the folder is a
 * <code>VirtualFolder</code> or a <code>AbstractLocalFolder</code>.
 * <li>Copying messages into one folder.
 * </ul>
 * 
 * @author redsolo
 */
public class TreeViewTransferHandler extends TransferHandler {
	/** {@inheritDoc} */
	public boolean importData(JComponent comp, Transferable transferProxy) {
		boolean dataWasImported = false;

		if (comp instanceof TreeView) {
			TreeView treeView = (TreeView) comp;

			try {
				DataFlavor[] dataFlavors = transferProxy
						.getTransferDataFlavors();

				for (int i = 0; (i < dataFlavors.length) && (!dataWasImported); i++) {
					if (dataFlavors[i].equals(FolderTransfer.FLAVOR)) {
						dataWasImported = importFolderReferences(treeView,
								(FolderTransfer) transferProxy
										.getTransferData(FolderTransfer.FLAVOR));
					} else if (dataFlavors[i]
							.equals(MessageReferencesTransfer.FLAVOR)) {
						MessageReferencesTransfer messageTransferable = (MessageReferencesTransfer) transferProxy
								.getTransferData(MessageReferencesTransfer.FLAVOR);
						dataWasImported = importMessageReferences(treeView,
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
	 * Try to import the folder references. Current implementation can only MOVE
	 * folders, it cannot copy them to a new destination. The actual MOVE call
	 * is done in the <code>exportDone()</code> method.
	 * <p>
	 * This method returns true if dragged folder and destination folder is not
	 * null.
	 * 
	 * @param treeView
	 *            the tree view to import data into.
	 * @param transferable
	 *            the folder references.
	 * @return true if the folders could be imported; false otherwise.
	 */
	private boolean importFolderReferences(TreeView treeView,
			FolderTransfer transferable) {
		boolean dataWasImported = false;

		AbstractFolder destFolder = treeView.getDropTargetFolder();
		AbstractMessageFolder draggedFolder = transferable.getFolderReference();

		if ((destFolder != null) && (draggedFolder != null)) {
			// We're always doing a MOVE
			// and this is handled in the exportDone method.
			dataWasImported = true;
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
	private boolean importMessageReferences(TreeView treeView,
			MessageReferencesTransfer transferable) {
		boolean dataWasImported = false;

		AbstractMessageFolder destFolder = (AbstractMessageFolder) treeView
				.getDropTargetFolder();

		IMailFolderCommandReference result = transferable.getFolderReferences();
		result.setDestinationFolder(destFolder);

		CopyMessageCommand command = new MoveMessageCommand(result);
		CommandProcessor.getInstance().addOp(command);
		dataWasImported = true;

		return dataWasImported;
	}

	/** {@inheritDoc} */
	protected void exportDone(JComponent source, Transferable data, int action) {
		if (source instanceof TreeView) {
			TreeView treeView = (TreeView) source;

			if (data instanceof FolderTransfer) {
				AbstractMessageFolder draggedFolder = ((FolderTransfer) data)
						.getFolderReference();
				exportFolder(treeView, draggedFolder);
			}
		}
	}

	/**
	 * Export the folder. Since there is only Virtual Folders who can be copied,
	 * then all other actions are MOVE.
	 * 
	 * @param treeView
	 *            the treeview that has dragged folder
	 * @param folder
	 *            the folder to move.
	 */
	private void exportFolder(TreeView treeView, AbstractMessageFolder folder) {
		MailFolderCommandReference commandRef = new MailFolderCommandReference(folder);
		commandRef.setDestinationFolder(treeView.getDropTargetFolder());
		treeView.resetDropTargetFolder();

		CommandProcessor.getInstance().addOp(new MoveFolderCommand(commandRef));
	}

	/** {@inheritDoc} */
	public int getSourceActions(JComponent c) {
		int action = TransferHandler.NONE;

		if (c instanceof TreeView) {
			action = TransferHandler.MOVE;
		}

		return action;
	}

	/** {@inheritDoc} */
	protected Transferable createTransferable(JComponent c) {
		Transferable exportObject = null;

		if (c instanceof TreeView) {
			TreeView treeView = (TreeView) c;
			TreePath path = treeView.getSelectionModel().getSelectionPath();

			AbstractFolder folderNode = (AbstractFolder) path
					.getLastPathComponent();

			if (folderNode.supportsMove()) {
				exportObject = new FolderTransfer((AbstractMessageFolder) folderNode);
			}
		}

		return exportObject;
	}

	/** {@inheritDoc} */
	public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
		boolean canHandleOneOfDataFlavors = false;

		if (comp instanceof TreeView) {
			TreeView treeView = (TreeView) comp;

			AbstractFolder dropTarget = treeView.getDropTargetFolder();

			if (dropTarget != null) {
				for (int k = 0; (k < transferFlavors.length)
						&& (!canHandleOneOfDataFlavors); k++) {
					if (transferFlavors[k]
							.equals(MessageReferencesTransfer.FLAVOR)) {
						canHandleOneOfDataFlavors = canHandleMessageImport(
								treeView, dropTarget);
					} else if (transferFlavors[k].equals(FolderTransfer.FLAVOR)) {
						canHandleOneOfDataFlavors = canHandleFolderImport(
								treeView, dropTarget);
					}
				}
			}
		}

		return canHandleOneOfDataFlavors;
	}

	/**
	 * Returns true if the dragged folder can be imported to the dropped folder.
	 * 
	 * @param treeView
	 *            the treeview containing the drag/drop folders.
	 * @param dropTarget
	 *            the folder node that is intended for the drop action.
	 * @return true if the dragged folder can be imported to the dropped folder.
	 */
	private boolean canHandleFolderImport(TreeView treeView,
			AbstractFolder dropTarget) {
		boolean canImport = false;

		AbstractFolder dragTarget = treeView.getSelectedNodeBeforeDragAction();

		if ((dragTarget != null) && (!dragTarget.isNodeDescendant(dropTarget))
				&& (dragTarget != dropTarget)) {
			canImport = dropTarget.supportsAddFolder(dragTarget.getType());
		}

		return canImport;
	}

	/**
	 * Returns true if the dragged messages can be imported to the dropped
	 * folder.
	 * 
	 * @param treeView
	 *            the treeview containing the drop folder.
	 * @param dropTarget
	 *            the folder node that is intended for the drop action.
	 * @return true if the dragged messages can be imported to the dropped
	 *         folder.
	 */
	private boolean canHandleMessageImport(TreeView treeView,
			AbstractFolder dropTarget) {
		boolean canImport = false;

		AbstractFolder dragTarget = treeView.getSelectedNodeBeforeDragAction();

		if (dragTarget != dropTarget) {
			canImport = dropTarget.supportsAddMessage();
		}

		return canImport;
	}
}