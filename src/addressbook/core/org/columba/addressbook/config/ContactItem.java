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



package org.columba.addressbook.config;


import org.w3c.dom.Document;


public class ContactItem extends DefaultItem
{
    private AdapterNode address, uid, firstname, lastname, displayName;
        
    
    public ContactItem( Document root)
    {
        super( root );
    
    }
    
    
        
    public void setAddressNode( AdapterNode node )
    {
        address = node;
    }
    
    public void setUidNode( AdapterNode node )
    {
        uid = node;
    }

    public void setFirstNameNode( AdapterNode node )
    {
        firstname = node;
    }

    public void setLastNameNode( AdapterNode node )
    {
        lastname = node;
    }

    public void setDisplayNameNode( AdapterNode node )
    {
        displayName = node;
    }
    

    
    public void setUid( int i )
    {
        Integer h = new Integer( i );

        setTextValue( uid, h.toString() );
    }

            
    public void setAddress( String str )
    {
        setCDATAValue( address, str );
    }

    public void setFirstName( String str )
    {
        setTextValue( firstname, str );
    }

    public void setLastName( String str )
    {
        setTextValue( lastname, str );
    }

    public void setDisplayName( String str )
    {
        setTextValue( displayName, str );
    }
    


    

    
    

    public String getAddress()
    {
        return getCDATAValue( address );
    }

   
    public int getUid()
    {
        Integer i = new Integer(  getTextValue( uid ) );
        
        return i.intValue();
    }

    public String getFirstName()
    {
        return getTextValue( firstname );
    }

    public String getLastName()
    {
        return getTextValue( lastname );
    }
    
    public String getDisplayName()
    {
        return getTextValue( displayName );
    }

     
}
    



