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

package org.columba.addressbook.gui.util;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.columba.addressbook.config.AdapterNode;
import org.columba.addressbook.config.ContactItem;
import org.columba.addressbook.main.AddressbookInterface;
import org.columba.core.config.DefaultXmlConfig;
import org.columba.core.gui.util.CPanel;
import org.columba.core.gui.util.DialogStore;
import org.columba.core.xml.XmlElement;

public class EditContactDialog implements ActionListener, DocumentListener
{
	private JDialog dialog;

	private JLabel firstNameLabel;
	private JTextField firstNameTextField;
	private JLabel addressLabel;
	private JTextField addressTextField;
	private JLabel lastNameLabel;
	private JTextField lastNameTextField;

	private JLabel displayNameLabel;
	private JTextField displayNameTextField;
	private JButton displayButton;

	private JButton okButton;
	private JButton cancelButton;
	private JButton helpButton;

	private AddressbookInterface addressbookInterface;
	private AdapterNode contactNode;

	private DefaultXmlConfig config;

	public EditContactDialog(XmlElement root)
	{
		dialog = DialogStore.getDialog();

		//this.addressbookInterface = i;
		//this.contactNode = contactNode;

		//config = AddressbookConfig.get("addressbook");

		init();

	}

	/*
	public EditContactDialog(XmlElement root)
	{
		dialog = DialogStore.getDialog();

		//this.addressbookXmlConfig = config;
		//this.contactNode = contactNode;

		//this.config = config;

		init();

	}
	*/
	protected void init()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		//panel.setLayout( new BoxLayout( panel, BoxLayout.Y_AXIS ) );

		//panel.add( Box.createRigidArea( new Dimension(0,10) ) );

		JPanel displayNamePanel = new JPanel();
		//Border border = new BeveledBorder(5,5,5,5);
		//displayNamePanel.setBorder( BorderFactory.createTitledBorder( border, " Display Name " ) );

		displayNamePanel.setLayout(new BoxLayout(displayNamePanel, BoxLayout.X_AXIS));

		displayNamePanel.add(Box.createRigidArea(new Dimension(10, 0)));
		displayNameLabel = new JLabel("Display Name:");
		displayNamePanel.add(displayNameLabel);

		displayNamePanel.add(Box.createRigidArea(new Dimension(10, 0)));
		displayNamePanel.add(Box.createHorizontalGlue());

		displayNameTextField = new JTextField(26);
		//displayNameTextField.setMaximumSize( new Dimension( 150, 25 ) );

		displayNamePanel.add(displayNameTextField);

		displayNamePanel.add(Box.createRigidArea(new Dimension(5, 0)));

		displayButton = new JButton("Create");
		displayButton.setActionCommand("CREATE");
		displayButton.addActionListener(this);

		displayNamePanel.add(displayButton);

		displayNamePanel.add(Box.createRigidArea(new Dimension(10, 0)));

		panel.add(displayNamePanel, BorderLayout.NORTH);

		//panel.add( Box.createRigidArea( new Dimension(0,10) ) );

		CPanel identityPanel = new CPanel(" Identity ");

		JPanel firstNamePanel = new JPanel();
		firstNamePanel.setLayout(new BoxLayout(firstNamePanel, BoxLayout.X_AXIS));
		firstNamePanel.add(Box.createRigidArea(new Dimension(10, 0)));
		firstNameLabel = new JLabel("First Name:");
		firstNamePanel.add(firstNameLabel);

		firstNamePanel.add(Box.createHorizontalGlue());
		firstNamePanel.add(Box.createRigidArea(new Dimension(10, 0)));

		firstNameTextField = new JTextField(26);
		//firstNameTextField.setMaximumSize( new Dimension( 150, 25 ) );

		firstNamePanel.add(firstNameTextField);

		firstNamePanel.add(Box.createRigidArea(new Dimension(10, 0)));

		identityPanel.add(firstNamePanel);

		identityPanel.add(Box.createRigidArea(new Dimension(0, 10)));

		JPanel lastNamePanel = new JPanel();
		lastNamePanel.setLayout(new BoxLayout(lastNamePanel, BoxLayout.X_AXIS));
		lastNamePanel.add(Box.createRigidArea(new Dimension(10, 0)));
		lastNameLabel = new JLabel("Last Name:");
		lastNamePanel.add(lastNameLabel);
		lastNamePanel.add(Box.createRigidArea(new Dimension(10, 0)));
		lastNamePanel.add(Box.createHorizontalGlue());
		lastNameTextField = new JTextField(26);
		//lastNameTextField.setMaximumSize( new Dimension( 150,25) );

