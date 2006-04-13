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
package org.columba.chat.ui.action;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import org.columba.api.gui.frame.IFrameMediator;
import org.columba.chat.command.ChatCommandReference;
import org.columba.chat.command.OpenChatCommand;
import org.columba.chat.model.api.IBuddyStatus;
import org.columba.chat.resourceloader.ResourceLoader;
import org.columba.chat.ui.frame.api.IChatFrameMediator;
import org.columba.core.command.CommandProcessor;
import org.columba.core.gui.action.AbstractColumbaAction;

/**
 * @author fdietz
 * 
 */
public class OpenConversationAction extends AbstractConnectionAwareAction {

	/**
	 * @param mediator
	 * @param name
	 */
	public OpenConversationAction(IFrameMediator mediator) {
		super(mediator, "Chat...");

		putValue(AbstractColumbaAction.TOOLBAR_NAME, "Chat");

		putValue(AbstractColumbaAction.LARGE_ICON, ResourceLoader
				.getIcon("internet-group-chat.png"));
		putValue(AbstractColumbaAction.SMALL_ICON, ResourceLoader
				.getSmallIcon("internet-group-chat.png"));

	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent arg0) {

		String jabberId = "";

		// selected buddy in buddylist
		IBuddyStatus buddy = (IBuddyStatus) ((IChatFrameMediator) frameMediator)
				.getRoasterTree().getSelected();

		if (buddy != null) {
			// use selected buddy
			jabberId = buddy.getJabberId();
		} else {
			// prompt for jabber id
			jabberId = JOptionPane.showInputDialog(null, "Enter Jabber ID");
		}
		CommandProcessor.getInstance()
		.addOp(
				new OpenChatCommand((IChatFrameMediator) getFrameMediator(),new ChatCommandReference(
						jabberId)));

	}
}