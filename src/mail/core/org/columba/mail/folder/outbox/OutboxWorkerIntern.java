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
package org.columba.mail.folder.outbox;



/*
public class OutboxWorkerIntern extends Worker {
	SendListManager sendListManager = new SendListManager();
	OutboxOperator outboxRoot;
	AccountItem account;
	IdentityItem identity;
	SmtpItem smtpItem;
	int sentFolder;
	String fromAddress;
	String host;
	SMTPProtocol smtp;

	OutboxOperation smtpOp;

	public Object construct() {

		Vector sentList = new Vector();

		outboxRoot = (OutboxOperator) root;
		smtpOp = (OutboxOperation) op;

		int opLength = 0;

		System.out.println("OutboxWorkerIntern started...");

		if (smtpOp.uidList != null) {
			opLength = Array.getLength(smtpOp.uidList);
			setProgressBarMaximum(opLength + 1);
		}

		setText("Sending Messages");

		setProgressBarValue(0);

		switch (smtpOp.mode) {


			case (Operation.SMTP_SENDALL) :
				{
					smtpOp.uidList = outboxRoot.outboxFolder.getUids();
					smtpOp.mode = Operation.SMTP_SEND;
					opLength = Array.getLength(smtpOp.uidList);
					setProgressBarMaximum(opLength + 1);
				}

			case (Operation.SMTP_SEND) :
				{

					int actAccountUid = -1;
					boolean open = false;

					for (int i = 0; i < opLength; i++) {

						try {

							

							if (outboxRoot
								.outboxFolder
								.exists(smtpOp.uidList[i])
								== true) {
								SendableMessage message =
									(SendableMessage) outboxRoot
											.outboxFolder
											.getMessage(
										smtpOp.uidList[i],this);
								sendListManager.add(message);
							}
							setProgressBarValue(1);

							

							while (sendListManager.hasMoreMessages()) {
								SendableMessage message =
									sendListManager.getNextMessage();

								
								if (message.getAccountUid() != actAccountUid) {
									
									if (sentList.size() != 0) {
										outboxRoot.moveSent(
											sentList.toArray(),
											sentFolder);
										sentList.clear();
									}

									actAccountUid = message.getAccountUid();

								
									open = openConnection(actAccountUid);
								}

								
								if (open) {
									
									try {
										smtp.setupMessage(
											AddressParser.normalizeAddress(
												fromAddress),
											message.getRecipients());

										smtp.sendMessage(message.getSource());

										sentList.add(message.getUID());
									} catch (Exception e) {
										e.printStackTrace();
										NotifyDialog dialog =
											new NotifyDialog();
										dialog.showDialog(e);
									
									}
								}

								setProgressBarValue(i + 2);
							}

						} catch (Exception ex) {
							if (ex instanceof UnknownHostException) {
								Exception ex1 =
									new Exception(
										"Unknown host: "
											+ ex.getMessage()
											+ "\nAre you  online?");
								NotifyDialog dialog = new NotifyDialog();
								dialog.showDialog(ex1);

							} else {
								NotifyDialog dialog = new NotifyDialog();
								dialog.showDialog(ex);
							}
							ex.printStackTrace();
						}
					}
				}
		}

		if (sentList.size() > 0) {
			outboxRoot.moveSent(sentList.toArray(), sentFolder);
		}

		unregister();

		return null;
	}

	private boolean openConnection(int accountUid) {
		String username;
		String password;
		String method;

		int smtpMode;
		boolean authenticate;
		boolean cont = false;

		PasswordDialog passDialog = new PasswordDialog();

		// Init Values

		account = outboxRoot.getAccountItem(accountUid);

		identity = account.getIdentityItem();
		fromAddress = identity.getAddress();

		smtpItem = account.getSmtpItem();
		host = smtpItem.getHost();

		SpecialFoldersItem specialFoldersItem = account.getSpecialFoldersItem();
		Integer i = new Integer(specialFoldersItem.getSent());
		sentFolder = i.intValue();

		authenticate = account.getSmtpItem().getESmtp().equals("true");
		String authType = account.getSmtpItem().getLoginMethod();
		boolean popbeforesmtp = false;
		if (authType.equalsIgnoreCase("POP before SMTP"))
			popbeforesmtp = true;

		//boolean popbeforesmtp = account.getSmtpItem().getPopBeforeSmtp();

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
		setText("Opening Port to " + host);
		try {
			smtp = new SMTPProtocol(host, smtpItem.getPort());

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

			smtpMode = smtp.openPort();
		} catch (Exception e) {
			NotifyDialog dialog = new NotifyDialog();
			dialog.showDialog(e);

			return false;
		}

		//if ((smtpMode == SMTPProtocol.ESMTP) & authenticate)
		if ((authenticate) && (popbeforesmtp == false)) {

			setText("Authenticating");

			System.out.println("authentication--->");
			username = account.getSmtpItem().getUser();
			password = account.getSmtpItem().getPassword();
			method = account.getSmtpItem().getLoginMethod();

			if ((username.length() == 0) || (password.length() == 0)) {

				
				passDialog.showDialog(
					account.getIdentityItem().getAddress(),
					password,
					account.getSmtpItem().isSavePassword());
				

				if (passDialog.success()) {

					//username = passDialog.getUser();
					password = new String(passDialog.getPassword());
					//method = passDialog.getLoginMethod();

				} else {
					return false;
				}
			}

			while (!cont) {
				if (getCancel() == true)
					break;

				cont = true;

				try {
					smtp.authenticate(username, password, method);
				} catch (Exception e) {
					cont = false;

					

					passDialog.showDialog(
						account.getIdentityItem().getAddress(),
						password,
						account.getSmtpItem().isSavePassword());


					if (!passDialog.success())
						return false;
					else {
						//username = passDialog.getUser();
						password = new String(passDialog.getPassword());
						//method = passDialog.getLoginMethod();
					}

				}
			}

			account.getSmtpItem().setUser(username);
			Boolean bool = new Boolean(passDialog.getSave());
			account.getSmtpItem().setSavePassword(bool.toString());
			//account.getSmtpItem().setLoginMethod(method);

			if (passDialog.getSave() == true) {
				account.getSmtpItem().setPassword(password);
			}
		}

		return true;

	}

	private void closeConnection() {
		// Close Port

		setText("Closing Connection to " + host);

		try {
			smtp.closePort();
		} catch (Exception e) {
			NotifyDialog dialog = new NotifyDialog();
			dialog.showDialog(e);

		}

	}

	private void pop3Authentification() throws Exception {
		String password = new String("");
		//String user = "";
		String method = new String("");
		boolean save = false;
		boolean login = false;
		boolean cancel = false;
		PopItem item = account.getPopItem();
		PasswordDialog dialog = null;

		while ((login == false) && (cancel == false)) {
			if (item.getPassword().length() == 0) {
				dialog = new PasswordDialog();


				dialog.showDialog(
					account.getIdentityItem().getAddress(),
					password,
				
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
				setText(item.getHost() + " : Login...");

				startTimer();
				// authenticate

				POP3Protocol pop3Connection = new POP3Protocol();
				// open socket, query for host
				pop3Connection.openPort(
					item.getHost(),
					(new Integer(item.getPort())).intValue());

				pop3Connection.setLoginMethod(method);
				login = pop3Connection.login(item.getUser(), password);
				stopTimer();

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

}
*/