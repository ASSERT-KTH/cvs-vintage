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
	/**
	 * Quote each line of a body of text with the default greater than 
	 * character.
	 *  
	 * @param text The text that will be quoted.
	 * @return the text passed in with each line quoted with the default greater
	 * than sign.
	 */
	public static String quote(String text)
	{
		return quote("> ", text);
	}
	
	/**
	 * Quote each line of a body of text with a given string prefix.
	 * 
	 * @param prefix The string prefix to prepend to each line of text. This was
	 * made a String instead of a char so you could prefix a line with ANY 
	 * values, not just single chars.
	 * @param text The text that will have each of its lines prepended with the
	 * prefix value.
	 * @return the text passed in with each line quoted with the passed in 
	 * prefix string.
	 */
	public static String quote(String prefix, String text)
	{
		/* Lazily create the buffer, because we can better guess its initial
		 * size and avoid resizing of the internal array
		 */
		StringBuffer buffer = null;
		
		/* Check if the text is null or empty */
		if (text == null || text.length() == 0)
			return null;

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

//		int end = 0;
//		int start = 0;
//		while (end < text.length()) {
//			end = text.indexOf('\n', start);
//			if (end == -1) {
//				end = text.length();
//			}
//			buf.append("> ");
//			buf.append(text.substring(start, end));
//			buf.append("\n");
//			start = end + 1;
//		}

		/* RIYAD: Lets use regexp and simplify the code, they are superfast */
		String[] textLines = text.split("\n");
		/* Make the buffer the size of the original text + 2 new chars per each line */
		buffer = new StringBuffer(text.length() + 2 * (textLines.length+1));
		
		for(int i = 0; i < textLines.length; i++) {
			buffer.append(prefix).append(textLines[i]);

			// dont forget the linebreak!
			buffer.append("\n");
		}
		return buffer.toString();
	}
}