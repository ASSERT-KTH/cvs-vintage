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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.columba.addressbook.folder.ContactCard;

/**
 * @version 	1.0
 * @author
 */
public class NetscapeLDIFAddressbookImporter extends DefaultAddressbookImporter
{
	public void importAddressbook(File file) throws Exception
	{
		System.out.println("importing addressbook::::");

		BufferedReader in = new BufferedReader(new FileReader(file));
		String str;
		ContactCard card = new ContactCard();
		while ((str = in.readLine()) != null)
		{
			// start parsing line by line

			if (str.length() == 0)
			{
				// empty line, means new contactcard

				saveContact(card);

				card = new ContactCard();
			}
			else
			{
				// parse key:value lines
				int index = str.indexOf(":");

				if (index != -1)
				{
					String key = str.substring(0, index);
					String value = str.substring(index + 1, str.length());
					value = value.trim();

					if (key.equalsIgnoreCase("cn"))
					{
						card.set("displayname", value);
					}
					else if (key.equalsIgnoreCase("givenname"))
					{
						card.set("n", "given", value);
					}
					else if (key.equalsIgnoreCase("sn"))
					{
						card.set("n", "family", value);
					}
					else if (key.equalsIgnoreCase("mail"))
					{
						card.set("email", "internet", value);
					}
					else if (key.equalsIgnoreCase("xmozillanickname"))
					{
						card.set("nickname", value);
					}
					else if (key.equalsIgnoreCase("o"))
					{
						card.set("organisation", value);
					}
					else if (key.equalsIgnoreCase("telephonenumber"))
					{
						card.set("tel", "work", value);
					}
					else if (key.equalsIgnoreCase("homephone"))
					{
						card.set("tel", "home", value);
					}
					else if (key.equalsIgnoreCase("facsimiletelephonenumber"))
					{
						card.set("tel", "fax", value);
					}
					else if (key.equalsIgnoreCase("pagerphone"))
					{
						card.set("tel", "pager", value);
					}
					else if (key.equalsIgnoreCase("cellphone"))
					{
						card.set("tel", "mobile", value);
					}
					else if (key.equalsIgnoreCase("homeurl"))
					{
						card.set("url", value);
					}

				}
			}

		}

		in.close();
	}
}