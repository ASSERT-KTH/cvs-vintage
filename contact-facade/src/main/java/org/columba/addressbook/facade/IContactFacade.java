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

import org.columba.api.exception.StoreException;

/**
 * Provides high-level contact management methods. This Facade API is the only
 * way to access functionality of this component.
 * <p>
 * In case the facade implementation doesn't support specific functionality, an
 * <code>java.lang.IllegalArgumentException</code> should be thrown.
 * Components making use of this facade, should try to handle missing method
 * implementations gracefully, if possible.
 * <p>
 * Unchecked exceptions based on <code>java.lang.RuntimeException</code> are
 * used only. A <code>StoreException</code> is thrown in case of an internal
 * store backend error or failure a client usually can't resolve gracefully. A
 * <code>IllegalArgumentException</code> is used in case the client didn't
 * make use of the method correctly.
 * <p>
 * Following an example on how to register a new facade implementation:
 * 
 * <pre>
 * ServiceRegistry.register(MyFacadeImpl.class, IContactFacade)
 * </pre>
 * 
 * @author fdietz
 */
public interface IContactFacade {

	/**
	 * Add new contact to contact folder with specified id.
	 * 
	 * @param id
	 *            contact folder unique id
	 * @param contactItem The IContactItem to add to the specified folder
	 * @throws StoreException
	 *             in case of an internal storage backend failure
	 * @throws IllegalArgumentException
	 *             in case of invalid arguments, in case this method is not
	 *             supported by a facade implementation
	 */
	void addContact(String id, IContactItem contactItem) throws StoreException,
			IllegalArgumentException;

	/**
	 * Add an array of contacts to the contact folder with specified id.
	 * 
	 * @param id
	 *            contact folder unique id
	 * @param contactItems Array of IContactItemS to add to the specified folder
	 * @throws StoreException
	 *             in case of an internal storage backend failure
	 * @throws IllegalArgumentException
	 *             in case of invalid arguments, in case this method is not
	 *             supported by a facade implementation
	 */
	void addContacts(String id, IContactItem[] contactItem) throws StoreException,
			IllegalArgumentException;

	/**
	 * Add new contact to a contact folder. Implementation
	 * should prompt user for a destination contact folder.
	 * @param contactItem The IContactItem to add to the specified folder
	 * 
	 * @throws StoreException
	 *             in case of an internal storage backend failure
	 * @throws IllegalArgumentException
	 *             in case of invalid arguments, in case this method is not
	 *             supported by a facade implementation
	 */
	void addContact(IContactItem contactItem) throws StoreException,
			IllegalArgumentException;

	/**
	 * Add an array of contacts to a contact folder. Implementation
	 * should prompt user for a destination contact folder.
	 * @param contactItems Array of IContactItemS to add to the specified folder
	 * 
	 * @throws StoreException
	 *             in case of an internal storage backend failure
	 * @throws IllegalArgumentException
	 *             in case of invalid arguments, in case this method is not
	 *             supported by a facade implementation
	 */
	void addContacts(IContactItem[] contactItems) throws StoreException,
			IllegalArgumentException;

	/**
	 * Retrieve contact item with given id from specified contact folder.
	 * 
	 * @param folderId
	 *            unique contact folder id
	 * @param contactId
	 *            unique contact id
	 * @return contact item instance if available, <code>null</code>otherwise.
	 * @throws StoreException
	 *             in case of an internal storage backend failure
	 * @throws IllegalArgumentException
	 *             in case of invalid arguments, in case this method is not
	 *             supported by the facade implementation
	 */
	public IContactItem getContactItem(String folderId, String contactId)
			throws StoreException, IllegalArgumentException;

	/**
	 * Retrieve all <code>IHeaderItem</code> instances from contact folder
	 * with selected unique id.
	 * 
	 * @param folderId
	 *            contact folder unique id
	 * @param flattenGroupItems
	 *            If true, convert <code>IGroupItem</code> to
	 *            <code>List<IContactItem></code>. The result will be a list
	 *            containing only <code>IContactItem</code> instances.
	 *            Otherwise, the list will contain <code>IContactItem</code>
	 *            and <code>IGroupItem</code> instances.
	 * @return list of <code>IHeaderItem</code> instances, never
	 *         <code>null</code>
	 * @throws StoreException
	 *             in case of an internal storage backend failure
	 * @throws IllegalArgumentException
	 *             in case of invalid arguments, in case this method is not
	 *             supported by a facade implementation
	 */
	public List<IHeaderItem> getAllHeaderItems(String folderId,
			boolean flattenGroupItems) throws StoreException,
			IllegalArgumentException;

	/**
	 * Retrieve all <code>IContactItem</code> instances from a contact folder
	 * with selected unique id.
	 * 
	 * @param folderId
	 *            contact folder unique id
	 * @return list of <code>IContactItem</code> instances, never
	 *         <code>null</code>
	 * @throws StoreException
	 *             in case of an internal storage backend failure
	 * @throws IllegalArgumentException
	 *             in case of invalid arguments, in case this method is not
	 *             supported by a facade implementation
	 */
	public List<IContactItem> getAllContacts(String folderId)
			throws StoreException, IllegalArgumentException;

	/**
	 * Retrieve all <code>IGroupItem</code> instances from a contact folder
	 * with selected unique id.
	 * 
	 * @param folderId
	 *            contact folder unique id
	 * @return list of <code>IGroupItem</code> instances, never
	 *         <code>null</code>
	 * @throws StoreException
	 *             in case of an internal storage backend failure
	 * @throws IllegalArgumentException
	 *             in case of invalid arguments, in case this method is not
	 *             supported by a facade implementation
	 */
	public List<IGroupItem> getAllGroups(String folderId)
			throws StoreException, IllegalArgumentException;

	/**
	 * Find a contact by email address in specified contact folder.
	 * 
	 * @param folderId
	 *            contact folder unique id
	 * @param emailAddress
	 *            email address to search for
	 * @return contact id, in case a matching contact was found.
	 *         <code>null</code>otherwise.
	 * @throws StoreException
	 *             in case of an internal storage backend failure
	 * @throws IllegalArgumentException
	 *             in case of invalid arguments, in case this method is not
	 *             supported by a facade implementation
	 */
	public String findByEmailAddress(String folderId, String emailAddress)
			throws StoreException, IllegalArgumentException;

	/**
	 * Find a contact by name in specified contact folder.
	 * <p>
	 * First tries to find a vCard "SORT_AS", then vCard "LASTNAME" and last
	 * vCard "FIRSTNAME", until a match is found. If several contacts match the
	 * first one is used and all other results are ignored.
	 * 
	 * @param folderId
	 *            contact folder unique id
	 * @param name
	 *            name to search for
	 * @return contact id, in case a matching contact was found.
	 *         <code>null</code>otherwise.
	 * @throws StoreException
	 *             in case of an internal storage backend failure
	 * @throws IllegalArgumentException
	 *             in case of invalid arguments, in case this method is not
	 *             supported by a facade implementation
	 */
	public String findByName(String folderId, String name)
			throws StoreException, IllegalArgumentException;

}