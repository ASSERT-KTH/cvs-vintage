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
package org.columba.addressbook.parser;

import java.util.Vector;

import org.columba.addressbook.folder.ContactCard;
import org.columba.addressbook.folder.Folder;
import org.columba.addressbook.folder.GroupListCard;
import org.columba.addressbook.folder.HeaderItem;

/**
 * @version 	1.0
 * @author
 */
public class ListParser
{

	public ListParser()
	{
	}

	public static Vector parseString(String list)
	{
		Vector result = new Vector();

		int pos = 0;
		boolean bracket = false;
		StringBuffer buf = new StringBuffer();
		while (pos < list.length())
		{
			char ch = list.charAt(pos);
			//System.out.println("ch=" + ch);

			if ((ch == ',') && (bracket == false))
			{
				// found new message
				String address = buf.toString();
				result.add(address);
				//System.out.println("address:" + address);

				buf = new StringBuffer();
				pos++;
			}
			else if (ch == '"')
			{
				buf.append(ch);

				pos++;
				
				if (bracket == false)
					bracket = true;
				else
					bracket = false;
			}
			else
			{
				buf.append(ch);

				pos++;
			}
		}

		String address = buf.toString();
		result.add(address);
		//System.out.println("address:" + address);

		return result;
	}

	public static Vector parseVector(Vector list)
	{
		Vector result = new Vector();

		int size = list.size();

		for (int i = 0; i < size; i++)
		{
			HeaderItem item = (HeaderItem) list.get(i);
			if ( item == null ) continue;
			
			if (item.isContact())
			{
				String address = isValid(item);
				
				if ( address == null ) continue;
				
				result.add(address);
				System.out.println("parsed item:"+ address );
			}
			else
			{
				// group item

				Object uid = item.getUid();
				Folder folder = item.getFolder();

				GroupListCard card = (GroupListCard) folder.get(uid);
				for (int j = 0; j < card.members(); j++)
				{
					Object memberID = card.getMember(j);
					//System.out.println("memberID:" + memberID);

					ContactCard contactCard = (ContactCard) folder.get(memberID);
					String address = contactCard.get("email", "internet");
					//System.out.println("address:" + address);

					result.add(address.trim());
					
					System.out.println("parsed item:"+ address );
				}

			}

			

		}

		return result;
	}

	public static String parse(Vector list)
	{

		StringBuffer output = new StringBuffer();
		int size = list.size();

		for (int i = 0; i < size; i++)
		{
			HeaderItem item = (HeaderItem) list.get(i);
			if ( item == null ) continue;
			
			if (item.isContact())
			{
				String address = isValid(item);
				
				if ( address == null ) continue;
				
				output.append(address);
				System.out.println("parsed item:"+ address );
				output.append(",");
			}
			else
			{
				// group item
				

				Object uid = item.getUid();
				Folder folder = item.getFolder();

				GroupListCard card = (GroupListCard) folder.get(uid);
				for (int j = 0; j < card.members(); j++)
				{
					Object memberID = card.getMember(j);
					//System.out.println("memberID:" + memberID);

					ContactCard contactCard = (ContactCard) folder.get(memberID);
					String address = contactCard.get("email", "internet");
					//System.out.println("address:" + address);

					output.append(address);
					System.out.println("parsed item:"+ address );
					output.append(",");
				}
			}
		}

		if ( output.length() > 0 )
			output.deleteCharAt(output.length() - 1);
		
		return output.toString();
	}
	
	protected static String isValid( HeaderItem headerItem )
	{
		String address = (String) headerItem.get("email;internet");
		System.out.println("address to parse:"+address);
		if ( AddressParser.isValid(address) ) return address.trim();
		
		address = (String) headerItem.get("displayname");
		System.out.println("address to parse:"+address);
		if ( AddressParser.isValid(address) ) return address.trim();
		
		return null;
	}

}