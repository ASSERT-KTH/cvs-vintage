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
    



