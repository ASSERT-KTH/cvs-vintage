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
package org.columba.addressbook.folder.importfilter;

import java.io.File;

/**
 * @version 	1.0
 * @author
 */
public class OldColumbaAddressbookImporter extends DefaultAddressbookImporter
{
	public void importAddressbook(File file) throws Exception
	{
		System.out.println("importing addressbook::::");
		
		// FIXME
		
		/*
		AddressbookXmlConfig c = new AddressbookXmlConfig(file);
		c.load();
		
		AdapterNode root = c.getRootNode();
		System.out.println("name:"+root.getName());
		
		AdapterNode addressbook = root.getChild("addressbook");
		AdapterNode list = addressbook.getChild("list");
		
		for ( int i=0; i<list.getChildCount(); i++ )
		{
			AdapterNode contact = list.getChildAt(i);
			System.out.println("contact:"+contact.getName() );
			
			AdapterNode displayname = contact.getChild("displayname");
			AdapterNode firstname = contact.getChild("firstname");
			AdapterNode lastname = contact.getChild("lastname");
			AdapterNode address = contact.getChild("address");
			
			ContactCard card = new ContactCard();
			
			card.set("displayname", displayname.getValue() );
			card.formatSet("fn", displayname.getValue() );
			card.set("n","given", firstname.getValue() );
			card.set("n","familiy", lastname.getValue() );
			card.set("email","internet", address.getValue() );
			
			saveContact( card );
			
		}
		*/
	}
}
