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

package org.columba.addressbook.parser;


import java.io.File;

import org.columba.addressbook.folder.ContactCard;
import org.columba.addressbook.folder.GroupListCard;
import org.columba.core.config.AdapterNode;
import org.columba.core.config.DefaultXmlConfig;

/**
 * @version 	1.0
 * @author
 */
public class DefaultCardLoader extends DefaultXmlConfig
{
	   
    public DefaultCardLoader(File file )
    {
    	super( file );       
    }
         
    
    public ContactCard createContactCard()
    {
    	return new ContactCard( getDocument(), null );
    }
    
    public GroupListCard createGroupListCard()
    {
    	return new GroupListCard( getDocument(), null );
    }
    
    public boolean isContact()
    {
    	AdapterNode rootNode = new AdapterNode( getDocument() );
    	
    	AdapterNode child = rootNode.getChildAt(0);
    	if ( child != null )
    	{
    		System.out.println("iscontact() ----->"+ child.getName() );
    		
    		if ( child.getName().equals("vcard") )
    		{
    			return true;
    		}
    		else
    		{
    			return false;
    		}
    	}
    	else
    	
    	return false;
    }
    
}
