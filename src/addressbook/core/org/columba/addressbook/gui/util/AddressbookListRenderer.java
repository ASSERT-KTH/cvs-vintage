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

package org.columba.addressbook.gui.util;

import java.awt.*;
import javax.swing.*;

import org.columba.core.config.*;
import org.columba.core.gui.util.*;
import org.columba.core.gui.util.ImageLoader;


import org.columba.addressbook.folder.*;
import org.columba.addressbook.gui.table.util.*;



public class AddressbookListRenderer extends JLabel implements ListCellRenderer
{
    
    ImageIcon image1 = ImageLoader.getSmallImageIcon("contact_small.png");
    ImageIcon image2 = ImageLoader.getSmallImageIcon("group_small.png");
    
    public AddressbookListRenderer()
    {
        setOpaque(true);
        setHorizontalAlignment(LEFT);
        setVerticalAlignment(CENTER);
        
        
    }
    public Component getListCellRendererComponent (
                                                    JList list,
                                                    Object value,
                                                    int index,
                                                    boolean isSelected,
                                                    boolean cellHasFocus)
    {
        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }

		
        
        HeaderItem item = (HeaderItem) value;
        
        if ( item.isContact() )
        {
        	setIcon( image1 );
        	
        	String displayname = (String) item.get("displayname");
        	
        	setText( displayname );
        	
        	
        	StringBuffer buf = new StringBuffer();
        	buf.append( "<html>Name: "+ convert( displayname)  );
        	buf.append( "<br>EMail: "+ convert( (String) item.get("email;internet") ) );
        	buf.append( "</html>");
        	setToolTipText( buf.toString() );
        }
        else
        {
        	setIcon( image2 );
        	String displayname = (String) item.get("displayname");
        	setText( displayname );
        	setToolTipText( "" );
        }
            
            
        return this;
    }
    
    protected String convert( String str )
    {
    	if ( str == null ) return "";
    	
    	StringBuffer result = new StringBuffer();
    	int pos = 0;
    	char ch;
    	
    	while ( pos<str.length() )
    	{
    		ch = str.charAt(pos);
    		
    		if ( ch == '<' )
    		{
    			result.append("&lt;");			
    		}
    		else if ( ch == '>' )
    		{
    			result.append("&gt;");		
    		}
    		else
    		{
    			result.append( ch );
    		}
    		
    		pos++;
    		
    	}
    	
    	return result.toString();
    }
} 
