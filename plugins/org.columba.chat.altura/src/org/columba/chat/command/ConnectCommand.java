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
package org.columba.chat.command;

import javax.swing.JOptionPane;

import org.columba.api.command.IWorkerStatusController;
import org.columba.api.gui.frame.IFrameMediator;
import org.columba.chat.AlturaComponent;
import org.columba.chat.config.Account;
import org.columba.chat.config.Config;
import org.columba.chat.frame.AlturaFrameMediator;
import org.columba.chat.jabber.MessageListener;
import org.columba.chat.jabber.PresenceListener;
import org.columba.chat.jabber.SubscriptionListener;
import org.columba.core.command.Command;
import org.columba.core.command.DefaultCommandReference;
import org.columba.mail.gui.util.PasswordDialog;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.SSLXMPPConnection;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;

public class ConnectCommand extends Command {

	private boolean success;

	private IFrameMediator m;

	public ConnectCommand(IFrameMediator mediator) {
		super(new DefaultCommandReference());

		this.m = mediator;
	}

	/**
	 * @see org.columba.api.command.Command#execute(org.columba.api.command.IWorkerStatusController)
	 */
	public void execute(IWorkerStatusController worker) throws Exception {
		Account account = Config.getInstance().getAccount();
		AlturaFrameMediator mediator = (AlturaFrameMediator) m;

		try {

			if (account.isEnableSSL())
				AlturaComponent.connection = new SSLXMPPConnection(account
						.getHost(), account.getPort());
			else
				AlturaComponent.connection = new XMPPConnection(account
						.getHost(), account.getPort());

		} catch (XMPPException e) {

			JOptionPane.showMessageDialog(null, e.getMessage());

			e.printStackTrace();

			return;

		}

		char[] password = account.getPassword();

		PasswordDialog dialog = new PasswordDialog();

		success = false;

		while (!success) {

			if ((password == null) || (password.length == 0)) {
				dialog.showDialog("<html>Enter password for <b>"
						+ account.getId() + "</b> at <b>" + account.getHost()
						+ "</b></html>", "", false);

				if (dialog.success()) {
					password = dialog.getPassword();

					if (dialog.getSave())
						account.setPassword(dialog.getPassword());
				}
			}

			try {
				AlturaComponent.connection.login(account.getId(), new String(
						password));
				success = true;
			} catch (XMPPException e) {
				JOptionPane.showMessageDialog(null, e.getMessage());
				e.printStackTrace();
			}

		}

		if (!success)
			return;

		AlturaComponent.connection.getRoster().setSubscriptionMode(
				Roster.SUBSCRIPTION_MANUAL);

		AlturaComponent.connection.addPacketListener(new MessageListener(
				mediator), new PacketTypeFilter(Message.class));
		AlturaComponent.connection.addPacketListener(new PresenceListener(
				mediator), new PacketTypeFilter(Presence.class));
		AlturaComponent.connection.addPacketListener(new SubscriptionListener(
				mediator), new PacketTypeFilter(Presence.class));

	}

	/**
	 * @see org.columba.api.command.Command#updateGUI()
	 */
	public void updateGUI() throws Exception {

		if (!success)
			return;

		final AlturaFrameMediator mediator = (AlturaFrameMediator) m;

		// awt-event-thread
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				mediator.getBuddyTree().setEnabled(true);
				mediator.getBuddyTree().populate();
			}
		});
	}
}