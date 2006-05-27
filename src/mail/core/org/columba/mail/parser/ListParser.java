// The contents of this file are subject to the Mozilla Public License Version
// 1.1
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
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.mail.parser;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * Provides parsers for creating String representations from List objects and
 * vice versa.
 * 
 * @author fdietz
 */
public class ListParser {

	public final static char SEPARATOR_CHAR = ';';

	public final static String SEPARATOR_STRING = ";";

	public ListParser() {
	}

	/**
	 * Create list from String containing semicolon-separated email addresses.
	 * 
	 * @param str
	 *            semicolon separated email address list
	 * 
	 * @return list list of email addresses, never <code>null</code>
	 */
	public static List<String> createListFromString(String str) {
		if (str == null)
			throw new IllegalArgumentException("str == null");

		List<String> result = new Vector<String>();
		if (str.length() == 0)
			return result;

		int pos = 0;
		boolean bracket = false;
		StringBuffer buf = new StringBuffer();
		int listLength = str.length();

		while (pos < listLength) {
			char ch = str.charAt(pos);

			if ((ch == SEPARATOR_CHAR) && (bracket == false)) {
				// found new message
				String address = buf.toString();
				result.add(address);

				buf = new StringBuffer();
				pos++;
			} else if (ch == '"') {
				buf.append(ch);

				pos++;

				if (bracket == false) {
					bracket = true;
				} else {
					bracket = false;
				}
			} else {
				buf.append(ch);

				pos++;
			}
		}

		String address = buf.toString();
		// remove whitespaces
		address = address.trim();
		result.add(address);

		return result;
	}

	/**
	 * Create comma-separated String representation of a list of String objects.
	 * 
	 * @param list
	 *            list containing String objects
	 * @return String representation, never <code>null</code
	 */
	public static String createStringFromList(List<String> list,
			String separator) {
		if (list == null)
			throw new IllegalArgumentException("list == null");
		if (separator == null)
			throw new IllegalArgumentException("separator == null");

		StringBuffer output = new StringBuffer();

		for (Iterator it = list.iterator(); it.hasNext();) {
			String address = (String) it.next();
			if (address == null) {
				continue;
			}
			output.append(address);
			output.append(separator);
		}

		if (output.length() > 0) {
			output.deleteCharAt(output.length() - 1);
		}

		return output.toString();
	}
}