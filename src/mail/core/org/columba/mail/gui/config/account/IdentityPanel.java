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
package org.columba.mail.gui.config.account;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.columba.core.gui.util.CheckBoxWithMnemonic;
import org.columba.core.gui.util.DefaultFormBuilder;
import org.columba.core.gui.util.LabelWithMnemonic;
import org.columba.mail.config.AccountItem;
import org.columba.mail.config.IdentityItem;
import org.columba.mail.config.MailConfig;
import org.columba.mail.util.MailResourceLoader;

import com.jgoodies.forms.layout.FormLayout;

public class IdentityPanel extends DefaultPanel implements ActionListener {

	private JLabel nameLabel;
	private JTextField nameTextField;
	private JLabel addressLabel;
	private JTextField addressTextField;
	private JLabel organisationLabel;
	private JTextField organisationTextField;
	private JLabel replyaddressLabel;
	private JTextField replyaddressTextField;
	private JLabel accountnameLabel;
	private JTextField accountnameTextField;

	private JCheckBox defaultAccountCheckBox;

	private JButton selectSignatureButton;

	private JCheckBox attachsignatureCheckBox;
	private JTextArea attachsignatureTextArea;

	private IdentityItem identityItem;
	private AccountItem accountItem;

	//private ConfigFrame frame;

	public IdentityPanel(AccountItem account, IdentityItem identity) {
		super();

		//this.frame = frame;
		this.accountItem = account;
		this.identityItem = identity;

		initComponents();

		layoutComponents();

		updateComponents(true);

	}

	protected String getAccountName() {
		return accountnameTextField.getText();
	}

	protected String getAddress() {
		return addressTextField.getText();
	}

	protected void updateComponents(boolean b) {
		if (b) {
			accountnameTextField.setText(accountItem.getName());
			nameTextField.setText(identityItem.get("name"));
			addressTextField.setText(identityItem.get("address"));
			replyaddressTextField.setText(identityItem.get("reply_address"));
			organisationTextField.setText(identityItem.get("organisation"));

			selectSignatureButton.setText(identityItem.get("attach_file"));
			/*
			attachsignatureTextArea.setText(identityItem.getSignature());
			*/

			attachsignatureCheckBox.setSelected(
				identityItem.getBoolean("attach_signature"));

			defaultAccountCheckBox.setSelected(
				MailConfig.getAccountList().getDefaultAccountUid()
					== accountItem.getInteger("uid"));

		} else {
			if (nameTextField.getText() != null)
				identityItem.set("name", nameTextField.getText());

			identityItem.set("address", addressTextField.getText());
			identityItem.set("organisation", organisationTextField.getText());
			identityItem.set("reply_address", replyaddressTextField.getText());

			identityItem.set("signature_file", selectSignatureButton.getText());
			/*
			identityItem.setSignature(attachsignatureTextArea.getText());
			*/

			identityItem.set(
				"attach_signature",
				attachsignatureCheckBox.isSelected());

			accountItem.setName(accountnameTextField.getText());

			if (defaultAccountCheckBox.isSelected()) {
				MailConfig.getAccountList().setDefaultAccount(
					accountItem.getUid());
			}

		}
	}

	protected void initComponents() {
		accountnameLabel =
			new LabelWithMnemonic(
				MailResourceLoader.getString(
					"dialog",
					"account",
					"identity_accountname"));
					
		/*
		accountnameLabel.setDisplayedMnemonic(
			MailResourceLoader.getMnemonic(
				"dialog",
				"account",
				"identity_accountname"));
				*/
		accountnameTextField = new JTextField();
		accountnameLabel.setLabelFor(accountnameTextField);
		defaultAccountCheckBox =
			new CheckBoxWithMnemonic(
				MailResourceLoader.getString(
					"dialog",
					"account",
					"defaultaccount"));
		/*
		defaultAccountCheckBox.setMnemonic(
			MailResourceLoader.getMnemonic(
				"dialog",
				"account",
				"defaultaccount"));
		*/
		
		nameLabel =
			new LabelWithMnemonic(
				MailResourceLoader.getString("dialog", "account", "yourname"));
		/*
		nameLabel.setDisplayedMnemonic(
			MailResourceLoader.getMnemonic("dialog", "account", "yourname"));
		*/
		
		nameTextField = new JTextField();
		nameLabel.setLabelFor(nameTextField);
		addressLabel =
			new LabelWithMnemonic(
				MailResourceLoader.getString("dialog", "account", "address"));
		/*
		addressLabel.setDisplayedMnemonic(
			MailResourceLoader.getMnemonic("dialog", "account", "address"));
		*/
		addressTextField = new JTextField();
		addressLabel.setLabelFor(addressTextField);
		replyaddressLabel =
			new LabelWithMnemonic(
				MailResourceLoader.getString(
					"dialog",
					"account",
					"replytoaddress"));
		/*
		replyaddressLabel.setDisplayedMnemonic(
			MailResourceLoader.getMnemonic(
				"dialog",
				"account",
				"replytoaddress"));
		*/
		
		organisationLabel =
			new LabelWithMnemonic(
				MailResourceLoader.getString(
					"dialog",
					"account",
					"organisation"));
					
		/*
		organisationLabel.setDisplayedMnemonic(
			MailResourceLoader.getMnemonic(
				"dialog",
				"account",
				"organisation"));
		*/
		
		replyaddressTextField = new JTextField();
		replyaddressLabel.setLabelFor(replyaddressTextField);
		organisationTextField = new JTextField();
		organisationLabel.setLabelFor(organisationTextField);

		attachsignatureCheckBox =
			new CheckBoxWithMnemonic(
				MailResourceLoader.getString(
					"dialog",
					"account",
					"attachthissignature"));
		/*
		attachsignatureCheckBox.setMnemonic(
			MailResourceLoader.getMnemonic(
				"dialog",
				"account",
				"attachthissignature"));
		*/
		
		selectSignatureButton = new JButton("~/.signature");
		selectSignatureButton.setActionCommand("CHOOSE");
		selectSignatureButton.addActionListener(this);
	}

