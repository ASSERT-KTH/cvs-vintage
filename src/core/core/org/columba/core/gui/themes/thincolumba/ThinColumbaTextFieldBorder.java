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

import javax.swing.border.*;

public class ThinColumbaTextFieldBorder extends AbstractBorder implements UIResource
{

       private static final Insets insets = new Insets(1, 1, 2, 2);

        public void paintBorder(Component c, Graphics g, int x, int y,
			  int w, int h) {

            JTextField tf = (JTextField) c;
            
            g.translate( x, y);

            g.setColor( MetalLookAndFeel.getControlDarkShadow() );
            g.drawLine( 0,0,0, h-1 );
            g.drawLine( 0,0,w-1,0 );
            
            g.setColor( MetalLookAndFeel.getControlHighlight() );
            g.drawLine( 0, h-1, w-1, h-1 );
            g.drawLine( w-1, 0, w-1, h-1 );
              //g.drawLine( 0, h, w, h );
              //g.drawLine( w, 0, w, h );
            
                        
            g.setColor( MetalLookAndFeel.getControl() );
            
            g.translate( -x, -y);

            
        }

        public Insets getBorderInsets(Component c)       {
            return insets;
        }
    }
