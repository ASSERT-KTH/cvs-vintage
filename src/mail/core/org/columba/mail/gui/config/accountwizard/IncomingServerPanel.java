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
package org.columba.mail.gui.config.accountwizard;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.columba.core.gui.util.MultiLineLabel;
import org.columba.core.gui.util.wizard.DefaultWizardPanel;
import org.columba.core.gui.util.wizard.WizardTextField;
import org.columba.mail.util.MailResourceLoader;

public class IncomingServerPanel
	extends DefaultWizardPanel
	
{
	private JLabel loginLabel;
	private JTextField loginTextField;

	private JLabel hostLabel;
	private JTextField hostTextField;

	private JLabel addressLabel;

	private JLabel typeLabel;
	private JComboBox typeComboBox;

	public IncomingServerPanel(
		JDialog dialog,
		ActionListener listener,
		String title,
		String description,
		ImageIcon icon)
	{
		super(dialog, listener, title, description, icon);
		
		JPanel panel = this;
				panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
				panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 20, 30));

				MultiLineLabel label =
					new MultiLineLabel(MailResourceLoader.getString("dialog","accountwizard","please_specify_your_incoming_mail_server_properties")); //$NON-NLS-1$

				panel.add(label);

				panel.add(Box.createRigidArea(new java.awt.Dimension(0, 40)));

				WizardTextField middlePanel = new WizardTextField();
				JLabel nameLabel = new JLabel(MailResourceLoader.getString("dialog","account","login")); //$NON-NLS-1$
				nameLabel.setDisplayedMnemonic(MailResourceLoader.getMnemonic("dialog","account","login")); //$NON-NLS-1$
				middlePanel.addLabel(nameLabel);
				loginTextField = new JTextField("");
				
				nameLabel.setLabelFor(loginTextField);
				//register(loginTextField);
				middlePanel.addTextField(loginTextField);
				JLabel exampleLabel = new JLabel(MailResourceLoader.getString("dialog","accountwizard","example__billgates")); //$NON-NLS-1$
				middlePanel.addExample(exampleLabel);

				addressLabel = new JLabel(MailResourceLoader.getString("dialog","accountwizard","host_pop3_server")); //$NON-NLS-1$
				addressLabel.setDisplayedMnemonic(MailResourceLoader.getMnemonic("dialog","accountwizard","host_pop3_server")); //$NON-NLS-1$)
				middlePanel.addLabel(addressLabel);
				hostTextField = new JTextField("");
				addressLabel.setLabelFor(hostTextField);
				//register(hostTextField);
				middlePanel.addTextField(hostTextField);
				JLabel addressExampleLabel = new JLabel(MailResourceLoader.getString("dialog","accountwizard","example__mail.microsoft.com")); //$NON-NLS-1$
				middlePanel.addExample(addressExampleLabel);

				JLabel typeLabel = new JLabel(MailResourceLoader.getString("dialog","account","type")); //$NON-NLS-1$
				typeLabel.setDisplayedMnemonic( MailResourceLoader.getMnemonic("dialog","account","type"));
				middlePanel.addLabel(typeLabel);
				typeComboBox = new JComboBox();
				typeLabel.setLabelFor(typeComboBox);
				typeComboBox.addItem("POP3");
				typeComboBox.addItem("IMAP4");
				typeComboBox.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						if (typeComboBox.getSelectedIndex() == 0)
						{
							addressLabel.setText(MailResourceLoader.getString("dialog","accountwizard","host_pop3_server")); //$NON-NLS-1$
							addressLabel.setDisplayedMnemonic( MailResourceLoader.getMnemonic("dialog","accountwizard","host_pop3_server"));
						}
						else
						{
							addressLabel.setText(MailResourceLoader.getString("dialog","accountwizard","host_imapserver")); //$NON-NLS-1$
							addressLabel.setDisplayedMnemonic( MailResourceLoader.getMnemonic("dialog","accountwizard","host_imapserver"));
						}
					}
				});

				middlePanel.addTextField(typeComboBox);
				middlePanel.addEmptyExample();
				panel.add(middlePanel);

	}

	public boolean isPopAccount()
	{
		if (typeComboBox.getSelectedIndex() == 0)
			return true;
		else
			return false;
	}
	
	public JTextField getIncomingHostTextField()
	{
		return hostTextField;
	}

	public String getHost()
	{
		return hostTextField.getText();
	}

	public String getLogin()
	{
		return loginTextField.getText();
	}

	/*
	protected JPanel createPanel(ActionListener listener)
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 20, 30));

		MultiLineLabel label =
			new MultiLineLabel(MailResourceLoader.getString("dialog","accountwizard","please_specify_your_incoming_mail_server_properties")); //$NON-NLS-1$

		panel.add(label);

		panel.add(Box.createRigidArea(new java.awt.Dimension(0, 40)));

		WizardTextField middlePanel = new WizardTextField();
		JLabel nameLabel = new JLabel(MailResourceLoader.getString("dialog","accountwizard","login")); //$NON-NLS-1$
		nameLabel.setDisplayedMnemonic(MailResourceLoader.getMnemonic("dialog","accountwizard","login")); //$NON-NLS-1$
		middlePanel.addLabel(nameLabel);
		loginTextField = new JTextField("");
		nameLabel.setLabelFor(loginTextField);
		//register(loginTextField);
		middlePanel.addTextField(loginTextField);
		JLabel exampleLabel = new JLabel(MailResourceLoader.getString("dialog","accountwizard","example__billgates")); //$NON-NLS-1$
		middlePanel.addExample(exampleLabel);

		addressLabel = new JLabel(MailResourceLoader.getString("dialog","accountwizard","host_pop3_server")); //$NON-NLS-1$
		addressLabel.setDisplayedMnemonic(MailResourceLoader.getMnemonic("dialog","accountwizard","host_pop3_server")); //$NON-NLS-1$)
		middlePanel.addLabel(addressLabel);
		hostTextField = new JTextField("");
		addressLabel.setLabelFor(hostTextField);
		//register(hostTextField);
		middlePanel.addTextField(hostTextField);
		JLabel addressExampleLabel = new JLabel(MailResourceLoader.getString("dialog","accountwizard","example__mail.microsoft.com")); //$NON-NLS-1$
		middlePanel.addExample(addressExampleLabel);

		JLabel typeLabel = new JLabel(MailResourceLoader.getString("dialog","accountwizard","type")); //$NON-NLS-1$
		typeLabel.setDisplayedMnemonic( MailResourceLoader.getMnemonic("dialog","accountwizard","type"));
		middlePanel.addLabel(typeLabel);
		typeComboBox = new JComboBox();
		typeLabel.setLabelFor(typeComboBox);
		typeComboBox.addItem(MailResourceLoader.getString("dialog","accountwizard","pop3")); //$NON-NLS-1$
		typeComboBox.addItem(MailResourceLoader.getString("dialog","accountwizard","imap4")); //$NON-NLS-1$
		typeComboBox.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (typeComboBox.getSelectedIndex() == 0)
				{
					addressLabel.setText(MailResourceLoader.getString("dialog","accountwizard","host_pop3_server")); //$NON-NLS-1$
					addressLabel.setDisplayedMnemonic( MailResourceLoader.getMnemonic("dialog","accountwizard","host_pop3_server"));
				}
				else
				{
					addressLabel.setText(MailResourceLoader.getString("dialog","accountwizard","host_imapserver")); //$NON-NLS-1$
					addressLabel.setDisplayedMnemonic( MailResourceLoader.getMnemonic("dialog","accountwizard","host_imapserver"));
				}
			}
		});

		middlePanel.addTextField(typeComboBox);
		middlePanel.addEmptyExample();
		panel.add(middlePanel);

		return panel;
	}
	*/
	
	/**
	 * @see org.columba.core.gui.util.wizard.DefaultWizardPanel#getFocusComponent()
	 */
	public JComponent getFocusComponent() {
		return loginTextField;
	}

}
