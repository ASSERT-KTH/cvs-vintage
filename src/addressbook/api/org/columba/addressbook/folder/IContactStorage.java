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

import org.columba.addressbook.model.IContactModel;
import org.columba.api.exception.StoreException;

/**
 * Contact storage facility.
 * 
 * @author fdietz
 * 
 */
public interface IContactStorage extends IFolder {

	int count() throws StoreException;

	/**
	 * Find contact by email address. Search ignores case.
	 * 
	 * @param emailAddress
	 *            email address
	 * @return contact id, if match found. Otherwise, <code>null</code>
	 * @throws StoreException
	 */
	String findByEmailAddress(String emailAddress) throws StoreException;

	/**
	 * Find contact by name. Search ignores case.
	 * <p>
	 * First tries to find a vCard "SORT_AS", then vCard "LASTNAME" and last
	 * vCard "FIRSTNAME", until a match is found. If several contacts match the
	 * first one is used and all other results are ignored.
	 * 
	 * @param name
	 *            email address
	 * @return contact id, if match found. Otherwise, <code>null</code>
	 * @throws StoreException
	 */
	String findByName(String name) throws StoreException;

	boolean exists(String id) throws StoreException;

	IContactModel get(String id) throws StoreException;

	void remove(String id) throws StoreException;

	void modify(String id, IContactModel contact) throws StoreException;

	Object add(IContactModel contact) throws StoreException;

}