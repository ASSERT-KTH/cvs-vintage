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
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class SearchResultParser {

	/**
	 * Constructor for SearchResultParser.
	 */
	public SearchResultParser() {
		super();
	}

	public static LinkedList parse(IMAPResponse[] responses) {
		LinkedList v = new LinkedList();

		for (int i = 0; i < responses.length - 1; i++) {
			if (responses[i] == null)
				continue;

			// skip "*"
			String data = parseLine(responses[i].getSource());

			if ( data.startsWith("OK") ) continue;
			
			// skip "SEARCH"
			data = parseLine(data);
			
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
