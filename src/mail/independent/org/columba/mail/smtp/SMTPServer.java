package org.columba.mail.smtp;

import java.net.UnknownHostException;

import org.columba.addressbook.parser.AddressParser;
import org.columba.core.command.WorkerStatusController;
import org.columba.core.gui.util.NotifyDialog;
import org.columba.mail.composer.SendableMessage;
import org.columba.mail.config.AccountItem;
import org.columba.mail.config.IdentityItem;
import org.columba.mail.config.PopItem;
import org.columba.mail.config.SmtpItem;
import org.columba.mail.config.SpecialFoldersItem;
import org.columba.mail.gui.util.PasswordDialog;
import org.columba.mail.pop3.protocol.POP3Protocol;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class SMTPServer {

	protected SMTPProtocol smtpProtocol;
	protected AccountItem accountItem;
	protected IdentityItem identityItem;
	protected String fromAddress;
	/**
	 * Constructor for SMTPServer.
	 */
	public SMTPServer(AccountItem accountItem) {
		super();

		this.accountItem = accountItem;

		identityItem = accountItem.getIdentityItem();
	}

	public boolean openConnection() {
		String username;
		String password;
		String method;

		int smtpMode;
		boolean authenticate;
		boolean cont = false;

		PasswordDialog passDialog = new PasswordDialog();

		// Init Values

		fromAddress = identityItem.get("address");

		SmtpItem smtpItem = accountItem.getSmtpItem();
		String host = smtpItem.get("host");

		SpecialFoldersItem specialFoldersItem =
			accountItem.getSpecialFoldersItem();
		Integer i = new Integer(specialFoldersItem.get("sent"));
		int sentFolder = i.intValue();

		String authType = accountItem.getSmtpItem().get("login_method");
		authenticate = !authType.equals("NONE");

		boolean popbeforesmtp = false;
		if (authType.equalsIgnoreCase("POP before SMTP"))
			popbeforesmtp = true;

		//boolean popbeforesmtp = accountItem.getSmtpItem().getPopBeforeSmtp();

		if (popbeforesmtp == true) {
			// no esmtp - use POP3-before-SMTP instead
			try {
				pop3Authentification();
			} catch (Exception e) {
				if (e instanceof UnknownHostException) {
					Exception ex =
						new Exception(
							"Unknown host: "
								+ e.getMessage()
								+ "\nAre you  online?");
					NotifyDialog dialog = new NotifyDialog();
					dialog.showDialog(ex);

				} else {
					NotifyDialog dialog = new NotifyDialog();
					dialog.showDialog(e);
				}

				return false;
			}
		}

		// find host
		//setText("Opening Port to " + host);
		try {
			smtpProtocol = new SMTPProtocol(host, smtpItem.getInteger("port"));

		} catch (Exception e) {
			if (e instanceof UnknownHostException) {
				Exception ex =
					new Exception(
						"Unknown host: "
							+ e.getMessage()
							+ "\nAre you  online?");

			} else {
				NotifyDialog dialog = new NotifyDialog();
				dialog.showDialog(e);
			}

			return false;
		}

		// Start login procedure

		try {

			smtpMode = smtpProtocol.openPort();
		} catch (Exception e) {
			NotifyDialog dialog = new NotifyDialog();
			dialog.showDialog(e);

			return false;
		}

		//if ((smtpMode == SMTPProtocol.ESMTP) & authenticate)
		if ((authenticate) && (popbeforesmtp == false)) {

			//setText("Authenticating");

			System.out.println("authentication--->");
			username = accountItem.getSmtpItem().get("user");
			password = accountItem.getSmtpItem().get("password");
			method = accountItem.getSmtpItem().get("login_method");

			if ((username.length() == 0) || (password.length() == 0)) {

				passDialog.showDialog(
					accountItem.getIdentityItem().get("address"),
					password,
					accountItem.getSmtpItem().getBoolean("save_password"));

				if (passDialog.success()) {

					//username = passDialog.getUser();
					password = new String(passDialog.getPassword());
					//method = passDialog.getLoginMethod();

				} else {
					return false;
				}
			}

			while (!cont) {
				/*
				if (getCancel() == true)
					break;
				*/
				cont = true;

				try {
					smtpProtocol.authenticate(username, password, method);
				} catch (Exception e) {
					cont = false;

					passDialog.showDialog(
						accountItem.getIdentityItem().get("address"),
						password,
						accountItem.getSmtpItem().getBoolean("save_password"));

					if (!passDialog.success())
						return false;
					else {
						//username = passDialog.getUser();
						password = new String(passDialog.getPassword());
						//method = passDialog.getLoginMethod();
					}

				}
			}

			accountItem.getSmtpItem().set("user", username);
			accountItem.getSmtpItem().set("password", password);
			accountItem.getSmtpItem().set("save_password", passDialog.getSave());
			//accountItem.getSmtpItem().setLoginMethod(method);

		}

		return true;

	}

	public void closeConnection() {
		// Close Port

		//setText("Closing Connection to " + host);

		try {
			smtpProtocol.closePort();
		} catch (Exception e) {
			NotifyDialog dialog = new NotifyDialog();
			dialog.showDialog(e);

		}

	}

	protected void pop3Authentification() throws Exception {
		String password = new String("");
		//String user = "";
		String method = new String("");
		boolean save = false;
		boolean login = false;
		boolean cancel = false;
		PopItem item = accountItem.getPopItem();
		PasswordDialog dialog = null;

		while ((login == false) && (cancel == false)) {
			if (item.get("password").length() == 0) {
				dialog = new PasswordDialog();

				dialog.showDialog(
					accountItem.getIdentityItem().get("address"),
					password,
					accountItem.getPopItem().getBoolean("save_password"));

				char[] name;

				if (dialog.success() == true) {
					// ok pressed
					name = dialog.getPassword();
					password = new String(name);
					//user = dialog.getUser();
					save = dialog.getSave();
					//method = dialog.getLoginMethod();

					//System.out.println("pass:<"+password+">");
					cancel = false;
				} else {
					// cancel pressed
					cancel = true;
				}
			} else {
				password = item.get("password");
				//user = item.getUser();
				save = item.getBoolean("save_password");
				//method = item.getLoginMethod();
			}

			if (cancel == false) {
				//System.out.println("trying to login");
				//setText(item.getHost() + " : Login...");

				//startTimer();
				// authenticate

				POP3Protocol pop3Connection = new POP3Protocol();
				// open socket, query for host
				pop3Connection.openPort(
					item.get("host"),
					item.getInteger("port"));

				pop3Connection.setLoginMethod(method);
				login = pop3Connection.login(item.get("user"), password);
				//stopTimer();

				if (login == false) {
					NotifyDialog d = new NotifyDialog();
					d.showDialog("Authentification failed");

					item.set("password","");
				}

			}

		}

		if (login) {
			//item.setUser(user);
			item.set("save_password", save);
			item.set("login_method",method);

			if (save) {
				// save plain text password in config file
				// this is a security risk !!!
				item.set("password", password);
			}
		}
	}

	public void sendMessage(SendableMessage message, WorkerStatusController workerStatusController) throws Exception {
		smtpProtocol.setupMessage(
			AddressParser.normalizeAddress(fromAddress),
			message.getRecipients());

		smtpProtocol.sendMessage(message.getSource(), workerStatusController);
	}

}
