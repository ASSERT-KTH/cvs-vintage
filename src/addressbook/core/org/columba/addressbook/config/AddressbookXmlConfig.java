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

import java.io.File;
import java.util.Vector;

import org.columba.core.config.AdapterNode;
import org.columba.core.config.DefaultXmlConfig;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Element;

public class AddressbookXmlConfig extends DefaultXmlConfig
{
	private File file;

	public AddressbookXmlConfig(File file)
	{
		super(file);
	}

	public AdapterNode getRootNode()
	{
		AdapterNode node = new AdapterNode(getDocument());

		AdapterNode rootNode = node.getChild(0);

		return rootNode;
	}

	public int count()
	{
		AdapterNode parent = getRootNode();

		int count = parent.getChildCount();

		return count;
	}

	// create uid list from all accounts
	protected void getUids(Vector v, AdapterNode parent)
	{

		int childCount = parent.getChildCount();

		if (childCount > 0)
		{
			for (int i = 0; i < childCount; i++)
			{

				AdapterNode child = parent.getChild(i);

				getUids(v, child);

				//System.out.println("name: "+ child.getName() );

				if ((child.getName().equals("contact")) || (child.getName().equals("group")))
				{
					AdapterNode uidNode = child.getChild("uid");

					Integer j = new Integer(uidNode.getValue());

					v.add(j);
				}

			}
		}
	}

	// find a free uid for a new account
	protected String createUid()
	{
		Vector v = new Vector();

		AdapterNode rootNode = getRootNode();
		AdapterNode addressbookNode = rootNode.getChild(0);
		AdapterNode listNode = addressbookNode.getChild("list");

		getUids(v, listNode);

		int result = -1;
		boolean hit;
		boolean exit = false;

		while (exit == false)
		{
			hit = false;
			result++;
			for (int i = 0; i < v.size(); i++)
			{
				Integer j = (Integer) v.get(i);

				if (j.intValue() == result)
				{
					hit = true;
				}
			}
			if (hit == false)
				exit = true;
		}

		Integer newUid = new Integer(result);

		return newUid.toString();
	}

	protected AdapterNode contactExists(String str)
	{

		AdapterNode addressbook = getAddressbookNode(0);
		AdapterNode list = addressbook.getChild("list");

		AdapterNode child = null;

		for (int i = 0; i < list.getChildCount(); i++)
		{
			child = list.getChild(i);

			if (isContact(child))
			{

				AdapterNode address = child.getChild("address");
				String adr = address.getCDATAValue();

				if (adr.equals(str))
				{
					return child;
				}
			}

		}

		return null;
	}

	/*
	public AdapterNode createEmptyContact()
	{

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		Document doc=null;
		try
		{
			DocumentBuilder builder = factory.newDocumentBuilder();
			doc = builder.newDocument(); 

			Element root = (Element) doc.createElement("vcard");
			doc.appendChild(root);
			

		}
		catch (ParserConfigurationException pce)
		{
			// Parser with specified options can't be built
			pce.printStackTrace();
		}
		
		return new AdapterNode( doc );

	}
	*/

	public AdapterNode addContact(
		String displayname,
		String firstname,
		String lastname,
		String address)
	{
		AdapterNode exists = contactExists(address.trim());

		if (exists != null)
			return exists;

		Element parentElement = createElementNode("contact");

		Element childElement = createTextElementNode("displayname", displayname);
		addElement(parentElement, childElement);

		childElement = createTextElementNode("firstname", firstname);
		addElement(parentElement, childElement);

		childElement = createTextElementNode("lastname", lastname);
		addElement(parentElement, childElement);

		Element subChild = createElementNode("address");
		CDATASection cdataChild = createCDATAElementNode(address);
		addCDATASection(subChild, cdataChild);
		addElement(parentElement, subChild);

		childElement = createTextElementNode("uid", createUid());
		addElement(parentElement, childElement);

		AdapterNode node = getRootNode();
		AdapterNode addressbookNode = node.getChild("addressbook");
		AdapterNode list = addressbookNode.getChild("list");

		AdapterNode childNode = list.addElement(parentElement);

		return childNode;
	}

	public AdapterNode addGroup(String name)
	{
		Element parentElement = createElementNode("group");

		Element childElement = createTextElementNode("name", name);
		addElement(parentElement, childElement);

		childElement = createTextElementNode("uid", createUid());
		addElement(parentElement, childElement);

		childElement = createElementNode("grouplist");
		addElement(parentElement, childElement);

		AdapterNode node = getRootNode();
		AdapterNode addressbookNode = node.getChild("addressbook");
		AdapterNode list = addressbookNode.getChild("list");

		AdapterNode childNode = list.addElement(parentElement);

		return childNode;
	}

