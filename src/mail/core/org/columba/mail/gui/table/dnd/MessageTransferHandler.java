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
package org.columba.mail.gui.table.dnd;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.TransferHandler;

import org.columba.core.main.MainInterface;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.folder.MessageFolder;
import org.columba.mail.folder.command.CopyMessageCommand;
import org.columba.mail.gui.frame.MailFrameMediator;
import org.columba.mail.gui.table.TableController;

/**
 * @author frd
 * 
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class MessageTransferHandler extends TransferHandler {
	JTable source = null;

	DataFlavor localFolderCommandReferenceFlavor;

	DataFlavor serialFolderCommandReferenceFlavor;

	String localFolderCommandReferenceType = DataFlavor.javaJVMLocalObjectMimeType
			+ ";class=org.columba.mail.command.FolderCommandReference";

	int[] indices = null;

	TableController tableController;

	public MessageTransferHandler(TableController tableController) {
		this.tableController = tableController;

		try {
			localFolderCommandReferenceFlavor = new DataFlavor(
					localFolderCommandReferenceType);
		} catch (ClassNotFoundException e) {
			System.out
					.println("ArrayListTransferHandler: unable to create data flavor");
		}

		serialFolderCommandReferenceFlavor = new DataFlavor(
				FolderCommandReference[].class, "FolderCommandReference");
	}

	public boolean importData(JComponent c, Transferable t) {
		JTree target = null;

		FolderCommandReference reference = null;

		if (!canImport(c, t.getTransferDataFlavors())) {
			return false;
		}

		try {
			target = (JTree) c;

			if (hasLocalArrayListFlavor(t.getTransferDataFlavors())) {
				reference = (FolderCommandReference) t
						.getTransferData(localFolderCommandReferenceFlavor);
			} else if (hasSerialArrayListFlavor(t.getTransferDataFlavors())) {
				reference = (FolderCommandReference) t
						.getTransferData(serialFolderCommandReferenceFlavor);
			} else {
				return false;
			}
		} catch (UnsupportedFlavorException ufe) {
			System.out.println("importData: unsupported data flavor");

			return false;
		} catch (IOException ioe) {
			System.out.println("importData: I/O exception");

			return false;
		}

		/*
		 * if (source.equals(target)) return true;
		 */

		// do the work here
		MessageFolder destFolder = (MessageFolder) target.getSelectionPath()
				.getLastPathComponent();
		reference.setDestinationFolder(destFolder);

		CopyMessageCommand command = new CopyMessageCommand(reference);

		MainInterface.processor.addOp(command);

		return true;
	}

	private boolean hasLocalArrayListFlavor(DataFlavor[] flavors) {
		if (localFolderCommandReferenceFlavor == null) {
			return false;
		}

		for (int i = 0; i < flavors.length; i++) {
			if (flavors[i].equals(localFolderCommandReferenceFlavor)) {
				return true;
			}
		}

		return false;
	}

	private boolean hasSerialArrayListFlavor(DataFlavor[] flavors) {
		if (serialFolderCommandReferenceFlavor == null) {
			return false;
		}

		for (int i = 0; i < flavors.length; i++) {
			if (flavors[i].equals(serialFolderCommandReferenceFlavor)) {
				return true;
			}
		}

		return false;
	}

	public boolean canImport(JComponent c, DataFlavor[] flavors) {
		if (hasLocalArrayListFlavor(flavors)) {
			return true;
		}

		if (hasSerialArrayListFlavor(flavors)) {
			return true;
		}

		return false;
	}

	protected Transferable createTransferable(JComponent c) {
		if (c instanceof JTable) {
			source = (JTable) c;

			FolderCommandReference r = ((MailFrameMediator) tableController
					.getFrameController()).getTableSelection();

			return new FolderCommandReferenceTransferable(r);
		}

		return null;
	}

	public int getSourceActions(JComponent c) {
		return COPY_OR_MOVE;
	}

	public class FolderCommandReferenceTransferable implements Transferable {
		FolderCommandReference data;

		public FolderCommandReferenceTransferable(FolderCommandReference c) {
			data = c;
		}

		public Object getTransferData(DataFlavor flavor)
				throws UnsupportedFlavorException {
			if (!isDataFlavorSupported(flavor)) {
				throw new UnsupportedFlavorException(flavor);
			}

			return data;
		}

		public DataFlavor[] getTransferDataFlavors() {
			return new DataFlavor[] { localFolderCommandReferenceFlavor,
					serialFolderCommandReferenceFlavor };
		}

		public boolean isDataFlavorSupported(DataFlavor flavor) {
			if (localFolderCommandReferenceFlavor.equals(flavor)) {
				return true;
			}

			if (serialFolderCommandReferenceFlavor.equals(flavor)) {
				return true;
			}

			return false;
		}
	}
}