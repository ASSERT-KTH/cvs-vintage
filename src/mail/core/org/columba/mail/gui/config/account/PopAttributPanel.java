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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.columba.mail.config.PopItem;
import org.columba.mail.util.MailResourceLoader;

/**
 *
 * @author  freddy
 * @version
 */

public class PopAttributPanel extends JPanel implements ActionListener {

	private PopItem item;

	private JCheckBox secureCheckBox;
	private JCheckBox leaveOnServerCheckBox;
	private JCheckBox storePasswordCheckBox;
	private JCheckBox excludeCheckBox;
	//private JCheckBox intervalCheckingCheckBox;
	//private JButton mailcheckButton;

	//private JCheckBox limitMessageDownloadCheckBox;

	private JCheckBox limitMessageDownloadCheckBox;
	private JLabel limitMessageDownloadLabel2;
	private JTextField limitMessageDownloadTextField;

	private JPanel jPanel1;
	private JPanel jPanel4;

	private JPanel deleteLocallyPanel;
	private JCheckBox deleteLocallyCheckBox;

	private JPanel jPanel2;
	private JPanel jPanel3;
	//private JLabel destinationLabel;
	//private JTextField destinationTextField;
	private JButton selectButton;

	//MailCheckDialog mailCheckDialog;

	// private ConfigFrame frame;

	public PopAttributPanel(PopItem item) {
		super();
		this.item = item;
		//this.frame = frame;

		//mailCheckDialog = new MailCheckDialog( item );
		initComponents();

	}

	/*
	public String getDestinationFolder()
	{
	    return destinationTextField.getText();
	}
	*/

	public void updateComponents(boolean b) {
		//mailCheckDialog.updateComponents(b);

		if (b) {
			leaveOnServerCheckBox.setSelected(
				item.getBoolean("leave_messages_on_server"));

			excludeCheckBox.setSelected(
				item.getBoolean("exclude_from_checkall",false));

			limitMessageDownloadCheckBox.setSelected(
				item.getBoolean("enable_download_limit"));

			limitMessageDownloadTextField.setText(item.get("download_limit"));

		} else {
			item.set("leave_messages_on_server", leaveOnServerCheckBox.isSelected()); //$NON-NLS-1$

			item.set("exclude_from_checkall", excludeCheckBox.isSelected()); //$NON-NLS-1$

			item.set("download_limit", limitMessageDownloadTextField.getText());
			
			item.set("enable_download_limit", limitMessageDownloadCheckBox.isSelected());
		}
	}

