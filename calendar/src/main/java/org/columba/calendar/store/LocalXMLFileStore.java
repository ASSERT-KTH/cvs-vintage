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
package org.columba.calendar.store;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Logger;

import org.columba.calendar.store.api.StoreException;
import org.columba.core.io.DiskIO;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

public class LocalXMLFileStore {

	/** JDK 1.4+ logging framework logger, used for logging. */
	private static final Logger LOG = Logger
			.getLogger("org.columba.calendar.store");

	private File directory;

	public LocalXMLFileStore(File directory) throws StoreException {
		super();

		if (directory == null)
			throw new IllegalArgumentException("directory == null");

		this.directory = directory;

		DiskIO.ensureDirectory(directory);

	}

	public Document load(Object id) throws StoreException {
		if (id == null)
			throw new IllegalArgumentException("uuid == null");

		File file = getFile(id);

		SAXBuilder builder = new SAXBuilder();
		// builder.setValidation(true);
		builder.setIgnoringElementContentWhitespace(true);
		Document doc = null;
		try {
			doc = builder.build(file);

		} catch (JDOMException e) {
			throw new StoreException(e);
		} catch (IOException e) {
			throw new StoreException(e);
		}

		return doc;
	}

	public void save(Object id, Document document) throws StoreException {
		if (id == null)
			throw new IllegalArgumentException("id == null");
		if (document == null)
			throw new IllegalArgumentException("document == null");

		File file = getFile(id);

		XMLOutputter outp = new XMLOutputter();

		try {
			outp.output(document, new FileOutputStream(file));
		} catch (FileNotFoundException e) {
			throw new StoreException(e);
		} catch (IOException e) {
			throw new StoreException(e);
		}
	}

	public void modify(Object id, Document document) throws StoreException {
		if (id == null)
			throw new IllegalArgumentException("id == null");
		if (document == null)
			throw new IllegalArgumentException("document == null");

		save(id, document);

	}

	public void remove(Object id) throws StoreException {
		if (id == null)
			throw new IllegalArgumentException("id == null");

		File file = getFile(id);
		file.delete();
	}

	/**
	 * @param uid
	 * @return
	 */
	private File getFile(Object id) throws StoreException {

		if (id == null)
			throw new IllegalArgumentException("id == null");

		File file = new File(directory.toString() + File.separator
				+ (id.toString()) + ".xcs");

		return file;
	}

	public boolean exists(Object uid) throws StoreException {

		if (uid == null)
			throw new IllegalArgumentException("uid == null");

		return (getFile(uid) != null);
	}

	public Iterator iterator() throws StoreException {
		return new StoreIterator();
	}

	class StoreIterator implements Iterator {

		private File[] files;

		int nextIndex = 0;

		StoreIterator() {
			files = directory.listFiles();
		}

		public boolean hasNext() {
			if (nextIndex < files.length)
				return true;

			return false;
		}

		public Object next() {

			// filename = "uuid.xcs"
			String filename = files[nextIndex].getName();
			// remove ".xcs"
			String uid = filename.substring(0, filename.indexOf("."));

			Document document = null;

			document = load(uid);
			nextIndex++;

			return document;
		}

		public void remove() {
			String uid = files[nextIndex].getName();

			LocalXMLFileStore.this.remove(uid);

			nextIndex++;
		}

	}
}
