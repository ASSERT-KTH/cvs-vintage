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

import java.awt.Component;
import java.awt.LayoutManager;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

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




