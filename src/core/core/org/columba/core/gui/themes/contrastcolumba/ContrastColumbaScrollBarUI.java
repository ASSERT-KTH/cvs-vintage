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

package org.columba.core.gui.themes.contrastcolumba;

import javax.swing.*;
import javax.swing.plaf.basic.*;
import javax.swing.plaf.metal.*;
import javax.swing.plaf.*;

import java.awt.*;

public class ContrastColumbaScrollBarUI extends BasicScrollBarUI
{
      
    public static ComponentUI createUI( JComponent c )
    {
        return new ContrastColumbaScrollBarUI();
    }

      
    protected JButton createDecreaseButton( int orientation )
    {
        return new ContrastColumbaArrowButton( orientation );    
    }


    protected JButton createIncreaseButton( int orientation )
    {
        return new ContrastColumbaArrowButton( orientation );
    }

      
    protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds)  
    {
	if(thumbBounds.isEmpty() || !scrollbar.isEnabled())	{
	    return;
	}

        int w = thumbBounds.width;
        int h = thumbBounds.height;		

	g.translate(thumbBounds.x, thumbBounds.y);

        drawFlush3DBorder( g, 1,1, w-2, h-2 );
        
	g.translate(-thumbBounds.x, -thumbBounds.y);
    }

    protected void paintTrack( Graphics g, JComponent c, Rectangle trackBounds )
    {
        int w=c.getWidth();
        int h=c.getHeight();
        
        g.setColor( MetalLookAndFeel.getControlDarkShadow() );
        g.drawLine( 0, 0, 0, h-1 );
        g.drawLine( 0,0,w-1,0 );

        g.setColor( MetalLookAndFeel.getControlHighlight() );
        g.drawLine( 1,h-1, w-1,h-1 );
        g.drawLine( w-1,1, w-1, h-1 );
    }
    
    protected void drawFlush3DBorder(Graphics g, int x, int y, int w, int h)
    {
        g.translate( x, y);
        
        g.setColor( MetalLookAndFeel.getControlDarkShadow() );
        g.drawLine( w-2, 2, w-2, h-2 );
        g.drawLine( 2,h-2, w-2, h-2 );
        
        g.setColor( Color.black );
        g.drawLine( w-1, 0, w-1, h-1 );
        g.drawLine( 0, h-1, w-1, h-1 );
        
        
        g.setColor( MetalLookAndFeel.getControlHighlight() );
        g.drawLine( 0,0, w-2,0 );
        g.drawLine( 0,0, 0, h-2 );
                

        g.translate( -x, -y);
    }
      
}






