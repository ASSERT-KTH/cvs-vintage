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
package org.columba.mail.gui.composer.util;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;

import org.columba.addressbook.folder.HeaderItem;
import org.columba.addressbook.gui.table.util.HeaderColumnInterface;
import org.columba.core.gui.util.ImageLoader;

/**
 * @author frd
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class DisplaynameHeaderColumn extends JLabel implements HeaderColumnInterface, TableCellRenderer{
	
	protected Border unselectedBorder = null;
	protected Border selectedBorder = null;
	protected boolean isBordered = true;

	protected String value;
	protected String name;
	
	ImageIcon contactIcon = ImageLoader.getSmallImageIcon("contact_small.png");
    ImageIcon groupIcon = ImageLoader.getSmallImageIcon("group_small.png");
	
	public DisplaynameHeaderColumn(String name)
	{
		this.value = null;
		this.name  = name;
		setOpaque(true);
		
		isBordered = true;
	}
	
	public DisplaynameHeaderColumn(String name, String value)
	{
		this.value = value;
		this.name  = name;
		setOpaque(true);
		
		isBordered = true;
	}
	
	public Component getTableCellRendererComponent(
		JTable table,
		Object object,
		boolean isSelected,
		boolean hasFocus,
		int row,
		int column)
	{
		
		if (isBordered)
		{
			if (isSelected)
			{
				if (selectedBorder == null)
				{
					selectedBorder =
						BorderFactory.createMatteBorder(2, 5, 2, 5, table.getSelectionBackground());
				}
				
				setBackground(table.getSelectionBackground());
				setForeground(table.getSelectionForeground());
			}
			else
			{
				if (unselectedBorder == null)
				{
					unselectedBorder =
						BorderFactory.createMatteBorder(2, 5, 2, 5, table.getBackground());
				}
				setBackground(table.getBackground());
				
				
				setForeground(table.getForeground());
			}
		}
		
		
		//System.out.println("value="+object);
		
		HeaderItem item = (HeaderItem) object;
		
		String name = (String) item.get("displayname");
		
		if ( name == null ) 
			name = (String) item.get("email;internet");
			
		setText( name );
		
		if ( item.isContact() )
			setIcon( contactIcon );
		else
			setIcon( groupIcon );
		
		return this;
	}

	public Object getValue( HeaderItem item )
	{
		
		return item;
		
		
		/*
		if ( name == null ) return "";
		
		Object o = item.get( (String) name);
		if ( o == null ) return "";
		
		return o;		
		*/
	}
	
	
	public String getName()
	{
		return name;
	}
	
	public String getValueString()
	{
		return value;
	}
	
	public int getColumnSize()
	{
		return -1;
	}
	
	
}
