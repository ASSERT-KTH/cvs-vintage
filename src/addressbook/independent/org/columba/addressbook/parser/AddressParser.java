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

package org.columba.addressbook.parser;

import java.util.StringTokenizer;
import java.util.Vector;
/**
 * @version 	1.0
 * @author
 */
public class AddressParser {
	public static boolean isValid(String str) {
		if (str == null)
			return false;

		if (str.length() == 0)
			return false;

		if (str.indexOf("@") != -1)
			return true;
		else
			return false;

	}

	public static String getAddress(String str) {
		str = str.trim();

		StringBuffer buf = new StringBuffer();

		int index = str.indexOf("<");
		if (index != -1) {
			int index2 = str.indexOf(">");

			return str.substring(index + 1, index2);
		} else
			return str;
	}

	public static String getDisplayname(String str) {
		str = str.trim();

		StringBuffer buf = new StringBuffer();

		int index = str.indexOf("\"");
		if (index != -1) {
			int index2 = str.indexOf("\"", index + 1);

			return str.substring(index + 1, index2);
		} else if (str.indexOf("<") != -1) {
			int index2 = str.indexOf("<");

			if (index2 != 0)
				return str.substring(0, index2);
			else
				return str;
		} else
			return str;
	}

	public static String quoteAddress(String str) {
		str = str.trim();

		//System.out.println("address ["+str+"]");

		StringBuffer buf = new StringBuffer();

		int index = str.indexOf("<");
		// example: fdietz@gmx.de
		if (index == -1)
			return str;
		// example: <fdietz@gmx.de
		if (index == 0)
			return str;

		// whats left :
		//  - Frederik Dietz <fdietz@gmx.de>
		//  - Frederik Dietz<fdietz@gmx.de>
		//  - "Frederik Dietz"<fdietz@gmx.de>
		//  - "Frederik Dietz" <fdietz@gmx.de>
		//  - "FrederikDietz"<fdietz@gmx.de>

		int index2 = str.indexOf(" ");
		// example: Frederik<fdietz@gmx.de>
		if (index2 == -1)
			return str;

		// examples: 
		//  - FrederikDietz <fdietz@gmx.de>
		//  - "FrederikDietz" <fdietz@gmx.de>
		if (index2 == index - 1) {
			// remove space
			buf.append(str.substring(0, index2));
			buf.append(str.substring(index, str.length()));

			//System.out.println("address:"+buf);

			return buf.toString();
		}

		// examples:
		//  - Frederik Dietz<fdietz@gmx.de>
		//  - "Frederik Dietz"<fdietz@gmx.de>
		int index3 = str.indexOf("\"");
		if (index3 == -1) {
			// no " character

			buf.append("\"");
			buf.append(str.substring(0, index - 1));
			buf.append("\"");
			buf.append(str.substring(index, str.length()));

			return buf.toString();
		} else {
			int index4 = str.indexOf("\"", index3 + 1);
			if ((index2 > index3) && (index2 < index4)) {
				int index5 = str.indexOf(" ", index2 + 1);

				if (index5 != -1) {
					buf.append(str.substring(0, index5));
					buf.append(str.substring(index, str.length()));
					return buf.toString();
				} else {
					return str;
				}
			}
		}

		return buf.toString();
	}

	/**
	 * to normalize Mail-addresses given in an Vector in
	 *
	 * for example there ar mails as strings in the vector with following formats:
	 * Frederik Dietz <fdietz@gmx.de>
	 * fdietz@gmx.de
	 * <fdietz@gmx.de>
	 * this formats must be normalized to <fdietz@gmx.de>. Formats in the form "name <name@de>"
	 * never exists, while the " character alrady removed
	 * @param in Vector of Strings with mailaddresses in any format
	 * @return Vector of Strings with mailaddress in format <fdietz@gmx.de>
	*/

	public static Vector normalizeRCPTVector(Vector in) {
		int v_size = in.size();
		String mailaddress = "";
		String new_address = "";
		Vector out = new Vector();
		for (int i = 0; i < v_size; i++) {
			// get the String from the Vector
			mailaddress = (String) in.elementAt(i);
			if (mailaddress == null)
				continue;
			if (mailaddress.length() == 0)
				continue;

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
			System.out.println("[DEBUG2!!!!] newaddress: "+new_address);
			out.addElement(new_address);
			new_address = "";
		}
		return out;
	}

	public static String normalizeAddress(String mailaddress) {

		String new_address = "";
		Vector out = new Vector();

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

		

		return new_address;
	}

}
