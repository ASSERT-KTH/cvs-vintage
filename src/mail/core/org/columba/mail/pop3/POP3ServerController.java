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
package org.columba.mail.pop3;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import org.columba.core.action.BasicAction;
import org.columba.core.logging.ColumbaLogger;
import org.columba.mail.command.POP3CommandReference;
import org.columba.mail.config.AccountItem;
import org.columba.mail.config.PopItem;
import org.columba.mail.pop3.command.*;
import org.columba.core.main.MainInterface;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class POP3ServerController implements ActionListener {

	private final static int ONE_SECOND = 1000;

	protected POP3Server server;

	private boolean hide;

	public BasicAction checkAction;
	private BasicAction manageAction;

	private Timer timer;

	private int uid;
	/**
	 * Constructor for POP3ServerController.
	 */
	public POP3ServerController(AccountItem accountItem) {
		server = new POP3Server(accountItem);

		hide = true;

		uid = accountItem.getUid();
		checkAction =
			new BasicAction(
				accountItem.getName()
					+ " ("
					+ accountItem.getIdentityItem().get("address")
					+ ")",
				"",
				"",
				"CHECK",
				null,
				null,
				0,
				null);
		checkAction.addActionListener(this);

		manageAction =
			new BasicAction(
				accountItem.getName()
					+ " ("
					+ accountItem.getIdentityItem().get("address")
					+ ")",
				"",
				"",
				"MANAGE",
				null,
				null,
				0,
				null);
		manageAction.addActionListener(this);
		manageAction.setEnabled(false);
		
		restartTimer();

	}

	public POP3Server getServer() {
		return server;
	}

	public AccountItem getAccountItem() {
		return getServer().getAccountItem();
	}

	public void restartTimer() {
		PopItem item = getAccountItem().getPopItem();

		if (item.getBoolean("enable_mailcheck")) {
			int interval = item.getInteger("mailcheck_interval");

			timer = new Timer(ONE_SECOND * interval * 60, this);
			timer.restart();

			//System.out.println("---------------->timer restarted");
		} else {
			//System.out.println("----------------->timer stopped");

			if (timer != null) {
				timer.stop();
				timer = null;
			}
		}
	}

	public void updateAction() {
		checkAction.setName(
			getAccountItem().getName()
				+ " ("
				+ getAccountItem().getIdentityItem().get("address")
				+ ")");
		manageAction.setName(
			getAccountItem().getName()
				+ " ("
				+ getAccountItem().getIdentityItem().get("address")
				+ ")");
		uid = getAccountItem().getUid();
	}

	public BasicAction getCheckAction() {
		return checkAction;
	}

	public BasicAction getManageAction() {
		return manageAction;
	}

	public void enableActions(boolean b) {
		getCheckAction().setEnabled(b);
		getManageAction().setEnabled(b);
	}

	public void setHide(boolean b) {
		hide = b;
	}

	public boolean getHide() {
		return hide;
	}

	public void fetch() {
		POP3ServerController controller = (POP3ServerController) this;
		
		
		POP3CommandReference[] r = new POP3CommandReference[1];
		r[0] = new POP3CommandReference(controller.getServer());

		FetchNewMessagesCommand c =
			new FetchNewMessagesCommand( r);

		MainInterface.processor.addOp(c);
	}
	
	public void check() {
		POP3ServerController controller = (POP3ServerController) this;

		POP3CommandReference[] r = new POP3CommandReference[1];
		r[0] = new POP3CommandReference(controller.getServer());

		CheckForNewMessagesCommand c =
			new CheckForNewMessagesCommand( r);

		MainInterface.processor.addOp(c);
	}

	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();

		if (src.equals(timer)) {
			if ((checkAction.isEnabled() == true)
				&& (manageAction.isEnabled() == true))
				check();
			else
				timer.restart();

			return;
		}

		String action = e.getActionCommand();
		if (action == null)
			return;
		//System.out.println("action: " +e.getActionCommand() );

		if (action.equals(checkAction.getActionCommand())) {
			//System.out.println("check");
			//boolean result = fetchAll(false);
			//System.out.println("result collection: "+result );

			fetch();

		} else if (action.equals(manageAction.getActionCommand())) {
			//System.out.println("manage");
			ColumbaLogger.log.info("not yet implemented");
			//showWindow(true);
		}

	}
	public int getUid() {
		return uid;
	}

}
