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
package org.columba.mail.gui.table.action;

import java.awt.event.ActionEvent;

import org.columba.api.gui.frame.IFrameMediator;
import org.columba.core.command.CommandProcessor;
import org.columba.core.gui.action.AbstractColumbaAction;
import org.columba.mail.command.IMailFolderCommandReference;
import org.columba.mail.folder.IMailbox;
import org.columba.mail.gui.frame.MailFrameMediator;
import org.columba.mail.gui.table.command.ViewHeaderListCommand;

/**
 * View HeaderList in message list viewer.
 * 
 * @author fdietz
 */
public class ViewHeaderListAction extends AbstractColumbaAction {
	/**
	 * @param controller
	 */
	public ViewHeaderListAction(IFrameMediator controller) {
		super(controller, "ViewHeaderListAction");
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt) {
		IMailFolderCommandReference references = ((MailFrameMediator) getFrameMediator())
				.getTreeSelection();

		if (references != null
				&& (references.getSourceFolder() instanceof IMailbox)) {
			// view message list
			CommandProcessor.getInstance().addOp(
					new ViewHeaderListCommand(getFrameMediator(), references));
		} else {
			// clear message list viewer
			new ClearHeaderlistAction(getFrameMediator()).actionPerformed(evt);

		}
	}

}