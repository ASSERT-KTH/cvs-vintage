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

package org.columba.addressbook.gui.util;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class LabelTextFieldPanel extends JPanel
{
	private JTextField textField;
	private GridBagLayout layout;
	private int y = 0;
	
	public LabelTextFieldPanel()
	{
		layout = new GridBagLayout();
		setLayout(layout);
	}
	
	public void addLabel(JComponent label) {
		GridBagConstraints c = new GridBagConstraints();

		c.gridx = 0;
		c.gridy = y;
		c.weightx = 0.0;
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(0, 0, 0, 11);
		layout.setConstraints(label, c);
		add(label);
	}

	public void addTextField(JComponent component) {
		GridBagConstraints c = new GridBagConstraints();

		c.gridx = 1;
		c.weightx = 1.0;
		c.gridy = y;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(5, 0, 0, 0);
		c.anchor = GridBagConstraints.EAST;
		c.gridwidth = GridBagConstraints.REMAINDER;
		layout.setConstraints(component, c);
		add(component);
		
		y += 1;
	}
	
	public void addSeparator()
	{
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.weightx = 1.0;
		c.gridy = y;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(0,0,0,0 );
		c.gridwidth = GridBagConstraints.REMAINDER;
		Component component = Box.createVerticalStrut(11);
		layout.setConstraints( component, c );
		add( component );
		
		y+=1;
	}
}
