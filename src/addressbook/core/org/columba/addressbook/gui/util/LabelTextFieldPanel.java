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

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

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
