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

package org.columba.mail.gui.table.action;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.columba.core.config.HeaderItem;
import org.columba.core.config.TableItem;
import org.columba.mail.gui.table.TableController;


public class HeaderItemActionListener implements ActionListener
{
    private TableController headerTableViewer;
    private TableItem headerTableItem;

    public HeaderItemActionListener(TableController headerTableViewer, TableItem headerTableItem)
    {
	this.headerTableViewer = headerTableViewer;
        this.headerTableItem = headerTableItem;
    }

    public void actionPerformed(ActionEvent e)
    {
	String action  = e.getActionCommand();

        TableItem v = headerTableItem;

        String c;

        if ( action.equals("Ascending") )
        {
            headerTableViewer.getView().getTableModelSorter().setSortingOrder( true );
            headerTableViewer.getHeaderTableModel().update();
        }
        else if ( action.equals("Descending") )
        {
            headerTableViewer.getView().getTableModelSorter().setSortingOrder( false );
            headerTableViewer.getHeaderTableModel().update();
        }
        else if ( action.equals("In Order Received") )
        {
            headerTableViewer.getView().getTableModelSorter().setSortingColumn( "In Order Received" );
            headerTableViewer.getHeaderTableModel().update();
        }
        else
        {
        for ( int i=0; i< v.count(); i++ )
        {
        	HeaderItem headerItem = v.getHeaderItem(i);
            c = (String) headerItem.get("name");
            boolean enabled = headerItem.getBoolean("enabled");

            if ( enabled == true )
            {
                if ( action.equals(c) )
                {
                    headerTableViewer.getView().getTableModelSorter().setSortingColumn( c );

                    headerTableViewer.getHeaderTableModel().update();

                }

            }
        }


        }
    }

}













