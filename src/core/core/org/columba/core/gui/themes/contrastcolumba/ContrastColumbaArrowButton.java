package org.columba.core.gui.themes.contrastcolumba;


import javax.swing.plaf.basic.*;
import javax.swing.plaf.metal.*;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Color;


import javax.swing.*;


public class ContrastColumbaArrowButton extends BasicArrowButton
{
    public ContrastColumbaArrowButton(int direction)
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
                    g.drawLine(mid-i-1, i-1, mid+i-1, i-1);
                }
                
                g.drawLine( mid-size+1-1, size-1, mid+size-1-1, size-1 );
                
                if(!isEnabled)  {
                    g.setColor(UIManager.getColor("controlLtHighlight"));
                    g.drawLine(mid-i+2, i, mid+i, i);
                }
                break;
            case SOUTH:
                if(!isEnabled)  {
                    g.translate(1, 1);
                    g.setColor(UIManager.getColor("controlLtHighlight"));

                    g.drawLine( mid-size, size-1, mid+size-2, size-1 );
                    
                                
                    for(i = size-1; i >= 0; i--)   {
                        g.drawLine(mid-i-1, j-1, mid+i-1, j-1);
                        j++;
                    }
                    
		    g.translate(-1, -1);
		    g.setColor(UIManager.getColor("controlShadow"));
		}
		
		j = 0;
                g.drawLine( mid-size, -1, mid+size-2, -1 );
                
                for(i = size-1; i >= 0; i--)   {
                    g.drawLine(mid-i-1, j, mid+i-1, j);
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

            if ( direction == NORTH )
            {
                
                g.setColor(getBackground());
                g.fillRect(1+1, 1+1, w-2-1, h-2-1);
                
                  /// Draw the proper Border
                if (isPressed) {
                    g.setColor(UIManager.getColor("controlShadow"));
                    g.drawRect(1+1, 1+1, w-2-1, h-2-1);
                } else
                {
                    
                    g.setColor( MetalLookAndFeel.getControlDarkShadow() );
                    g.drawLine( w-2-1, 3, w-2-1, h-2 );
                    g.drawLine( 3,h-2, w-2-1, h-2 );
                    
                    g.setColor( Color.black );
                    g.drawLine( w-1-1, 1, w-1-1, h-1 );
                    g.drawLine( 1, h-1, w-1-1, h-1 );
                    
                    
                    g.setColor( MetalLookAndFeel.getControlHighlight() );
                    g.drawLine( 1,1, w-2-1,1 );
                    g.drawLine( 1,1, 1, h-2 );
                    
                    
                    
                }
            }
            else if ( direction == SOUTH )
            {
                g.setColor(getBackground());
                g.fillRect(1+1, 1+1, w-2-1, h-2-1);
                
                  /// Draw the proper Border
                if (isPressed) {
                    g.setColor(UIManager.getColor("controlShadow"));
                    g.drawRect(1+1, 1+1, w-2-1, h-2-1);
                } else
                {
                    
                    g.setColor( MetalLookAndFeel.getControlDarkShadow() );
                    g.drawLine( w-2-1, 2, w-2-1, h-3 );
                    g.drawLine( 3,h-3, w-2-1, h-3 );
                    
                    g.setColor( Color.black );
                    g.drawLine( w-1-1, 0, w-1-1, h-2 );
                    g.drawLine( 1, h-2, w-1-1, h-2 );
                    
                    
                    g.setColor( MetalLookAndFeel.getControlHighlight() );
                    g.drawLine( 1,0, w-2-1,0 );
                    g.drawLine( 1,0, 1, h-2 );
                    
                    
                    
                }
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





