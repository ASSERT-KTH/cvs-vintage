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
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;

import org.columba.addressbook.model.ContactModelFactory;
import org.columba.addressbook.model.ContactModelXMLFactory;
import org.columba.addressbook.model.IContactModel;
import org.columba.addressbook.model.IContactModelPartial;
import org.columba.api.exception.StoreException;
import org.columba.core.xml.XmlNewIO;
import org.jdom.Document;

/**
 * Contact item cache storage.
 * 
 * @author fdietz
 * 
 */
public class ContactItemCacheStorageImpl implements ContactItemCacheStorage {

	/** JDK 1.4+ logging framework logger, used for logging. */
	private static final Logger LOG = Logger
			.getLogger("org.columba.addressbook.folder");

	/**
	 * 
	 * keeps a list of HeaderItem's we need for the table-view
	 * 
	 */
	private Hashtable<String, IContactModelPartial> map;

	/**
	 * 
	 * binary file named "header"
	 * 
	 */
	private File headerFile;

	/**
	 * directory where contact files are stored
	 */
	private File directoryFile;

	private AbstractFolder folder;

	/**
	 * 
	 */
	public ContactItemCacheStorageImpl(AbstractFolder folder) {
		super();

		this.folder = folder;

		map = new Hashtable<String, IContactModelPartial>();

		directoryFile = folder.getDirectoryFile();

		headerFile = new File(directoryFile, ".header");

		/*
		 * if (headerFile.exists()) { try { load(); headerCacheAlreadyLoaded =
		 * true; } catch (Exception ex) { ex.printStackTrace();
		 * 
		 * headerCacheAlreadyLoaded = false; } } else { sync(); }
		 */
		sync();
	}

	/**
	 * @see org.columba.addressbook.folder.ContactItemCacheStorage#getHeaderItemMap()
	 */
	public Map<String, IContactModelPartial> getContactItemMap()
			throws StoreException {
		return map;
	}

	/**
	 * @see org.columba.addressbook.folder.ContactItemCacheStorage#add(IContactModel)
	 */
	public void add(String uid, IContactModelPartial item)
			throws StoreException {
		getContactItemMap().put(uid, item);

	}

	/**
	 * @see org.columba.addressbook.folder.ContactItemCacheStorage#remove(java.lang.Object)
	 */
	public void remove(String uid) throws StoreException {
		getContactItemMap().remove(uid);

	}

	/**
	 * @see org.columba.addressbook.folder.ContactItemCacheStorage#modify(java.lang.Object,
	 *      IContactModel)
	 */
	public void modify(String uid, IContactModelPartial item)
			throws StoreException {
		getContactItemMap().remove(item);
		getContactItemMap().put(uid, item);

	}

	/**
	 * @see org.columba.addressbook.folder.ContactItemCacheStorage#save()
	 */
	public void save() throws StoreException {

	}

	/**
	 * @see org.columba.addressbook.folder.ContactItemCacheStorage#load()
	 */
	public void load() throws StoreException {

	}

	public void sync() {

		File[] list = directoryFile.listFiles();
		List<File> v = new Vector<File>();

		for (int i = 0; i < list.length; i++) {
			File file = list[i];
			File renamedFile;
			String name = file.getName();
			int index = name.indexOf("header");

			if (index == -1) {

				if ((file.exists()) && (file.length() > 0)) {
					renamedFile = new File(file.getParentFile(),
							file.getName() + '~');
					file.renameTo(renamedFile);

					v.add(renamedFile);
				}

			} else {
				// header file found
				headerFile.delete();
			}
		}

		for (int i = 0; i < v.size(); i++) {
			File file = (File) v.get(i);

			File newFile = new File(file.getParentFile(), (new Integer(i))
					.toString()
					+ ".xml");
			file.renameTo(newFile);
			try {

				Document doc = XmlNewIO.load(newFile);

				IContactModel model = ContactModelXMLFactory.unmarshall(doc,
						new Integer(i).toString());

				IContactModelPartial item = ContactModelFactory.createContactModelPartial(model, new Integer(i).toString());
				add(new Integer(i).toString(), item);

				folder.setNextMessageUid(i + 1);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		LOG.info("map-size()==" + map.size());

	}

	/**
	 * @see org.columba.addressbook.folder.ContactItemCacheStorage#count()
	 */
	public int count() {
		return map.size();
	}

	/**
	 * @see org.columba.addressbook.folder.ContactItemCacheStorage#exists(java.lang.Object)
	 */
	public boolean exists(String uid) {
		return map.containsKey(uid);
	}

	/**
	 * @see org.columba.addressbook.folder.ContactItemCacheStorage#getContactItemMap(java.lang.String[])
	 */
	public Map<String, IContactModelPartial> getContactItemMap(String[] ids)
			throws StoreException {
		if (ids == null)
			throw new IllegalArgumentException("ids == null");

		Map<String, IContactModelPartial> result = new Hashtable<String, IContactModelPartial>();

		for (int i = 0; i < ids.length; i++) {
			// skip, if null
			if (ids[i] == null)
				continue;

			IContactModelPartial p = map.get(ids[i]);
			if (p != null)
				result.put(ids[i], p);
		}

		return result;
	}
}