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
package org.columba.mail.gui.tree.util;
/**
 * @version 	1.0
 * @author
 */

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

import javax.swing.tree.TreePath;

/**
* This represents a TreePath (a node in a JTree) that can be transferred between a drag source and a drop target.
*/
public class CTransferableTreePath implements Transferable {
	// The type of DnD object being dragged...
	public static final DataFlavor TREEPATH_FLAVOR =
		new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType, "TreePath");

	private TreePath _path;

	private DataFlavor[] _flavors = { TREEPATH_FLAVOR };

	/**
	* Constructs a transferrable tree path object for the specified path.
	*/
	public CTransferableTreePath(TreePath path) {
		_path = path;
	}

	// Transferable interface methods...
	public DataFlavor[] getTransferDataFlavors() {
		return _flavors;
	}

	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return java.util.Arrays.asList(_flavors).contains(flavor);
	}

	public synchronized Object getTransferData(DataFlavor flavor)
		throws UnsupportedFlavorException {
		if (flavor.isMimeTypeEqual(TREEPATH_FLAVOR.getMimeType()))
			// DataFlavor.javaJVMLocalObjectMimeType))
			return _path;
		else
			throw new UnsupportedFlavorException(flavor);
	}

}
