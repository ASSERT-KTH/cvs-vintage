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
import org.columba.chat.base.Parser;
import org.columba.chat.command.AddContactCommand;
import org.columba.chat.command.ChatCommandReference;
import org.columba.chat.resourceloader.ResourceLoader;
import org.columba.chat.ui.frame.api.IChatFrameMediator;
import org.columba.core.command.CommandProcessor;
import org.columba.core.gui.action.AbstractColumbaAction;

/**
 * @author fdietz
 * 
 */

public class AddContactAction extends AbstractConnectionAwareAction {

	/**
	 * @param mediator
	 * @param name
	 */
	public AddContactAction(IFrameMediator mediator) {
		super(mediator, "Add Contact...");

		putValue(AbstractColumbaAction.TOOLBAR_NAME, "Add Contact");

		putValue(AbstractColumbaAction.LARGE_ICON, ResourceLoader
				.getIcon("system-users.png"));
		putValue(AbstractColumbaAction.SMALL_ICON, ResourceLoader
				.getSmallIcon("system-users.png"));

	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent arg0) {
		// prompt for jabber id
		String jabberId = JOptionPane.showInputDialog(null, "Enter jabber ID");

		// if user cancelled action
		if (jabberId == null)
			return;

		// example: fdietz@jabber.org/Jabber-client
		// -> remove "/Jabber-client"
		String normalizedFrom = Parser.normalizeFrom(jabberId);

		CommandProcessor.getInstance()
				.addOp(
						new AddContactCommand((IChatFrameMediator) getFrameMediator(), new ChatCommandReference(
								normalizedFrom)));

	}
}
