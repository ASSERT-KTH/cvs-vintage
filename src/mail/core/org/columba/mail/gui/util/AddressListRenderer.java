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

package org.columba.mail.gui.util;

import java.util.Vector;

import org.columba.ristretto.message.Address;

/**
 * An HTML link renderer for the Address class The addresses are rendered as
 * HTML links: <code>
 * &lt;A HREF="mailto:[email-address]"&gt;[display-name]&lt;/A&lt;
 * </code>
 * 
 * @author Timo Stich <tstich@users.sourceforge.net>
 */
public class AddressListRenderer {

	/**
	 * Makes sure that noone creates instances of this class. This is a helper
	 * class, the static methods should be used instead.
	 */
	private AddressListRenderer() {
	}

	/**
	 * Returns a string buffer with the addresses in HTML links. The addresses
	 * will be put into HTML link: <code>
	 * &lt;A HREF="mailto:[email-address]"&gt;[display-name]&lt;/A&lt;
	 * </code>
	 * 
	 * @param addresses
	 *            addresses to render as HTML links.
	 * @return a String buffer.
	 */
	public static String renderToHTMLWithLinks(Address[] addresses) {
		StringBuffer result = new StringBuffer();

		if ((addresses != null) && (addresses.length > 0)) {
			result.append(createAddressHTMLString(addresses[0]));

			for (int i = 1; i < addresses.length; i++) {
				// result.append(", ");
				result.append(createAddressHTMLString(addresses[i]));
			}
		}

		return result.toString();
	}

	public static String[] renderToHTMLWithLinksStringArray(Address[] addresses) {
		Vector vector = new Vector();

		if ((addresses != null) && (addresses.length > 0)) {
			String str = createAddressString(addresses[0]);
			vector.add(str);

			for (int i = 1; i < addresses.length; i++) {
				// result.append(", ");
				str = createAddressString(addresses[i]);
				vector.add(str);
			}
		}

		return (String[]) vector.toArray(new String[0]);
	}

	/**
	 * Adds the address to the string buffer as a HTML link.
	 * 
	 * @param address
	 *            the address to render as a HTML link.
	 * @param result
	 *            the string buffer to add the HTML link to.
	 */
	private static String createAddressHTMLString(Address address) {
		StringBuffer result = new StringBuffer();
		result.append("<html><A HREF=\"mailto:");
		if (address.getDisplayName().length() != 0) {
			result.append(address.getDisplayName());
			result.append(" ");
			result.append("<" + address.getMailAddress() + ">");

		} else
			result.append(address.getMailAddress());

		result.append("\">");

		if (address.getDisplayName().length() != 0) {

			result.append(address.getDisplayName());
			result.append(" ");
			result.append("&lt;" + address.getMailAddress() + "&gt;");

		} else
			result.append(address.getShortAddress());

		result.append("</A></html>");

		return result.toString();
	}

	private static String createAddressString(Address address) {
		StringBuffer result = new StringBuffer();

		if (address.getDisplayName().length() != 0) {
			result.append(address.getDisplayName());
			result.append(" ");
			result.append("<" + address.getMailAddress() + ">");

		} else
			result.append(address.getMailAddress());

		return result.toString();
	}

}