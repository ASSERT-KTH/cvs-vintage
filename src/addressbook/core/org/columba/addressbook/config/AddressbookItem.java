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

import java.util.Vector;

import org.columba.core.config.DefaultItem;
import org.columba.core.xml.XmlElement;


public class AddressbookItem extends DefaultItem
{
    private AdapterNode name, uid, list;
        
    public AddressbookItem( XmlElement root )
    {
        super( root );
    }
    
        
    public void setNameNode( AdapterNode node )
    {
        name = node;
    }
    
    public void setUidNode( AdapterNode node )
    {
        uid= node;
    }

    public void setListNode( AdapterNode node )
    {
        list = node;
    }
    

       


      /******************************************** set ***************************************/
    
    
    public void setUid( int i )
    {
        Integer h = new Integer( i );

	 //       setTextValue( uid, h.toString() );
    }

            
    public void setName( String str )
    {
      //  setTextValue( name, str );
    }

    
      /**************************************************** get *********************************/


    

    
    

    public String getName()
    {
        //return getTextValue( name );
        return "";
    }

   
    public int getUid()
    {
    	/*
        Integer i = new Integer(  getTextValue( uid ) );
        
        return i.intValue();
        */
        
        return -1;
    }


    public Vector getGroupList()
    {
        
        int count = list.getChildCount();
        Vector v = new Vector();

        for ( int i=0; i<count; i++ )
        {
            AdapterNode child = list.getChild(i);

            if ( child.getName().equals("group") )
                v.add( child );
        }

        return v;
    }
    
        
    
}
    



