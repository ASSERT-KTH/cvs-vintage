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
