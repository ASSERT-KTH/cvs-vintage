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
