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
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.columba.addressbook.folder.ContactCard;
import org.columba.addressbook.gui.util.LabelTextFieldPanel;
import org.columba.core.util.Compatibility;

public class FullNameDialog extends JDialog implements ActionListener
{
	JLabel titleLabel;
	JTextField titleTextField;
	
	JLabel fornameLabel;
	JTextField fornameTextField;
	
	JLabel middlenameLabel;
	JTextField middlenameTextField;
	
	JLabel lastnameLabel;
	JTextField lastnameTextField;
	
	JLabel suffixLabel;
	JTextField suffixTextField;
	
	JButton okButton;
	
	JButton changeButton;
	
	
	IdentityPanel identityPanel;
	
	public FullNameDialog( JDialog frame, IdentityPanel identityPanel )
	{
	
		super(frame, true);
	
		this.identityPanel = identityPanel;
			
		initComponents();
		
		pack();
		
		//		for jdk1.3 compatibility, this is called dynamically
		Compatibility.simpleSetterInvoke(
			this,
			"setLocationRelativeTo",
			Component.class,
			null);
	}
	
	public void updateComponents( ContactCard card, boolean b )
	{
		if ( b == true )
		{
			titleTextField.setText( card.get("n","prefix") );
			lastnameTextField.setText( card.get("n","family") );
			fornameTextField.setText( card.get("n","given") );
			middlenameTextField.setText( card.get("n","middle") );
			suffixTextField.setText( card.get("n","suffix") );
		}
		else
		{
			card.set("n","prefix", titleTextField.getText() );
			card.set("n","family", lastnameTextField.getText() );
			card.set("n","given", fornameTextField.getText() );
			card.set("n","middle", middlenameTextField.getText() );
			card.set("n","suffix", suffixTextField.getText() );
		}
	}
	
	public void initComponents()
	{
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout( new BoxLayout( mainPanel, BoxLayout.Y_AXIS )  );		
		mainPanel.setBorder( BorderFactory.createEmptyBorder(10,10,10,10) );
		getContentPane().setLayout( new BorderLayout() );
		getContentPane().add( mainPanel );
		
		LabelTextFieldPanel panel = new LabelTextFieldPanel();
		mainPanel.add( panel, BorderLayout.CENTER );
		
		titleLabel = new JLabel("Title:");
		titleTextField = new JTextField(20);		
		panel.addLabel( titleLabel );
		panel.addTextField( titleTextField );	
		
		fornameLabel = new JLabel("First Name:");
		fornameTextField = new JTextField(20);		
		panel.addLabel( fornameLabel );
		panel.addTextField( fornameTextField );	
			
		middlenameLabel = new JLabel("Middle Name:");
		middlenameTextField = new JTextField(20);		
		panel.addLabel( middlenameLabel );
		panel.addTextField( middlenameTextField );	
		
		lastnameLabel = new JLabel("Last Name:");
		lastnameTextField = new JTextField(20);		
		panel.addLabel( lastnameLabel );
		panel.addTextField( lastnameTextField );	
		
		suffixLabel = new JLabel("Suffix:");
		suffixTextField = new JTextField(20);		
		panel.addLabel( suffixLabel );
		panel.addTextField( suffixTextField );	
		
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout( new BorderLayout() );
		bottomPanel.setBorder( BorderFactory.createEmptyBorder(10,0,0,0) );
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout( new GridLayout(0,2,10,0) );
		bottomPanel.add( buttonPanel, BorderLayout.EAST );
		
		changeButton = new JButton("Change formatted name");
		changeButton.setActionCommand("CHANGE");
		changeButton.addActionListener( this );
		buttonPanel.add( changeButton );
		okButton = new JButton("Close");
		okButton.setActionCommand("OK");
		okButton.addActionListener( this );
		buttonPanel.add( okButton );
			
		mainPanel.add( bottomPanel, BorderLayout.SOUTH );
	}
	
	public void actionPerformed(ActionEvent event)
	{
		String action = event.getActionCommand();

		if (action.equals("OK"))
		{
			setVisible(false);
			
			if ( identityPanel.fnIsEmpty() == true )
				setFormattedName();
		}
		
		else if (action.equals("CHANGE"))
		{
			setFormattedName();	
		}
	}
	
	protected void setFormattedName()
	{
		StringBuffer buf = new StringBuffer();
		if ( titleTextField.getText().length() > 0 )
			buf.append( titleTextField.getText() + " " );
		
		if ( fornameTextField.getText().length() > 0 )
			buf.append( fornameTextField.getText() + " " );
			
		if ( middlenameTextField.getText().length() > 0 )
			buf.append( middlenameTextField.getText() + " " );
			
		if ( lastnameTextField.getText().length() > 0 )
			buf.append( lastnameTextField.getText() + " " );
			
		if ( suffixTextField.getText().length() > 0 )
			buf.append( suffixTextField.getText() + " " );
		
		identityPanel.setFn( buf.toString() );
	}
	
}