	public AdapterNode addContact(String s)
	{
		String name = new String("");

		/*
		    // if we have more than one address, take only the first one
		      // and skip the rest
		      if ( s.indexOf(",") != -1 )
		      {
		      s = s.substring( 0, s.indexOf(",") );
		      }
		*/

		// do we have a name before the email address
		// example: "Thomas Mustermann" <muster@muster.com>
		//
		// or do we have only a address
		// example: muster@muster.com
		if (s.indexOf("<") != -1)
		{
			if (s.startsWith("<"))
				name = "";
			else
				name = s.substring(0, s.indexOf("<") - 1);
		}

		// remove the leading and trailing " from the name
		if (name.startsWith("\""))
			name = name.substring(1, name.length() - 1);
		if (name.endsWith("\""))
			name = name.substring(0, name.length() - 2);

		// if a whitespace exists in name
		// divide the full name in first and last name
		String firstname = new String("");
		String lastname = name;
		name = name.trim();
		if (name.indexOf(" ") != -1)
		{
			firstname = name.substring(0, name.indexOf(" "));
			lastname = name.substring(name.indexOf(" ") + 1, name.length());

			/*
			  lastname = s.substring( s.indexOf(" "), s.indexOf("<") );
			  lastname = lastname.trim();
			  
			  if ( lastname.endsWith("\"") ) lastname = lastname.substring(0,lastname.length()-2);
			*/
		}

		String address = new String("");
		if (s.indexOf("<") != -1)
			address = s.substring(s.indexOf("<") + 1, s.indexOf(">"));
		else
		{
			address = s;
		}

		String displayname;

		if (name.length() != 0)
			displayname = firstname + " " + lastname;
		else
			displayname = address;

		return addContact(displayname, firstname, lastname, address);

	}

	/*
	public AdapterNode addContact( String address, String name, AdapterNode addressbook )
	{
	  Element parentElement = createElementNode("contact");
	
	  
	  Element subChild = createElementNode("address");
	CDATASection cdataChild = createCDATAElementNode(address);
	addCDATASection( subChild, cdataChild );
	  addElement( parentElement, subChild );
	  
	  Element childElement = createTextElementNode("uid",createUid() );
	  addElement( parentElement, childElement );
	  childElement = createTextElementNode("name",name );
	  addElement( parentElement, childElement );
	  
	
	  AdapterNode childNode =  addressbook.addElement( parentElement );
	  
	          
	  return childNode;
	}
	*/

	public ContactItem getContactItem(AdapterNode node)
	{
		if (node != null)
		{
			if (node.getName().equals("contact"))
			{
				ContactItem item = new ContactItem(getDocument());

				item.setAddressNode(node.getChild("address"));
				item.setUidNode(node.getChild("uid"));
				item.setFirstNameNode(node.getChild("firstname"));
				item.setLastNameNode(node.getChild("lastname"));
				item.setDisplayNameNode(node.getChild("displayname"));

				return item;
			}

		}

		return null;
	}

	public GroupItem getGroupItem(AdapterNode node)
	{
		if (node != null)
		{
			if (node.getName().equals("group"))
			{
				GroupItem item = new GroupItem(getDocument());

				item.setNameNode(node.getChild("name"));
				item.setUidNode(node.getChild("uid"));
				item.setListNode(node.getChild("grouplist"));

				return item;
			}

		}

		return null;
	}

	public AddressbookItem getAddressbookItem(AdapterNode node)
	{
		if (node != null)
		{
			if (node.getName().equals("addressbook"))
			{
				AddressbookItem item = new AddressbookItem(getDocument());

				item.setNameNode(node.getChild("name"));
				item.setUidNode(node.getChild("uid"));
				item.setListNode(node.getChild("list"));

				return item;
			}

		}

		return null;
	}

	public Vector getList()
	{
		Vector v = new Vector();

		AdapterNode node = getRootNode();
		AdapterNode addressbookNode = node.getChild("addressbook");
		AdapterNode list = addressbookNode.getChild("list");

		for (int i = 0; i < list.getChildCount(); i++)
		{
			v.add(list.getChild(i));
		}

		return v;
	}

	public boolean isContact(AdapterNode node)
	{
		if (node.getName().equals("contact"))
			return true;
		else
			return false;
	}

	public AdapterNode getNode(int number)
	{
		Vector v = getList();

		AdapterNode node;
		AdapterNode uidNode;
		int uid;

		for (int i = 0; i < v.size(); i++)
		{
			node = (AdapterNode) v.get(i);
			uidNode = node.getChild("uid");
			uid = (new Integer(uidNode.getValue())).intValue();

			if (uid == number)
				return node;
		}

		return null;
	}

	public AddressbookItem get(int i)
	{
		AdapterNode parent = getRootNode();

		AdapterNode addressbookNode = parent.getChild(i);

		AddressbookItem item = getAddressbookItem(addressbookNode);

		return item;
	}

	public AdapterNode getAddressbookNode(int i)
	{
		AdapterNode parent = getRootNode();

		AdapterNode addressbookNode = parent.getChild(i);

		return addressbookNode;
	}

}