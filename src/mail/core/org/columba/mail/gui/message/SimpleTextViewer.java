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

package org.columba.mail.gui.message;

import java.awt.Component;
import java.awt.Font;
import java.awt.Insets;

import javax.swing.JTextArea;

import org.columba.core.config.Config;

public class SimpleTextViewer extends JTextArea implements DocumentViewer
{
    boolean active = false;

    public SimpleTextViewer()
    {
        super();
        setLineWrap( true );
	Font font = new Font( Config.getOptionsConfig().getThemeItem().getTextFontName(),
			      Font.PLAIN,
			      Config.getOptionsConfig().getThemeItem().getTextFontSize() );
        setFont( font );
        setMargin( new Insets(5,5,5,5) );
        setEditable(false);
    }

    public void setDoc( String str )
    {
        if ( str.length() > 0 ) super.setText( str );
        else setText("");
    }
    
    public void setHeader( Component header ) {  }

    public String getDoc()
    {
        return super.getText();
    }

    public void clearDoc()
    {
        setText("");
    }

    public void setActive( boolean b )
    {
        active = b;
    }

    public boolean isActive()
    {
        return active;
    }
}

