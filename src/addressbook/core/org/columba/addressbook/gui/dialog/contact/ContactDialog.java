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
