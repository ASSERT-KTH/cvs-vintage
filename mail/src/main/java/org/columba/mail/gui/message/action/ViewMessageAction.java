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
package org.columba.mail.gui.message.action;

import java.awt.event.ActionEvent;

import org.columba.api.gui.frame.IFrameMediator;
import org.columba.core.command.CommandProcessor;
import org.columba.core.gui.action.AbstractColumbaAction;
import org.columba.mail.command.MailFolderCommandReference;
import org.columba.mail.gui.frame.MailFrameMediator;
import org.columba.mail.gui.message.command.ViewMessageCommand;

/**
 * View selected message content.
 * 
 * @author fdietz
 */
public class ViewMessageAction extends AbstractColumbaAction {

	protected static Object oldUid;

	/**
	 * @param controller
	 */
	public ViewMessageAction(IFrameMediator controller) {
		super(controller, "ViewMessageAction");
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt) {
		MailFolderCommandReference references = (MailFolderCommandReference) ((MailFrameMediator) getFrameMediator())
				.getTableSelection();
		
		Object[] uids = references.getUids();

		if (uids.length == 1) {
			// show selected message
			CommandProcessor.getInstance().addOp(
					new ViewMessageCommand(getFrameMediator(), references));
		} else {
			// clear message viewer
			new ClearMessageViewerAction(getFrameMediator()).actionPerformed(evt);
		}
	}
}
