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

import org.columba.api.gui.frame.IFrameMediator;
import org.columba.chat.MainInterface;
import org.columba.chat.command.ChatCommandReference;
import org.columba.chat.command.ConnectCommand;
import org.columba.chat.config.api.IAccount;
import org.columba.chat.conn.api.ConnectionChangedEvent;
import org.columba.chat.conn.api.IConnectionChangedListener;
import org.columba.chat.conn.api.IConnection.STATUS;
import org.columba.chat.resourceloader.ResourceLoader;
import org.columba.chat.ui.dialog.AccountDialog;
import org.columba.chat.ui.frame.api.IChatFrameMediator;
import org.columba.core.command.CommandProcessor;
import org.columba.core.gui.action.AbstractColumbaAction;

/**
 * @author fdietz
 * 
 */

public class ConnectAction extends AbstractColumbaAction implements
		IConnectionChangedListener {

	/**
	 * @param mediator
	 * @param name
	 */
	public ConnectAction(IFrameMediator mediator) {
		super(mediator, "Connect...");

		putValue(AbstractColumbaAction.TOOLBAR_NAME, "Connect");
		putValue(AbstractColumbaAction.LARGE_ICON, ResourceLoader
				.getIcon("network-receive.png"));
		putValue(AbstractColumbaAction.SMALL_ICON, ResourceLoader
				.getSmallIcon("network-receive.png"));
		MainInterface.connection.addConnectionChangedListener(this);
	}

	/**
	 * @see org.columba.chat.conn.api.IConnectionChangedListener#connectionChanged(org.columba.chat.conn.api.ConnectionChangedEvent)
	 */
	public void connectionChanged(ConnectionChangedEvent object) {
		STATUS status = object.getStatus();

		if (status == STATUS.ONLINE)
			setEnabled(false);
		else if (status == STATUS.OFFLINE)
			setEnabled(true);
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent arg0) {
		IAccount account = MainInterface.config.getAccount();

		if ((account.getHost() == null) || (account.getId() == null))
			new AccountDialog(account);

		CommandProcessor.getInstance().addOp(
				new ConnectCommand((IChatFrameMediator) getFrameMediator(),
						new ChatCommandReference()));
	}
}