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
    



