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

package org.columba.core.gui.themes.thincolumba;

import javax.swing.*;
import java.awt.*;
import javax.swing.plaf.*;
import javax.swing.plaf.metal.*;
import org.columba.mail.gui.util.*;

import javax.swing.border.*;

public class ThinColumbaUtil 
{

    static void drawRolloverBorder(Graphics g, int x, int y, int w, int h )
    {
        g.translate( x, y);
        g.setColor( MetalLookAndFeel.getControlDarkShadow() );
         
        g.drawLine( w-1, 1, w-1, h-1 );
        g.drawLine( 1,h-1, w-1, h-1 );

                 
        g.setColor( MetalLookAndFeel.getControlHighlight() );
         
        g.drawLine( 0,0, w-2,0 );
        g.drawLine( 0,0, 0, h-2 );
        
        
        g.setColor( MetalLookAndFeel.getControl() );
          //g.drawLine( 0, h-1, 1, h-2 );
          //g.drawLine( w-1, 0, w-2, 1 );
        g.translate( -x, -y);
    }
    
     
    static void drawButtonBorder(Graphics g, int x, int y, int w, int h, boolean active)
    {
        if (active)
        {
            drawActiveButtonBorder(g, x, y, w, h);	    
        } else
        {
            drawFlush3DBorder(g, x, y, w, h);
	}
    }

    static void drawDefaultButtonBorder(Graphics g, int x, int y, int w, int h, boolean active)
    {
        if (active)
        {
            drawDefaultActiveButtonBorder(g, x, y, w, h);	    
        } else
        {
            drawDefaultFlush3DBorder(g, x, y, w, h);
	}
    }

    static void drawActiveButtonBorder(Graphics g, int x, int y, int w, int h)
    {
        drawFlush3DBorder(g, x, y, w, h);
        g.setColor( MetalLookAndFeel.getPrimaryControlDarkShadow() );
        g.drawRect( x,y, w,h );        
          /*
        g.setColor( MetalLookAndFeel.getPrimaryControl() );
	g.drawLine( x+1, y+1, x+1, h-3 );
	g.drawLine( x+1, y+1, w-3, x+1 );
        g.setColor( MetalLookAndFeel.getPrimaryControlDarkShadow() );
	g.drawLine( x+2, h-2, w-2, h-2 );
	g.drawLine( w-2, y+2, w-2, h-2 );
          */
    }

    static void drawDefaultActiveButtonBorder(Graphics g, int x, int y, int w, int h)
    {
        drawFlush3DBorder(g, x, y, w, h);
          /*
          g.setColor( MetalLookAndFeel.getPrimaryControl() );
          g.drawLine( x+1, y+1, x+1, h-3 );
          g.drawLine( x+1, y+1, w-3, x+1 );
          //g.setColor( MetalLookAndFeel.getPrimaryControlDarkShadow() );
          //g.drawRect( x,y, w,h );
        
          g.drawLine( x+2, h-2, w-2, h-2 );
          g.drawLine( w-2, y+2, w-2, h-2 );
          */

        g.setColor( MetalLookAndFeel.getPrimaryControlDarkShadow() );
        g.drawRect( x,y, w,h );        
    }

      /*
    static void drawDefaultButtonBorder(Graphics g, int x, int y, int w, int h, boolean active)
    {
        drawButtonBorder(g, x+1, y+1, w-1, h-1, active);	    
        g.setColor( MetalLookAndFeel.getControlDarkShadow() );
	g.drawRect( x, y, w-3, h-3 );
	g.drawLine( w-2, 0, w-2, 0);
	g.drawLine( 0, h-2, 0, h-2);
    }
      */
    
    /**
      * This draws the "Flush 3D Border" which is used throughout the Metal L&F
      */
    static void drawFlush3DBorder(Graphics g, int x, int y, int w, int h)
    {
        g.translate( x, y);
        g.setColor( MetalLookAndFeel.getControlDarkShadow() );
          //g.drawLine( w-2, 2, w-2, h-2 );
          //g.drawLine( 2,h-2, w-2, h-2 );
        g.drawLine( w-1, 1, w-1, h-1 );
        g.drawLine( 1,h-1, w-1, h-1 );

          //g.setColor( Color.black );
          //g.drawLine( w-1, 0, w-1, h-1 );
          //g.drawLine( 0, h-1, w-1, h-1 );
        
        
        g.setColor( MetalLookAndFeel.getControlHighlight() );
          //g.drawLine( 0,0, w-2,0 );
          //g.drawLine( 0,0, 0, h-2 );
        g.drawLine( 0,0, w-1,0 );
        g.drawLine( 0,0, 0, h-1 );
        

        g.translate( -x, -y);
    }


