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

package org.columba.addressbook.gui.util;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.columba.addressbook.folder.HeaderItem;
import org.columba.core.gui.util.ImageLoader;



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
