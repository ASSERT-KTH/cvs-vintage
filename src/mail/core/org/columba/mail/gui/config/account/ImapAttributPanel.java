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
public class ImapAttributPanel extends JPanel
{

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

	public ImapAttributPanel(ImapItem item)
	{
		//super( " Imap4 Settings " );

		this.item = item;
		initComponents();
	}

	public void updateComponents(boolean b)
	{

		if (b)
		{

			/*
			if ( item.isSavePassword() )
			    storePasswordCheckBox.setSelected(true);
			    */

			if (item.isAutomaticallyApplyFilterEnabled())
				automaticallyApplyFilterCheckBox.setSelected(true);
		}
		else
		{

			/*
			if ( storePasswordCheckBox.isSelected() == true )
			    item.setSavePassword("true");
			else
			    item.setSavePassword("false");
			    */

			if (automaticallyApplyFilterCheckBox.isSelected())
				item.setAutomaticallyApplyFilter(true);
			else
				item.setAutomaticallyApplyFilter(false);

		}

	}

	private void initComponents()
	{

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
		setLayout( layout );
		
		
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
		intervalCheckingCheckBox.setMnemonic(MailResourceLoader.getMnemonic(
				"dialog/account",
				"imapattributpanel",
				"enable_interval_message_checking") );
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
				"dialog","account",
			
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
		emptyTrashPanel.setLayout(new BoxLayout(emptyTrashPanel, BoxLayout.X_AXIS));
		emptyTrashCheckBox = new JCheckBox();
		emptyTrashCheckBox.setEnabled(false);
		emptyTrashCheckBox.setText(
			MailResourceLoader.getString(
				"dialog","account",
				
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
			new JCheckBox(MailResourceLoader.getString(
				"dialog","account",	
				"apply_filter"));
		automaticallyApplyFilterCheckBox.setMnemonic(MailResourceLoader.getMnemonic(
				"dialog","account",	
				"apply_filter_mnemonic"));
				
		c.gridx = 0;
		c.weightx = 1.0;
		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = GridBagConstraints.REMAINDER;
		layout.setConstraints(automaticallyApplyFilterCheckBox, c);
		add(automaticallyApplyFilterCheckBox);
	}

}