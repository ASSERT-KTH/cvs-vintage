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
package org.columba.mail.gui.message.action;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import org.columba.mail.gui.message.MessageController;

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