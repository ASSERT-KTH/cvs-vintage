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

/**
 * @version 	1.0
 * @author
 */
public class VCardParser
{
	public static ContactCard parse(String str)
	{

		//char[] chars = new char[ str.length() ];
		ContactCard card = new ContactCard();

		Vector keys = new Vector();
		Vector values = new Vector();
		int pos = 0;
		char ch;
		StringBuffer line = new StringBuffer();
		StringBuffer keybuf = new StringBuffer();
		StringBuffer valuebuf = new StringBuffer();
		boolean mode = false;
		while (pos < str.length())
		{
			ch = str.charAt(pos);

			if (ch == '\n')
			{
				values.add(valuebuf.toString());
				//System.out.println("value:" + word);

				//System.out.println("finished parsing line:" + line);
				if (keys.size() > 1)
				{
					String key0 = (String) keys.get(0);

					
					if (key0.toLowerCase().equalsIgnoreCase("label"))
					{
						for (int i = 1; i < keys.size(); i++)
						{

							String keyi = (String) keys.get(i);
							System.out.println("label-key:"+keyi);
							
							card.formatSet(key0.toLowerCase(), keyi.toLowerCase(), valuebuf.toString());
							System.out.println("card:" + key0 + " - " + keyi + " :" + valuebuf.toString());

						}
					}
					else
					{
						for (int i = 1; i < keys.size(); i++)
						{

							String keyi = (String) keys.get(i);
							card.set(key0.toLowerCase(), keyi.toLowerCase(), valuebuf.toString());
							System.out.println("card:" + key0 + " - " + keyi + " :" + valuebuf.toString());

						}
					}

				}
				else if (keys.size() == 1)
				{
					String key0 = (String) keys.get(0);

					if (key0.toLowerCase().equalsIgnoreCase("BEGIN"))
					{
						String s = (String) keys.get(0);
						if (s.toLowerCase().equalsIgnoreCase("BEGIN"))
						{
							System.out.println("vcard section begin");
						}
						else if (s.toLowerCase().equalsIgnoreCase("END"))
						{
							System.out.println("vcard section end");
						}

					}
					else if (key0.toLowerCase().equalsIgnoreCase("n"))
					{
						String s = null;
						if ( values.size()>0 )
						{
							s = (String) values.get(0);
							card.set("n", "family", s);
						}
							
						
						
						if ( values.size()>1 )
						{
							card.set("n", "given", s);
							s = (String) values.get(1);
						}
						
						if ( values.size()>2 )
						{
							card.set("n", "middle", s);
							s = (String) values.get(2);
						}
						
						if ( values.size()>3 )
						{
							card.set("n", "prefix", s);
							s = (String) values.get(3);
						}
						
						if ( values.size()>4 )
						{
							s = (String) values.get(4);
							card.set("n", "suffix", s);
						}

					}
					else if (key0.toLowerCase().equalsIgnoreCase("fn"))
					{
						card.formatSet(key0.toLowerCase(), valuebuf.toString());	
					}
					/*
					else if ( key0.toLowerCase().equalsIgnoreCase("adr") )
					{
						
						String s = (String) values.get(0);
						card.set( "adr","street", s );
						s = (String) values.get(1);
						card.set( "adr","locality", s );
						s = (String) values.get(2);
						card.set( "adr","region", s );
						s = (String) values.get(3);
						card.set( "adr","pcode", s );
						s = (String) values.get(4);
						card.set( "adr","country", s );
						
						
					
					}
					*/
					else
					{

						card.set(key0.toLowerCase(), valuebuf.toString());
						//System.out.println("card:"+keys.get(0)+ " - " + word.toString() );

					}
				}
				else
				{
					System.out.println("unable to parse clueful information");
				}

				line = new StringBuffer();
				keybuf = new StringBuffer();
				valuebuf = new StringBuffer();
				keys = new Vector();
				values = new Vector();
				mode = false;
			}
			else if (ch == ';')
			{
				//System.out.println("key:" + word);
				if (mode == false)
				{
					//key
					keys.add(keybuf.toString());
					System.out.println("key:" + keybuf);

					keybuf = new StringBuffer();
				}
				else
				{
					//value
					values.add(valuebuf.toString());
					System.out.println("value:" + valuebuf);

					valuebuf = new StringBuffer();
				}
			}
			else if (ch == ':')
			{
				//System.out.println("key:" + word);
				keys.add(keybuf.toString());
				System.out.println("key:" + keybuf);

				keybuf = new StringBuffer();
				mode = true;
				//System.out.println("value starts here");
			}
			else
			{
				// just collect characters
				//word.append(ch);
				//System.out.print(ch);
				if (mode == false)
				{
					//key
					keybuf.append(ch);
				}
				else
				{
					//value
					valuebuf.append(ch);
				}

			}

			pos++;
			line.append(ch);

		}
		
		String email = card.get("email","internet");
		String fn = card.formatGet("fn");
		
		if ( fn.length() != 0 ) card.set("displayname", fn );
		else if  (email.length() != 0 ) card.set("displayname", email);
		

		return card;

	}

}