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

package org.columba.addressbook.folder;

import java.util.Vector;

import org.columba.addressbook.folder.*;


/**
 * @version 	1.0
 * @author
 */
public class HeaderItemList
{
	private Vector list;
	
	public HeaderItemList()
	{
		list = new Vector();
	}
	
	public HeaderItemList( Vector v )
	{
		list = v;
	}
	
	public void insertElementAt( HeaderItem item, int index )
	{
		list.insertElementAt(item, index );
	}
	
	public void remove( HeaderItem item )
	{
		list.remove( item  );
	}
	
	public int indexOf( HeaderItem item )
	{
		return list.indexOf( item );
	}
	
	public Vector getVector()
	{
		return list;
	}
	
	public void add( HeaderItem item )
	{
		if ( item != null )
		{
			
			list.add( item );
		}
		else
		{
			System.out.println("item == null!!!");
		}
	}
	
	public void replace( int index, HeaderItem item )
	{
		if ( ( index < list.size() ) && ( index >= 0 ) )
		{
		list.remove(index);
		list.insertElementAt(item,index);
		}
	}
	
	public void uidRemove( Object uid )
	{
		for ( int i=0; i<count(); i++ )
		{
			HeaderItem item = (HeaderItem) get(i);
			Object u = item.getUid();
			if ( u.equals(uid) ) 
			{
				list.remove(i);
				break;
			}
		}
		
		
	}
	
	public HeaderItem uidGet( Object uid )
	{
		for ( int i=0; i<count(); i++ )
		{
			HeaderItem item = (HeaderItem) get(i);
			Object u = item.getUid();
			if ( u.equals(uid) ) 
			{
				return item;
				
				
			}
		}
		
		return null;
	}
		
	public HeaderItem get( int index )
	{
		HeaderItem item = (HeaderItem) list.get(index);
		
		return item;
	}
	
	public int count()
	{
		return list.size();
	}
	
	public void clear()
	{
		list.clear();
	}

}
