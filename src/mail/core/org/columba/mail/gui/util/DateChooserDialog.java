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

package org.columba.mail.gui.util;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import java.util.*;

/**
 * @version 	1.0
 * @author
 */
public class DateChooserDialog extends JDialog implements ActionListener
{
	DateChooser dateChooser;
	JButton okButton;
	JButton cancelButton;
	
	JPanel panel;
	
	boolean success = false;
	
	public DateChooserDialog ( JFrame frame )
	{
		super( frame, true );
		
		setTitle( "Choose Date..." );
		
		dateChooser = new DateChooser();
		
		panel = new JPanel();
		panel.setLayout( new BorderLayout() );
		
		getContentPane().add( panel, BorderLayout.CENTER );
		
		panel.setBorder( BorderFactory.createEmptyBorder(10,10,10,10) );
		
		panel.add( dateChooser, BorderLayout.CENTER );
		
		
		
		JPanel bottomPanel = new JPanel();
		/*
		bottomPanel.setBorder(new WizardTopBorder());
		Border border = bottomPanel.getBorder();
		Border margin = BorderFactory.createEmptyBorder(15, 10, 10, 10);
		bottomPanel.setBorder(new CompoundBorder(border, margin));
		*/
		bottomPanel.setLayout(new BorderLayout());
		bottomPanel.setBorder( BorderFactory.createEmptyBorder(10,0,0,0) );
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(1, 2, 10, 10));
		bottomPanel.add(buttonPanel, BorderLayout.EAST);
		
		cancelButton = new JButton("Cancel");
		cancelButton.setActionCommand("CANCEL");
		cancelButton.addActionListener(this);
		okButton = new JButton("Ok");
		okButton.setActionCommand("OK");
		okButton.addActionListener(this);

		buttonPanel.add(cancelButton);
		buttonPanel.add(okButton);
		
		panel.add( bottomPanel, BorderLayout.SOUTH );
		
		pack();
	}
	
	public Date getDate()
	{
		return dateChooser.getSelectedDate().getTime();
	}
	
	public void setDate( Date d )
	{
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		dateChooser.setSelectedDate( c );
	}
	
	public boolean success()
	{
		return success;
	}
	
	public void actionPerformed( ActionEvent ev )
	{
		String action = ev.getActionCommand();
		
		if ( action.equals("OK") )
		{
			success = true;
			setVisible(false);
		}
		else if ( action.equals("CANCEL") )
		{
			success = false;
			setVisible(false);
		}

	}
}
