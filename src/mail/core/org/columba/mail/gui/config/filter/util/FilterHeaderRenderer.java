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


package org.columba.mail.gui.config.filter.util;


import org.columba.mail.gui.util.*;

import javax.swing.*;
import java.awt.*;
import javax.swing.border.*;
import javax.swing.table.*;

public class FilterHeaderRenderer extends JButton implements TableCellRenderer
    {
        Border unselectedBorder = null;
        Border selectedBorder = null;
        boolean isBordered = true;
        String str,name;
        //private String fontName;
        //private int fontSize;



        public void updateUI()
        {
            super.updateUI();

            setBorder( UIManager.getBorder( "TableHeader.cellBorder" ) );
        }



        public FilterHeaderRenderer( String name )
            {
                super();
                //this.str = str;
                this.name = name;


                //this.tableModelSorter = tableModelSorter;


		setHorizontalAlignment( SwingConstants.LEFT );
                setHorizontalTextPosition( SwingConstants.LEFT );
                    //setIconTextGap( 5 );
                setOpaque(true); //MUST do this for background to show up.

                setBorder( UIManager.getBorder( "TableHeader.cellBorder" ) );

                  //setMargin( new Insets(0,0,0,0) );

            }




        public Component getTableCellRendererComponent(
                                JTable table, Object str,
                                boolean isSelected, boolean hasFocus,
                                int row, int column)
        {



              //setBorder( new CTableBorder(true) );
                //super.setFont( new Font("Helvetica", Font.PLAIN, 9) );

            setText( this.name );

            return this;
        }
    }


