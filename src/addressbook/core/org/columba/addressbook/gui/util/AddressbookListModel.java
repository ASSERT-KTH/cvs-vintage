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

package org.columba.addressbook.gui.util;

import java.util.Vector;

import javax.swing.AbstractListModel;

import org.columba.addressbook.folder.HeaderItem;
import org.columba.addressbook.folder.HeaderItemList;
import org.columba.addressbook.gui.table.util.TableModelPlugin;

/**
 * @version 	1.0
 * @author
 */
public class AddressbookListModel extends AbstractListModel
{
	private Vector listClone;
	private Vector list;

	private String patternString = new String();

	public AddressbookListModel()
	{
		super();
		list = new Vector();
		listClone = new Vector();

	}

	public Object getElementAt(int index)
	{
		return (HeaderItem) list.get(index);
	}

	public int getSize()
	{
		return list.size();
	}

	public String getPatternString()
	{
		return patternString;
	}

	public void setPatternString(String s) throws Exception
	{
		patternString = s;

		//manipulateModel(TableModelPlugin.STRUCTURE_CHANGE);

	}

	public void clear()
	{
		list.clear();
	}

	public void addElement(Object item)
	{
		list.addElement(item);
		int index = list.indexOf(item);
		
		fireIntervalAdded( this, index, index );
	}

	public void setHeaderList(HeaderItemList l)
	{

		System.out.println("list size:" + l.count());

		list = (Vector) l.getVector().clone();

		fireContentsChanged(this, 0, list.size() - 1);

	}

	public HeaderItem get(int i)
	{
		return (HeaderItem) list.get(i);
	}

	public boolean addItem(HeaderItem header)
	{
		boolean result1 = false;

		Object o = header.get("displayname");
		if (o != null)
		{
			if (o instanceof String)
			{
				String item = (String) o;
				//System.out.println("add item?:"+item);

				item = item.toLowerCase();
				String pattern = getPatternString().toLowerCase();

				if (item.indexOf(pattern) != -1)
					result1 = true;
				else
					result1 = false;
			}
			else
				result1 = false;
		}
		else
			result1 = false;

		return result1;
	}

	public Object[] toArray()
	{
		return list.toArray();
	}

	public void remove(int index)
	{
		list.removeElementAt(index);
		fireIntervalRemoved(this,index,index);
	}

	public void removeElement(Object o)
	{
		int index = list.indexOf(o);
		
		remove( index );
	}

	public boolean manipulateModel(int mode) throws Exception
	{
		listClone = (Vector) list.clone();

		switch (mode)
		{
			case TableModelPlugin.STRUCTURE_CHANGE :
				{

					if (getSize() == 0)
						return false;

					//System.out.println("starting filtering");

					HeaderItem item = null;

					for (int i = 0; i < getSize(); i++)
					{

						item = (HeaderItem) get(i);

						boolean result = addItem(item);

						//ystem.out.println("item: "+i+" - result: "+result);

						if (!result)
						{
							//System.out.println("removing item:"+item);

							list.removeElement(item);
							i--;
							/*
							Object uid = list.getUid(i);
							MessageNode childNode = new MessageNode( header, uid );
							rootNode.add( childNode );
							*/
						}

					}

					// System.out.println("finished filtering");

					return true;

				}

			case TableModelPlugin.NODES_INSERTED :
				{

					return true;

				}
		}

		return false;
	}

}