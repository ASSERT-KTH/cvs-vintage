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

import org.columba.mail.gui.table.*;

import java.awt.*;
import java.awt.event.*;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:
 * @author
 * @version 1.0
 */

public class HeaderTableMouseListener extends MouseAdapter
{
    private TableController headerTableViewer;

    public HeaderTableMouseListener( TableController headerTableViewer )
    {
        super();

        this.headerTableViewer = headerTableViewer;
    }

    public void mousePressed( MouseEvent event )
    {

        if ( event.isPopupTrigger() )
        {
        	/*
            Object uids[] = headerTableViewer.getUids();
            if ( uids.length > 1 )
            {
            }
            else
            {
                headerTableViewer.getHeaderTable().getTable().clearSelection();
                java.awt.Point point = event.getPoint();
                int row = headerTableViewer.getHeaderTable().getTable().rowAtPoint( point );
                headerTableViewer.getHeaderTable().getTable().addRowSelectionInterval( row, row );
            }
			*/
			
            headerTableViewer.getPopupMenu().show(event.getComponent(),  event.getX(), event.getY());
        }
        else
        {
        	//headerTableViewer.clearMessageNodeList();
        }

    }


    public void mouseReleased( MouseEvent event )
    {

        if ( event.isPopupTrigger() )
        {
        	/*
            Object uids[] = headerTableViewer.getUids();
            if ( uids.length > 1 )
            {
            }
            else
            {
                headerTableViewer.getHeaderTable().getTable().clearSelection();
                java.awt.Point point = event.getPoint();
                int row = headerTableViewer.getHeaderTable().getTable().rowAtPoint( point );
                headerTableViewer.getHeaderTable().getTable().addRowSelectionInterval( row, row );
            }
			*/
			
            headerTableViewer.getPopupMenu().show(event.getComponent(),  event.getX(), event.getY());
        }
        else
        {
        	//headerTableViewer.clearMessageNodeList();
        }

    }

    public void mouseClicked( MouseEvent event )
    {
		headerTableViewer.showMessage();
        if ( (  !event.isAltDown() ) && (  !event.isControlDown() ) &&
             (  !event.isMetaDown() ) && (  !event.isShiftDown() ) )
		{
			//headerTableViewer.clearMessageNodeList();
			
            // headerTableViewer.showMessage();
		}

    }



    private void processDoubleClick()
    {

    }
}