    static void drawDefaultFlush3DBorder(Graphics g, int x, int y, int w, int h)
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

    
    /**
      * This draws a variant "Flush 3D Border"
      * It is used for things like pressed buttons.
      */
    static void drawPressed3DBorder(Graphics g, Rectangle r) {
        drawPressed3DBorder( g, r.x, r.y, r.width, r.height );
    }

    static void drawDisabledBorder(Graphics g, int x, int y, int w, int h) {
        g.translate( x, y);
        g.setColor( MetalLookAndFeel.getControlShadow() );
	g.drawRect( 0, 0, w-1, h-1 );
    }

    /**
      * This draws a variant "Flush 3D Border"
      * It is used for things like pressed buttons.
      */
    static void drawPressed3DBorder(Graphics g, int x, int y, int w, int h) {
        g.translate( x, y);
          /*
        g.setColor( MetalLookAndFeel.getControlShadow() );
	g.drawLine( 0, 0, 0, h-1 );
	g.drawLine( 0, 0, w-1, 0 );
          */
        g.setColor( MetalLookAndFeel.getControlHighlight() );
          //g.drawLine( w-2, 2, w-2, h-2 );
          //g.drawLine( 2,h-2, w-2, h-2 );
        g.drawLine( w-1, 1, w-1, h-1 );
        g.drawLine( 1,h-1, w-1, h-1 );

          //g.setColor( Color.black );
          //g.drawLine( w-1, 0, w-1, h-1 );
          //g.drawLine( 0, h-1, w-1, h-1 );
        
        
        g.setColor( MetalLookAndFeel.getControlDarkShadow() );
          //g.drawLine( 0,0, w-2,0 );
          //g.drawLine( 0,0, 0, h-2 );
        g.drawLine( 0,0, w-1,0 );
        g.drawLine( 0,0, 0, h-1 );
        g.translate( -x, -y);
    }

     static void drawTogglePressed3DBorder(Graphics g, int x, int y, int w, int h) {
        g.translate( x, y);
          /*
        g.setColor( MetalLookAndFeel.getControlShadow() );
	g.drawLine( 0, 0, 0, h-1 );
	g.drawLine( 0, 0, w-1, 0 );
          */
        g.setColor( MetalLookAndFeel.getControlHighlight() );
          //g.drawLine( w-2, 2, w-2, h-2 );
          //g.drawLine( 2,h-2, w-2, h-2 );
          //g.drawLine( w-1, 1, w-1, h-1 );
          //g.drawLine( 1,h-1, w-1, h-1 );

          //g.setColor( Color.black );
          //g.drawLine( w-1, 0, w-1, h-1 );
          //g.drawLine( 0, h-1, w-1, h-1 );
        
        
        g.setColor( MetalLookAndFeel.getControlDarkShadow() );
          //g.drawLine( 0,0, w-2,0 );
          //g.drawLine( 0,0, 0, h-2 );
        g.drawLine( 0,0, w-1,0 );
        g.drawLine( 0,0, 0, h-1 );
        g.translate( -x, -y);
    }

    /**
      * This draws a variant "Flush 3D Border"
      * It is used for things like active toggle buttons.
      * This is used rarely.
      */
    static void drawDark3DBorder(Graphics g, Rectangle r) {
        drawDark3DBorder(g, r.x, r.y, r.width, r.height);
    }

    /**
      * This draws a variant "Flush 3D Border"
      * It is used for things like active toggle buttons.
      * This is used rarely.
      */
    static void drawDark3DBorder(Graphics g, int x, int y, int w, int h) {
        g.translate( x, y);

        drawFlush3DBorder(g, 0, 0, w, h);

        g.setColor( MetalLookAndFeel.getControl() );
	g.drawLine( 1, 1, 1, h-2 );
	g.drawLine( 1, 1, w-2, 1 );
        g.setColor( MetalLookAndFeel.getControlShadow() );
	g.drawLine( 1, h-2, 1, h-2 );
	g.drawLine( w-2, 1, w-2, 1 );
        g.translate( -x, -y);
    }
    }
