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

package org.columba.mail.gui.config.accountlist;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.javaprog.ui.wizard.plaf.basic.SingleSideEtchedBorder;

import org.columba.core.config.Config;
import org.columba.core.gui.util.ButtonWithMnemonic;
import org.columba.core.gui.util.DialogStore;
import org.columba.core.help.HelpManager;
import org.columba.core.main.MainInterface;
import org.columba.mail.config.AccountItem;
import org.columba.mail.config.AccountList;
import org.columba.mail.config.MailConfig;
import org.columba.mail.folder.Folder;
import org.columba.mail.folder.FolderTreeNode;
import org.columba.mail.gui.config.account.AccountDialog;
import org.columba.mail.gui.config.accountwizard.AccountWizardLauncher;
import org.columba.mail.util.MailResourceLoader;

public class ConfigFrame
	implements ActionListener, ListSelectionListener //, TreeSelectionListener
{
	/*
	private JTextField textField;
	private JPanel leftPanel;
	private JPanel rightPanel;
	private JButton addpopButton;
	private JButton addimapButton;
	private JButton removeButton;
	private JButton defaultButton;
	private JButton wizardButton;
	private JButton closeButton;
	private JDialog frame;
	private AccountTree tree;
	*/

	private JDialog dialog;
	private Config config;

	//private AccountTreeNode selected;

	/*
	private IdentityPanel identityPanel;
	private IncomingServerPanel incomingPanel;
	private OutgoingServerPanel outgoingServerPanel;
	*/
	private AccountListTable listView;
	//private JSplitPane splitPane;

	private AccountList accountList;
	private AccountItem accountItem;

	//private int panel = -1;

	JTextField nameTextField = new JTextField();

	JButton addButton;
	//JButton enableButton = new JButton();
	//JButton disableButton = new JButton();
	JButton removeButton, editButton;

	//JButton moveupButton = new JButton();
	//JButton movedownButton = new JButton();

	private int index;

	public ConfigFrame() {
		dialog = DialogStore.getDialog();
		dialog.setTitle(
			MailResourceLoader.getString("dialog", "account", "dialog_title"));
		config = MainInterface.config;
		accountList = MailConfig.getAccountList();

		initComponents();
		dialog.getRootPane().registerKeyboardAction(
			this,
			"CLOSE",
			KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
			JComponent.WHEN_IN_FOCUSED_WINDOW);
                dialog.getRootPane().registerKeyboardAction(
                        this,
                        "HELP",
                        KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0),
                        JComponent.WHEN_IN_FOCUSED_WINDOW);
		dialog.pack();
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);
	}

	public AccountItem getSelected() {
		return accountItem;
	}

	public void setSelected(AccountItem item) {
		accountItem = item;
	}

	public void initComponents() {
		dialog.getContentPane().setLayout(new BorderLayout());
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout(5, 0));
		mainPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 11, 11));

		/*
		Border b1 = BorderFactory.createEtchedBorder();
		Border b2 = BorderFactory.createTitledBorder(
				b1,
				MailResourceLoader.getString(
					"dialog",
					"account",
					"account_information"));
		
		Border emptyBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5);
		Border border = BorderFactory.createCompoundBorder(emptyBorder,b2);
		Border border2 = BorderFactory.createCompoundBorder(border,emptyBorder);
		mainPanel.setBorder(border2);
		*/

		addButton = new ButtonWithMnemonic(MailResourceLoader.getString("dialog", "account", "addaccount")); //$NON-NLS-1$
		/*
		addButton.setMnemonic(
			MailResourceLoader.getMnemonic("dialog", "account", "addacount"));
			*/
			
		//addButton.setIcon( ImageLoader.getImageIcon("stock_add_16.png") );
		addButton.setActionCommand("ADD"); //$NON-NLS-1$
		addButton.addActionListener(this);

		removeButton = new ButtonWithMnemonic(MailResourceLoader.getString("dialog", "account", "removeaccount")); //$NON-NLS-1$
		
		/*
		removeButton.setMnemonic(
			MailResourceLoader.getMnemonic(
				"dialog",
				"account",
				"removeacount"));
				*/
				
		removeButton.setActionCommand("REMOVE"); //$NON-NLS-1$
		//removeButton.setIcon( ImageLoader.getImageIcon("stock_remove_16.png") );
		removeButton.setEnabled(false);
		removeButton.addActionListener(this);

		editButton = new ButtonWithMnemonic(MailResourceLoader.getString("dialog", "account", "editsettings")); //$NON-NLS-1$
		/*
		 editButton.setMnemonic(
			MailResourceLoader.getMnemonic(
				"dialog",
				"account",
				"editsettings"));
				*/
				
		editButton.setActionCommand("EDIT"); //$NON-NLS-1$
		//editButton.setIcon( ImageLoader.getImageIcon("stock_properties_16.png") );
		editButton.setEnabled(false);
		editButton.addActionListener(this);

		/*
		enableButton.setText("Enable");
		enableButton.setActionCommand("ENABLE");
		enableButton.addActionListener( this );
		
		disableButton.setText("Disable");
		disableButton.setActionCommand("DISABLE");
		disableButton.addActionListener( this );
		*/

		/*
		moveupButton.setText("Move up");
		moveupButton.setActionCommand("MOVEUP");
		moveupButton.setEnabled( false );
		moveupButton.addActionListener( this );
		
		movedownButton.setText("Move down");
		movedownButton.setActionCommand("MOVEDOWN");
		movedownButton.setEnabled( false );
		movedownButton.addActionListener( this );
		*/

		// top panel

		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
		GridBagLayout gridBagLayout = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		//topPanel.setLayout( );

		JPanel topBorderPanel = new JPanel();
		topBorderPanel.setLayout(new BorderLayout());
		topBorderPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
		topBorderPanel.add(topPanel, BorderLayout.CENTER);

		//mainPanel.add( topBorderPanel, BorderLayout.NORTH );

		JLabel nameLabel = new JLabel(MailResourceLoader.getString("dialog", "account", "name")); //$NON-NLS-1$
		nameLabel.setEnabled(false);
		topPanel.add(nameLabel);

		topPanel.add(Box.createRigidArea(new java.awt.Dimension(10, 0)));
		topPanel.add(Box.createHorizontalGlue());

		nameTextField.setText(MailResourceLoader.getString("dialog", "account", "name")); //$NON-NLS-1$
		nameTextField.setEnabled(false);
		topPanel.add(nameTextField);

		Component glue = Box.createVerticalGlue();
		c.anchor = GridBagConstraints.EAST;
		c.gridwidth = GridBagConstraints.REMAINDER;
		//c.fill = GridBagConstraints.HORIZONTAL;
		gridBagLayout.setConstraints(glue, c);

		gridBagLayout = new GridBagLayout();
		c = new GridBagConstraints();
		JPanel eastPanel = new JPanel(gridBagLayout);
		mainPanel.add(eastPanel, BorderLayout.EAST);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		gridBagLayout.setConstraints(addButton, c);
		eastPanel.add(addButton);

		Component strut1 = Box.createRigidArea(new Dimension(30, 5));
		gridBagLayout.setConstraints(strut1, c);
		eastPanel.add(strut1);

		gridBagLayout.setConstraints(removeButton, c);
		eastPanel.add(removeButton);

		Component strut = Box.createRigidArea(new Dimension(30, 5));
		gridBagLayout.setConstraints(strut, c);
		eastPanel.add(strut);

		gridBagLayout.setConstraints(editButton, c);
		eastPanel.add(editButton);

		strut = Box.createRigidArea(new Dimension(30, 20));
		gridBagLayout.setConstraints(strut, c);
		eastPanel.add(strut);

		glue = Box.createVerticalGlue();
		c.fill = GridBagConstraints.BOTH;
		c.weighty = 1.0;
		gridBagLayout.setConstraints(glue, c);
		eastPanel.add(glue);

		/*
		c.gridheight = GridBagConstraints.REMAINDER;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weighty = 0;
		gridBagLayout.setConstraints(closeButton, c);
		eastPanel.add(closeButton);
		*/

		listView = new AccountListTable(accountList, this);
		listView.getSelectionModel().addListSelectionListener(this);
		JScrollPane scrollPane = new JScrollPane(listView);
		scrollPane.setPreferredSize(new Dimension(300, 250));
		scrollPane.getViewport().setBackground(Color.white);
		mainPanel.add(scrollPane);
		dialog.getContentPane().add(mainPanel);
		JPanel bottomPanel = new JPanel(new BorderLayout());
		bottomPanel.setBorder(new SingleSideEtchedBorder(SwingConstants.TOP));
		JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 5, 0));
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(17, 0, 11, 11));
		JButton closeButton =
			new JButton(MailResourceLoader.getString("global", "close"));
		closeButton.setActionCommand("CLOSE"); //$NON-NLS-1$
		closeButton.addActionListener(this);
		buttonPanel.add(closeButton);
		JButton helpButton =
			new JButton(MailResourceLoader.getString("global", "help"));		
		// associate with JavaHelp
		HelpManager.enableHelpOnButton(helpButton, "configuring_columba");		
		buttonPanel.add(helpButton);
		
		bottomPanel.add(buttonPanel, BorderLayout.EAST);
		dialog.getContentPane().add(bottomPanel, BorderLayout.SOUTH);
		dialog.getRootPane().setDefaultButton(closeButton);
	}

	public void valueChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting())
			return;
		DefaultListSelectionModel theList =
			(DefaultListSelectionModel) e.getSource();
		if (theList.isSelectionEmpty()) {
			removeButton.setEnabled(false);
			editButton.setEnabled(false);
		} else {
			removeButton.setEnabled(true);
			editButton.setEnabled(true);
			//String value = (String) theList.getSelectedValue();
			index = theList.getAnchorSelectionIndex();

			setSelected(accountList.get(index));
		}
	}

	public void showAccountDialog() {
		AccountItem parent = getSelected();
		if (parent != null) {
			AccountDialog dialog = new AccountDialog(parent);
		}
	}

	public void actionPerformed(ActionEvent e) {
		String action = e.getActionCommand();
		if (action.equals("CLOSE")) //$NON-NLS-1$
			{
			try {
				Config.save();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			dialog.setVisible(false);
		} else if (action.equals("ADD")) //$NON-NLS-1$
			{
			try {
				new AccountWizardLauncher().launchWizard();
				listView.update();
			} catch (Exception ex) {
				ex.printStackTrace();
			}

		} else if (action.equals("REMOVE")) //$NON-NLS-1$
			{
			AccountItem item = accountList.remove(index);

			if (item.isPopAccount()) {
				MainInterface.popServerCollection.removePopServer(
					item.getUid());

			} else {
				Folder folder = (Folder) MainInterface.treeModel.getImapFolder(
						item.getUid());
				try {

					FolderTreeNode parentFolder = (FolderTreeNode) folder.getParent();
					folder.removeFolder();
					MainInterface.treeModel.nodeStructureChanged(parentFolder);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

			removeButton.setEnabled(false);
			editButton.setEnabled(false);
			listView.update();

		} else if (action.equals("EDIT")) //$NON-NLS-1$
			{
			showAccountDialog();
			listView.update();
		} 
	}
}
