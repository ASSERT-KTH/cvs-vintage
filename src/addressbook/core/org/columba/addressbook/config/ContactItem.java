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
    



