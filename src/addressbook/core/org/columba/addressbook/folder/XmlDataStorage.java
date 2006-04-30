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

import java.io.File;
import java.io.IOException;

import org.columba.addressbook.model.ContactModelFactory;
import org.columba.addressbook.model.IContactModel;
import org.columba.addressbook.parser.SyntaxException;
import org.columba.core.xml.XmlNewIO;
import org.jdom.Document;

/**
 * Datastorage implementation using a MH-style mailbox approach with
 * one folder containing one XML file per contact.
 * 
 * @author fdietz
 *  
 */
public class XmlDataStorage implements DataStorage {

	private AbstractFolder folder;

	/**
	 *  
	 */
	public XmlDataStorage(AbstractFolder folder) {
		super();

		this.folder = folder;
	}

	/**
	 * @see org.columba.addressbook.folder.DataStorage#load(java.lang.Object)
	 */
	public IContactModel load(Object uid) throws StoreException {
		File file = getFile(uid);

		Document doc = XmlNewIO.load(file);

		if ( doc == null) return null;
		
		IContactModel model;
		try {
			model = ContactModelFactory.unmarshall(doc, ((Integer) uid).toString());
		}  catch (SyntaxException e) {
			throw new StoreException(e);
		}	

		return model;
	}

	/**
	 * @param uid
	 * @return
	 */
	private File getFile(Object uid) throws StoreException {
		File file = new File(folder.getDirectoryFile().toString() + "/"
				+ (uid.toString()) + ".xml");
		return file;
	}

	/**
	 * @see org.columba.addressbook.folder.DataStorage#save(java.lang.Object,
	 *      IContactModel)
	 */
	public void save(Object uid, IContactModel contact) throws StoreException {
		File file = getFile(uid);

		Document doc;
		try {
			doc = ContactModelFactory.marshall(contact);
		} catch (SyntaxException e) {
			throw new StoreException(e);
		}
		
		try {
			XmlNewIO.save(doc, file);
		} catch (IOException e) {
			throw new StoreException(e);
		}

	}

	/**
	 * @see org.columba.addressbook.folder.DataStorage#modify(java.lang.Object,
	 *      IContactModel)
	 */
	public void modify(Object uid, IContactModel contact) throws StoreException {
		save(uid, contact);

	}

	/**
	 * @see org.columba.addressbook.folder.DataStorage#remove(java.lang.Object)
	 */
	public void remove(Object uid) throws StoreException {
		File file = getFile(uid);
		file.delete();

	}

}