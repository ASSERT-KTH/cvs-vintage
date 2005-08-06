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

import org.columba.core.action.AbstractColumbaAction;
import org.columba.core.command.Command;
import org.columba.core.command.CommandProcessor;
import org.columba.core.gui.ClipboardManager;
import org.columba.core.gui.frame.IFrameMediator;
import org.columba.mail.command.MailFolderCommandReference;
import org.columba.mail.folder.command.CopyMessageCommand;
import org.columba.mail.folder.command.MoveMessageCommand;
import org.columba.mail.gui.frame.MailFrameMediator;
import org.columba.mail.gui.frame.TableViewOwner;
import org.columba.mail.gui.table.ITableController;

/**
 * @author frd
 * 
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class PasteAction extends AbstractColumbaAction {
	ITableController tableController;

	IFrameMediator frameController;

	public PasteAction(IFrameMediator frameController) {
		super(frameController, "PasteAction");
		this.tableController = ((TableViewOwner) frameController)
				.getTableController();
		this.frameController = frameController;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent arg0) {
		
		// TODO:
		/*
		MailFolderCommandReference ref = (MailFolderCommandReference) ClipboardManager.getInstance()
				.getSelection();

		if (ref == null) {
			return;
		}

		MailFolderCommandReference dest = (MailFolderCommandReference) ((MailFrameMediator) frameController)
				.getTableSelection();
		ref.setDestinationFolder(dest.getSourceFolder());

		Command c = null;

		if (ClipboardManager.getInstance().isCutAction()) {
			c = new MoveMessageCommand(ref);
		} else {
			c = new CopyMessageCommand(ref);
		}

		if (ClipboardManager.getInstance().isCutAction()) {
			ClipboardManager.getInstance().clearSelection();
		}


				CommandProcessor.getInstance().addOp(c);
				*/
	}
}