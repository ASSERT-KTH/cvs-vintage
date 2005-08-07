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
package org.columba.chat.ui.roaster;

import javax.swing.JOptionPane;

import org.columba.chat.AlturaComponent;
import org.columba.chat.api.IRoasterTree;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;

/**
 * @author fdietz
 * 
 */
public class SubscriptionListener implements PacketListener {

	private IRoasterTree roasterTree;

	/**
	 * 
	 */
	public SubscriptionListener(IRoasterTree roasterTree) {
		super();

		this.roasterTree = roasterTree;
	}

	/**
	 * @see org.jivesoftware.smack.PacketListener#processPacket(org.jivesoftware.smack.packet.Packet)
	 */

	public void processPacket(Packet p) {
		Presence presence = (Presence) p;

		// we are only interested on subscription requests
		if (presence.getType().equals(Presence.Type.SUBSCRIBE)) {
			// ask the user
			String from = presence.getFrom();

			// example: fdietz@jabber.org/Jabber-client
			// -> remove "/Jabber-client"
			String normalizedFrom = from.replaceAll("\\/.*", "");

			int option = JOptionPane
					.showConfirmDialog(
							null,
							"The user "
									+ from
									+ " requests presence notification.\nDo you wish to allow them to see your "
									+ "online presence?",
							"Subscription Request", JOptionPane.YES_NO_OPTION);

			if (option == JOptionPane.YES_OPTION) {
				// if already in roster
				if (AlturaComponent.connection.getRoster().contains(
						normalizedFrom))
					return;

				Presence packet = new Presence(Presence.Type.SUBSCRIBED);
				packet.setTo(normalizedFrom);
				AlturaComponent.connection.sendPacket(packet);

			} else {
				return;
			}

			option = JOptionPane.showConfirmDialog(null, "Do you wish to add "
					+ from + " to your roaster?", "Add user",
					JOptionPane.YES_NO_OPTION);

			if (option == JOptionPane.YES_OPTION) {
				try {
					// add contact to roaster, nickname="", group=null
					AlturaComponent.connection.getRoster().createEntry(from,
							"", null);

					roasterTree.populate();
				} catch (XMPPException e) {
					JOptionPane.showMessageDialog(null, e.getMessage());
					e.printStackTrace();
				}
			}
		}

	}

}