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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.columba.addressbook.folder.ContactCard;
import org.columba.addressbook.gui.util.LabelTextFieldPanel;

public class IdentityPanel extends JPanel
implements ActionListener
{
	JButton nameButton;
	JTextField nameTextField;

	JLabel organisationLabel;
	JTextField organisationTextField;

	JLabel positionLabel;
	JTextField positionTextField;
	
	JLabel nickNameLabel;
	JTextField nickNameTextField;
	
	JLabel displayNameLabel;
	JTextField displayNameTextField;

	JLabel urlLabel;
	JTextField urlTextField;

	AttributComboBox emailComboBox;
	JTextField emailTextField;
	Vector emailList;
	
	public FullNameDialog dialog;

	public IdentityPanel()
	{
		initComponent();
	}
	
	public void setFn( String s )
	{
		nameTextField.setText(s);
	}
	
	public boolean fnIsEmpty()
	{
		return nameTextField.getText().length() == 0;
	}
	
	protected void set(ContactCard card, String key, JTextField textField)
	{
		
		String value = card.get(key);
		if ( value != null ) textField.setText( value );
	}

	protected void get( ContactCard card, String key, JTextField textField)
	{
		card.set( key, textField.getText() );
		
	}

	public void updateComponents(ContactCard card, boolean b)
	{
		emailComboBox.updateComponents(card, b);
		
		if (b == true)
		{
			
			nameTextField.setText( card.formatGet("fn") );
			organisationTextField.setText( card.get("org") );
			displayNameTextField.setText( card.get("displayname") );
			nickNameTextField.setText( card.get("nickname") );
			positionTextField.setText( card.get("role") );
			urlTextField.setText( card.get("url") );
			
			
			
			
		}
		else
		{
			
			card.formatSet("fn", nameTextField.getText() );
			card.set("org", organisationTextField.getText() );
			card.set("displayname", displayNameTextField.getText() );
			card.set("nickname", nickNameTextField.getText() );
			card.set("role", positionTextField.getText() );
			card.set("url", urlTextField.getText() );

		}

	}

	protected void initComponent()
	{
		setLayout( new BorderLayout() );
		setBorder(BorderFactory.createEmptyBorder(12,12,11,11));

		LabelTextFieldPanel panel = new LabelTextFieldPanel();
		//panel.setAlignmentY(0);
		add(panel, BorderLayout.NORTH );

		//LOCALIZE
		nameButton = new JButton("Full Name..");
		nameButton.setActionCommand("NAME");
		nameButton.addActionListener(this);
		
		nameTextField = new JTextField(20);
		panel.addLabel(nameButton);
		panel.addTextField(nameTextField);

		//LOCALIZE
		nickNameLabel = new JLabel("Nickname:");
		nickNameTextField = new JTextField(20);
		panel.addLabel( nickNameLabel );
		panel.addTextField( nickNameTextField );
		
		//LOCALIZE
		displayNameLabel = new JLabel("Sorting Displayname:");
		displayNameTextField = new JTextField(20);
		panel.addLabel(displayNameLabel);
		panel.addTextField(displayNameTextField);
		
		panel.addSeparator();
		
		//LOCALIZE
		positionLabel = new JLabel("Position:");
		positionTextField = new JTextField(20);
		panel.addLabel( positionLabel );
		panel.addTextField( positionTextField );
		
		//LOCALIZE
		organisationLabel = new JLabel("Organisation:");
		organisationTextField = new JTextField(20);
		panel.addLabel(organisationLabel);
		panel.addTextField(organisationTextField);

		panel.addSeparator();

		//LOCALIZE
		urlLabel = new JLabel("Website:");
		urlTextField = new JTextField(20);
		panel.addLabel(urlLabel);
		panel.addTextField(urlTextField);

		emailList = new Vector();
		emailList.add("internet");
		emailList.add("x400");
		emailList.add("x-email2");
		emailList.add("x-email3");
		emailTextField = new JTextField(20);
		emailComboBox = new AttributComboBox("email", emailList, emailTextField);

		panel.addLabel(emailComboBox);
		panel.addTextField(emailTextField);

	}
	
	public void actionPerformed(ActionEvent ev)
	{
		String action = ev.getActionCommand();

		if (action.equals("NAME"))
		{
			dialog.setVisible(true);
		}
	}
}
