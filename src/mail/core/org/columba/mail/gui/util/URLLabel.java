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
package org.columba.mail.gui.util;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;

import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

public class URLLabel extends JLabel implements MouseListener
{
    private JPopupMenu popup;
    private JMenuItem menuItem;

    boolean entered = false;
    boolean mousehover;

   

    public URLLabel( URL url )
    {
	this(url,url.toString());
    }

    public URLLabel( URL url, String str )
    {
        super( str );
        
        addMouseListener( this );
        setForeground( Color.blue );
        mousehover = false;

        URLController controller = new URLController();
        controller.setLink( url );
        popup = controller.createLinkMenu();
    }

    public void mouseClicked( MouseEvent e )
    {
        Point point = e.getPoint();
        popup.show( e.getComponent(),
                    e.getX(), e.getY() );
    }

    public void mouseEntered( MouseEvent e )
    {
        setCursor( Cursor.getPredefinedCursor( Cursor.HAND_CURSOR ) );
        entered = true;

        if ( mousehover == true )repaint();
    }

    public void mouseExited( MouseEvent e )
    {
        setCursor( Cursor.getDefaultCursor() );
        entered = false;

        if ( mousehover == true )repaint();
    }

    public void mousePressed( MouseEvent e ){}
    public void mouseReleased( MouseEvent e ){}

    public void paint( Graphics g )
    {
        super.paint( g );

        if ( ( entered == true ) || ( mousehover == false ) )
        {
            Rectangle r = g.getClipBounds();

            g.drawLine(0,
                     r.height - this.getFontMetrics(this.getFont()).getDescent(),
                     this.getFontMetrics(this.getFont()).stringWidth(this.getText()),
                     r.height - this.getFontMetrics(this.getFont()).getDescent());
        }
    }
}
