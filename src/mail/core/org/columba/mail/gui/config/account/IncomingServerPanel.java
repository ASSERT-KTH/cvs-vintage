// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Library General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

package org.columba.mail.gui.config.account;

import javax.swing.*;
import javax.swing.border.*;

import java.awt.*;
import java.awt.event.*;

import org.columba.mail.gui.util.*;
import org.columba.mail.config.*;
import org.columba.mail.util.*;
import org.columba.mail.folder.*;
import org.columba.mail.message.*;
import org.columba.mail.gui.tree.util.*;
import org.columba.main.MainInterface;

/**
 *
 * @author  freddy
 * @version
 */
public class IncomingServerPanel
	extends DefaultPanel
	implements ActionListener {

	private JLabel loginLabel;
	private JTextField loginTextField;
	private JLabel passwordLabel;
	private JTextField passwordTextField;
	private JLabel hostLabel;
	private JTextField hostTextField;
	private JLabel portLabel;
	private JTextField portTextField;
	private JLabel typeLabel;
	private JComboBox typeComboBox;
	private JButton okButton;
	private JButton cancelButton;
	private JButton helpButton;
	private JCheckBox storePasswordCheckBox;
	private JCheckBox secureCheckBox;

	private JLabel authenticationLabel;
	private JComboBox authenticationComboBox;

	private JLabel typeDescriptionLabel;

	private PopAttributPanel popPanel;
	private ImapAttributPanel imapPanel;

	private PopItem popItem = null;
	private ImapItem imapItem = null;
	private AccountItem accountItem;

	private JCheckBox defaultAccountCheckBox;

	private ReceiveOptionsPanel receiveOptionsPanel;
	//private ConfigFrame frame;

	public IncomingServerPanel(AccountItem account, PopItem item, ReceiveOptionsPanel receiveOptionsPanel) {
		super();
		//super( frame, item );

		//this.frame = frame;
		this.accountItem = account;
		this.popItem = item;
		this.receiveOptionsPanel = receiveOptionsPanel;

		initComponents();

		updateComponents(true);

	}

	public IncomingServerPanel(AccountItem account, ImapItem item, ReceiveOptionsPanel receiveOptionsPanel) {
		//super( frame, item );
		super();
		//this.frame = frame;
		this.accountItem = account;
		this.imapItem = item;
		this.receiveOptionsPanel = receiveOptionsPanel;

		initComponents();

		updateComponents(true);
	}

	public String getHost() {
		return hostTextField.getText();
	}

	public String getLogin() {
		return loginTextField.getText();
	}

	public boolean isPopAccount() {
		if (popItem != null)
			return true;
		else
			return false;
	}

	protected void updateComponents(boolean b) {

		if (b) {
			if (isPopAccount()) {
				loginTextField.setText(popItem.getUser());
				passwordTextField.setText(popItem.getPassword());
				hostTextField.setText(popItem.getHost());
				portTextField.setText(popItem.getPort());

				if (popItem.isSavePassword())
					storePasswordCheckBox.setSelected(true);

				authenticationComboBox.setSelectedItem(
					popItem.getLoginMethod());

				if (popItem.isUseDefaultAccount())
					defaultAccountCheckBox.setSelected(true);
				else
					defaultAccountCheckBox.setSelected(false);

				

			} else {

				loginTextField.setText(imapItem.getUser());
				passwordTextField.setText(imapItem.getPassword());
				hostTextField.setText(imapItem.getHost());
				portTextField.setText(imapItem.getPort());

				if (imapItem.isSavePassword())
					storePasswordCheckBox.setSelected(true);

				if (imapItem.isUseDefaultAccount())
					defaultAccountCheckBox.setSelected(true);
				else
					defaultAccountCheckBox.setSelected(false);

			}
			
			if (MailConfig
					.getAccountList()
					.getDefaultAccountUid()
					== accountItem.getUid()) {
					defaultAccountCheckBox.setEnabled(false);
				} else {
					defaultAccountCheckBox.setEnabled(true);
				}
				
			if ( defaultAccountCheckBox.isEnabled() && defaultAccountCheckBox.isSelected() ) 
			{
				showDefaultAccountWarning();
			}
			else
			{
				layoutComponents();
			}
				
				
		} else {
			if (isPopAccount()) {
				popItem.setUser(loginTextField.getText());
				popItem.setHost(hostTextField.getText());
				popItem.setPassword(passwordTextField.getText());
				popItem.setPort(portTextField.getText());

				if (storePasswordCheckBox.isSelected() == true)
					popItem.setSavePassword("true"); //$NON-NLS-1$
				else
					popItem.setSavePassword("false"); //$NON-NLS-1$

				popItem.setLoginMethod(
					(String) authenticationComboBox.getSelectedItem());

				popItem.setUseDefaultAccount(
					defaultAccountCheckBox.isSelected());

			} else {

				imapItem.setUser(loginTextField.getText());
				imapItem.setHost(hostTextField.getText());
				imapItem.setPassword(passwordTextField.getText());
				imapItem.setPort(portTextField.getText());

				if (storePasswordCheckBox.isSelected() == true)
					imapItem.setSavePassword("true"); //$NON-NLS-1$
				else
					imapItem.setSavePassword("false"); //$NON-NLS-1$

				imapItem.setUseDefaultAccount(
					defaultAccountCheckBox.isSelected());

			}

		}
	}
	
	protected void showDefaultAccountWarning()
	{
		
		
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		GridBagLayout mainLayout = new GridBagLayout();
		GridBagConstraints mainConstraints = new GridBagConstraints();

		setLayout(mainLayout);
		
		mainConstraints.gridwidth = GridBagConstraints.REMAINDER;
		mainConstraints.anchor = GridBagConstraints.NORTHWEST;
		mainConstraints.weightx = 1.0;
		mainConstraints.insets = new Insets(0, 10, 5, 0);
		mainLayout.setConstraints(defaultAccountCheckBox, mainConstraints);
		add(defaultAccountCheckBox);
		
		
		mainConstraints = new GridBagConstraints();
		mainConstraints.weighty = 1.0;
		mainConstraints.gridwidth = GridBagConstraints.REMAINDER;
		/*
		mainConstraints.fill = GridBagConstraints.BOTH;
		mainConstraints.insets = new Insets(0, 0, 0, 0);
		mainConstraints.gridwidth = GridBagConstraints.REMAINDER;
		mainConstraints.weightx = 1.0;
		mainConstraints.weighty = 1.0;
		*/
		
		JLabel label = new JLabel(MailResourceLoader.getString("dialog","account","using_default_account_settings") );
		Font newFont = label.getFont().deriveFont( Font.BOLD );
		label.setFont( newFont );
		mainLayout.setConstraints( label, mainConstraints );
		add( label );
		
		
		
			
	}
	
	protected void layoutComponents()
	{
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		GridBagLayout mainLayout = new GridBagLayout();
		GridBagConstraints mainConstraints = new GridBagConstraints();

		mainConstraints.anchor = GridBagConstraints.NORTHWEST;
		mainConstraints.fill = GridBagConstraints.HORIZONTAL;
		mainConstraints.weightx = 1.0;

		setLayout(mainLayout);
		
		mainConstraints.gridwidth = GridBagConstraints.REMAINDER;
		mainConstraints.insets = new Insets(0, 10, 5, 0);
		mainLayout.setConstraints(defaultAccountCheckBox, mainConstraints);
		add(defaultAccountCheckBox);

		JPanel typePanel = new JPanel();
		Border b1 = BorderFactory.createEtchedBorder();
		Border b2 =
			BorderFactory.createTitledBorder(
				b1,
				MailResourceLoader.getString("dialog", "account", "type"));
		Border emptyBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5);
		Border border = BorderFactory.createCompoundBorder(b2, emptyBorder);
		typePanel.setBorder(border);
		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		typePanel.setLayout(layout);

		mainConstraints.gridwidth = GridBagConstraints.REMAINDER;
		mainConstraints.insets = new Insets(0, 0, 0, 0);
		mainLayout.setConstraints(typePanel, mainConstraints);
		add(typePanel);

		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.WEST;
		c.weightx = 0.0;
		c.gridwidth = GridBagConstraints.RELATIVE;
		layout.setConstraints(typeLabel, c);
		typePanel.add(typeLabel);

		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 0.0;
		c.insets = new Insets(0, 5, 0, 0);
		layout.setConstraints(typeComboBox, c);
		typePanel.add(typeComboBox);

		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 0.0;
		c.insets = new Insets(0, 0, 0, 0);
		layout.setConstraints(typeDescriptionLabel, c);
		typePanel.add(typeDescriptionLabel);

		JPanel configPanel = new JPanel();
		b1 = BorderFactory.createEtchedBorder();
		b2 =
			BorderFactory.createTitledBorder(
				b1,
				MailResourceLoader.getString(
					"dialog",
					"account",
					"configuration"));
		emptyBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5);
		border = BorderFactory.createCompoundBorder(b2, emptyBorder);
		configPanel.setBorder(border);
		layout = new GridBagLayout();
		c = new GridBagConstraints();
		configPanel.setLayout(layout);

		mainConstraints.gridwidth = GridBagConstraints.REMAINDER;
		mainConstraints.insets = new Insets(0, 0, 0, 0);
		mainLayout.setConstraints(configPanel, mainConstraints);
		add(configPanel);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.weightx = 0.1;
		c.gridwidth = GridBagConstraints.RELATIVE;
		layout.setConstraints(loginLabel, c);
		configPanel.add(loginLabel);

		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 0.9;
		layout.setConstraints(loginTextField, c);
		configPanel.add(loginTextField);

		c.gridwidth = GridBagConstraints.RELATIVE;
		c.weightx = 0.1;
		layout.setConstraints(hostLabel, c);
		configPanel.add(hostLabel);

		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 0.9;
		layout.setConstraints(hostTextField, c);
		configPanel.add(hostTextField);

		c.gridwidth = GridBagConstraints.RELATIVE;
		c.weightx = 0.1;
		layout.setConstraints(portLabel, c);
		configPanel.add(portLabel);

		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 0.9;
		layout.setConstraints(portTextField, c);
		configPanel.add(portTextField);

		JPanel securityPanel = new JPanel();
		b1 = BorderFactory.createEtchedBorder();
		b2 =
			BorderFactory.createTitledBorder(
				b1,
				MailResourceLoader.getString("dialog", "account", "security"));

		emptyBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5);
		border = BorderFactory.createCompoundBorder(b2, emptyBorder);
		securityPanel.setBorder(border);

		mainConstraints.gridwidth = GridBagConstraints.REMAINDER;
		mainLayout.setConstraints(securityPanel, mainConstraints);
		add(securityPanel);

		layout = new GridBagLayout();
		c = new GridBagConstraints();
		securityPanel.setLayout(layout);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = GridBagConstraints.RELATIVE;
		c.weightx = 0.0;
		layout.setConstraints(authenticationLabel, c);
		securityPanel.add(authenticationLabel);

		c.gridwidth = GridBagConstraints.REMAINDER;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 1.0;
		c.insets = new Insets(0, 5, 0, 0);
		layout.setConstraints(authenticationComboBox, c);
		securityPanel.add(authenticationComboBox);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.insets = new Insets(0, 0, 0, 0);
		layout.setConstraints(storePasswordCheckBox, c);
		securityPanel.add(storePasswordCheckBox);

		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 1.0;
		layout.setConstraints(secureCheckBox, c);
		securityPanel.add(secureCheckBox);

		mainConstraints.gridheight = GridBagConstraints.REMAINDER;
		mainConstraints.weighty = 1.0;
		mainConstraints.fill = GridBagConstraints.VERTICAL;
		Component vglue = Box.createVerticalGlue();
		mainLayout.setConstraints(vglue, mainConstraints);
		add(vglue);
	}

	protected void initComponents() {
		

		defaultAccountCheckBox =
			new JCheckBox(
				MailResourceLoader.getString(
					"dialog",
					"account",
					"use_default_account_settings"));
		defaultAccountCheckBox.setMnemonic(
			MailResourceLoader.getMnemonic(
				"dialog",
				"account",
				"use_default_account_settings"));
		defaultAccountCheckBox.setActionCommand("DEFAULT_ACCOUNT");
		defaultAccountCheckBox.addActionListener(this);
		
		//defaultAccountCheckBox.setEnabled(false);
		typeLabel =
			new JLabel(
				MailResourceLoader.getString(
					"dialog",
					"account",
					"server_type"));
		typeLabel.setDisplayedMnemonic(
			MailResourceLoader.getMnemonic("dialog", "account", "server_type"));
		typeComboBox = new JComboBox();
		typeComboBox.addItem("POP3");
		typeComboBox.addItem("IMAP4");
		if (accountItem.isPopAccount())
			typeComboBox.setSelectedIndex(0);
		else
			typeComboBox.setSelectedIndex(1);

		typeLabel.setLabelFor(typeComboBox);
		typeComboBox.setEnabled(false);

		typeDescriptionLabel =
			new JLabel("Description: To connect to and fetch new messages from a POP3-server.");
		typeDescriptionLabel.setEnabled(false);

		loginLabel = new JLabel();
		loginLabel.setText(
			MailResourceLoader.getString("dialog", "account", "login"));
		loginLabel.setDisplayedMnemonic(
			MailResourceLoader.getMnemonic("dialog", "account", "login"));
		//$NON-NLS-1$
		loginTextField = new JTextField();
		loginLabel.setLabelFor(loginTextField);
		passwordLabel = new JLabel();
		passwordLabel.setText(
			MailResourceLoader.getString("dialog", "account", "password"));
		//$NON-NLS-1$
		passwordTextField = new JTextField();

		hostLabel = new JLabel();
		hostLabel.setText(
			MailResourceLoader.getString("dialog", "account", "host"));
		hostLabel.setDisplayedMnemonic(
			MailResourceLoader.getMnemonic("dialog", "account", "host"));
		//$NON-NLS-1$

		hostTextField = new JTextField();
		hostLabel.setLabelFor(hostTextField);

		portLabel = new JLabel();
		portLabel.setText(
			MailResourceLoader.getString("dialog", "account", "port"));
		portLabel.setDisplayedMnemonic(
			MailResourceLoader.getMnemonic("dialog", "account", "port"));
		//$NON-NLS-1$
		portTextField = new JTextField();
		portLabel.setLabelFor(portTextField);

		storePasswordCheckBox = new JCheckBox();
		storePasswordCheckBox.setText(
			MailResourceLoader.getString(
				"dialog",
				"account",
				"store_password_in_configuration_file"));
		storePasswordCheckBox.setMnemonic(
			MailResourceLoader.getMnemonic(
				"dialog",
				"account",
				"store_password_in_configuration_file"));

		secureCheckBox = new JCheckBox();
		secureCheckBox.setText(
			MailResourceLoader.getString(
				"dialog",
				"account",
				"use_SSL_for_secure_connection"));
		secureCheckBox.setEnabled(false);
		secureCheckBox.setMnemonic(
			MailResourceLoader.getMnemonic(
				"dialog",
				"account",
				"use_SSL_for_secure_connection"));
		authenticationLabel =
			new JLabel(
				MailResourceLoader.getString(
					"dialog",
					"account",
					"authentication_type"));
		authenticationLabel.setDisplayedMnemonic(
			MailResourceLoader.getMnemonic(
				"dialog",
				"account",
				"authentication_type"));

		authenticationComboBox = new JComboBox();
		authenticationLabel.setLabelFor(authenticationComboBox);
		if (accountItem.isPopAccount()) {
			authenticationComboBox.addItem("USER");
			authenticationComboBox.addItem("APOP");
		} else {
			authenticationComboBox.addItem("LOGIN");

		}

		

	}

	public void actionPerformed(ActionEvent e) {
		String action = e.getActionCommand();

		if (action.equals("SERVER")) //$NON-NLS-1$
			{
			System.out.println("selection changed");

		}
		else if ( action.equals("DEFAULT_ACCOUNT") )
		{
			removeAll();
			receiveOptionsPanel.removeAll();
			
			if( defaultAccountCheckBox.isSelected() )
			{
				showDefaultAccountWarning();
				receiveOptionsPanel.showDefaultAccountWarning();
			}
			else
			{
				layoutComponents();
				receiveOptionsPanel.layoutComponents();
			}
				
			revalidate();
			receiveOptionsPanel.revalidate();
		}
		
	}

	public boolean isFinished() {
		boolean result = false;

		String host = getHost();
		String login = getLogin();

		if (host.length() == 0) {
			JOptionPane.showMessageDialog(
				null,
				MailResourceLoader.getString(
					"dialog",
					"account",
					"You_have_to_enter_a_host_name"));
			//$NON-NLS-1$
		} else if (login.length() == 0) {
			JOptionPane.showMessageDialog(
				null,
				MailResourceLoader.getString(
					"dialog",
					"account",
					"You_have_to_enter_a_login_name"));
			//$NON-NLS-1$

		} else {

			result = true;
		}

		return result;
	}

}