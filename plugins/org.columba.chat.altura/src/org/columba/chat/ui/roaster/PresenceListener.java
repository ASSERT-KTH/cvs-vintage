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

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import org.columba.chat.api.IBuddyStatus;
import org.columba.chat.api.IRoasterTree;
import org.columba.chat.jabber.BuddyList;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;

/**
 * @author fdietz
 * 
 */
public class PresenceListener implements PacketListener {

	private static final Logger LOG = Logger
			.getLogger("org.columba.chat.jabber");

	private IRoasterTree roasterTree;

	/**
	 * 
	 */
	public PresenceListener(IRoasterTree roasterTree) {
		super();
		this.roasterTree = roasterTree;

	}

	/**
	 * @see org.jivesoftware.smack.PacketListener#processPacket(org.jivesoftware.smack.packet.Packet)
	 */
	public void processPacket(Packet packet) {
		Presence presence = (Presence) packet;

		String from = presence.getFrom();

		if ((presence.getType() != Presence.Type.AVAILABLE)
				&& (presence.getType() != Presence.Type.UNAVAILABLE))
			return;

		LOG.info("From=" + from);
		LOG.info("Presence Mode=" + presence.getMode());

		// example: fdietz@jabber.org/Jabber-client
		// -> remove "/Jabber-client"
		String normalizedFrom = from.replaceAll("\\/.*", "");

		final IBuddyStatus status = BuddyList.getInstance().getBuddy(
				normalizedFrom);
		// just ignore unknown people
		if (status == null)
			return;

		status.setPresenceMode(presence.getMode());
		if (presence.getType() == Presence.Type.AVAILABLE) {
			status.setSignedOn(true);

		} else if (presence.getType() == Presence.Type.UNAVAILABLE) {
			status.setSignedOn(false);

		}

		Runnable updateAComponent = new Runnable() {

			public void run() {
				roasterTree.updateBuddyPresence(status);
			}
		};

		try {
			SwingUtilities.invokeAndWait(updateAComponent);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}