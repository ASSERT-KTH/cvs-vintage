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













