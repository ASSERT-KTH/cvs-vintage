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
package org.columba.addressbook.gui.util;

import org.columba.addressbook.model.ContactItem;
import org.columba.addressbook.model.GroupItem;
import org.columba.addressbook.model.HeaderItem;

/**
 * @author fdietz
 *  
 */
public final class ToolTipFactory {

	public static String createToolTip(HeaderItem item) {
		StringBuffer buf = new StringBuffer();

		buf.append("<html><body>&nbsp;Name: " + item.getDisplayName());
		buf.append("</body></html>");

		return buf.toString();
	}

	public static String createToolTip(ContactItem item) {

		StringBuffer buf = new StringBuffer();

		buf.append("<html><body>&nbsp;Name: " + item.getDisplayName());
		if (item.getAddress() != null) {
			buf.append("<br>&nbsp;eMail: "
					+ convert((String) item.getAddress()));
		}
		if (item.getWebsite() != null) {
			buf.append("<br>&nbsp;Website: "
					+ convert((String) item.getWebsite()));
		}
		buf.append("</body></html>");

		return buf.toString();
	}

	public static String createToolTip(GroupItem item) {

		StringBuffer buf = new StringBuffer();

		buf.append("<html><body>&nbsp;Name: " + item.getDisplayName());
		if (item.getDescription() != null) {
			buf.append("<br>&nbsp;Description: " + item.getDescription());
		}
		buf.append("</body></html>");

		return buf.toString();
	}

	private static String convert(String str) {
		if (str == null) {
			return "";
		}

		StringBuffer result = new StringBuffer();
		int pos = 0;
		char ch;

		while (pos < str.length()) {
			ch = str.charAt(pos);

			if (ch == '<') {
				result.append("&lt;");
			} else if (ch == '>') {
				result.append("&gt;");
			} else {
				result.append(ch);
			}

			pos++;
		}

		return result.toString();
	}
}