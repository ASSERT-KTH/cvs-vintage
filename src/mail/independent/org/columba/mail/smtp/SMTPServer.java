package org.columba.mail.smtp;

import java.net.UnknownHostException;

import org.columba.addressbook.parser.AddressParser;
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

		fromAddress = identityItem.getAddress();

		SmtpItem smtpItem = accountItem.getSmtpItem();
		String host = smtpItem.getHost();

		SpecialFoldersItem specialFoldersItem =
			accountItem.getSpecialFoldersItem();
		Integer i = new Integer(specialFoldersItem.getSent());
		int sentFolder = i.intValue();

		authenticate = accountItem.getSmtpItem().getESmtp().equals("true");
		String authType = accountItem.getSmtpItem().getLoginMethod();
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
			smtpProtocol = new SMTPProtocol(host, smtpItem.getPort());

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
			username = accountItem.getSmtpItem().getUser();
			password = accountItem.getSmtpItem().getPassword();
			method = accountItem.getSmtpItem().getLoginMethod();

			if ((username.length() == 0) || (password.length() == 0)) {

				passDialog.showDialog(
					accountItem.getIdentityItem().getAddress(),
					password,
					accountItem.getSmtpItem().isSavePassword());

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
						accountItem.getIdentityItem().getAddress(),
						password,
						accountItem.getSmtpItem().isSavePassword());

					if (!passDialog.success())
						return false;
					else {
						//username = passDialog.getUser();
						password = new String(passDialog.getPassword());
						//method = passDialog.getLoginMethod();
					}

				}
			}

			accountItem.getSmtpItem().setUser(username);
			Boolean bool = new Boolean(passDialog.getSave());
			accountItem.getSmtpItem().setSavePassword(bool.toString());
			//accountItem.getSmtpItem().setLoginMethod(method);

			if (passDialog.getSave() == true) {
				accountItem.getSmtpItem().setPassword(password);
			}
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
			if (item.getPassword().length() == 0) {
				dialog = new PasswordDialog();

				dialog.showDialog(
					accountItem.getIdentityItem().getAddress(),
					password,
					accountItem.getPopItem().isSavePassword());

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
				password = item.getPassword();
				//user = item.getUser();
				save = item.isSavePassword();
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
					item.getHost(),
					(new Integer(item.getPort())).intValue());

				pop3Connection.setLoginMethod(method);
				login = pop3Connection.login(item.getUser(), password);
				//stopTimer();

				if (login == false) {
					NotifyDialog d = new NotifyDialog();
					d.showDialog("Authentification failed");

					item.setPassword("");
				}

			}

		}

		if (login) {
			//item.setUser(user);
			item.setSavePassword(save);
			item.setLoginMethod(method);

			if (save) {

				// save plain text password in config file
				// this is a security risk !!!
				item.setPassword(password);

			}
		}
	}

	public Object sendMessage(SendableMessage message) {
		try {
			smtpProtocol.setupMessage(
				AddressParser.normalizeAddress(fromAddress),
				message.getRecipients());

			smtpProtocol.sendMessage(message.getSource());

			
		} catch (Exception e) {
			e.printStackTrace();
			NotifyDialog dialog = new NotifyDialog();
			dialog.showDialog(e);

		}
		
		return message.getHeader().get("columba.uid");
	}

}
