// The contents of this file are subject to the Mozilla Public License Version
// 1.1
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
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.mail.gui.tree.action;

import java.awt.event.ActionEvent;

import org.columba.core.action.AbstractColumbaAction;
import org.columba.core.command.CommandProcessor;
import org.columba.core.gui.frame.FrameMediator;
import org.columba.mail.command.MailFolderCommandReference;
import org.columba.mail.folder.AbstractMessageFolder;
import org.columba.mail.gui.frame.MessageViewOwner;
import org.columba.mail.gui.frame.TableViewOwner;
import org.columba.mail.gui.message.IMessageController;
import org.columba.mail.gui.table.ITableController;
import org.columba.mail.gui.table.command.ViewHeaderListCommand;

public class ViewHeaderListAction extends AbstractColumbaAction {
	/**
	 * @param controller
	 */
	public ViewHeaderListAction(FrameMediator controller) {
		super(controller, "ViewHeaderListAction");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt) {
		MailFolderCommandReference references = (MailFolderCommandReference) getFrameMediator()
				.getSelectionManager().getSelection("mail.tree");

		if (references != null && (references.getSourceFolder() instanceof AbstractMessageFolder)) {
			// view message list
			CommandProcessor.getInstance().addOp(new ViewHeaderListCommand(
					getFrameMediator(), references));
		} else {
			// clear message list
			ITableController c = ((TableViewOwner) getFrameMediator())
					.getTableController();

			c.clear();
			
			// clear message-list selection
			c.clearSelection();

			// clear the folder info bar
			

			// clear message-viewer
			IMessageController m = ((MessageViewOwner) getFrameMediator())
					.getMessageController();
			m.clear();

		}
	}

}