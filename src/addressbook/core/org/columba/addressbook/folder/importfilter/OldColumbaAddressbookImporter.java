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

package org.columba.addressbook.folder.importfilter;

import org.columba.addressbook.config.*;
import org.columba.core.config.AdapterNode;
import org.columba.addressbook.folder.*;

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
	}
}
