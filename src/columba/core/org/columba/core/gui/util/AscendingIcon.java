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

package org.columba.core.gui.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.ImageIcon;

public class AscendingIcon extends ImageIcon
    {
        public AscendingIcon()
            {
                super();        
            }
        public void paintIcon(Component c, Graphics g, int x, int y)
            {
                Graphics2D g2 = (Graphics2D) g;
                            
                int[] xp = new int[3];
                int[] yp = new int[3];
                xp[0]=x;
                xp[1]=x+12;
                xp[2]=x+6;
                  
                yp[0]=y - (c.getHeight()/4);
                yp[1]=y - (c.getHeight()/4);
                yp[2]=y + (c.getHeight()/4);
                  

                  /*
                yp[0]=y - (c.getHeight()/6);
                yp[1]=y - (c.getHeight()/6);
                yp[2]=y + (c.getHeight()/6);
                  */
                g2.setColor( Color.white );
                g2.drawLine(xp[0],yp[0],xp[1],yp[1]);
                g2.drawLine(xp[1],yp[1],xp[2],yp[2]);
                g2.setColor( Color.gray );
                g2.drawLine(xp[2],yp[2],xp[0],yp[0]);
            }
    }
