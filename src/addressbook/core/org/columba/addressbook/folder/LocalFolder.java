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
package org.columba.addressbook.folder;

import org.columba.addressbook.config.FolderItem;
import org.columba.addressbook.model.IContactModel;
import org.columba.api.exception.StoreException;

/**
 * 
 * AbstractLocalFolder-class gives as an additional abstraction-layer: -->
 * IDataStorage
 * 
 * this makes it very easy to add other folder-formats
 * 
 * the important methods from Folder are just mapped to the corresponding
 * methods from IDataStorage
 * 
 * 
 */
public abstract class LocalFolder extends AbstractFolder {

	protected DataStorage dataStorage;

	public LocalFolder(String name, String path) {
		super(name, path);
	}

	public LocalFolder(FolderItem item) {
		super(item);

	}

	public abstract DataStorage getDataStorageInstance();

	/**
	 * @see org.columba.addressbook.folder.IContactStorage#add(IContactModel)
	 */
	public String add(IContactModel contact) throws StoreException {
		String uid = super.add(contact);

		getDataStorageInstance().save(uid, contact);

		return uid;
	}

	/**
	 * @see org.columba.addressbook.folder.IContactStorage#get(java.lang.Object)
	 */
	public IContactModel get(String uid) throws StoreException {
		return getDataStorageInstance().load(uid);
	}

	/**
	 * @see org.columba.addressbook.folder.IContactStorage#modify(java.lang.Object,
	 *      IContactModel)
	 */
	public void modify(String uid, IContactModel contact) throws StoreException {
		super.modify(uid, contact);

		getDataStorageInstance().modify(uid, contact);

	}

	/**
	 * @see org.columba.addressbook.folder.IContactStorage#remove(java.lang.Object)
	 */
	public void remove(String uid) throws StoreException {
		super.remove(uid);

		getDataStorageInstance().remove(uid);

	}
}
