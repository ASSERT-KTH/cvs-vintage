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

import org.columba.addressbook.model.IContactItemMap;
import org.columba.addressbook.model.IContactModel;
import org.columba.addressbook.model.IGroup;

/**
 * @author fdietz
 *
 */
public interface IGroupFolder {
	/**
	 * @see org.columba.addressbook.folder.IContactStorage#add(IContactModel)
	 */
	Object add(IContactModel contact) throws Exception;

	/**
	 * @see org.columba.addressbook.folder.IContactStorage#count()
	 */
	int count();

	/**
	 * @see org.columba.addressbook.folder.IContactStorage#exists(java.lang.Object)
	 */
	boolean exists(Object uid);

	/**
	 * @see org.columba.addressbook.folder.IContactStorage#get(java.lang.Object)
	 */
	IContactModel get(Object uid) throws Exception;

	/**
	 * @see org.columba.addressbook.folder.IContactStorage#modify(java.lang.Object,
	 *      IContactModel)
	 */
	void modify(Object uid, IContactModel contact) throws Exception;

	/**
	 * @see org.columba.addressbook.folder.IContactStorage#remove(java.lang.Object)
	 */
	void remove(Object uid) throws Exception;

	/**
	 * @see org.columba.addressbook.folder.IContactStorage#getHeaderItemList()
	 */
	IContactItemMap getContactItemMap() throws Exception;

	/**
	 * @return Returns the group.
	 */
	IGroup getGroup();
}