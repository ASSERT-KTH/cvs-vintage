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

public class MultiLineLabel extends JTextArea
{

    public MultiLineLabel( String s )
    {
        super( s );

        //setBorder( UIManager.getBorder( "Label.border" ) );
        //setBorder( BorderFactory.createEmptyBorder( 5,5,5,5 ) );
        
        setEditable( false );
        setBackground( UIManager.getColor( "Label.background" ) );
        setFont( UIManager.getFont( "Label.font") );
        setWrapStyleWord( true );

    }

    public MultiLineLabel( String[] s )
    {
        StringBuffer buf = new StringBuffer();

        for ( int i=0; i<s.length; i++ )
        {
            buf.append( s[i] );
        }

        setText( buf.toString() );
        setBorder( BorderFactory.createEmptyBorder( 10,10,10,10 ) );
        setEditable( false );
        setBackground( UIManager.getColor( "Label.background" ) );
        setFont( UIManager.getFont( "Label.font") );
        setWrapStyleWord( true );
    }




}