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

import javax.swing.plaf.basic.*;
import javax.swing.plaf.metal.*;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Color;

import javax.swing.*;

public class ThinColumbaArrowButton extends BasicArrowButton
{
    public ThinColumbaArrowButton(int direction)
    {
        super( direction );
        setRequestFocusEnabled(false);
        setDirection(direction);
        setBackground(UIManager.getColor("control"));        
    }

    public void paintTriangle(Graphics g, int x, int y, int size, 
					int direction, boolean isEnabled) {
	    Color oldColor = g.getColor();
	    int mid, i, j;

	    j = 0;
            size = Math.max(size, 2);
	    mid = size / 2;
	
	    g.translate(x, y);
	    if(isEnabled)
                  g.setColor( Color.black );//g.setColor(UIManager.getColor("controlDkShadow"));
	    else
		g.setColor(UIManager.getColor("controlShadow"));

            switch(direction)       {
            case NORTH:
                for(i = 0; i < size; i++)      {
                    g.drawLine(mid-i, i, mid+i, i);
                }
                if(!isEnabled)  {
                    g.setColor(UIManager.getColor("controlLtHighlight"));
                    g.drawLine(mid-i+2, i, mid+i, i);
                }
                break;
            case SOUTH:
                if(!isEnabled)  {
                    g.translate(1, 1);
                    g.setColor(UIManager.getColor("controlLtHighlight"));
                    for(i = size-1; i >= 0; i--)   {
                        g.drawLine(mid-i, j, mid+i, j);
                        j++;
                    }
		    g.translate(-1, -1);
		    g.setColor(UIManager.getColor("controlShadow"));
		}
		
		j = 0;
                for(i = size-1; i >= 0; i--)   {
                    g.drawLine(mid-i, j, mid+i, j);
                    j++;
                }
                break;
            case WEST:
                for(i = 0; i < size; i++)      {
                    g.drawLine(i, mid-i, i, mid+i);
                }
                if(!isEnabled)  {
                    g.setColor(UIManager.getColor("controlLtHighlight"));
                    g.drawLine(i, mid-i+2, i, mid+i);
                }
                break;
            case EAST:
                if(!isEnabled)  {
                    g.translate(1, 1);
                    g.setColor(UIManager.getColor("controlLtHighlight"));
                    for(i = size-1; i >= 0; i--)   {
                        g.drawLine(j, mid-i, j, mid+i);
                        j++;
                    }
		    g.translate(-1, -1);
		    g.setColor(UIManager.getColor("controlShadow"));
                }

		j = 0;
                for(i = size-1; i >= 0; i--)   {
                    g.drawLine(j, mid-i, j, mid+i);
                    j++;
                }
		break;
            }
	    g.translate(-x, -y);	
	    g.setColor(oldColor);
	}

    public void paint(Graphics g)
    {
	    Color origColor;
	    boolean isPressed, isEnabled;
	    int w, h, size;

            w = getSize().width;
            h = getSize().height;
	    origColor = g.getColor();
	    isPressed = getModel().isPressed();
	    isEnabled = isEnabled();

            g.setColor(getBackground());
            g.fillRect(1, 1, w-2, h-2);

            /// Draw the proper Border
            if (isPressed) {
                g.setColor(UIManager.getColor("controlShadow"));
                //g.drawRect(1, 1, w-2, h-2);
                g.drawRect(1, 1, w-2, h-2);
            } else
            {
                g.setColor( MetalLookAndFeel.getControlDarkShadow() );
                g.drawLine( w-1, 1, w-1, h-1 );
                g.drawLine( 1,h-1, w-1, h-1 );
                
                g.setColor( MetalLookAndFeel.getControlHighlight() );
                g.drawLine( 1,1, w-1,1 );
                g.drawLine( 1,1, 1, h-1 );
                
                
            }

            // If there's no room to draw arrow, bail
            if(h < 5 || w < 5)      {
                g.setColor(origColor);
                return;
            }

            if (isPressed) {
                g.translate(1, 1);
            }

            // Draw the arrow
            size = Math.min((h - 4) / 3, (w - 4) / 3);
            size = Math.max(size, 2);
	    paintTriangle(g, (w - size) / 2, (h - size) / 2,
				size, direction, isEnabled);

            // Reset the Graphics back to it's original settings
            if (isPressed) {
                g.translate(-1, -1);
	    }
	    g.setColor(origColor);

        }
    
}





