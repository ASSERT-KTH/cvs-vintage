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
package org.columba.mail.imap.parser;

import java.util.LinkedList;
import java.util.StringTokenizer;

import org.columba.mail.imap.IMAPResponse;

/**
 * @author fdietz
 *
 * See RFC 2060 IMAP4 (http://rfc-editor.org)
 * 
 * Example:    
 * 
 * C: A282 SEARCH FLAGGED SINCE 1-Feb-1994 NOT FROM "Smith"
 * S: * SEARCH 2 84 882
 * S: A282 OK SEARCH completed
 * 
 * 
 */

//7.2.5.  SEARCH Response
//
//   Contents:   zero or more numbers
//
//	  The SEARCH response occurs as a result of a SEARCH or UID SEARCH
//	  command.  The number(s) refer to those messages that match the
//	  search criteria.  For SEARCH, these are message sequence numbers;
//	  for UID SEARCH, these are unique identifiers.  Each number is
//	  delimited by a space.
//
//   Example:    S: * SEARCH 2 3 6
			   
public class SearchResultParser {

	/**
	 * Constructor for SearchResultParser.
	 */
	public SearchResultParser() {
		super();
	}

	public static LinkedList parse(IMAPResponse[] responses) {
		LinkedList v = new LinkedList();

		for (int i = 0; i < responses.length; i++) {
			if (responses[i] == null)
				continue;

			String data = responses[i].getSource();
			
			if ( data.startsWith("OK") ) continue;
			
			// "* SEARCH 7090 8110"
			if ( data.startsWith("* SEARCH") == false ) continue;
			
			data = data.substring(8);
			
			StringTokenizer tok = new StringTokenizer(data, " ");
			while (tok.hasMoreTokens()) {
				String index = (String) tok.nextToken();
				System.out.println("token=<"+index+">");
				
				v.add(index.trim());
			}

		}

		return v;
	}

	protected static String parseLine(String data) {

		int index = data.indexOf(" ");

		return data.substring(index + 1, data.length());
	}

}
