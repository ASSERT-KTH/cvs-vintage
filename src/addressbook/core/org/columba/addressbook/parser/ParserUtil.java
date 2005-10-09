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
package org.columba.addressbook.parser;

import java.awt.Image;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class ParserUtil {

	/**
	 * this method tries to break the display name into something meaningful to
	 * put under Given Name, Family Name and Middle Name. It'll miss prefix and
	 * suffix!
	 */
	public static String[] tryBreakName(String displayName) {
		String[] names = new String[] { "", "", "" };
		int firstName = -1;
		if ((firstName = displayName.indexOf(' ')) > 0)
			names[0] = displayName.substring(0, firstName);
		else
			return names; // exit immediately, nothing more to do

		int lastName = -1;
		if ((lastName = displayName.lastIndexOf(' ')) >= firstName)
			names[2] = displayName.substring(lastName + 1);
		else
			return names; // exit immediately, nothing more to do

		if (lastName > firstName)
			names[1] = displayName.substring(firstName, lastName).trim();

		return names;
	}

	/**
	 * Create array from comma-separated string.
	 * 
	 * @param s
	 *            comma-separated string
	 * @param separator
	 * 
	 * @return string array
	 */
	public static String[] getArrayOfString(String s, String separator) {
		ArrayList list = new ArrayList();

		StringTokenizer tok = new StringTokenizer(s, separator);
		while (tok.hasMoreTokens()) {
			String t = tok.nextToken();
			list.add(t);
		}

		return (String[]) list.toArray(new String[] { "" });

	}

	/**
	 * Create comma-separated string from string array.
	 * 
	 * @param s
	 *            string array
	 * @param separator
	 * 
	 * @return comma separated string
	 */
	public static String getStringOfArray(String[] s, String separator) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < s.length; i++) {
			if (s[i] != null && s[i].length() > 0) {
				buf.append(s[i]);
				if (i < s.length - 1)
					buf.append(separator);
			}
		}

		return buf.toString();
	}

	public static Image createImageFromBase64String(String s) {

		// TODO implement me!

		return null;
	}

	public static String createBase64StringFromImage(Image image) {

		// TODO implement me!

		return null;
	}

}
