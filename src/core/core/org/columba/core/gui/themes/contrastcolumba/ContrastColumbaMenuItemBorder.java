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
import java.awt.*;
import javax.swing.plaf.*;
import javax.swing.plaf.metal.*;
import org.columba.mail.gui.util.*;

import javax.swing.border.*;

public class ContrastColumbaMenuItemBorder extends AbstractBorder implements UIResource
{

    protected static Insets borderInsets = new Insets( 4, 4, 4, 4 );
      
    public void paintBorder(Component c, Graphics g, int x, int y, int w, int h)
    {
        JMenuItem item = (JMenuItem) c;
        ButtonModel model = item.getModel();

        if (  model.isArmed() || ( c instanceof JMenu && model.isSelected() ) )
        {
            g.setColor( UIManager.getColor("controlDkShadow")  );
            g.drawLine( 0, 0, w - 1, 0 );
            
            g.setColor( UIManager.getColor("controlHighlight") );
            g.drawLine( 0, h - 1, w - 1, h - 1 );

        } else
        {
              //g.setColor( MetalLookAndFeel.getPrimaryControlHighlight() );
              //g.drawLine( 0, 0, 0, h - 1 );
        }
        
          /*
        if ( item.isSelected() )
        {
            
            drawFlush3DBorder( g, x, y, w, h );
        }
          */
    }
    
    public Insets getBorderInsets( Component c )
    {
        return borderInsets;
    }
    

    private void drawFlush3DBorder(Graphics g, int x, int y, int w, int h)
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










