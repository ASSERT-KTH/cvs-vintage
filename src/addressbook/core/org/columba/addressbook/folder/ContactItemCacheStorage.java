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
package org.columba.addressbook.folder;

import org.columba.addressbook.model.ContactItem;
import org.columba.addressbook.model.ContactItemMap;

/**
 * Contact item cache storage.
 * <p>
 * All items needed by JTable to display a contact folder contents
 * are cached here to allow faster loading time.
 * <p>
 * These items currently include a displayname, the email address
 * and the website of a contact.
 * 
 * @author fdietz
 *
 */
public interface ContactItemCacheStorage {

	ContactItemMap getContactItemMap() throws Exception;
	
	void add(Object uid, ContactItem item) throws Exception;
	
	void remove(Object uid) throws Exception;
	
	void modify(Object uid, ContactItem item) throws Exception;
	
	int count();
	
	boolean exists(Object uid);
	
	void save() throws Exception;
	
	void load() throws Exception;
}
