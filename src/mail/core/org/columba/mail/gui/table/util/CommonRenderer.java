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

package org.columba.mail.gui.table.util;

import javax.swing.*;
import java.awt.*;
import javax.swing.border.*;
import javax.swing.table.*;
import org.columba.mail.gui.util.*;

public class CommonRenderer extends JLabel implements TableCellRenderer
{
    Border unselectedBorder = null;
    Border selectedBorder = null;
    boolean isBordered = true;
    
    
    public CommonRenderer(boolean isBordered)
    {
        super();
        this.isBordered = isBordered;
        setOpaque(true); //MUST do this for background to show up.
        
    }
    
    public Component getTableCellRendererComponent(
        JTable table, Object value, 
        boolean isSelected, boolean hasFocus,
        int row, int column)
    {
        if (isBordered)
        {
            if (isSelected)
            {
                if (selectedBorder == null)
                {
                    selectedBorder = BorderFactory.createMatteBorder(2,5,2,5,table.getSelectionBackground() );
                }
                setBorder(selectedBorder);
                setBackground( table.getSelectionBackground() );
                setForeground( table.getSelectionForeground() );
            } else
            {
                if (unselectedBorder == null)
                {
                    unselectedBorder = BorderFactory.createMatteBorder(2,5,2,5,
                                                                       table.getBackground() );
                }
                setBackground( table.getBackground() );
                setBorder(unselectedBorder);
                setForeground( table.getForeground() );
            }
        }
        
        if ( value == null ) 
		{
			setText("");
			return this;
		}
        setText( (String) value );           
        return this;
    }
}
