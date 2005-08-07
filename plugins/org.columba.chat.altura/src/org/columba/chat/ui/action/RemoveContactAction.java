//The contents of this file are subject to the Mozilla Public License Version
//1.1
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
//Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.chat.ui.action;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import org.columba.api.gui.frame.IFrameMediator;
import org.columba.chat.AlturaComponent;
import org.columba.chat.api.IAlturaFrameMediator;
import org.columba.chat.api.IBuddyStatus;
import org.columba.core.gui.action.AbstractColumbaAction;
import org.jivesoftware.smack.RosterEntry;

/**
 * @author fdietz
 *  
 */
public class RemoveContactAction extends AbstractColumbaAction {

	/**
	 * @param mediator
	 * @param name
	 */
	public RemoveContactAction(IFrameMediator mediator) {
		super(mediator, "Remove Contact...");
		
		putValue(AbstractColumbaAction.TOOLBAR_NAME, "Remove Contact");

	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent arg0) {
		String jabberId = "";

		// selected buddy in buddylist
		IBuddyStatus buddy = (IBuddyStatus) ((IAlturaFrameMediator) frameMediator)
				.getRoasterTree().getSelected();

		if (buddy != null) {
			// use selected buddy
			jabberId = buddy.getJabberId();
		} else {
			// prompt for jabber id
			jabberId = JOptionPane.showInputDialog(null, "Enter jabber ID");
		}

		RosterEntry entry = AlturaComponent.connection.getRoster().getEntry(
				jabberId);

		int option = JOptionPane.showConfirmDialog(null,
				"Do you really want to remove " + jabberId
						+ " from your roster?", "Remove Contact",
				JOptionPane.YES_NO_OPTION);

		if (option == JOptionPane.YES_OPTION) {
			AlturaComponent.connection.getRoster().removeEntry(entry);
			System.out.println("update tree");
			((IAlturaFrameMediator) frameMediator).getRoasterTree().populate();
		}

		/*
		 * try {
		 *  } catch (XMPPException e) {
		 * 
		 * JOptionPane.showMessageDialog(MainInterface.mediator.getView(), e
		 * .getMessage());
		 * 
		 * e.printStackTrace(); }
		 */

	}
}