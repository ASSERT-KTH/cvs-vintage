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
package org.columba.core.config;

import org.columba.core.xml.XmlElement;

public class TableItem extends DefaultItem {


	public TableItem( XmlElement root )
	{
		super(root);
	}
	
	/*
	private Vector list;
	
	
	public HeaderTableItem()
	    {
	        list = new Vector();
	    }
	
	public HeaderTableItem( Vector v )
	{
	    list  = v;
	}
	
	public int count()
	{
	    return list.size();
	}
	
	
	public void addHeaderItem( AdapterNode str, AdapterNode b, AdapterNode pos, AdapterNode size )
	{
	    Item item = new Item();
	    item.setNameNode( str );
	
	    item.setEnabledNode( b );
	
	    item.setSizeNode( size );
	    item.setPositionNode( pos );
	
	
	    list.add( item );
	}
	
	public void removeEnabledItem()
	{
	
	    for ( int i=0; i< count(); i++ )
	    {
	        if ( !getEnabled(i) )
	            {
	                list.removeElementAt(i);
	                i--;
	            }
	    }
	}
	
	public Object clone()
	{
	    Vector v = (Vector) list.clone();
	
	    HeaderTableItem item = new HeaderTableItem( v );
	
	    return item;
	}
	*/
	
	public HeaderItem getHeaderItem( int index )
	{
		//XmlElement list = getElement("columnlist");
		
		return new HeaderItem(getRoot().getElement(index));
	}
	
	public int count()
	{
		//XmlElement list = getElement("columnlist");
		
		
		return getRoot().count();
	}
	
	
	/*
	public int getSize( int i )
	{
	   return getChildElement("column", i).getInteger("size");
	}
	
	public int getPosition( int i )
	{
	    Item item = (Item ) list.get(i);
	
	
	    int pos = item.getPosition();
	
	    return pos;
	}
	
	public void setSize( int i, int size )
	{
	
	    Item item = (Item ) list.get(i);
	
	    item.setSize( size );
	}
	
	public void setPosition( int i, int pos )
	{
	
	    Item item = (Item ) list.get(i);
	
	    item.setPosition( pos );
	}
	
	
	public String getName( int i )
	{
	    Item item = (Item) list.get(i);
	
	
	    String name = item.getName();
	    return name;
	
	}
	public boolean getEnabled( int i )
	{
	    Item item = (Item) list.get(i);
	
	    return item.getEnabled();
	}
	
	public int getSize( String name )
	{
	    for ( int i=0; i<count(); i++ )
	    {
	        String str = getName(i);
	        if ( str.equalsIgnoreCase( name ) ) return i;
	    }
	
	    return -1;
	}
	*/
	
	
	/*
	public Vector getList()
	{
	    return list;
	}
	
	public class Item
	{
	    AdapterNode enabledNode;
	    AdapterNode nameNode;
	    AdapterNode sizeNode;
	    AdapterNode positionNode;
	
	
	    public void setEnabledNode( AdapterNode node )
	    {
	        enabledNode = node;
	    }
	
	    public void setNameNode( AdapterNode node )
	    {
	        nameNode = node;
	    }
	
	    public void setSizeNode( AdapterNode node )
	    {
	        sizeNode = node;
	    }
	
	    public void setPositionNode( AdapterNode node )
	    {
	        positionNode = node;
	    }
	
	    public void setEnabled( boolean b )
	    {
	        if ( b == true )
	        {
	            enabledNode.setValue("true");
	        }
	        else
	            enabledNode.setValue("false");
	
	    }
	
	    public boolean getEnabled()
	    {
	        boolean b = ( new Boolean( enabledNode.getValue() ) ).booleanValue();
	
	        return b;
	    }
	
	    public void setName( String s )
	    {
	        nameNode.setValue( s );
	    }
	
	    public String getName()
	    {
	        return nameNode.getValue();
	    }
	
	    public void setSize( int s )
	    {
	        Integer i = new Integer( s );
	
	        sizeNode.setValue( i.toString() );
	    }
	
	    public int getSize()
	    {
	        int i = ( new Integer( sizeNode.getValue() ) ).intValue();
	
	        return i;
	    }
	
	    public void setPosition( int s )
	    {
	        Integer i = new Integer( s );
	
	        positionNode.setValue( i.toString() );
	    }
	
	    public int getPosition()
	    {
	        int i = ( new Integer( positionNode.getValue() ) ).intValue();
	
	        return i;
	
	    }
	}
	
	*/

}
