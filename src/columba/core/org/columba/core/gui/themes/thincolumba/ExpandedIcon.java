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

