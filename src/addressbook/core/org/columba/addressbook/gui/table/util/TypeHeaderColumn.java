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
import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.SwingConstants;

import org.columba.core.gui.util.ImageLoader;

/**
 * @version 	1.0
 * @author
 */
public class TypeHeaderColumn extends HeaderColumn
{
	ImageIcon image1 = ImageLoader.getSmallImageIcon("contact_small.png");
    ImageIcon image2 = ImageLoader.getSmallImageIcon("group_small.png");
    
	public TypeHeaderColumn( String name )
	{
		super(name);
		
		setHorizontalAlignment( SwingConstants.CENTER );
		
	}
	
	public TypeHeaderColumn( String name, String value )
	{
		super(name, value);
		setHorizontalAlignment( SwingConstants.CENTER );
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
				//setBorder(selectedBorder);
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
				//setBorder(unselectedBorder);
				setForeground(table.getForeground());
			}
		}
		
		
		
		
		String type = (String) object;
		
		//setText( "type="+type );
		
		if ( type.equals("contact") )
			setIcon( image1 );
		else
			setIcon ( image2 );
		
		return this;
	}
	
	public int getColumnSize()
	{
		return 23;
	}
}
