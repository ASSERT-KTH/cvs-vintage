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

package org.columba.mail.gui.message.action;

import org.columba.mail.gui.message.*;

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

public class MessagePopupListener extends MouseAdapter
{
    private MessageController messageViewer;

    public MessagePopupListener( MessageController messageViewer )
    {
        this.messageViewer = messageViewer;
    }


        public void mousePressed(MouseEvent e)
            {
                maybeShowPopup(e);
            }

        public void mouseReleased(MouseEvent e)
            {
                maybeShowPopup(e);
            }

        private void maybeShowPopup(MouseEvent e)
            {
            	// FIXME
            	/*
                if (e.isPopupTrigger())
                {
                    java.awt.Point point = e.getPoint();

                    if ( messageViewer.getView().isAdvancedViewActive() )
                    {
                        HyperlinkTextViewer viewer = (HyperlinkTextViewer) messageViewer.getView().getViewer( MessageView.ADVANCED                        );
                        int pos = viewer.viewToModel( point );

                        if ( !viewer.isLink( pos ) )
                           messageViewer.getPopupMenu().show(e.getComponent(),  e.getX(), e.getY());
                    }
                }
                */
            }


}