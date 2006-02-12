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
import java.util.Iterator;
import java.util.logging.Logger;

import org.columba.calendar.base.UUIDGenerator;
import org.columba.calendar.model.HeaderItem;
import org.columba.calendar.model.HeaderItemList;
import org.columba.calendar.model.ICalendarModel;
import org.columba.calendar.model.IHeaderItemList;
import org.columba.calendar.model.VCalendarModelFactory;
import org.columba.calendar.parser.SyntaxException;
import org.columba.core.io.DiskIO;
import org.columba.core.util.InternalException;
import org.jdom.Document;

public class LocalCalendarStore extends AbstractCalendarStore implements
		ICalendarStore {

	/** JDK 1.4+ logging framework logger, used for logging. */
	private static final Logger LOG = Logger
			.getLogger("org.columba.calendar.store");

	/**
	 * folder where we save everything in
	 */
	private File directory;

	private LocalXMLFileStore dataStorage;

	public LocalCalendarStore(File directory) throws IllegalArgumentException,
			InternalException {
		super();

		if (directory == null)
			throw new IllegalArgumentException("directory == null");

		this.directory = directory;

		DiskIO.ensureDirectory(directory);

		dataStorage = new LocalXMLFileStore(directory);
	}

	/**
	 * @see org.columba.calendar.store.AbstractCalendarStore#add(org.columba.calendar.model.ICalendarModel)
	 */
	public void add(ICalendarModel basicModel) throws IllegalArgumentException,
			InternalException {

		if (basicModel == null)
			throw new IllegalArgumentException("basicModel == null");

		String id = basicModel.getId();
		if (id == null)
			id = new UUIDGenerator().newUUID();

		Document document = null;
		try {
			document = VCalendarModelFactory.marshall(basicModel);
		} catch (SyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		dataStorage.save(id, document);

		fireItemAdded(id);
	}

	/**
	 * @see org.columba.calendar.store.AbstractCalendarStore#exists(java.lang.Object)
	 */
	public boolean exists(Object id) throws IllegalArgumentException,
			InternalException {

		if (id == null)
			throw new IllegalArgumentException("id == null");

		return dataStorage.exists(id);
	}

	/**
	 * @see org.columba.calendar.store.AbstractCalendarStore#get(java.lang.Object)
	 */
	public ICalendarModel get(Object id) throws IllegalArgumentException,
			InternalException {

		if (id == null)
			throw new IllegalArgumentException("id == null");
		
		Document document = dataStorage.load(id);
		if ( document == null ) throw new InternalException("document == null, id="+id);

		ICalendarModel basicModel = null;
		try {
			basicModel = VCalendarModelFactory.unmarshall(document);
		} catch (SyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return basicModel;
	}

	/**
	 * @see org.columba.calendar.store.AbstractCalendarStore#modify(java.lang.Object,
	 *      org.columba.calendar.model.ICalendarModel)
	 */
	public void modify(Object id, ICalendarModel basicModel)
			throws IllegalArgumentException, InternalException {
		if (id == null)
			throw new IllegalArgumentException("id == null");

		if (basicModel == null)
			throw new IllegalArgumentException("basicModel == null");

		dataStorage.remove(id);

		Document document = null;
		try {
			document = VCalendarModelFactory.marshall(basicModel);
		} catch (SyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		super.modify(id, basicModel);
	}

	/**
	 * @see org.columba.calendar.store.AbstractCalendarStore#remove(java.lang.Object)
	 */
	public void remove(Object id) throws IllegalArgumentException, InternalException {
		if (id == null)
			throw new IllegalArgumentException("id == null");

		dataStorage.remove(id);

		super.remove(id);
	}

	/**
	 * @see org.columba.calendar.store.AbstractCalendarStore#getHeaderItemList()
	 */
	public IHeaderItemList getHeaderItemList() throws IllegalArgumentException,
			InternalException {

		HeaderItemList list = new HeaderItemList();

		Iterator it = dataStorage.iterator();
		while (it.hasNext()) {
			Document document = (Document) it.next();

			ICalendarModel basicModel = null;
			try {
				basicModel = VCalendarModelFactory.unmarshall(document);
			} catch (SyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			HeaderItem item = new HeaderItem(basicModel.getId(), basicModel.getSummary(),
					basicModel.getDtStart(), basicModel.getDtEnt());
			list.add(item);
		}

		return list;
	}

}
