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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.metal.*;
import javax.swing.plaf.basic.*;

import javax.swing.tree.*;

public class ContrastColumbaTreeUI extends MetalTreeUI
{

    protected static ContrastColumbaTreeUI treeUI;

    final static float dash1[] = {1.1f};
    final static BasicStroke dashed = new BasicStroke(0.5f, 
                                                      BasicStroke.CAP_BUTT, 
                                                      BasicStroke.JOIN_ROUND, 
                                                      10.0f, dash1, 0.0f);

      
    public ContrastColumbaTreeUI()
    {
        super();
                
    }
    
    public static ComponentUI createUI(JComponent x) {
	return new ContrastColumbaTreeUI();
    }

    protected void installDefaults()
    {
        super.installDefaults();

        setExpandedIcon( new ExpandedIcon(true) );
        setCollapsedIcon( new ExpandedIcon(false) );
    
    }
   
    
    
    protected void paintVerticalLine(Graphics g, JComponent c, int x, int top,
				    int bottom) {

        Graphics2D graphics = (Graphics2D) g;

        g.setColor( MetalLookAndFeel.getControlDarkShadow() );
          //graphics.setColor( Color.darkGray );
        graphics.setStroke( dashed );
        
        g.drawLine(x, top, x, bottom);
    }
    
    
    protected void paintHorizontalLine(Graphics g, JComponent c, int y,
				      int left, int right) {
        Graphics2D graphics = (Graphics2D) g;

        g.setColor( MetalLookAndFeel.getControlDarkShadow() );
        
        graphics.setStroke( dashed );
	g.drawLine(left, y, right, y);
    }
    

        
}


