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

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.columba.core.gui.util.DefaultFormBuilder;
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
	/* TODO: reactivate it
	 private JCheckBox automaticallyApplyFilterCheckBox;
	 */

	private JCheckBox intervalCheckingCheckBox;
	private JPanel jPanel1;
	private JLabel intervalCheckingLabel;
	private JLabel intervalCheckingLabel2;
	private JTextField intervalCheckingTextField;

	private JCheckBox cleanupCheckBox;
	private JPanel cleanupPanel;

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
			/* TODO: reactivate it
			automaticallyApplyFilterCheckBox.setSelected(
				item.getBoolean("automatically_apply_filter"));
				*/
		} else {

			/*
			if ( storePasswordCheckBox.isSelected() == true )
			    item.setSavePassword("true");
			else
			    item.setSavePassword("false");
			    */
			/* TODO: reactivate it
			item.set(
				"automatically_apply_filter",
				automaticallyApplyFilterCheckBox.isSelected());
				*/
			// TODO: delete this if the automatically_apply_filter action is reactivated
			item.set("automatically_apply_filter", false);
			// end Todo
		}

	}

	public void createPanel(DefaultFormBuilder builder) {
		// all options are de-actived right now
		
		// re-activate this
		/*
		builder.append(automaticallyApplyFilterCheckBox, 3);
		builder.nextLine();
				
		builder.append(cleanupCheckBox, 3);
		builder.nextLine();
		*/
		
	}

	protected void initComponents() {
		

		cleanupCheckBox = new JCheckBox();
		cleanupCheckBox.setEnabled(false);
		cleanupCheckBox.setText(
			MailResourceLoader.getString(
				"dialog",
				"account",
				"Expunge_Inbox_on_Exit"));

		// TODO: reactivate it
		/*
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
		*/

	}

	/*
	private void initComponents() {

		
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
		
	}
	*/

}