		lastNamePanel.add(lastNameTextField);
		lastNamePanel.add(Box.createRigidArea(new Dimension(10, 0)));

		identityPanel.add(lastNamePanel);

		identityPanel.add(Box.createRigidArea(new Dimension(0, 10)));

		JPanel addressPanel = new JPanel();
		addressPanel.setLayout(new BoxLayout(addressPanel, BoxLayout.X_AXIS));
		addressPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		addressLabel = new JLabel("E-Message:");
		addressPanel.add(addressLabel);
		addressPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		addressPanel.add(Box.createHorizontalGlue());
		addressTextField = new JTextField(26);
		//addressTextField.setMaximumSize( new Dimension( 150,25) );
		addressTextField.getDocument().addDocumentListener(this);

		addressPanel.add(addressTextField);
		addressPanel.add(Box.createRigidArea(new Dimension(10, 0)));

		identityPanel.add(addressPanel);

		panel.add(identityPanel, BorderLayout.CENTER);

		JPanel c = (JPanel)dialog.getContentPane();
		c.setLayout(new BorderLayout(0,17));
		c.setBorder(BorderFactory.createEmptyBorder(12,12,11,11));

		c.add(panel, BorderLayout.CENTER);

		JPanel bottom = new JPanel(new BorderLayout(0,0));
		JPanel buttonPanel = new JPanel(new GridLayout(1,3,5,0));
		okButton = new JButton("Ok");
		okButton.addActionListener(this);
		okButton.setActionCommand("OK");
		okButton.setEnabled(false);
		buttonPanel.add(okButton);
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);
		cancelButton.setActionCommand("CANCEL");
		buttonPanel.add(cancelButton);
		helpButton = new JButton("Help");
		helpButton.setEnabled(false);
		buttonPanel.add(helpButton);
		bottom.add(buttonPanel,BorderLayout.EAST);
		c.add(bottom, BorderLayout.SOUTH);

		dialog.pack();
		dialog.setLocationRelativeTo(null);

		// FIXME
		/*
		if (contactNode != null)
		{
			ContactItem item = config.getContactItem(contactNode);

			firstNameTextField.setText(item.getFirstName());
			lastNameTextField.setText(item.getLastName());
			addressTextField.setText(item.getAddress());
			displayNameTextField.setText(item.getDisplayName());

		}
		*/
		
		dialog.setVisible(true);
	}

	public void actionPerformed(ActionEvent e)
	{
		String command;

		command = e.getActionCommand();

		if (command.equals("CANCEL"))
		{
			if (addressbookInterface == null)
			{
				// EditContactDialog was instantiated by MessageHeader
				// remove contact

				contactNode.remove();
			}

			dialog.setVisible(false);
		}
		else if (command.equals("OK"))
		{
			if (contactNode != null)
			{
				// edit contact
				
				//ContactCard card = new ContactCard()
				
				/*
					ContactItem item = config.getContactItem(contactNode);

					item.setFirstName(firstNameTextField.getText());
					item.setLastName(lastNameTextField.getText());
					item.setAddress(addressTextField.getText());
					item.setDisplayName(displayNameTextField.getText());
				*/

			}
			else
			{
				// create new contact

				//FIXME
				/*
				AddressbookXmlConfig config =
					AddressbookConfig.getAddressbookConfig();

				String firstName = firstNameTextField.getText();
				String lastName = lastNameTextField.getText();
				String address = addressTextField.getText();
				String displayName = displayNameTextField.getText();

				AdapterNode node = config.addContact(displayName, firstName, lastName, address);
				*/

			}

			if (addressbookInterface != null)
			{
				addressbookInterface.table.update();
			}

			dialog.setVisible(false);
		}
		else if (command.equals("CREATE"))
		{
			String firstName = firstNameTextField.getText();
			String lastName = lastNameTextField.getText();
			String address = addressTextField.getText();

			String displayName = firstName + " " + lastName;
			if (displayName.length() <= 1)
				displayName = address;

			displayNameTextField.setText(displayName);
		}

	}

	public void showDialog()
	{
		dialog.setVisible(true);
	}

	public void insertUpdate(DocumentEvent e)
	{
		update();
	}

	public void removeUpdate(DocumentEvent e)
	{
		update();
	}

	public void changedUpdate(DocumentEvent e)
	{
		update();
	}

	public void update()
	{
		String str;

		str = addressTextField.getText();

		if (str.length() == 0)
			okButton.setEnabled(false);
		else
			okButton.setEnabled(true);

	}

}