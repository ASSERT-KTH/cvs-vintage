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


import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;

public class ExpandedIcon implements Icon
{

    static private final Dimension treeControlSize = new Dimension( 22, 22 );
        // This data member should not have been exposed.  It's called
        // isLight, but now it really means isCollapsed.  Since we can't change
        // any APIs... that's life.
        protected boolean isLight;


    public ExpandedIcon( boolean b)
    {
	    isLight = b;    
    }

    transient Image image;
    transient boolean cachedOrientation = true;
    
    public void paintIcon(Component c, Graphics g, int x, int y) {
        
       
            image = new BufferedImage(getIconWidth(), getIconHeight(),
                                      BufferedImage.TYPE_INT_ARGB);
            Graphics imageG = image.getGraphics();		
            paintMe(c,imageG,x,y);
            imageG.dispose();
            
       
        
        g.drawImage(image, x, y, x+21, y+21, 
                    0,0, 21, 21, null);
         
    }

    public void paintMe(Component c, Graphics g, int x, int y)
    {
        
        g.setColor( MetalLookAndFeel.getPrimaryControlInfo() );
        
       
        g.setColor( Color.white );
        g.fillRect( 6, 7, 8, 8 );
        
     
        
        g.setColor( UIManager.getColor("controlShadow") );
        
        g.drawRect( 6, 7, 8, 8 );

        g.setColor( Color.black );
        g.drawLine( 8,11,12,11 );
        
        if ( !isLight )
            g.drawLine( 10,9,10,13 );
        
    }
    
    public int getIconWidth() { return treeControlSize.width; }
    public int getIconHeight() { return treeControlSize.height; }
}

