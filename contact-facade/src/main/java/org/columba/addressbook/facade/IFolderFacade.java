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

/**
 * Provides access to contact and group folders.
 * 
 * @author fdietz
 */
public interface IFolderFacade {

	/**
	 * Get a Folder object
	 * 
	 * @param uid
	 *            unique id of folder
	 * @return Folder selected folder
	 */
	IFolder getFolder(String uid);

	/**
	 * Get "Collected Addresses" contact folder.
	 * <p>
	 * This is a special type of contact folder which could be used to
	 * automatically collect all most frequently used contacts. This information
	 * can later be used to prefill lists or autocomplete forms.
	 * 
	 * @return Folder collected address contact store
	 */
	IFolder getCollectedAddresses();

	/**
	 * Get "Personal Addressbook" folder.
	 * 
	 * @return local addressbook folder
	 */
	IFolder getLocalAddressbook();

	/**
	 * Get folder with <code>name</code>.
	 * 
	 * @param name
	 *            name of folder
	 * @return selected folder
	 */
	IFolder getFolderByName(String name);

	/**
	 * Return iterator of <code>IFolder</code>
	 * 
	 * @return iterator of <code>IFolder</code>
	 */
	List<IFolder> getAllFolders();

	/**
	 * Return root folder. This might come in handy when traversing the whole
	 * tree structure.
	 * 
	 * @return root folder
	 */
	IFolder getRootFolder();
}