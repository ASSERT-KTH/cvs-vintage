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
package org.columba.mail.gui.table.action;

import java.awt.event.ActionEvent;

import org.columba.core.action.JAbstractAction;
import org.columba.core.main.MainInterface;
import org.columba.mail.command.FolderCommand;
import org.columba.mail.command.FolderCommandAdapter;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.folder.command.CopyMessageCommand;
import org.columba.mail.folder.command.MoveMessageCommand;
import org.columba.mail.gui.frame.AbstractMailFrameController;
import org.columba.mail.gui.frame.TableOwner;
import org.columba.mail.gui.table.TableController;

/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class PasteAction extends JAbstractAction {

	TableController tableController;
	AbstractMailFrameController frameController;

	public PasteAction(AbstractMailFrameController frameController) {
		super();
		this.tableController =
			((TableOwner) frameController).getTableController();
		this.frameController = frameController;

	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent arg0) {	

		FolderCommandReference[] ref = new FolderCommandReference[2];

		FolderCommandReference[] source =
			MainInterface.clipboardManager.getMessageSelection();
		if (source == null)
			return;

		ref[0] = source[0];

		FolderCommandReference[] dest =
			(FolderCommandReference[]) frameController.getTableSelection();

		FolderCommandAdapter adapter = new FolderCommandAdapter(dest);

		ref[1] = adapter.getSourceFolderReferences()[0];

		FolderCommand c = null;

		if (MainInterface.clipboardManager.isCutAction())
			c = new MoveMessageCommand(ref);
		else
			c = new CopyMessageCommand(ref);
			
		if (MainInterface.clipboardManager.isCutAction())
			MainInterface.clipboardManager.clearMessageSelection();

		MainInterface.processor.addOp(c);

	}

}
