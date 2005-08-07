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
package org.columba.chat.ui.conversation;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.logging.Logger;

import org.columba.chat.api.IBuddyStatus;
import org.columba.chat.api.IConversationController;
import org.columba.chat.jabber.BuddyList;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;

/**
 * @author fdietz
 *  
 */
public class MessageListener implements PacketListener {

	private static final Logger LOG = Logger
			.getLogger("org.columba.chat.jabber");

	private IConversationController conversationController;

	public MessageListener(IConversationController conversationController) {

		this.conversationController = conversationController;
	}
	

	/**
	 * @see org.jivesoftware.smack.PacketListener#processPacket(org.jivesoftware.smack.packet.Packet)
	 */
	public void processPacket(Packet packet) {
		final Message message = (Message) packet;

		// time of packet arrival
		Date date = new GregorianCalendar().getTime();

		LOG.finest("message" + message.toString());
		//log.info(message.toString());

		if ((message.getType() != Message.Type.NORMAL)
				&& (message.getType() != Message.Type.CHAT))
			return;

		String from = message.getFrom();

		LOG.info("From=" + from);

		// example: fdietz@jabber.org/Jabber-client
		// -> remove "/Jabber-client"
		final String normalizedFrom = from.replaceAll("\\/.*", "");
		final IBuddyStatus buddyStatus = BuddyList.getInstance().getBuddy(
				normalizedFrom);

		if (buddyStatus != null) {

			//	awt-event-thread
			javax.swing.SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					conversationController
							.addChat(normalizedFrom);

					buddyStatus.getChatMediator()
							.displayReceivedMessage(message, buddyStatus);

					buddyStatus.getChatMediator().sendTextFieldRequestFocus();

				}
			});

		}

	}
}