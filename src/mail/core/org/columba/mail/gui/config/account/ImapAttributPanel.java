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

/*
 * PopAttributPanel.java
 *
 * Created on 2. November 2000, 00:12
 */

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.columba.mail.config.ImapItem;
import org.columba.mail.util.MailResourceLoader;

/**
 *
 * @author  freddy
 * @version
 */
public class ImapAttributPanel extends JPanel {

	private ImapItem item;

	private JCheckBox secureCheckBox;
	private JCheckBox storePasswordCheckBox;

	private JCheckBox automaticallyApplyFilterCheckBox;

	private JCheckBox intervalCheckingCheckBox;
	private JPanel jPanel1;
	private JLabel intervalCheckingLabel;
	private JLabel intervalCheckingLabel2;
	private JTextField intervalCheckingTextField;

	private JCheckBox cleanupCheckBox;
	private JPanel cleanupPanel;

	private JCheckBox emptyTrashCheckBox;
	private JPanel emptyTrashPanel;

	//private ConfigFrame frame;

	public ImapAttributPanel(ImapItem item) {
		//super( " Imap4 Settings " );

		this.item = item;
		initComponents();
	}

	public void updateComponents(boolean b) {

		if (b) {

			/*
			if ( item.isSavePassword() )
			    storePasswordCheckBox.setSelected(true);
			    */

			automaticallyApplyFilterCheckBox.setSelected(
				item.getBoolean("automatically_apply_filter"));
		} else {

			/*
			if ( storePasswordCheckBox.isSelected() == true )
			    item.setSavePassword("true");
			else
			    item.setSavePassword("false");
			    */

			item.set(
				"automatically_apply_filter",
				automaticallyApplyFilterCheckBox.isSelected());

		}

	}

	private void initComponents() {

		/*
		setLayout(new BorderLayout());
		setBorder(
			javax.swing.BorderFactory.createTitledBorder(
				javax.swing.BorderFactory.createEtchedBorder(),
				MailResourceLoader.getString("dialog","account", "imap")));
		//$NON-NLS-1$
		
		JPanel innerPanel = new JPanel();
		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		innerPanel.setLayout(layout);
		innerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		
		add(innerPanel, BorderLayout.NORTH);
		*/
		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		setLayout(layout);

		JPanel intervalCheckingPanel = new JPanel();
		//intervalCheckingPanel.add( Box.createRigidArea( new java.awt.Dimension(10,0) ) );
		intervalCheckingPanel.setLayout(
			new BoxLayout(intervalCheckingPanel, BoxLayout.X_AXIS));
		intervalCheckingCheckBox = new JCheckBox();
		intervalCheckingCheckBox.setEnabled(false);
		intervalCheckingCheckBox.setText(
			MailResourceLoader.getString(
				"dialog/account",
				"imapattributpanel",
				"enable_interval_message_checking"));
		intervalCheckingCheckBox.setMnemonic(
			MailResourceLoader.getMnemonic(
				"dialog/account",
				"imapattributpanel",
				"enable_interval_message_checking"));
		//$NON-NLS-1$
		intervalCheckingCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		intervalCheckingPanel.add(intervalCheckingCheckBox);
		intervalCheckingPanel.add(Box.createHorizontalGlue());
		c.gridx = 0;
		c.weightx = 1.0;
		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = GridBagConstraints.REMAINDER;
		layout.setConstraints(intervalCheckingPanel, c);
		add(intervalCheckingPanel);

		/*
		jPanel1 = new JPanel();
		jPanel1.setLayout(new BoxLayout(jPanel1, BoxLayout.X_AXIS));
		jPanel1.add(Box.createRigidArea(new java.awt.Dimension(30, 0)));
		intervalCheckingLabel = new JLabel();
		intervalCheckingLabel.setEnabled(false);
		intervalCheckingLabel.setText(
			GlobalResourceLoader.getString(
				"dialog/account",
				"imapattributpanel",
				"Check_for_new_messages_every"));
		//$NON-NLS-1$
		jPanel1.add(intervalCheckingLabel);
		jPanel1.add(Box.createRigidArea(new java.awt.Dimension(10, 0)));
		intervalCheckingTextField = new JTextField(5);
		intervalCheckingTextField.setEnabled(false);
		intervalCheckingTextField.setText("18"); //$NON-NLS-1$
		intervalCheckingTextField.setMaximumSize(new java.awt.Dimension(50, 25));
		jPanel1.add(intervalCheckingTextField);
		jPanel1.add(Box.createRigidArea(new java.awt.Dimension(10, 0)));
		intervalCheckingLabel2 = new JLabel();
		intervalCheckingLabel2.setEnabled(false);
		intervalCheckingLabel2.setText(
			GlobalResourceLoader.getString("dialog","account", "minutes"));
		//$NON-NLS-1$
		jPanel1.add(intervalCheckingLabel2);
		jPanel1.add(Box.createHorizontalGlue());
		c.gridx = 0;
		c.weightx = 1.0;
		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = GridBagConstraints.REMAINDER;
		layout.setConstraints(jPanel1, c);
		add(jPanel1);
		*/

		JPanel cleanupPanel = new JPanel();
		//cleanupPanel.add( Box.createRigidArea( new java.awt.Dimension(10,0) ) );
		cleanupPanel.setLayout(new BoxLayout(cleanupPanel, BoxLayout.X_AXIS));
		cleanupCheckBox = new JCheckBox();
		cleanupCheckBox.setEnabled(false);
		cleanupCheckBox.setText(
			MailResourceLoader.getString(
				"dialog",
				"account",
				"Expunge_Inbox_on_Exit"));
		//$NON-NLS-1$
		cleanupCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		cleanupPanel.add(cleanupCheckBox);
		cleanupPanel.add(Box.createHorizontalGlue());
		c.gridx = 0;
		c.weightx = 1.0;
		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = GridBagConstraints.REMAINDER;
		layout.setConstraints(cleanupPanel, c);
		add(cleanupPanel);

		JPanel emptyTrashPanel = new JPanel();
		//emptyTrashPanel.add( Box.createRigidArea( new java.awt.Dimension(10,0) ) );
		emptyTrashPanel.setLayout(
			new BoxLayout(emptyTrashPanel, BoxLayout.X_AXIS));
		emptyTrashCheckBox = new JCheckBox();
		emptyTrashCheckBox.setEnabled(false);
		emptyTrashCheckBox.setText(
			MailResourceLoader.getString(
				"dialog",
				"account",
				"Empty_Trash_on_Exit"));
		//$NON-NLS-1$
		emptyTrashCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		emptyTrashPanel.add(emptyTrashCheckBox);
		emptyTrashPanel.add(Box.createHorizontalGlue());
		c.gridx = 0;
		c.weightx = 1.0;
		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = GridBagConstraints.REMAINDER;
		layout.setConstraints(emptyTrashPanel, c);
		add(emptyTrashPanel);

		automaticallyApplyFilterCheckBox =
			new JCheckBox(
				MailResourceLoader.getString(
					"dialog",
					"account",
					"apply_filter"));
		automaticallyApplyFilterCheckBox.setMnemonic(
			MailResourceLoader.getMnemonic(
				"dialog",
				"account",
				"apply_filter_mnemonic"));

		c.gridx = 0;
		c.weightx = 1.0;
		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = GridBagConstraints.REMAINDER;
		layout.setConstraints(automaticallyApplyFilterCheckBox, c);
		add(automaticallyApplyFilterCheckBox);
	}

}