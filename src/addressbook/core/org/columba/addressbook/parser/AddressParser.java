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

import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * @version 1.0
 * @author
 */
public class AddressParser {
	public static boolean isValid(String str) {
		if (str == null) {
			return false;
		}

		if (str.length() == 0) {
			return false;
		}

		if (str.indexOf("@") != -1) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * to normalize Mail-addresses given in an Vector in
	 * 
	 * for example there ar mails as strings in thelist with following formats:
	 * Frederik Dietz <fdietz@gmx.de>fdietz@gmx.de <fdietz@gmx.de>this formats
	 * must be normalized to <fdietz@gmx.de>. Formats in the form "name
	 * <name@de>" never exists, while the " character alrady removed
	 * 
	 * @param in
	 *            List of Strings with mailaddresses in any format
	 * @return List of Strings with mailaddress in format <fdietz@gmx.de>
	 */
	public static List normalizeRCPTVector(List in) {
		int v_size = in.size();
		String mailaddress = "";
		String new_address = "";
		List out = new Vector();

		for (Iterator it = in.iterator(); it.hasNext();) {
			mailaddress = (String) it.next();

			//		for (int i = 0; i < v_size; i++) {
			//			// get the String from the Vector
			//			mailaddress = (String) in.elementAt(i);
			if (mailaddress == null) {
				continue;
			}

			if (mailaddress.length() == 0) {
				continue;
			}

			// System.out.println("[DEBUG!!!!] mailaddress: "+mailaddress);
			StringTokenizer strToken = new StringTokenizer(mailaddress, "<");

			if (strToken.countTokens() == 2) {
				// the first token is irrelevant
				strToken.nextToken();

				// the next token is an token with the whole Mailaddress
				new_address = "<" + strToken.nextToken();

				// System.out.println("[DEBUG1] new_address: "+new_address);
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