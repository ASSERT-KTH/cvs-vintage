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

package org.columba.mail.gui.composer.util;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;
import org.columba.core.gui.util.ImageLoader;
import org.columba.addressbook.folder.HeaderItem;
import org.columba.addressbook.gui.table.util.HeaderColumnInterface;

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
