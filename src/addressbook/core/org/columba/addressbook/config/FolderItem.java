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

import org.columba.core.config.DefaultItem;
import org.columba.core.xml.XmlElement;

/**
 * @version 	1.0
 * @author
 */
public class FolderItem extends DefaultItem
{
	/*
	AdapterNode name;
	AdapterNode uid;
	AdapterNode type;
	AdapterNode rootNode;
	*/
	
	public FolderItem(XmlElement root)
	{
		super(root);

		/*
		this.rootNode = root;

		parse();

		createMissingElements();
		*/
		//filterList = new Vector();
	}

	/*
	protected void parse()
	{
		for (int i = 0; i < getRootNode().getChildCount(); i++)
		{
			AdapterNode child = getRootNode().getChildAt(i);

			if (child.getName().equals("name"))
			{
				name = child;
			}
			else if (child.getName().equals("uid"))
			{
				uid = child;
			}
			else if (child.getName().equals("type"))
			{
				type = child;
			}

		}
	}

	protected void createMissingElements()
	{

	}
	
	public AdapterNode getRootNode()
	{
		return rootNode;
	}

	public void setUid(int i)
	{
		Integer h = new Integer(i);

		setTextValue(uid, h.toString());
	}

	public void setName(String str)
	{
		setTextValue(name, str);
	}

	public int getUid()
	{
		if ( uid != null )
		{
		Integer i = new Integer(getTextValue(uid));

		return i.intValue();
		}
		else
		{
			return -1;
		}
	}

	public String getName()
	{
		if ( name != null ) 
			return getTextValue(name);
		else
			return "";
	}

	public String getType()
	{
		return getTextValue(type);
	}
	*/
}