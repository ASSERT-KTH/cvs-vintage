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

package org.columba.mail.gui.util;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JLabel;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:
 * @author
 * @version 1.0
 */

public class LinkLabel extends JLabel implements MouseListener
{
    boolean entered = false;
    boolean mousehover;

    ActionListener actionListener = null;

    public LinkLabel( String s )
    {
        super( s );

        addMouseListener( this );
        //setFont( UIManager.getFont("TextField.font") );
        setForeground( Color.blue );

        mousehover = false;
    }


    public void mouseClicked( MouseEvent e )
    {

    }

    public void mouseEntered( MouseEvent e )
    {
        setCursor( new Cursor( Cursor.HAND_CURSOR ) );
        entered = true;

        if ( mousehover == true )
           repaint();
    }

    public void mouseExited( MouseEvent e )
    {
        setCursor( new Cursor( Cursor.DEFAULT_CURSOR ) );
        entered = false;

        if ( mousehover == true )
           repaint();
    }

    public void mousePressed( MouseEvent e )
    {
    }

    public void mouseReleased( MouseEvent e )
    {}


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