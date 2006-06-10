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

import java.util.Map;

import org.columba.addressbook.model.IContactModel;
import org.columba.addressbook.model.IContactModelPartial;
import org.columba.addressbook.model.IGroupModel;
import org.columba.api.exception.StoreException;

/**
 * @author fdietz
 *
 */
public interface IGroupFolder {
	/**
	 * @see org.columba.addressbook.folder.IContactStorage#add(IContactModel)
	 */
	String add(IContactModel contact) throws StoreException;

	/**
	 * @see org.columba.addressbook.folder.IContactStorage#count()
	 */
	int count() throws StoreException;

	/**
	 * @see org.columba.addressbook.folder.IContactStorage#exists(java.lang.Object)
	 */
	boolean exists(String uid) throws StoreException;

	/**
	 * @see org.columba.addressbook.folder.IContactStorage#get(java.lang.Object)
	 */
	IContactModel get(String uid) throws StoreException;

	/**
	 * @see org.columba.addressbook.folder.IContactStorage#modify(java.lang.Object,
	 *      IContactModel)
	 */
	void modify(String uid, IContactModel contact) throws StoreException;

	/**
	 * @see org.columba.addressbook.folder.IContactStorage#remove(java.lang.Object)
	 */
	void remove(String uid) throws StoreException;

	/**
	 * @see org.columba.addressbook.folder.IContactStorage#getHeaderItemList()
	 */
	Map<String, IContactModelPartial> getContactItemMap() throws StoreException;

	/**
	 * @return Returns the group.
	 */
	IGroupModel getGroup() throws StoreException;
}