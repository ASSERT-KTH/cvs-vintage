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

import java.util.logging.Logger;

import org.columba.addressbook.config.FolderItem;
import org.columba.addressbook.model.Contact;


/**
 *
 * LocalFolder-class gives as an additional abstraction-layer:
 *  --> DataStorageInterface
 *
 * this makes it very easy to add other folder-formats
 *
 * the important methods from Folder are just mapped to
 * the corresponding methods from DataStorageInterface
 *
 *
 */
public abstract class LocalFolder extends AbstractFolder {
	/** JDK 1.4+ logging framework logger, used for logging. */
    private static final Logger LOG = Logger
            .getLogger("org.columba.addressbook.folder");
    
    protected DataStorage dataStorage;

    public LocalFolder(String name, String path) {
    	super(name, path);
    }

    public LocalFolder(FolderItem item) {
        super(item);
       
    }


    public abstract DataStorage getDataStorageInstance();

  
	/**
	 * @see org.columba.addressbook.folder.ContactStorage#add(org.columba.addressbook.folder.Contact)
	 */
	public Object add(Contact contact) throws Exception{
		Object uid = super.add(contact);

        getDataStorageInstance().save(uid, contact);

        return uid;
	}
	/**
	 * @see org.columba.addressbook.folder.ContactStorage#get(java.lang.Object)
	 */
	public Contact get(Object uid) throws Exception{
		return getDataStorageInstance().load(uid);
	}
	/**
	 * @see org.columba.addressbook.folder.ContactStorage#modify(java.lang.Object, org.columba.addressbook.folder.Contact)
	 */
	public void modify(Object uid, Contact contact) throws Exception{
		super.modify(uid, contact);
		
		getDataStorageInstance().modify(uid, contact);

	}
	/**
	 * @see org.columba.addressbook.folder.ContactStorage#remove(java.lang.Object)
	 */
	public void remove(Object uid) throws Exception{
		super.remove(uid);
		
		getDataStorageInstance().remove(uid);

	}
}
