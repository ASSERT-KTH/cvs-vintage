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

package org.columba.addressbook.gui.dialog.contact;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;

import org.columba.addressbook.folder.ContactCard;

public class ContactDialog extends JDialog implements ActionListener
{
	JTabbedPane centerPane;
	IdentityPanel identityPanel;
	AddressPanel addressPanel;
	JButton okButton;

	boolean result;

	public ContactDialog(JFrame frame)
	{
		super(frame,true);
		//LOCALIZE
		setTitle("Add Contact");
		initComponents();
		pack();
		setLocationRelativeTo(null);
	}

	public void updateComponents(ContactCard card, boolean b)
	{
		identityPanel.updateComponents(card, b);
		addressPanel.updateComponents(card, b);

		/*
		if ( b == true )
		{
			identityPanel.updateComponents( rootNode, b );
		}
		else
		{
		}
		*/
	}

	protected void initComponents()
	{
		JPanel contentPane = new JPanel(new BorderLayout(0,0));
		centerPane = new JTabbedPane();
		identityPanel = new IdentityPanel();
		identityPanel.dialog = new FullNameDialog(this,identityPanel);
		//LOCALIZE
		centerPane.add(identityPanel, "Identity");
		addressPanel = new AddressPanel();
		//LOCALIZE
		centerPane.add(addressPanel,"Address & Phone");
		contentPane.add(centerPane, BorderLayout.CENTER);
		JPanel bottomPanel = new JPanel(new BorderLayout());
		bottomPanel.setBorder(BorderFactory.createEmptyBorder(17,0,11,11));
		JPanel buttonPanel = new JPanel(new GridLayout(1,2,5,0));

		//LOCALIZE
		okButton = new JButton("Ok");
		//mnemonic
		okButton.setActionCommand("OK");
		okButton.addActionListener(this);
		buttonPanel.add(okButton);
		bottomPanel.add(buttonPanel, BorderLayout.EAST);
		contentPane.add(bottomPanel,BorderLayout.SOUTH);
		setContentPane(contentPane);
		getRootPane().setDefaultButton(okButton);
		//LOCALIZE
		JButton cancelButton = new JButton("Cancel");
		//mnemonic
		cancelButton.setActionCommand("CANCEL");
		cancelButton.addActionListener(this);
		buttonPanel.add(cancelButton);
		getRootPane().registerKeyboardAction(
				this,"CANCEL",
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);
	}

	public void actionPerformed(ActionEvent event)
	{
		String action = event.getActionCommand();
		if (action.equals("OK"))
		{
			result = true;
			setVisible(false);
		}
		else if (action.equals("CANCEL"))
		{
			result = false;
			setVisible(false);
		}
	}

	public boolean getResult()
	{
		return result;
	}

}