	private void initComponents() {
		/*
		setLayout(new BorderLayout());
		setBorder(
			javax.swing.BorderFactory.createTitledBorder(
				javax.swing.BorderFactory.createEtchedBorder(),
				MailResourceLoader.getString(
					"dialog","account",
					
					"_POP3_Settings")));
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

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		c.gridx = 0;
		c.weightx = 1.0;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.gridwidth = GridBagConstraints.REMAINDER;
		layout.setConstraints(panel, c);
		add(panel);

		/*
		intervalCheckingCheckBox = new JCheckBox();
		intervalCheckingCheckBox.setEnabled(true);
		intervalCheckingCheckBox.setActionCommand("INTERVAL_CHECKING");
		intervalCheckingCheckBox.addActionListener(this);
		intervalCheckingCheckBox.setText(
			MailResourceLoader.getString(
				"dialog","account",
			
				"Enable_interval_message_checking"));
		//$NON-NLS-1$
		c = new GridBagConstraints();
		c.gridwidth = GridBagConstraints.RELATIVE;
		c.gridx = 0;
		c.weightx = 0.75;
		c.anchor = GridBagConstraints.WEST;
		layout.setConstraints(intervalCheckingCheckBox, c);
		innerPanel.add(intervalCheckingCheckBox);
		
		mailcheckButton = new JButton("Options..");
		mailcheckButton.setMargin( new Insets(0,2,0,2) );
		mailcheckButton.addActionListener(this);
		mailcheckButton.setActionCommand("MAILCHECK");
		c.gridx = 1;
		c.anchor = GridBagConstraints.WEST;
		c.weightx = 0.25;
		c.gridwidth = GridBagConstraints.REMAINDER;
		layout.setConstraints( mailcheckButton, c);
		innerPanel.add( mailcheckButton );
		*/

		leaveOnServerCheckBox = new JCheckBox();
		leaveOnServerCheckBox.setText(
			MailResourceLoader.getString(
				"dialog",
				"account",
				"leave_messages_on_server"));
		leaveOnServerCheckBox.setMnemonic(
			MailResourceLoader.getMnemonic(
				"dialog",
				"account",
				"leave_messages_on_server"));
		//$NON-NLS-1$
		c = new GridBagConstraints();
		c.gridx = 0;
		c.weightx = 1.0;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.gridwidth = GridBagConstraints.REMAINDER;
		layout.setConstraints(leaveOnServerCheckBox, c);
		add(leaveOnServerCheckBox);

		JPanel limitMessageDownloadPanel = new JPanel();
		limitMessageDownloadPanel.setLayout(
			new BoxLayout(limitMessageDownloadPanel, BoxLayout.X_AXIS));
		c.gridx = 0;
		c.weightx = 1.0;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.gridwidth = GridBagConstraints.REMAINDER;
		layout.setConstraints(limitMessageDownloadPanel, c);
		add(limitMessageDownloadPanel);

		limitMessageDownloadCheckBox = new JCheckBox();
		limitMessageDownloadCheckBox.setEnabled(true);
		limitMessageDownloadCheckBox.setText(
			MailResourceLoader.getString(
				"dialog",
				"account",
				"limit_message_download_to"));
		limitMessageDownloadCheckBox.setMnemonic(
			MailResourceLoader.getMnemonic(
				"dialog",
				"account",
				"limit_message_download_to"));
		//$NON-NLS-1$
		limitMessageDownloadPanel.add(limitMessageDownloadCheckBox);
		limitMessageDownloadPanel.add(
			Box.createRigidArea(new java.awt.Dimension(5, 0)));

		limitMessageDownloadTextField = new JTextField(5);
		limitMessageDownloadTextField.setEnabled(true);
		limitMessageDownloadTextField.setText("18");
		limitMessageDownloadPanel.add(limitMessageDownloadTextField);
		limitMessageDownloadPanel.add(
			Box.createRigidArea(new java.awt.Dimension(5, 0)));

		limitMessageDownloadLabel2 = new JLabel();
		limitMessageDownloadLabel2.setEnabled(true);
		limitMessageDownloadLabel2.setText(
			MailResourceLoader.getString(
				"dialog",
				"account",
				"KB_per_message"));
		//$NON-NLS-1$
		limitMessageDownloadPanel.add(limitMessageDownloadLabel2);

		excludeCheckBox = new JCheckBox();
		excludeCheckBox.setText(
			MailResourceLoader.getString(
				"dialog",
				"account",
				"exclude_from_fetch_all"));
		excludeCheckBox.setMnemonic(
			MailResourceLoader.getMnemonic(
				"dialog",
				"account",
				"exclude_from_fetch_all"));
		//$NON-NLS-1$
		c = new GridBagConstraints();
		c.gridx = 0;
		c.weightx = 1.0;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.gridwidth = GridBagConstraints.REMAINDER;
		layout.setConstraints(excludeCheckBox, c);
		add(excludeCheckBox);

	}

	public void actionPerformed(ActionEvent e) {
		String action = e.getActionCommand();

		/*
		if ( action.equals("SELECT") )
		{
		    SelectFolderDialog dialog = MainInterface.treeViewer.getSelectFolderDialog();
		    //SelectFolderDialog dialog = new SelectFolderDialog( frame.mainInterface );
		    dialog.show();
		
		    if ( dialog.success() )
		    {
		        Folder selectedFolder = dialog.getSelectedFolder();
		        String path = selectedFolder.getTreePath();
		
		        destinationTextField.setText( path );
		
		        int uid = selectedFolder.getUid();
		        //int uid = frame.mainInterface.treeViewer.getUid( path );
		        item.setUid( uid );
		    }
		
		}
		*/

		/*
		if (action.equals("INTERVAL_CHECKING"))
		{
			if (intervalCheckingCheckBox.isSelected())
			{
				mailcheckButton.setEnabled(true);
			}
			else
			{
				mailcheckButton.setEnabled(false);
			}
		}
		else if ( action.equals("MAILCHECK") )
		{
			mailCheckDialog.setVisible(true);
		}
		*/

	}

}