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

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;

import org.columba.core.gui.util.AscendingIcon;
import org.columba.core.gui.util.DescendingIcon;


public class BooleanHeaderRenderer extends JButton implements TableCellRenderer
    {
        Border unselectedBorder = null;
        Border selectedBorder = null;
        boolean isBordered = true;
        String str;
	boolean bool;
        TableModelSorter tableModelSorter;
        ImageIcon image;


        public void updateUI()
        {
            super.updateUI();

            setBorder( UIManager.getBorder( "TableHeader.cellBorder" ) );
        }

        public BooleanHeaderRenderer(boolean bool, String str, TableModelSorter tableModelSorter)
            {
                super();
                this.str = str;
		this.bool = bool;

                this.tableModelSorter = tableModelSorter;

                    //this.image = image;
		setHorizontalAlignment( SwingConstants.LEFT );
                //setHorizontalTextPosition( SwingConstants.CENTER );
                    //setHorizontalAlignment( SwingConstants.CENTER );
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

	    //FIXME

            if ( tableModelSorter.getSortingColumn().equals( this.str ) )
            {
                if ( tableModelSorter.getSortingOrder() == true )
                    setIcon( new AscendingIcon() );
                else
                    setIcon( new DescendingIcon() );
            } else
            {
                setIcon( null );
            }

            return this;
        }
    }
