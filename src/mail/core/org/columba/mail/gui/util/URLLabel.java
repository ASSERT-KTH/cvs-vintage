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

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
import javax.swing.*;

import org.columba.mail.parser.*;
import org.columba.main.MainInterface;
import org.columba.mail.gui.message.*;

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
