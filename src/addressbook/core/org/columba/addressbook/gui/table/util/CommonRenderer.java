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

package org.columba.addressbook.gui.table.util;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;

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
                  //setBorder(selectedBorder);
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
                  //setBorder(unselectedBorder);
                setForeground( table.getForeground() );
            }
        }
        setText( (String) value );           
        return this;
    }
}
