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

package org.columba.addressbook.gui.table.util;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;

import org.columba.addressbook.folder.HeaderItem;

public class HeaderColumn extends JLabel implements HeaderColumnInterface, TableCellRenderer
{
	protected Border unselectedBorder = null;
	protected Border selectedBorder = null;
	protected boolean isBordered = true;

	protected String value;
	
	protected String name;
	protected String prefix;
	protected String suffix;
	boolean splitted;

	public HeaderColumn( String name )
	{
		super();
		this.name = name;
		this.value = null;
		int index = name.indexOf(";");

		setOpaque(true);
		
		isBordered = true;
	}
	
	public HeaderColumn( String name, String value )
	{
		super();
		this.name = name;
		isBordered = true;
		this.value = value;
		setOpaque(true);
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
		
		
		setText((String) object);
		return this;
	}

	public Object getValue( HeaderItem item )
	{
		if ( item == null ) return "";
		
		if ( name == null ) return "";
		
		Object o = item.get( (String) name);
		if ( o == null ) return "";
		
		return o;
		
		
		
		
		
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