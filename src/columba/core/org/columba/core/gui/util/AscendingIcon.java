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
