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

import javax.swing.*;
import javax.swing.border.*;

import java.awt.event.*;
import java.awt.*;

import org.columba.mail.config.*;
import org.columba.mail.util.*;
import org.columba.mail.gui.util.*;
import org.columba.mail.gui.tree.util.*;
import org.columba.main.*;
import org.columba.mail.folder.*;
import org.columba.main.MainInterface;

/**
 *
 * @author  freddy
 * @version
 */

public class PopAttributPanel extends JPanel implements ActionListener
{

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

	public PopAttributPanel(PopItem item)
	{
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

	public void updateComponents(boolean b)
	{
		//mailCheckDialog.updateComponents(b);

		if (b)
		{
			if (item.getLeaveMessage().equals("true")) //$NON-NLS-1$
				leaveOnServerCheckBox.setSelected(true);

			/*
			int uid = item.getUid();
			Folder folder = MainInterface.treeViewer.getFolder( uid );
			String treepath = folder.getTreePath();
			destinationTextField.setText( treepath );
			*/

			if (item.getExclude().equals("true")) //$NON-NLS-1$
				excludeCheckBox.setSelected(true);
			else
				excludeCheckBox.setSelected(false);

			/*
			if (item.getMailCheck().equals("true")) //$NON-NLS-1$
			{
				intervalCheckingCheckBox.setSelected(true);


			}
			else
			{
				intervalCheckingCheckBox.setSelected(false);


			}
			*/




			if (item.getLimit().equals("true")) //$NON-NLS-1$
			{
				limitMessageDownloadCheckBox.setSelected(true);
			}
			else
				limitMessageDownloadCheckBox.setSelected(false);

			limitMessageDownloadTextField.setText(item.getDownloadLimit());

		}
		else
		{
			if (leaveOnServerCheckBox.isSelected() == true)
				item.setLeaveMessage("true"); //$NON-NLS-1$
			else
				item.setLeaveMessage("false"); //$NON-NLS-1$

			/*
			if ( storePasswordCheckBox.isSelected() == true )
			item.setSavePassword("true");
			else
			item.setSavePassword("false");
			*/



			if (excludeCheckBox.isSelected() == true)
				item.setExclude("true"); //$NON-NLS-1$
			else
				item.setExclude("false"); //$NON-NLS-1$

			/*
			String treepath = destinationTextField.getText();
			TreeNodeList list = new TreeNodeList( treepath );
			Folder folder = MainInterface.treeViewer.getFolder( list );
			int uid = folder.getUid();

			item.setUid( uid );
			*/

			/*
			if (intervalCheckingCheckBox.isSelected())
				item.setMailCheck("true"); //$NON-NLS-1$
			else
				item.setMailCheck("false"); //$NON-NLS-1$

			if (limitMessageDownloadCheckBox.isSelected())
				item.setLimit("true"); //$NON-NLS-1$
			else
				item.setLimit("false"); //$NON-NLS-1$
			*/


			item.setDownloadLimit(limitMessageDownloadTextField.getText());
		}
	}

	private void initComponents()
	{
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
		
		setLayout( layout );
		
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
				"dialog","account",
				"leave_messages_on_server"));
		leaveOnServerCheckBox.setMnemonic(MailResourceLoader.getMnemonic(
				"dialog","account",
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
				"dialog","account",		
				"limit_message_download_to"));
		limitMessageDownloadCheckBox.setMnemonic(MailResourceLoader.getMnemonic(
				"dialog","account",		
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
				"dialog","account",
				
				"KB_per_message"));
		//$NON-NLS-1$
		limitMessageDownloadPanel.add(limitMessageDownloadLabel2);

		excludeCheckBox = new JCheckBox();
		excludeCheckBox.setText(
			MailResourceLoader.getString(
				"dialog","account",				
				"exclude_from_fetch_all"));
		excludeCheckBox.setMnemonic(MailResourceLoader.getMnemonic(
				"dialog","account",				
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

	public void actionPerformed(ActionEvent e)
	{
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