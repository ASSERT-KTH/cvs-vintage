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

package org.columba.addressbook.folder;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.columba.addressbook.config.AdapterNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class GroupListCard extends DefaultCard
{
	public GroupListCard()
	{
		super();
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		try
		{
			DocumentBuilder builder = factory.newDocumentBuilder();
			document = builder.newDocument();

			Element root = (Element) document.createElement("grouplist");
			document.appendChild(root);

		}
		catch (ParserConfigurationException pce)
		{
			// Parser with specified options can't be built
			pce.printStackTrace();
		}

		AdapterNode node = new AdapterNode(getDocument());
		this.rootNode = node.getChild(0);
	}
	
	public GroupListCard( Document doc, AdapterNode root )
	{
		super( doc, root );
		
		if (doc != null)
		{
			if (getRootNode() == null)
			{
				AdapterNode node = new AdapterNode(getDocument());

				this.rootNode = node.getChild(0);

			}
		}
	}
	
	public int members()
	{
		AdapterNode membersRoot = getRootNode().getChild("list");
		if (membersRoot != null )
		{
			return membersRoot.getChildCount();
		}
		else
		{
			return 0;	
		}
		
	}
	
	public String getMember( int index )
	{
		AdapterNode membersRoot = getRootNode().getChild("list");
		if ( membersRoot != null )
		{
			AdapterNode child = membersRoot.getChildAt(index);
			if ( child != null ) return getTextValue(child);
		}
		return "";
	}
	
	public void addMember( String uid )
	{
		AdapterNode membersRoot = getRootNode().getChild("list");
		if ( membersRoot != null )
		{
			addKey( membersRoot, "uid", uid );
		}
		else
		{
			Element e = createElementNode( "list" );
			AdapterNode newNode = new AdapterNode( e );
        		getRootNode().add( newNode );
        		addKey( newNode, "uid", uid);
		}
	}
	
	public void removeMembers()
	{
		AdapterNode membersRoot = getRootNode().getChild("list");
		if ( membersRoot != null )
		{
			membersRoot.removeChildren();
		}
	}
	
	public Object[] getUids()
	{
		Object[] uids = new Object[ members() ];
		
		for ( int i=0; i<members(); i++ )
		{
			Object uid = getMember(i);
			uids[i] = uid;
			System.out.println("uid["+i+"]="+uid);
		}
		
		return uids;
	}

}
