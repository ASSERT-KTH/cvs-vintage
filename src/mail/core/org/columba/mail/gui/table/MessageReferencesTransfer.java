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
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import org.columba.mail.command.IMailFolderCommandReference;

/**
 * A Transferable for moving message references.
 * 
 * @author redsolo
 */
public class MessageReferencesTransfer implements Transferable {
	/** The only <code>DataFlavor</code> that this transfer allows. */
	public static DataFlavor FLAVOR;

	static {
		try {
			FLAVOR = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + "-"
					+ MessageReferencesTransfer.class.getName());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private IMailFolderCommandReference reference;
	
	private int action;
	
	/**
	 * true, if operation is a drag'n'drop operation
	 */
	private boolean isDragOperation;
	
	/**
	 * true, if operation is a cut/copy/paste accelerator key operation
	 * using the clipboard
	 */
	private boolean isClipboardOperation;

	/**
	 * Creates a message transferable
	 * 
	 * @param ref
	 *            message references.
	 */
	public MessageReferencesTransfer(IMailFolderCommandReference ref) {
		super();
		reference = ref;
		
		isDragOperation = false;
		isClipboardOperation = false;
	}

	
	/**
	 * @return Returns the action.
	 */
	public int getAction() {
		return action;
	}
	
	/**
	 * @param action The action to set.
	 */
	public void setAction(int action) {
		this.action = action;
	}
	/**
	 * Returns the message references for this transfer.
	 * 
	 * @return the message references for this transfer.
	 */
	public IMailFolderCommandReference getFolderReferences() {
		return reference;
	}

	/** {@inheritDoc} */
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] { FLAVOR };
	}

	/** {@inheritDoc} */
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return FLAVOR.equals(flavor);
	}

	/** {@inheritDoc} */
	public Object getTransferData(DataFlavor flavor)
			throws UnsupportedFlavorException, IOException {
		if (!isDataFlavorSupported(flavor)) {
			throw new UnsupportedFlavorException(flavor);
		}

		return this;
	}
	/**
	 * @return Returns the isClipboardOperation.
	 */
	public boolean isClipboardOperation() {
		return isClipboardOperation;
	}
	/**
	 * @param isClipboardOperation The isClipboardOperation to set.
	 */
	public void setClipboardOperation(boolean isClipboardOperation) {
		this.isClipboardOperation = isClipboardOperation;
	}
	/**
	 * @return Returns the isDragOperation.
	 */
	public boolean isDragOperation() {
		return isDragOperation;
	}
	/**
	 * @param isDragOperation The isDragOperation to set.
	 */
	public void setDragOperation(boolean isDragOperation) {
		this.isDragOperation = isDragOperation;
	}
}