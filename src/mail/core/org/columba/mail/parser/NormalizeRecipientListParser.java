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
package org.columba.mail.parser;

import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Parsers for email address in RFC822 format.
 * 
 * @author fdietz
 */
public class NormalizeRecipientListParser {

	/**
	 * Normalize Mail-addresses given in an Vector in
	 * <p>
	 * For example there ar mails as strings in the list with following formats:
	 * <p>
	 * <ul>
	 * <li>Firstname Lastname <mail@mail.org></li>
	 * <li>"Lastname, Firstname" <mail@mail.org></li>
	 * <li>mail@mail.org</li>
	 * <li><mail@mail.org></li>
	 * </ul>
	 * <p>
	 * These formats must be normalized to <mail@mail.org>. 
	 * 
	 * @param list
	 *            List of Strings with mailaddresses in any format
	 * @return List of Strings with mailaddress in format <mail@mail.org>, never <code>null</code>
	 */
	public List<String> normalizeRCPTVector(List<String> list) {
		if (list == null)
			throw new IllegalArgumentException("list == null");

		String mailaddress = "";
		String new_address = "";
		List<String> out = new Vector<String>();

		for (Iterator it = list.iterator(); it.hasNext();) {
			mailaddress = (String) it.next();

			// skip
			if ((mailaddress == null) || (mailaddress.length() == 0)) {
				continue;
			}

			StringTokenizer strToken = new StringTokenizer(mailaddress, "<");

			if (strToken.countTokens() == 2) {
				// the first token is irrelevant
				strToken.nextToken();

				// the next token is an token with the whole Mailaddress
				new_address = "<" + strToken.nextToken();
			} else {
				// just look if the first character alrady an <
				// so can use this mailaddress as the correct address
				if (mailaddress.charAt(0) == '<') {
					new_address = mailaddress;
				} else {
					new_address = "<" + mailaddress + ">";
				}
			}

			out.add(new_address);
			new_address = "";
		}

		return out;
	}
}