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
import org.columba.chat.Connection;
import org.columba.chat.MainInterface;
import org.columba.chat.config.api.IAccount;
import org.columba.chat.conn.api.IConnection.STATUS;
import org.columba.chat.ui.frame.api.IChatFrameMediator;
import org.columba.core.command.Command;
import org.columba.mail.gui.util.PasswordDialog;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.SSLXMPPConnection;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

public class ConnectCommand extends Command {

	private boolean success;

	private IChatFrameMediator mediator;

	private PopulateRoasterCommand populateCommand;

	public ConnectCommand(IChatFrameMediator mediator, ChatCommandReference ref) {
		super(ref);

		this.mediator = mediator;

		populateCommand = new PopulateRoasterCommand(mediator, ref);
	}

	/**
	 * @see org.columba.api.command.Command#execute(org.columba.api.command.IWorkerStatusController)
	 */
	public void execute(IWorkerStatusController worker) throws Exception {
		IAccount account = MainInterface.config.getAccount();

		try {

			if (account.isEnableSSL())
				Connection.XMPPConnection = new SSLXMPPConnection(account
						.getHost(), account.getPort());
			else
				Connection.XMPPConnection = new XMPPConnection(account
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
				Connection.XMPPConnection.login(account.getId(),
						new String(password));
				success = true;
			} catch (XMPPException e) {
				JOptionPane.showMessageDialog(null, e.getMessage());
				e.printStackTrace();
			}

		}

		if (!success)
			return;

		
		
		populateCommand.execute(worker);

		Connection.XMPPConnection.getRoster().setSubscriptionMode(
				Roster.SUBSCRIPTION_MANUAL);

		

	}

	/**
	 * @see org.columba.api.command.Command#updateGUI()
	 */
	public void updateGUI() throws Exception {

		if (!success)
			return;

		MainInterface.connection.setStatus(STATUS.ONLINE);
		
		populateCommand.updateGUI();

		mediator.getRoasterTree().setEnabled(true);

	}
}