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

//import org.columba.modules.mail.gui.util.*;
import java.awt.Component;

import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;

import org.columba.core.gui.util.AscendingIcon;
import org.columba.core.gui.util.DescendingIcon;

public class AddressbookCommonHeaderRenderer
	extends JButton
	implements TableCellRenderer
{
	Border unselectedBorder = null;
	Border selectedBorder = null;
	boolean isBordered = true;
	String str, name;
	private String fontName;
	private int fontSize;

	private TableModelSorter sorter;
	
	public void updateUI()
	{
		super.updateUI();

		setBorder(UIManager.getBorder("TableHeader.cellBorder"));
	}

	public AddressbookCommonHeaderRenderer(String name, TableModelSorter sorter)
	{
		super();
		this.sorter = sorter;
		this.name = name;

		setHorizontalAlignment(SwingConstants.LEFT);
		setHorizontalTextPosition(SwingConstants.LEFT);

		setOpaque(true); //MUST do this for background to show up.

		setBorder(UIManager.getBorder("TableHeader.cellBorder"));

	}

	public Component getTableCellRendererComponent(
		JTable table,
		Object str,
		boolean isSelected,
		boolean hasFocus,
		int row,
		int column)
	{


		
		
		if (sorter.getSortingColumn().equals( str ) )
            {


                if ( sorter.getSortingOrder() == true )
                    setIcon( new AscendingIcon() );
                else
                    setIcon( new DescendingIcon() );

            } else
            {

                setIcon( null );
            }
         
            
		
		setText(this.name);

		
		//setIcon(null);

		return this;
	}
}