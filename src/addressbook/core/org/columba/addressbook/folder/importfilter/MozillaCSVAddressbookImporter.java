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
public class MozillaCSVAddressbookImporter extends DefaultAddressbookImporter
{

	public void importAddressbook(File file) throws Exception
	{
		System.out.println("importing addressbook::::");

		BufferedReader in = new BufferedReader(new FileReader(file));
		String str;

		while ((str = in.readLine()) != null)
		{
			// start parsing line
			int counter = -1;
			ContactCard card = new ContactCard();

			StringBuffer token = new StringBuffer();
			int pos = 0;
			while (pos < str.length())
			{
				char ch = str.charAt(pos);
				

				if (ch == ',')
				{
					// found new token
					counter++;

					if (counter == 0)
					{

						card.set("n", "given", token.toString());
					}
					else if (counter == 1)
					{

						card.set("n", "family", token.toString());
					}
					else if (counter == 2)
					{

						card.set("displayname", token.toString());
					}
					else if (counter == 3)
					{
						card.set("nickname", token.toString());
					}
					else if (counter == 4)
					{

						card.set("email", "internet", token.toString());
					}
					else if (counter == 5)
					{

						card.set("email", "x-email2", token.toString());
					}
					else if (counter == 8)
					{

						card.set("tel", "work", token.toString());
					}
					else if (counter == 9)
					{

						card.set("tel", "home", token.toString());
					}
					
					token = new StringBuffer();

				}
				else
				{
					token.append(ch);
				}

				pos++;

			}

			saveContact(card);

		}

		in.close();
	}
}