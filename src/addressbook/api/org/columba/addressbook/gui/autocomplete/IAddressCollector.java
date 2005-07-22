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
package org.columba.addressbook.gui.autocomplete;

import org.columba.addressbook.model.IHeaderItem;

/**
 * @author fdietz
 *
 */
public interface IAddressCollector  {
	/**
	 * Add all contacts and group items to hashmap.
	 * 
	 * @param uid			selected folder uid
	 * @param includeGroup	add groups if true. No groups, otherwise.
	 */
	void addAllContacts(int uid, boolean includeGroup);

	void addAddress(String add, IHeaderItem item);

	Object[] getAddresses();

	IHeaderItem getHeaderItem(String add);

	void clear();

	/**
	 * @see org.frappucino.addresscombobox.ItemProvider#getMatchingItems(java.lang.String)
	 */
	Object[] getMatchingItems(String s);
}