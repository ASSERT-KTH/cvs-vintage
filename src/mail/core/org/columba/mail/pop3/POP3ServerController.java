package org.columba.mail.pop3;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import org.columba.mail.config.AccountItem;
import org.columba.mail.config.PopItem;
import org.columba.mail.gui.action.BasicAction;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class POP3ServerController implements ActionListener{

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
					+ accountItem.getIdentityItem().getAddress()
					+ ")",
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
					+ accountItem.getIdentityItem().getAddress()
					+ ")",
				"",
				"MANAGE",
				null,
				null,
				0,
				null);
		manageAction.addActionListener(this);

		restartTimer();
		
	}
	
	public POP3Server getServer()
	{
		return server;
	}
	
	public AccountItem getAccountItem()
	{
		return getServer().getAccountItem();
	}
	
	public void restartTimer() {
		PopItem item = getAccountItem().getPopItem();

		if (item.isMailCheck() == true) {
			int interval = item.getMailCheckInterval();

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
				+ getAccountItem().getIdentityItem().getAddress()
				+ ")");
		manageAction.setName(
			getAccountItem().getName()
				+ " ("
				+ getAccountItem().getIdentityItem().getAddress()
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
	
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
	
		/*
		if (src.equals(timer)) {
			if ((checkAction.isEnabled() == true)
				&& (manageAction.isEnabled() == true))
				fetchAll(true);
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
			boolean result = fetchAll(false);
			//System.out.println("result collection: "+result );
		} else if (action.equals(manageAction.getActionCommand())) {
			//System.out.println("manage");

			showWindow(true);
		}
		*/
	}
	public int getUid() {
		return uid;
	}



}
