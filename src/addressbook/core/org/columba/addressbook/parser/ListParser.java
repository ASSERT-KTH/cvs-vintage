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

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.columba.addressbook.folder.ContactStorage;
import org.columba.addressbook.folder.GroupFolder;
import org.columba.addressbook.main.AddressbookInterface;
import org.columba.addressbook.model.Contact;
import org.columba.addressbook.model.ContactItem;
import org.columba.addressbook.model.ContactItemMap;
import org.columba.addressbook.model.HeaderItem;
import org.columba.addressbook.model.HeaderItemList;
import org.columba.addressbook.model.VCARD;
import org.columba.core.main.MainInterface;

/**
 * @version 1.0
 * @author
 */
public class ListParser {

	public ListParser() {
	}

	/**
	 * Create list from String containing comma-separated email addresses.
	 * 
	 * @param str
	 *            string
	 * @return list
	 */
	public static List createListFromString(String str) {
		List result = new Vector();

		int pos = 0;
		boolean bracket = false;
		StringBuffer buf = new StringBuffer();
		int listLength = str.length();

		while (pos < listLength) {
			char ch = str.charAt(pos);

			if ((ch == ',') && (bracket == false)) {
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
		result.add(address);

		return result;
	}

	/**
	 * Flatten mixed list containing contacts and groups to a new list
	 * containing only contacts.
	 * 
	 * @param list
	 *            mixed list
	 * @return list containing only contacts
	 */
	public static List flattenList(List list) {
		List result = new Vector();

		for (Iterator it = list.iterator(); it.hasNext();) {
			String s = (String) it.next();
			GroupFolder groupFolder = AddressbookInterface.addressbookTreeModel
					.getGroupFolder(s);
			// if its a group item
			if (groupFolder != null) {
				ContactItemMap map = null;
				try {
					map = groupFolder.getContactItemMap();
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (map == null)
					continue;

				Iterator it2 = map.iterator();
				while (it2.hasNext()) {
					ContactItem i = (ContactItem) it2.next();
					String address = i.getAddress();

					if (address == null) {
						continue;
					}

					result.add(address);
				}
			} else {
				// contact item

				// check if valid email address
				if ( AddressParser.isValid(s)) {
					// add address to list
					result.add(s);
					continue;
				}
				
				// this is not a valid email address
				// -> check if its a contact displayname
				// -> if so, retrieve email address from contact folder
				
				// look into both folders
				ContactStorage personal = (ContactStorage) AddressbookInterface.addressbookTreeModel
						.getFolder(101);
				ContactStorage collected = (ContactStorage) AddressbookInterface.addressbookTreeModel
						.getFolder(102);

				// try to find a matching contact item
				Contact item = null;
				try {

					Object uid = personal.exists(s);
					if (uid != null) {
						item = personal.get(uid);
					}

					uid = collected.exists(s);
					if (uid != null)
						item = collected.get(uid);

				} catch (Exception e) {
					if (MainInterface.DEBUG)
						e.printStackTrace();
				}

				// if match found
				if (item != null)
					result
							.add(item.get(VCARD.EMAIL,
									VCARD.EMAIL_TYPE_INTERNET));

			}

		}

		return result;
	}

	/**
	 * Create list containing only strings from a HeaderItemList containing
	 * HeaderItem objects.
	 * 
	 * @param list
	 *            HeaderItemList containing HeaderItem objects
	 * @return list containing only strings
	 */
	public static List createStringListFromItemList(HeaderItemList list) {
		List result = new Vector();

		for (Iterator it = list.iterator(); it.hasNext();) {
			HeaderItem item = (HeaderItem) it.next();

			if (item == null) {
				continue;
			}

			result.add(item.getDisplayName());
		}

		return result;
	}

	/**
	 * Create comma-separated String representation of a list of String objects.
	 * 
	 * @param list
	 *            list containing String objects
	 * @return String representation
	 */
	public static String createStringFromList(List list) {
		StringBuffer output = new StringBuffer();

		for (Iterator it = list.iterator(); it.hasNext();) {
			String address = (String) it.next();
			if (address == null) {
				continue;
			}
			output.append(address);
			output.append(",");
		}

		if (output.length() > 0) {
			output.deleteCharAt(output.length() - 1);
		}

		return output.toString();
	}

	protected static String isValid(ContactItem item) {

		String address = address = item.getDisplayName();

		if (AddressParser.isValid(address)) {
			return address.trim();
		}

		if (item.getAddress() != null) {
			address = item.getAddress();

			if (AddressParser.isValid(address)) {
				return address.trim();
			}
		}

		return null;

	}
}