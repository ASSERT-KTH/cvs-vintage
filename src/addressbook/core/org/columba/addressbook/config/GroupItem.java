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


public class GroupItem extends DefaultItem
{
    private AdapterNode name, uid, list;

    
    public GroupItem(Document root)
    {
        super( root );
    
    }
    
    /*
        
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
    

    
    public void setUid( int i )
    {
        Integer h = new Integer( i );

        setTextValue( uid, h.toString() );
    }

            
    public void setName( String str )
    {
        setTextValue( name, str );
    }

    
    
    

    public String getName()
    {
        return getTextValue( name );
    }

   
    public int getUid()
    {
        Integer i = new Integer(  getTextValue( uid ) );
        
        return i.intValue();
    }



    public void add( int number )
    {
        AdapterNode child;
        boolean hit = false;
        
        for ( int i=0; i<list.getChildCount(); i++ )
        {
            child = list.getChild(i);
            Integer j = new Integer( getTextValue( child ) );

            int uid = j.intValue();

            if ( uid == number ) hit = true;
        }

          // new uid does not exist in group
        if ( hit == false )
        {
            Element element = createTextElementNode("uid", (new Integer( number )).toString() );

            list.addElement(  element );
                        
        }
        
    }
    
    public Vector getListNodes()
    {
        Vector v = new Vector();

        for ( int i=0; i<list.getChildCount(); i++ )
        {
            v.add( list.getChild(i) );
        }
        return v;
    }

    public int getUid( int i )
    {
        AdapterNode node = list.getChild(i);
        String str = node.getValue();
        
        int uid = Integer.parseInt( str );

        return uid;
    }

    public AdapterNode getNode( int i )
    {
        AdapterNode node = list.getChild(i);

        return node;
    }
    
    
	*/    
    
}
    



