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

package org.columba.mail.parser.text;


/**
 * @author frd
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class BodyTextParser {

	public static String quote( String text )
	{
		if (text == null)
			return null;


		StringBuffer buf = new StringBuffer();

		/*
		 * *20030621, karlpeder* The StringTokenizer treats
		 * \n\n as a single newline => empty lines
		 * are discharged. Therefore the StringTokenizer is
		 * no longer used.
		 */
/*
		StringTokenizer tok = new StringTokenizer(text, "\n");
		while (tok.hasMoreElements()) {
			buf.append("> ");
			buf.append((String) tok.nextElement());
			buf.append("\n");
		}
		return buf.toString();
*/
		int end = 0;
		int start = 0;
		while (end < text.length()) {
			end = text.indexOf('\n', start);
			if (end == -1) {
				end = text.length();
			}
			buf.append("> ");
			buf.append(text.substring(start, end));
			buf.append("\n");
			start = end + 1;
		}
		return buf.toString();
	}

}