	protected void layoutComponents() {

		// Create a FormLayout instance. 
		FormLayout layout =
			new FormLayout("10dlu, max(100;default), 3dlu, fill:max(150dlu;default):grow",
			// 2 columns
	""); // rows are added dynamically (no need to define them here)

		// create a form builder
		DefaultFormBuilder builder = new DefaultFormBuilder(this, layout);

		// create EmptyBorder between components and dialog-frame 
		builder.setDefaultDialogBorder();

		// skip the first column
		builder.setLeadingColumnOffset(1);

		// Add components to the panel:

		builder.appendSeparator(
			MailResourceLoader.getString(
				"dialog",
				"account",
				"account_information"));
		builder.nextLine();

		builder.append(accountnameLabel, 1);
		builder.append(accountnameTextField);
		builder.nextLine();

		builder.append(defaultAccountCheckBox, 3);
		builder.nextLine();

		builder.appendSeparator(
			MailResourceLoader.getString(
				"dialog",
				"account",
				"needed_information"));
		builder.nextLine();

		builder.append(nameLabel, 1);
		builder.append(nameTextField);
		builder.nextLine();

		builder.append(organisationLabel, 1);
		builder.append(organisationTextField);
		builder.nextLine();

		builder.appendSeparator(
			MailResourceLoader.getString(
				"dialog",
				"account",
				"optional_information"));
		builder.nextLine();

		builder.append(replyaddressLabel, 1);
		builder.append(replyaddressTextField);
		builder.nextLine();

		JPanel panel = new JPanel();
		FormLayout l =
			new FormLayout("max(100;default), 3dlu, left:max(50dlu;default)",
			// 2 columns
	""); // rows are added dynamically (no need to define them here)

		// create a form builder
		DefaultFormBuilder b = new DefaultFormBuilder(panel, l);
		b.append(attachsignatureCheckBox, selectSignatureButton);
		//b.append(selectSignatureButton);

		builder.append(panel, 3);
		builder.nextLine();

		/*
		JPanel innerPanel = builder.getPanel();
		FormDebugUtils.dumpAll(innerPanel);
		setLayout(new BorderLayout());
		add(innerPanel, BorderLayout.CENTER);
		*/
		/*
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		
		GridBagLayout mainLayout = new GridBagLayout();
		GridBagConstraints mainConstraints = new GridBagConstraints();
		
		mainConstraints.anchor = GridBagConstraints.NORTHWEST;
		mainConstraints.fill = GridBagConstraints.HORIZONTAL;
		mainConstraints.weightx = 1.0;
		
		setLayout(mainLayout);
		
		JPanel accountPanel = new JPanel();
		Border b1 = BorderFactory.createEtchedBorder();
		Border b2 =
			BorderFactory.createTitledBorder(
				b1,
				MailResourceLoader.getString(
					"dialog",
					"account",
					"account_information"));
		
		Border emptyBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5);
		Border border = BorderFactory.createCompoundBorder(b2, emptyBorder);
		accountPanel.setBorder(border);
		
		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		accountPanel.setLayout(layout);
		
		//defaultAccountCheckBox.setEnabled(false);
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.weightx = 0.1;
		c.gridwidth = GridBagConstraints.RELATIVE;
		layout.setConstraints(accountnameLabel, c);
		accountPanel.add(accountnameLabel);
		
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 0.9;
		layout.setConstraints(accountnameTextField, c);
		accountPanel.add(accountnameTextField);
		
		c.gridwidth = GridBagConstraints.REMAINDER;
		layout.setConstraints(defaultAccountCheckBox, c);
		accountPanel.add(defaultAccountCheckBox);
		
		mainConstraints.gridwidth = GridBagConstraints.REMAINDER;
		
		mainLayout.setConstraints(accountPanel, mainConstraints);
		add(accountPanel);
		
		JPanel neededPanel = new JPanel();
		b1 = BorderFactory.createEtchedBorder();
		b2 =
			BorderFactory.createTitledBorder(
				b1,
				MailResourceLoader.getString(
					"dialog",
					"account",
					"needed_information"));
		
		emptyBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5);
		border = BorderFactory.createCompoundBorder(b2, emptyBorder);
		neededPanel.setBorder(border);
		
		layout = new GridBagLayout();
		c = new GridBagConstraints();
		neededPanel.setLayout(layout);
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.weightx = 0.1;
		
		c.gridwidth = GridBagConstraints.RELATIVE;
		layout.setConstraints(nameLabel, c);
		neededPanel.add(nameLabel);
		
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 0.9;
		layout.setConstraints(nameTextField, c);
		neededPanel.add(nameTextField);
		
		c.gridwidth = GridBagConstraints.RELATIVE;
		c.weightx = 0.1;
		layout.setConstraints(addressLabel, c);
		neededPanel.add(addressLabel);
		
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 0.9;
		layout.setConstraints(addressTextField, c);
		neededPanel.add(addressTextField);
		
		mainConstraints.gridwidth = GridBagConstraints.REMAINDER;
		
		mainLayout.setConstraints(neededPanel, mainConstraints);
		add(neededPanel);
		
		JPanel optionalPanel = new JPanel();
		b1 = BorderFactory.createEtchedBorder();
		b2 =
			BorderFactory.createTitledBorder(
				b1,
				MailResourceLoader.getString(
					"dialog",
					"account",
					"optional_information"));
		
		emptyBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5);
		border = BorderFactory.createCompoundBorder(b2, emptyBorder);
		
		optionalPanel.setBorder(border);
		
		layout = new GridBagLayout();
		c = new GridBagConstraints();
		optionalPanel.setLayout(layout);
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.weightx = 0.1;
		
		c.gridwidth = GridBagConstraints.RELATIVE;
		layout.setConstraints(replyaddressLabel, c);
		optionalPanel.add(replyaddressLabel);
		
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 0.9;
		layout.setConstraints(replyaddressTextField, c);
		optionalPanel.add(replyaddressTextField);
		
		c.gridwidth = GridBagConstraints.RELATIVE;
		c.weightx = 0.1;
		layout.setConstraints(organisationLabel, c);
		optionalPanel.add(organisationLabel);
		
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 0.9;
		layout.setConstraints(organisationTextField, c);
		optionalPanel.add(organisationTextField);
		
		c.gridwidth = GridBagConstraints.RELATIVE;
		c.weightx = 0.1;
		layout.setConstraints(attachsignatureCheckBox, c);
		optionalPanel.add(attachsignatureCheckBox);
		
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 0.9;
		layout.setConstraints(selectSignatureButton, c);
		optionalPanel.add(selectSignatureButton);
		
		mainConstraints.gridwidth = GridBagConstraints.REMAINDER;
		mainLayout.setConstraints(optionalPanel, mainConstraints);
		add(optionalPanel);
		
		mainConstraints.gridheight = GridBagConstraints.REMAINDER;
		mainConstraints.weighty = 1.0;
		mainConstraints.fill = GridBagConstraints.VERTICAL;
		Component vglue = Box.createVerticalGlue();
		mainLayout.setConstraints(vglue, mainConstraints);
		add(vglue);
		*/

	}

	public boolean isFinished() {
		boolean result = true;

		String name = getAccountName();
		String address = getAddress();

		if (name.length() == 0) {
			result = false;
			JOptionPane.showMessageDialog(
				null,
				MailResourceLoader.getString("dialog", "account", "namelabel"));
			//$NON-NLS-1$
		} else if (address.length() == 0) {
			result = false;
			JOptionPane.showMessageDialog(
				null,
				MailResourceLoader.getString(
					"dialog",
					"account",
					"addresslabel"));
			//$NON-NLS-1$
		} else {
			result = true;
		}

		return result;
	}

	public void actionPerformed(ActionEvent e) {
		String action = e.getActionCommand();

		if (action.equals("CHOOSE")) //$NON-NLS-1$
			{
			JFileChooser fc = new JFileChooser();
			int returnVal = fc.showOpenDialog(null);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				selectSignatureButton.setText(file.getPath());

			}
		}
	}
}