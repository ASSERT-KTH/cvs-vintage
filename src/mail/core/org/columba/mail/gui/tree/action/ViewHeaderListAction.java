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
import org.columba.core.gui.frame.FrameMediator;
import org.columba.core.main.MainInterface;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.folder.MessageFolder;
import org.columba.mail.gui.frame.MessageViewOwner;
import org.columba.mail.gui.frame.TableViewOwner;
import org.columba.mail.gui.message.MessageController;
import org.columba.mail.gui.table.TableController;
import org.columba.mail.gui.table.command.ViewHeaderListCommand;
import org.columba.mail.gui.table.selection.TableSelectionHandler;

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
		FolderCommandReference references = (FolderCommandReference) getFrameMediator()
				.getSelectionManager().getSelection("mail.tree");

		if ((references.getFolder() instanceof MessageFolder)) {
			// view message list
			MainInterface.processor.addOp(new ViewHeaderListCommand(
					getFrameMediator(), references));
		} else {
			// clear message list
			TableController c = ((TableViewOwner) getFrameMediator())
					.getTableController();

			// clear message-list selection
			c.getView().getSelectionModel().clearSelection();
			c.clear();

			// notify selection handler
			((TableSelectionHandler) frameMediator.getSelectionManager()
					.getHandler("mail.table")).setFolder(null);

			// clear message-viewer
			MessageController m = ((MessageViewOwner) getFrameMediator())
					.getMessageController();
			m.clear();

		}
	}

}