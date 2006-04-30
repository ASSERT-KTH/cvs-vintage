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
package org.columba.addressbook.facade;

import java.util.List;

import org.columba.addressbook.folder.StoreException;
import org.columba.addressbook.model.IHeaderItem;

/**
 * Provides high-level contact management methods.
 * 
 * @author fdietz
 */
public interface IContactFacade {
	
	/**
	 * Add new contact to addressbook.
	 * 
	 * @param uid		addressbook unique id
	 * @param address	new email address
	 */
	void addContact(String uid, String address) throws StoreException;

	
	/**
	 * Add new contact to addressbook. Implementation should prompt
	 * user for a destination addressbook.
	 * 
	 * @param address	new email address
	 */
	void addContact(String address) throws StoreException;
	
	/**
	 * Add new contacts to addressbook.
	 * 
	 * @param uid		addressbook unique id
	 * @param address	array containing new email addresses
	 */
	void addContact(String uid, String[] address) throws StoreException;
	
	/**
	 * Add new contacts to addressbook. Implementation should prompt user
	 * for a destination addressbook.
	 * 
	 * @param address	array containing new email addresses
	 */
	void addContact(String[] address) throws StoreException;
	
	/**
	 * Add new contact to "Collected Addresses".
	 * 
	 * @param address	new email address
	 */
	void addContactToCollectedAddresses(String address) throws StoreException;

	/**
	 * Add new contact to "Personal Addressbook".
	 * 
	 * @param address	new email address
	 */
	void addContactToPersonalAddressbook(String address) throws StoreException;

	/**
	 * Retrieve all <code>IHeaderItem</code> from contact folder
	 * with selected unique id.
	 * 
	 * @param uid	contact folder unique id
	 * @return		list of <code>IHeaderItem</code>
	 */
	public List<IHeaderItem> getAllHeaderItems(String uid) throws StoreException;
}