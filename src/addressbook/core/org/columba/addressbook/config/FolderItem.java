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