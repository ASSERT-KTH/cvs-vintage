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

import javax.swing.*;
import javax.swing.border.*;

import java.awt.*;

public class CPanel extends JPanel
{

    JPanel panel;

    JPanel innerPanel;

    static int size = 10;

    public CPanel( String title )
    {
        super();
        setBorder( BorderFactory.createEmptyBorder( size, size, size, size ) );

        innerPanel = new JPanel();
        innerPanel.setBorder( javax.swing.BorderFactory.createTitledBorder(
                   javax.swing.BorderFactory.createEtchedBorder(),
                   title ) );
        innerPanel.setLayout( new BoxLayout( innerPanel, BoxLayout.Y_AXIS ) );

        panel = new JPanel();
        panel.setBorder( BorderFactory.createEmptyBorder( size, size, size, size ) );
        panel.setLayout( new BoxLayout( panel, BoxLayout.Y_AXIS ) );


        setLayout( new BoxLayout( this, BoxLayout.Y_AXIS ) );


        innerPanel.add( panel );





          //add( panel, BorderLayout.CENTER );
        super.add( innerPanel );

    }


    public CPanel( String title, boolean b)
    {
        super();
        setBorder( BorderFactory.createEmptyBorder( size, size, size, size ) );

        innerPanel = new JPanel();
        innerPanel.setBorder( javax.swing.BorderFactory.createTitledBorder(
                   javax.swing.BorderFactory.createEtchedBorder(),
                   title ) );
        if ( b == true )
            innerPanel.setLayout( new BoxLayout( innerPanel, BoxLayout.Y_AXIS ) );
        else
            innerPanel.setLayout( new BoxLayout( innerPanel, BoxLayout.X_AXIS ) );

        panel = new JPanel();
        panel.setBorder( BorderFactory.createEmptyBorder( size, size, size, size ) );
        if ( b == true )
            panel.setLayout( new BoxLayout( panel, BoxLayout.Y_AXIS ) );
        else
            panel.setLayout( new BoxLayout( panel, BoxLayout.X_AXIS ) );


        if ( b == true )
            setLayout( new BoxLayout( this, BoxLayout.Y_AXIS ) );
        else
            setLayout( new BoxLayout( this, BoxLayout.X_AXIS ) );


        innerPanel.add( panel );





          //add( panel, BorderLayout.CENTER );
        super.add( innerPanel );
    }


    public Component add(Component comp)
    {
        return panel.add( comp );
    }

    public Component add(Component comp, int index)
    {
        return panel.add( comp, index );
    }

    public void setInnerLayout(LayoutManager mgr)
    {
        panel.setLayout( mgr );
    }
}




